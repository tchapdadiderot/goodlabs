package ca.goodlabs.files_processor;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class ArgumentsReaderTest {

    ArgumentsReader objectUnderTest;

    @Test
    void readTest() {
        var outputDir = "/home/guser/output";
        objectUnderTest = new ArgumentsReader(
                "-t", "pdf-100",
                "-t", "jpg",
                "-t", "png",
                "-o",
                outputDir
        );
        var arguments = objectUnderTest.read();

        assertThat(arguments)
                .isNotNull()
                .returns(Paths.get(outputDir), Arguments::destinationDirectory)
                .satisfies(
                        input -> assertThat(input.filesTypes())
                                .hasSize(3)
                                .contains(
                                        new FileType(".pdf", 100),
                                        new FileType(".jpg", null),
                                        new FileType(".png", null)
                                )
                );
    }

}