package org.globsframework.saxstack.writer;

import org.globsframework.saxstack.utils.XmlUtils;

import java.io.IOException;
import java.io.Writer;

// TODO: addCData

public final class PrettyPrintXmlTag extends XmlTag {
    private static final String EMPTY =
            "                                                                                                            ";

    private final Writer writer;
    private final XmlTag parent;
    private final int attributeCountOnLine;
    private String tagName;
    private boolean closed = false;
    private PrettyPrintXmlTag child;
    private int level;
    private int attributeCount;

    PrettyPrintXmlTag(Writer writer, XmlTag parent, String tag, int level, int attributeCountOnLine) {
        this.attributeCountOnLine = attributeCountOnLine;
        this.writer = writer;
        this.parent = parent;
        init(tag, level);
    }

    @Override
    public String getTagName() {
        return tagName;
    }

    @Override
    public XmlTag addAttribute(String attrName, Object attrValue) throws IOException {
        if (attrValue == null) {
            return this;
        }
        if (closed) {
            throw new RuntimeException("Bad use of 'addAttribute' method after tag closure");
        }
        if (attributeCountOnLine == attributeCount) {
            writer.write('\n');
            writeSpace(level + tagName.length() + 1);
            attributeCount = 0;
        } else {
            attributeCount++;
        }

        writer.write(' ');
        writer.write(attrName);
        writer.write("=\"");
        writer.write(normalize(attrValue.toString()));
        writer.write('"');
        return this;
    }

    private void writeSpace(int len) throws IOException {
        while (len > EMPTY.length()) {
            writer.write(EMPTY);
            len = len - EMPTY.length();
        }
        writer.write(EMPTY, 0, len);
    }

    // TODO: renvoyer une interface limitée puisqu'on ne peut plus faire addAttribute (closeSup déjà fait)
    @Override
    public XmlTag addValue(String value) throws IOException {
        value = value.replace("\n", "").trim();
        if (!value.isEmpty()) {
            closeAndIndent();
            writer.write(normalize(value));
        }
        return this;
    }

    private void closeAndIndent() throws IOException {
        closeSup();
        writer.write("\n");
        writeSpace(level + 2);
    }

    @Override
    public XmlTag addCDataValue(String value) throws IOException {
        closeAndIndent();
        writer.write(XmlUtils.addInCDataValue(value));
        return this;
    }

    @Override
    public XmlTag createChildTag(String tagName) throws IOException {
        closeAndIndent();
        writer.write("<");
        writer.write(tagName);
        if (child == null) {
            child = new PrettyPrintXmlTag(writer, this, tagName, level + 2, attributeCountOnLine);
        } else {
            child.init(tagName, level + 2);
        }
        return child;
    }

    @Override
    public XmlTag end() throws IOException {
        if (closed) {
            writer.write("\n");
            writeSpace(level);
            writer.write("</");
            writer.write(tagName);
            writer.write(">");
        } else {
            writer.write("/>");
        }
        return parent;
    }

    private void init(String tagName, int level) {
        this.level = level;
        this.tagName = tagName;
        closed = false;
        attributeCount = 0;
    }

    private void closeSup() throws IOException {
        if (!closed) {
            closed = true;
            writer.write(">");
        }
    }

    private String normalize(String value) {
        return XmlUtils.convertEntities(value);
    }

    @Override
    public XmlTag addXmlSubtree(String xml) throws IOException {
        closeSup();
        writer.write(xml);
        return this;
    }
}
