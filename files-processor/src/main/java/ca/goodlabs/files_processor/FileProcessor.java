package ca.goodlabs.files_processor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

public record FileProcessor(
        String type,
        Set<String> files,
        Path destinationDirectory,
        ScheduledFuture<?> fileCollectorFuture
) implements Runnable {
    public static final String LOGS_DIR_NAME = "__LOGS__";

    @Override
    public void run() {
        System.out.printf("[processor-%s]: processing...%n", type.substring(1));
        List<LogEntry> logs = new ArrayList<>();
        var processedFiles = Optional.ofNullable(files)
                .map(ArrayList::new)
                .filter(not(Collection::isEmpty))
                .stream()
                .flatMap(Collection::stream)
                .filter(
                        source -> {
                            try {
                                var sourcePath = Paths.get(source);
                                var name = sourcePath.toFile().getName();
                                if (Files.exists(sourcePath)) {
                                    Files.move(
                                            sourcePath,
                                            destinationDirectory.resolve(name),
                                            StandardCopyOption.ATOMIC_MOVE
                                    );
                                    var logEntry = new LogEntry(name, LocalDateTime.now());
                                    logs.add(logEntry);
                                    System.out.printf(
                                            "[processor-%s]: %s%n",
                                            type.substring(1),
                                            logEntry
                                    );
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                return false;
                            }
                            return true;
                        }
                )
                .toList();
        if (processedFiles.isEmpty() && (fileCollectorFuture.isDone() || fileCollectorFuture.isCancelled())) {
            throw new StopProcessException();
        }
        processedFiles.forEach(files::remove);

        updateLogs(logs);
        System.out.printf("[processor-%s]: processing finished...%n", type.substring(1));
    }

    private void updateLogs(List<LogEntry> logs) {
        if (logs.isEmpty()) {
            return;
        }
        System.out.printf(
                "[processor-%s]: Updating logs for %d entries...%n",
                type.substring(1),
                logs.size()
        );
        var logsDir = destinationDirectory.resolve(LOGS_DIR_NAME);
        try {
            Files.createDirectories(logsDir);
            var logFile = logsDir.resolve(type.substring(1).concat(".log"));
            Files.writeString(
                    logFile,
                    logs
                            .stream()
                            .map(Record::toString)
                            .collect(Collectors.joining()),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private record LogEntry(String file, LocalDateTime time) {

        @Override
        public String toString() {
            return file + " " + time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\n";
        }
    }

}
