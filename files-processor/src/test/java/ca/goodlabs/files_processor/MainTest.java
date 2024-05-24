package ca.goodlabs.files_processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class MainTest {
    public static final String OUTPUT_DIR = "/tmp/file-processor-main/output";
    Path inputDir;

    @BeforeEach
    void setUp() {
        inputDir = Paths.get(Application.INPUT_DIRECTORY);
        Utils.deleteDirectory(inputDir);
        var outputDir = Paths.get(OUTPUT_DIR);
        Utils.deleteDirectory(outputDir.resolve(FileProcessor.LOGS_DIR_NAME));
        Utils.deleteDirectory(outputDir);
        System.out.println("Generation of files started...");
        IntStream.range(0, 1_000_000)
                .parallel()
                .forEach(index -> {
                    if (index < 200_000) {
                        writeToFile(index, "png");
                    } else if (index < 600_000) {
                        writeToFile(index, "txt");
                    } else if (index < 800_000) {
                        writeToFile(index, "jpg");
                    } else {
                        writeToFile(index, "csv");
                    }
                });
        System.out.println("Generation of files finished...");
    }

    private void writeToFile(int index, String fileType) {
        var file = inputDir.resolve(index + "." + fileType);
        try {
            Files.writeString(
                    file,
                    "a",
                    StandardOpenOption.CREATE
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void mainTest() {
        assertThat(Utils.numberOfFileInDirectory(inputDir)).isEqualTo(1_000_000);

        Main.main(
                "-t", "png-500",
                "-t", "txt-1000",
                "-t", "jpg-500",
                "-o", OUTPUT_DIR
        );

        var outputDir = Paths.get(OUTPUT_DIR);
        assertThat(Utils.numberOfFileInDirectory(inputDir)).isEqualTo(200_000);
        assertThat(Utils.numberOfFileInDirectory(outputDir)).isEqualTo(800_001);
        assertThat(Utils.numberOfFileInDirectory(outputDir.resolve(FileProcessor.LOGS_DIR_NAME)))
                .isEqualTo(3);
    }
}