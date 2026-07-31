package org.globsframework.saxstack.writer;

import java.io.IOException;

public abstract class AbstractRootXmlTag extends XmlTag {
    @Override
    public String getTagName() {
        throw new XmlWriterException();
    }

    @Override
    public XmlTag addAttribute(String attrName, Object attrValue) throws IOException {
        throw new XmlWriterException();
    }

    @Override
    public XmlTag addValue(String value) throws IOException {
        throw new XmlWriterException();
    }

    @Override
    public XmlTag addCDataValue(String value) throws IOException {
        throw new XmlWriterException();
    }

    @Override
    public XmlTag end() throws IOException {
        throw new XmlWriterException();
    }

    @Override
    public XmlTag addXmlSubtree(String xml) throws IOException {
        throw new XmlWriterException();
    }
}
