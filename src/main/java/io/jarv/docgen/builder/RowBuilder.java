package io.jarv.docgen.builder;

import io.jarv.docgen.style.TextStyle;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;

import java.util.Objects;

public class RowBuilder {

    private final TableBuilder parent;
    private final Tr row;

    RowBuilder(TableBuilder parent, Tr row) {
        this.parent = parent;
        this.row = row;
    }

    public RowBuilder addCell(String text, TextStyle style) {
        Objects.requireNonNull(style, "style");
        Tc cell = WordDocumentBuilder.FACTORY.createTc();

        P paragraph = WordDocumentBuilder.FACTORY.createP();
        R run = WordDocumentBuilder.FACTORY.createR();
        ParagraphBuilder.applyTextStyle(run, style);
        Text t = WordDocumentBuilder.FACTORY.createText();
        t.setValue(text != null ? text : "");
        t.setSpace("preserve");
        run.getContent().add(WordDocumentBuilder.FACTORY.createRT(t));
        paragraph.getContent().add(run);

        cell.getContent().add(paragraph);
        row.getContent().add(cell);
        parent.recordCellForColumnCount();
        return this;
    }

    public TableBuilder endRow() {
        return parent;
    }
}
