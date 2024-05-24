package ca.goodlabs.files_processor;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

enum OptionData {
    FILE_TYPES(
            "t",
            "file-type",
            true,
            "type of file to support (eventually with buffer). Repeat it to pass a list of types (i.e -t pdf-100 -t jpg)"
    ),
    OUTPUT(
            "o",
            "output",
            true,
            "Directory where the files will be moved to"
    );

    private final String opt;
    private final String longOpt;
    private final boolean hasArg;
    private final String description;

    OptionData(String opt, String longOpt, boolean hasArg, String description) {
        this.opt = opt;
        this.longOpt = longOpt;
        this.hasArg = hasArg;
        this.description = description;
    }

    public Option create() {
        return Option.builder(opt)
                .longOpt(longOpt)
                .hasArg(hasArg)
                .desc(description)
                .build();
    }

    public String extractValueFrom(CommandLine commandLine) {
        return commandLine.getOptionValue(opt);
    }

    public String[] extractValuesFrom(CommandLine commandLine) {
        return commandLine.getOptionValues(opt);
    }

}
