package io.jarv.docgen.style;

/**
 * Marker for how to name an embedded image. docx4j detects the actual image format from the byte
 * header, so this enum is only used for the filename hint of the embedded part.
 */
public enum PictureType {
    PNG("png"),
    JPEG("jpg"),
    GIF("gif"),
    BMP("bmp");

    private final String extension;

    PictureType(String extension) {
        this.extension = extension;
    }

    public String extension() { return extension; }
}
