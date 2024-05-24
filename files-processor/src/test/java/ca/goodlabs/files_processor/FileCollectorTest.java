package ca.goodlabs.files_processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileCollectorTest {

    public static final String FILE_TYPE_PNG = ".png";
    public static final String SOURCE_DIR = "/tmp/file-collector-test/fileset";


    FileCollector objectUnderTest;

    @BeforeEach
    void setUp() throws IOException, URISyntaxException {
        var source = Paths.get(SOURCE_DIR);
        Utils.deleteDirectory(source);
        Files.createDirectories(source);

        try (Stream<Path> stream = Files.list(Path.of(ClassLoader.getSystemResource("fileset").toURI()))) {
            stream.forEach(path -> {
                try {
                    Files.copy(path, source.resolve(path.toFile().getName()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Test
    void runWhenThereIsPlaceInBufferTest() throws IOException {
        Files.deleteIfExists(Paths.get(SOURCE_DIR.concat("/3.png")));
        try (Stream<Path> stream = Files.list(Paths.get(SOURCE_DIR))) {
            assertThat(stream.toList())
                    .map(Path::toFile)
                    .map(File::getName)
                    .contains("1.txt")
                    .contains("1.png")
                    .contains("2.png")
                    .doesNotContain("3.png");
        }


        var files = Collections.synchronizedSet(
                new HashSet<>(List.of(
                        SOURCE_DIR.concat("/3.png")
                ))
        );
        objectUnderTest = new FileCollector(
                FILE_TYPE_PNG,
                files,
                SOURCE_DIR,
                2
        );
        objectUnderTest.run();

        assertThat(files)
                .filteredOn(file -> file.endsWith(".png"))
                .hasSize(2)
                .contains(SOURCE_DIR.concat("/3.png"));
    }


    @Test
    void runWhenThereIsNoMorePlaceInBufferTest() throws IOException {
        Files.deleteIfExists(Paths.get(SOURCE_DIR.concat("/3.png")));
        try (Stream<Path> stream = Files.list(Paths.get(SOURCE_DIR))) {
            assertThat(stream.toList())
                    .map(Path::toFile)
                    .map(File::getName)
                    .contains("1.txt")
                    .contains("1.png")
                    .contains("2.png")
                    .doesNotContain("3.png");
        }

        var files = Collections.synchronizedSet(
                new HashSet<>(List.of(
                        SOURCE_DIR.concat("/1.png"),
                        SOURCE_DIR.concat("/3.png")
                ))
        );
        objectUnderTest = new FileCollector(
                FILE_TYPE_PNG,
                files,
                SOURCE_DIR,
                2
        );
        objectUnderTest.run();

        assertThat(files)
                .hasSize(2)
                .contains(SOURCE_DIR.concat("/1.png"))
                .contains(SOURCE_DIR.concat("/3.png"));
    }

    @Test
    void runWhenThereIsNoBufferSpecifiedTest() throws IOException {
        Files.deleteIfExists(Paths.get(SOURCE_DIR.concat("/3.png")));
        try (Stream<Path> stream = Files.list(Paths.get(SOURCE_DIR))) {
            assertThat(stream.toList())
                    .map(Path::toFile)
                    .map(File::getName)
                    .contains("1.txt")
                    .contains("1.png")
                    .contains("2.png")
                    .doesNotContain("3.png");
        }

        var files = Collections.synchronizedSet(
                new HashSet<>(List.of(
                        SOURCE_DIR.concat("/1.png"),
                        SOURCE_DIR.concat("/3.png")
                ))
        );
        objectUnderTest = new FileCollector(
                FILE_TYPE_PNG,
                files,
                SOURCE_DIR,
                null
        );
        objectUnderTest.run();

        assertThat(files)
                .hasSize(3)
                .contains(SOURCE_DIR.concat("/1.png"))
                .contains(SOURCE_DIR.concat("/2.png"))
                .contains(SOURCE_DIR.concat("/3.png"));
    }


    @Test
    void runIgnoringNonSupportedTypesTest() throws IOException {
        Files.deleteIfExists(Paths.get(SOURCE_DIR.concat("/1.png")));
        Files.deleteIfExists(Paths.get(SOURCE_DIR.concat("/2.png")));
        Files.deleteIfExists(Paths.get(SOURCE_DIR.concat("/3.png")));
        try (Stream<Path> stream = Files.list(Paths.get(SOURCE_DIR))) {
            assertThat(stream.toList())
                    .map(Path::toFile)
                    .map(File::getName)
                    .contains("1.txt")
                    .doesNotContain("1.png")
                    .doesNotContain("2.png")
                    .doesNotContain("3.png");
        }

        var files = Collections.synchronizedSet(
                new HashSet<>(List.of(
                        SOURCE_DIR.concat("/3.png")
                ))
        );
        objectUnderTest = new FileCollector(
                FILE_TYPE_PNG,
                files,
                SOURCE_DIR,
                2
        );

        assertThatThrownBy(() -> objectUnderTest.run())
                .isExactlyInstanceOf(StopProcessException.class);

        assertThat(files)
                .hasSize(1)
                .contains(SOURCE_DIR.concat("/3.png"));
    }
}