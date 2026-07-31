package org.globsframework.saxstack.writer;

import java.io.IOException;

public class FilterNone implements Filter {
    @Override
    public XmlTag enter(XmlTag parent, String tagName) throws IOException {
        return parent.createChildTag(tagName);
    }

    @Override
    public void leave() {
    }
}
