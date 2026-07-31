package org.globsframework.saxstack.writer;

import java.io.IOException;
import java.io.Writer;

public class PrettyPrintRootXmlTag extends AbstractRootXmlTag {
    private final Writer writer;
    private final int attributeCountOnLine;

    public PrettyPrintRootXmlTag(Writer writer, int attributeCountOnLine) {
        this.writer = writer;
        this.attributeCountOnLine = attributeCountOnLine;
    }

    @Override
    public XmlTag createChildTag(String rootTag) throws IOException {
        writer.write('<');
        writer.write(rootTag);
        return new PrettyPrintXmlTag(writer, null, rootTag, 0, attributeCountOnLine);
    }
}
