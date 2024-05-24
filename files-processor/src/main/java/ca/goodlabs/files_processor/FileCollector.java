package ca.goodlabs.files_processor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

public class FileCollector implements Runnable {
    private final String type;
    private final Set<String> files;
    private final String input;
    private final Integer buffer;
    private int counter;

    public FileCollector(String type, Set<String> files, String input, Integer buffer) {
        this.type = type;
        this.files = files;
        this.input = input;
        this.buffer = buffer;
    }

    @Override
    public void run() {
        counter = 0;
        System.out.printf("[collector-%s]: reading...%n", type.substring(1));
        try {
            try (Stream<Path> stream = Files.list(Paths.get(input))) {
                stream
                        .parallel()
                        .filter(not(Files::isDirectory))
                        .map(Path::toAbsolutePath)
                        .map(Path::toString)
                        .filter(file -> file.endsWith(type))
                        .forEach(tryToAddFileToBuffer());
            }
        } catch (BufferReachedException e) {
            System.out.printf(
                    "[collector-%s]: Buffer (%d) reached. Let's try later.%n",
                    type.substring(1),
                    buffer
            );
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        if (counter == 0) {
            throw new StopProcessException();
        }
    }

    private Consumer<String> tryToAddFileToBuffer() {
        return file -> {
            counter++;
            if (isBufferReached()) {
                throw new BufferReachedException();
            }
            files.add(file);
        };
    }

    private boolean isBufferReached() {
        return Objects.nonNull(buffer) && files.size() >= buffer;
    }

    public String getType() {
        return type;
    }
}
