package org.globsframework.saxstack.parser;

public class XmlAttributeNotFoundException extends XmlParsingException {
    private final String attrName;

    public XmlAttributeNotFoundException(String xmlAttrName) {
        super("Xml attribute " + xmlAttrName + " is missing");
        attrName = xmlAttrName;
    }

    public String getAttrName() {
        return attrName;
    }
}
