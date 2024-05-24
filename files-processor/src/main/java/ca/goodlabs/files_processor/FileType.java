package ca.goodlabs.files_processor;

public record FileType(String type, Integer buffer) {
    public FileType createWithDot() {
        return new FileType(".".concat(type), buffer);
    }
}
