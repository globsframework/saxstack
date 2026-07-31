package org.globsframework.saxstack.utils;

import org.globsframework.saxstack.parser.ExceptionHolder;
import org.globsframework.saxstack.parser.XmlNode;
import org.globsframework.saxstack.writer.XmlTag;
import org.xml.sax.Attributes;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class XmlNodeToBuilder implements XmlNode {
    private final XmlTag xmlTag;

    public XmlNodeToBuilder(XmlTag xmlTag, Attributes xmlAttrs) throws IOException {
        this.xmlTag = xmlTag;
        if (xmlAttrs == null) {
            return;
        }
        int attrCount = xmlAttrs.getLength();
        Info[] attrs = new Info[attrCount];
        for (int i = 0; i < attrCount; i++) {
            attrs[i] = new Info(xmlAttrs.getLocalName(i), xmlAttrs.getValue(i));
        }
        Arrays.sort(attrs, Comparator.comparing(Info::attrName));
        for (Info attr : attrs) {
            xmlTag.addAttribute(attr.attrName(), attr.attrValue());
        }
    }

    record Info(String attrName, String attrValue) {
    }

    @Override
    public XmlNode getSubNode(String childName, Attributes xmlAttrs, String uri, String fullName) {
        try {
            return new XmlNodeToBuilder(xmlTag.createChildTag(childName), xmlAttrs);
        } catch (IOException e) {
            throw new ExceptionHolder(e);
        }
    }

    @Override
    public void setValue(String value) {
        try {
            xmlTag.addValue(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void complete() {
        try {
            xmlTag.end();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
