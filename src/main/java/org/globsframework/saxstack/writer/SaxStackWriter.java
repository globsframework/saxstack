package org.globsframework.saxstack.writer;

import org.globsframework.saxstack.utils.XmlUtils;

import java.io.IOException;
import java.io.Writer;

/**
 * SaxStackWriter implement SaxStackBuilder
 */

public class SaxStackWriter implements SaxStackBuilder {
    public static final String XML_HEADER = "<?xml version='1.0' encoding='UTF-8'?>";
    private final Writer writer;

    public SaxStackWriter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void write(XmlRootBuilder xmlBuilder) throws IOException {
        write(xmlBuilder, new FilterNone());
        writer.flush();
    }

    @Override
    public void write(XmlRootBuilder rootBuilder, Filter filter) throws IOException {
        XmlUtils.build(filter, rootBuilder, new RootXmlTag(writer));
        writer.flush();
    }

    public static void write(Writer writer, XmlRootBuilder xmlBuilder, Filter filter) throws IOException {
        new SaxStackWriter(writer).write(xmlBuilder, filter);
    }

    public static void write(Writer writer, XmlRootBuilder rootBuilder) throws IOException {
        new SaxStackWriter(writer).write(rootBuilder, new FilterNone());
    }

}
