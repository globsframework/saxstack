package org.globsframework.saxstack.utils;

import org.globsframework.saxstack.writer.Filter;
import org.globsframework.saxstack.writer.XmlTag;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class NodeTypeFilter implements Filter {
    public static final NodeType NULL_NODE_TYPE = new NodeType("", List.of());
    private final Deque<NodeType> nodeType = new ArrayDeque<>();

    public NodeTypeFilter(NodeType root) {
        nodeType.push(root);
    }

    @Override
    public XmlTag enter(XmlTag parent, String tagName) throws IOException {
        NodeType current = nodeType.peek();
        NodeType child = current.findChild(tagName);
        if (child == null) {
            nodeType.push(NULL_NODE_TYPE);
            return parent.createChildTag(tagName);
        }
        nodeType.push(child);
        return new FilteredXmlTag(parent, parent.createChildTag(child.getName()), child);
    }

    @Override
    public void leave() {
        nodeType.pop();
    }

    public static class FilteredXmlTag extends XmlTag {
        private final XmlTag tag;
        private final NodeType nodeType;
        private final XmlTag parentTag;

        public FilteredXmlTag(XmlTag parentTag, XmlTag tag, NodeType nodeType) {
            this.parentTag = parentTag;
            this.tag = tag;
            this.nodeType = nodeType;
        }

        @Override
        public String getTagName() {
            return tag.getTagName();
        }

        @Override
        public XmlTag addAttribute(String attrName, Object attrValue) throws IOException {
            if (nodeType.containAttr(attrName)) {
                tag.addAttribute(attrName, attrValue);
            }
            return this;
        }

        @Override
        public XmlTag addValue(String value) throws IOException {
            tag.addValue(value);
            return this;
        }

        @Override
        public XmlTag addCDataValue(String value) throws IOException {
            tag.addCDataValue(value);
            return this;
        }

        @Override
        public XmlTag createChildTag(String tagName) throws IOException {
            return tag.createChildTag(tagName);
        }

        @Override
        public XmlTag end() throws IOException {
            tag.end();
            return parentTag;
        }

        @Override
        public XmlTag addXmlSubtree(String xml) throws IOException {
            tag.addXmlSubtree(xml);
            return this;
        }
    }
}
