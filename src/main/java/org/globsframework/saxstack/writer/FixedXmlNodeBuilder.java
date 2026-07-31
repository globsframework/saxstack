package org.globsframework.saxstack.writer;

import java.io.IOException;

public class FixedXmlNodeBuilder implements XmlNodeBuilder {

    private final String tagName;
    private final boolean pruneIfEmpty;
    private final XmlNodeBuilder[] children;
    private boolean wasProcessed = false;

    public FixedXmlNodeBuilder(String tagName, XmlNodeBuilder child, boolean pruneIfEmpty) {
        this(tagName, new XmlNodeBuilder[]{child}, pruneIfEmpty);
    }

    public FixedXmlNodeBuilder(String tagName, XmlNodeBuilder[] children, boolean pruneIfEmpty) {
        this.children = children;
        this.tagName = tagName;
        this.pruneIfEmpty = pruneIfEmpty;
    }

    @Override
    public boolean hasNext() {
        if (wasProcessed) {
            return false;
        }
        if (!pruneIfEmpty) {
            return true;
        }
        for (XmlNodeBuilder child : children) {
            if (child.hasNext()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getNextTagName() {
        return tagName;
    }

    @Override
    public XmlNodeBuilder[] processNext(XmlTag tag) throws IOException {
        wasProcessed = true;
        return children;
    }

    @Override
    public void levelDone() {

    }
}
