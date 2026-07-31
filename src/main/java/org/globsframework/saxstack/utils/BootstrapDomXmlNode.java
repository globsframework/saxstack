package org.globsframework.saxstack.utils;

import org.globsframework.saxstack.parser.ExceptionHolder;
import org.globsframework.saxstack.parser.XmlNode;
import org.xml.sax.Attributes;

public class BootstrapDomXmlNode implements XmlNode {
    private DomXmlNode child;

    @Override
    public XmlNode getSubNode(String childName, Attributes xmlAttrs, String uri, String fullName) throws ExceptionHolder {
        if (child != null) {
            throw new RuntimeException("Only one child is allowed");
        }
        child = new DomXmlNode(childName, xmlAttrs);
        return child;
    }

    @Override
    public void setValue(String value) throws ExceptionHolder {
        throw new RuntimeException("setValue not allowed on bootstrap node");
    }

    @Override
    public void complete() throws ExceptionHolder {
    }

    public DomXmlNode getChild() {
        return child;
    }

    public boolean contains(BootstrapDomXmlNode otherRoot) {
        return child.contains(otherRoot.child);
    }
}
