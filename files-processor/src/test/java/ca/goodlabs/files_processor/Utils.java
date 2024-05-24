package ca.goodlabs.files_processor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class Utils {
    static void deleteDirectory(Path directory) {
        try {
            if (directory.toFile().exists()) {
                try (Stream<Path> stream = Files.list(directory)) {
                    stream.forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static int numberOfFileInDirectory(Path directory) {
        var counter = new Counter();
        try (Stream<Path> stream = Files.list(directory)) {
            stream.forEach(path -> counter.value++);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return counter.value;
    }

    private static class Counter {
        int value;
    }
}