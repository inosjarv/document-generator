package io.jarv.docgen.builder;

import io.jarv.docgen.style.DocumentTheme;
import jakarta.xml.bind.JAXBElement;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Body;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

/** Test helper: build a doc via the fluent API and read it back through docx4j. */
final class RoundTrip {

    private RoundTrip() {}

    @FunctionalInterface
    interface DocAction {
        void apply(WordDocumentBuilder b) throws Exception;
    }

    static Doc of(DocAction action) throws Exception {
        return of(DocumentTheme.defaults(), action);
    }

    static Doc of(DocumentTheme theme, DocAction action) throws Exception {
        byte[] bytes;
        try (WordDocumentBuilder builder = new WordDocumentBuilder(theme)) {
            action.apply(builder);
            bytes = builder.buildAsBytes();
        }
        return read(bytes);
    }

    static Doc read(byte[] bytes) throws Exception {
        return new Doc(WordprocessingMLPackage.load(new ByteArrayInputStream(bytes)));
    }

    /**
     * Thin AutoCloseable wrapper over the loaded package that exposes the collection accessors
     * the tests actually care about (paragraphs, tables, headers, body).
     */
    static final class Doc implements AutoCloseable {
        private final WordprocessingMLPackage pkg;

        Doc(WordprocessingMLPackage pkg) {
            this.pkg = pkg;
        }

        WordprocessingMLPackage pkg() { return pkg; }

        Body body() {
            try {
                return pkg.getMainDocumentPart().getContents().getBody();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        List<P> paragraphs() {
            return filter(body().getContent(), P.class);
        }

        List<Tbl> tables() {
            return filter(body().getContent(), Tbl.class);
        }

        List<HeaderPart> headers() {
            List<HeaderPart> found = new ArrayList<>();
            var rp = pkg.getMainDocumentPart().getRelationshipsPart();
            for (Relationship rel : rp.getJaxbElement().getRelationship()) {
                var part = rp.getPart(rel);
                if (part instanceof HeaderPart hp) found.add(hp);
            }
            return found;
        }

        Hdr firstHeaderContents() throws Exception {
            return (Hdr) headers().get(0).getContents();
        }

        @Override
        public void close() {
            // WordprocessingMLPackage doesn't require explicit close.
        }
    }

    @SuppressWarnings("unchecked")
    static <T> List<T> filter(List<Object> content, Class<T> type) {
        List<T> out = new ArrayList<>();
        for (Object o : content) {
            Object unwrapped = (o instanceof JAXBElement<?> je) ? je.getValue() : o;
            if (type.isInstance(unwrapped)) out.add((T) unwrapped);
        }
        return out;
    }
}
