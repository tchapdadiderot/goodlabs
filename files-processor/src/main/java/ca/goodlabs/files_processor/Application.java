package ca.goodlabs.files_processor;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Application {

    public static final String INPUT_DIRECTORY = "/home/guser/fileset";

    private final ScheduledExecutorService scheduledExecutorService;
    private final Map<String, Set<String>> filesByType;
    private final Arguments arguments;

    public Application(Arguments arguments) {
        this.arguments = arguments;
        scheduledExecutorService = Executors.newScheduledThreadPool(
                Runtime.getRuntime().availableProcessors()
        );
        filesByType = new ConcurrentHashMap<>();
    }

    public void run() {
        try {
            init();
            Map<String, ScheduledFuture<?>> collectorsScheduledFutures = scheduleFileCollectors();
            List<? extends ScheduledFuture<?>> processorsScheduledFutures = scheduleFilesProcessors(
                    collectorsScheduledFutures
            );
            ArrayList<? extends ScheduledFuture<?>> scheduledFutures = new ArrayList<>(processorsScheduledFutures);
            while (!scheduledFutures.isEmpty()) {
                processorsScheduledFutures.forEach(scheduledFuture -> {
                    if (scheduledFuture.isDone() || scheduledFuture.isCancelled()) {
                        scheduledFutures.remove(scheduledFuture);
                    }
                });
                Thread.sleep(250);
            }

        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(0);
        } finally {
            System.out.printf(
                    "The reports can be found in the folder located '%s'%n",
                    arguments
                            .destinationDirectory()
                            .toAbsolutePath()
                            .resolve(FileProcessor.LOGS_DIR_NAME)
                            .getFileName()
            );
            scheduledExecutorService.shutdown();
        }
    }

    private List<? extends ScheduledFuture<?>> scheduleFilesProcessors(
            Map<String, ScheduledFuture<?>> collectorsScheduledFutures
    ) {
        return arguments
                .filesTypes()
                .stream()
                .map(type -> new FileProcessor(
                        type.type(),
                        filesByType.get(type.type()),
                        arguments.destinationDirectory(),
                        collectorsScheduledFutures.get(type.type())
                ))
                .map(
                        fileProcessor -> scheduledExecutorService.scheduleWithFixedDelay(
                                fileProcessor,
                                0,
                                250L,
                                TimeUnit.MILLISECONDS
                        )
                )
                .toList();
    }

    private void init() throws IOException {
        Files.createDirectories(arguments.destinationDirectory().resolve(FileProcessor.LOGS_DIR_NAME));
        for (FileType fileType : arguments.filesTypes()) {
            filesByType.put(fileType.type(), Collections.synchronizedSet(new HashSet<>()));
        }
    }

    private Map<String, ScheduledFuture<?>> scheduleFileCollectors() {
        return arguments
                .filesTypes()
                .stream()
                .map(type -> new FileCollector(
                        type.type(),
                        filesByType.get(type.type()),
                        INPUT_DIRECTORY,
                        type.buffer()
                ))
                .collect(Collectors.toMap(
                        FileCollector::getType,
                        fileCollector -> scheduledExecutorService.scheduleWithFixedDelay(
                                fileCollector,
                                0,
                                250L,
                                TimeUnit.MILLISECONDS
                        )
                ));
    }
}
