package org.globsframework.saxstack.writer;

import java.io.IOException;

public class NullXmlTag extends XmlTag {

    private final String tagName;
    private final XmlTag parent;

    public NullXmlTag(XmlTag parent, String tagName) {
        this.tagName = tagName;
        this.parent = parent;
    }

    @Override
    public String getTagName() {
        return tagName;
    }

    @Override
    public XmlTag addAttribute(String attrName, Object attrValue) throws IOException {
        return this;
    }

    @Override
    public XmlTag addValue(String value) throws IOException {
        return this;
    }

    @Override
    public XmlTag addCDataValue(String value) throws IOException {
        return this;
    }

    @Override
    public XmlTag createChildTag(String tagName) throws IOException {
        return new NullXmlTag(this, tagName);
    }

    @Override
    public XmlTag end() throws IOException {
        return parent;
    }

    @Override
    public XmlTag addXmlSubtree(String xml) throws IOException {
        return this;
    }
}
