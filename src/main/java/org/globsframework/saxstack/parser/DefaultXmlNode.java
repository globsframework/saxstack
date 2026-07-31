package org.globsframework.saxstack.parser;

import org.xml.sax.Attributes;

public class DefaultXmlNode implements XmlNode {

    @Override
    public XmlNode getSubNode(String childName, Attributes xmlAttrs, String uri, String fullName) {
        return SilentXmlNode.INSTANCE;
    }

    @Override
    public void setValue(String value) {
    }

    /**
     * Default implementation does nothing.
     */
    @Override
    public void complete() {
    }

}
