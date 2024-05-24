package ca.goodlabs.files_processor;

import java.nio.file.Path;
import java.util.List;

public record Arguments(List<FileType> filesTypes, Path destinationDirectory) {
}
