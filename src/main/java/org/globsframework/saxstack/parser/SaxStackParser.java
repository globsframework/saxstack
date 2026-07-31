package org.globsframework.saxstack.parser;

import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Deque;

public class SaxStackParser extends DefaultHandler {
    private final Deque<XmlNode> nodesStack = new ArrayDeque<>();
    private final StringBuilder charBuffer = new StringBuilder();
    private final XMLReader parser;

    private SaxStackParser(XMLReader parser) {
        this.parser = parser;
        parser.setErrorHandler(this);
        parser.setContentHandler(this);
    }

    public static void parse(XMLReader parser, XmlNode rootNode, File xmlFile) throws ExceptionHolder, IOException {
        try (Reader reader = new BufferedReader(new FileReader(xmlFile))) {
            parse(parser, rootNode, reader);
        }
    }

    public static void parse(XMLReader parser, XmlNode rootNode, Reader reader) throws ExceptionHolder {
        new SaxStackParser(parser).parseXml(rootNode, reader, null);
    }

    public static void parse(XMLReader parser, XmlNode rootNode, Reader reader, EntityResolver entityResolver) throws ExceptionHolder {
        new SaxStackParser(parser).parseXml(rootNode, reader, entityResolver);
    }

    private void parseXml(XmlNode rootNode, Reader reader, EntityResolver entityResolver) throws ExceptionHolder {
        nodesStack.clear();
        nodesStack.push(rootNode);
        try {
            if (entityResolver != null) {
                parser.setEntityResolver(entityResolver);
            }
            parser.parse(new InputSource(reader));
        } catch (SAXExceptionHolder e) {
            throw e.getExceptionHolder();
        } catch (SAXException | IOException e) {
            throw new ExceptionHolder(e);
        }
    }

    @Override
    public void startElement(String uri, String local, String qName, Attributes atts) {
        charBuffer.setLength(0);
        nodesStack.push(nodesStack.peek().getSubNode(local, atts, uri, qName));
    }

    @Override
    public void characters(char[] chars, int start, int length) {
        charBuffer.append(chars, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        XmlNode node = nodesStack.pop();
        if (charBuffer.length() != 0) {
            node.setValue(charBuffer.toString());
        }
        charBuffer.setLength(0);
        node.complete();
    }

    public static class SAXExceptionHolder extends SAXException {
        private final ExceptionHolder exceptionHolder;

        public SAXExceptionHolder(ExceptionHolder e) {
            super(e.getInner());
            this.exceptionHolder = e;
        }

        public ExceptionHolder getExceptionHolder() {
            return exceptionHolder;
        }

    }

}
