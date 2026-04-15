package predawn.infrastructure.file;

import predawn.domain.file.exception.NotAllowedFileException;

import java.util.Set;

public class FileValidator {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "pdf", "txt");

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "application/pdf",
            "text/plain"
    );

    public static void validate(String ext, String contentType) {
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new NotAllowedFileException();
        }

        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new NotAllowedFileException();
        }
    }
}
