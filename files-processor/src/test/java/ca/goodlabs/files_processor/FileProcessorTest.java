package ca.goodlabs.files_processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Stream;

import static ca.goodlabs.files_processor.FileProcessor.LOGS_DIR_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

class FileProcessorTest {

    public static final String FILE_TYPE_PNG = ".png";
    public static final String SOURCE_DIR = "/tmp/file-processor-test/fileset";
    public static final String DESTINATION_DIR = "/tmp/file-processor-test/output";
    FileProcessor objectUnderTest;

    @BeforeEach
    void setUp() throws IOException, URISyntaxException {
        var source = Paths.get(SOURCE_DIR);
        var destination = Paths.get(DESTINATION_DIR);
        var logsDestination = destination.resolve(LOGS_DIR_NAME);
        Utils.deleteDirectory(source);
        Utils.deleteDirectory(logsDestination);
        Utils.deleteDirectory(destination);

        Files.createDirectories(source);
        Files.createDirectories(destination);
        try (Stream<Path> stream = Files.list(Path.of(ClassLoader.getSystemResource("fileset").toURI()))) {
            stream
                    .filter(path -> path.toFile().getName().endsWith(FILE_TYPE_PNG))
                    .forEach(path -> {
                        try {
                            Files.copy(path, source.resolve(path.toFile().getName()));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    @Test
    void runTest() throws IOException {
        var files = Collections.synchronizedSet(
                new HashSet<>(List.of(
                        SOURCE_DIR.concat("/1.png"),
                        SOURCE_DIR.concat("/2.png"),
                        SOURCE_DIR.concat("/3.png")
                ))
        );
        var destinationDirectory = Paths.get(DESTINATION_DIR);
        try (Stream<Path> stream = Files.list(destinationDirectory)) {
            assertThat(stream.toList()).isEmpty();
        }
        ScheduledFuture<?> scheduledFuture = Mockito.mock(ScheduledFuture.class);
        when(scheduledFuture.isDone()).thenReturn(false);
        when(scheduledFuture.isCancelled()).thenReturn(false);
        objectUnderTest = new FileProcessor(
                FILE_TYPE_PNG,
                files,
                destinationDirectory,
                scheduledFuture
        );
        objectUnderTest.run();

        try (Stream<Path> stream = Files.list(Paths.get(SOURCE_DIR))) {
            assertThat(stream.toList()).isEmpty();
        }

        try (Stream<Path> stream = Files.list(destinationDirectory)) {
            assertThat(stream.toList())
                    .map(Path::toFile)
                    .map(File::getName)
                    .contains("1.png")
                    .contains("2.png")
                    .contains("3.png");
        }

        var allLines = Files.readAllLines(destinationDirectory.resolve(LOGS_DIR_NAME).resolve("png.log"));
        assertThat(allLines.stream())
                .map(line -> line.split(" "))
                .map(array -> array[0])
                .contains("1.png")
                .contains("2.png")
                .contains("3.png");
        allLines.stream()
                .map(line -> line.split(" "))
                .map(array -> array[1])
                .map(LocalDateTime::parse)
                .forEach(
                        localDateTime -> assertThat(localDateTime)
                                .isCloseTo(LocalDateTime.now(), within(2, ChronoUnit.SECONDS))
                );
        assertThat(files).isEmpty();
    }
}