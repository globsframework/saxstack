package org.globsframework.saxstack.utils;

import org.globsframework.saxstack.parser.ExceptionHolder;
import org.globsframework.saxstack.parser.XmlNode;
import org.globsframework.saxstack.writer.XmlNodeBuilder;
import org.globsframework.saxstack.writer.XmlRootBuilder;
import org.globsframework.saxstack.writer.XmlTag;
import org.xml.sax.Attributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DomXmlNode implements XmlNode {
    private final Map<String, String> attrs = new TreeMap<>();
    private final List<DomXmlNode> children = new ArrayList<>();
    private String text = "";
    private String xmlTagName = "";

    public DomXmlNode(String xmlTagName, Attributes xmlAttrs) {
        this.xmlTagName = xmlTagName;
        for (int i = 0; i < xmlAttrs.getLength(); i++) {
            attrs.put(xmlAttrs.getLocalName(i), xmlAttrs.getValue(i));
        }
    }

    public DomXmlNode() {
    }

    @Override
    public XmlNode getSubNode(String childName, Attributes xmlAttrs, String uri, String fullName) throws ExceptionHolder {
        DomXmlNode expectedXmlNode = new DomXmlNode(childName, xmlAttrs);
        children.add(expectedXmlNode);
        return expectedXmlNode;
    }

    @Override
    public void setValue(String value) throws ExceptionHolder {
        text = XmlUtils.convertEntities(
                value.replace("\n", "")
                        .replace(XmlUtils.LINE_SEPARATOR, "")
                        .replaceAll(" +", " ")
                        .trim());
    }

    @Override
    public void complete() throws ExceptionHolder {
    }

    public boolean contains(DomXmlNode actualRoot) {
        if (!xmlTagName.equals(actualRoot.xmlTagName)) {
            return false;
        }
        if (!text.equals(actualRoot.text)) {
            return false;
        }
        for (Map.Entry<String, String> attr : attrs.entrySet()) {
            String actualValue = actualRoot.attrs.get(attr.getKey());
            if (actualValue == null || !actualValue.equals(attr.getValue())) {
                return false;
            }
        }
        if (children.size() != actualRoot.children.size()) {
            return false;
        }

        List<DomXmlNode> tempList = new ArrayList<>(actualRoot.children);
        for (DomXmlNode expected : children) {
            for (Iterator<DomXmlNode> actualIterator = tempList.iterator(); actualIterator.hasNext(); ) {
                if (expected.contains(actualIterator.next())) {
                    actualIterator.remove();
                    break;
                }
            }
        }
        return tempList.isEmpty();
    }

    public XmlRootBuilder getBuilder() {
        return new XmlRootBuilder() {
            @Override
            public String getTagName() {
                return DomXmlNode.this.xmlTagName;
            }

            @Override
            public XmlNodeBuilder[] process(XmlTag rootTag) throws IOException {
                DomXmlNodeBuilder.dumpAttributes(rootTag, DomXmlNode.this.attrs);
                return new XmlNodeBuilder[]{
                        new DomXmlNodeBuilder(DomXmlNode.this.children.iterator())
                };
            }
        };
    }

    private static class DomXmlNodeBuilder implements XmlNodeBuilder {
        private final Iterator<DomXmlNode> children;
        private DomXmlNode child;

        public DomXmlNodeBuilder(Iterator<DomXmlNode> children) {
            this.children = children;
        }

        @Override
        public boolean hasNext() {
            return children.hasNext();
        }

        @Override
        public String getNextTagName() {
            child = children.next();
            return child.xmlTagName;
        }

        @Override
        public XmlNodeBuilder[] processNext(XmlTag tag) throws IOException {
            dumpAttributes(tag, child.attrs);
            return new XmlNodeBuilder[]{
                    new DomXmlNodeBuilder(child.children.iterator())
            };
        }

        @Override
        public void levelDone() {

        }

        private static void dumpAttributes(XmlTag tag, Map<String, String> attrs) throws IOException {
            for (Map.Entry<String, String> entry : attrs.entrySet()) {
                tag.addAttribute(entry.getKey(), entry.getValue());
            }
        }
    }

    public void populateNodeType(NodeType parent) {
        NodeType child = parent.getOrCreate(xmlTagName, this.attrs.keySet());
        for (DomXmlNode node : children) {
            node.populateNodeType(child);
        }
    }
}
