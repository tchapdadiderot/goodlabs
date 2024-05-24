package ca.goodlabs.files_processor;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import java.nio.file.Paths;
import java.util.Arrays;

class ArgumentsReader {

    private final String[] args;

    public ArgumentsReader(String... args) {
        this.args = args;
    }

    public Arguments read() {
        var options = new Options()
                .addOption(OptionData.FILE_TYPES.create())
                .addOption(OptionData.OUTPUT.create());
        try {
            var commandLine = new DefaultParser().parse(options, args);
            return new Arguments(
                    Arrays.stream(OptionData.FILE_TYPES.extractValuesFrom(commandLine))
                            .map(type -> type.split("-"))
                            .map(
                                    typeAndBuffer -> new FileType(
                                            "." + typeAndBuffer[0],
                                            typeAndBuffer.length == 2 ? Integer.parseInt(typeAndBuffer[1]) : null
                                    )
                            )
                            .toList(),
                    Paths.get(OptionData.OUTPUT.extractValueFrom(commandLine))
            );
        } catch (Throwable e) {
            new HelpFormatter().printHelp("Usage:", options);
            System.exit(0);
            return null;
        }
    }

}
