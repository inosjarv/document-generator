package io.jarv.docgen.builder;

import io.jarv.docgen.style.DocumentTheme;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/** Test helper: build a doc via the fluent API and read it back through POI. */
final class RoundTrip {

    private RoundTrip() {}

    @FunctionalInterface
    interface DocAction {
        void apply(WordDocumentBuilder b) throws Exception;
    }

    static XWPFDocument of(DocAction action) throws Exception {
        return of(DocumentTheme.defaults(), action);
    }

    static XWPFDocument of(DocumentTheme theme, DocAction action) throws Exception {
        byte[] bytes;
        try (WordDocumentBuilder builder = new WordDocumentBuilder(theme)) {
            action.apply(builder);
            bytes = builder.buildAsBytes();
        }
        return read(bytes);
    }

    static XWPFDocument read(byte[] bytes) throws IOException {
        return new XWPFDocument(new ByteArrayInputStream(bytes));
    }
}
