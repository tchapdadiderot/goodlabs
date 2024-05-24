package ca.goodlabs.files_processor;

public class Main {

    public static void main(String... args) {
        new Application(new ArgumentsReader(args).read()).run();
    }

}
