package io.jarv.docgen.style;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

public enum PictureType {
    PNG(XWPFDocument.PICTURE_TYPE_PNG, "png"),
    JPEG(XWPFDocument.PICTURE_TYPE_JPEG, "jpg"),
    GIF(XWPFDocument.PICTURE_TYPE_GIF, "gif"),
    BMP(XWPFDocument.PICTURE_TYPE_BMP, "bmp");

    private final int poiType;
    private final String extension;

    PictureType(int poiType, String extension) {
        this.poiType = poiType;
        this.extension = extension;
    }

    public int poiType() { return poiType; }
    public String extension() { return extension; }
}
