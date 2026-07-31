package org.globsframework.saxstack.utils;

import org.globsframework.saxstack.parser.ExceptionHolder;
import org.globsframework.saxstack.parser.SaxStackParser;
import org.globsframework.saxstack.parser.XmlAttributeNotFoundException;
import org.globsframework.saxstack.writer.Filter;
import org.globsframework.saxstack.writer.FilterNone;
import org.globsframework.saxstack.writer.NullXmlTag;
import org.globsframework.saxstack.writer.PrettyPrintRootXmlTag;
import org.globsframework.saxstack.writer.XmlNodeBuilder;
import org.globsframework.saxstack.writer.XmlPrettyPrinter;
import org.globsframework.saxstack.writer.XmlRootBuilder;
import org.globsframework.saxstack.writer.XmlTag;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Built-in entity reference Replaces character
 * {@literal &amp;    & }
 * {@literal &lt;     < }
 * {@literal &gt;     > }
 * {@literal &quot;   " }
 * {@literal &apos;   ' }
 */
public class XmlUtils {
    public static final int AMP_CHARACTER = '&';
    public static final int LT_CHARACTER = '<';
    public static final int GT_CHARACTER = '>';
    public static final int QUOTE_CHARACTER = '"';
    public static final int APOS_CHARACTER = '\'';

    private static final String AMP_STRING = "&";
    private static final String LT_STRING = "<";
    private static final String GT_STRING = ">";
    private static final String QUOTE_STRING = "\"";
    private static final String APOS_STRING = "'";

    private static final String AMP_ENTITY = "&amp;";
    private static final String LT_ENTITY = "&lt;";
    private static final String GT_ENTITY = "&gt;";
    private static final String QUOTE_ENTITY = "&quot;";
    private static final String APOS_ENTITY = "&apos;";
    public static final String LINE_SEPARATOR = System.lineSeparator();

    private XmlUtils() {
    }

    public static String getAttrValue(String xmlAttrName, Attributes xmlAttributes) throws XmlAttributeNotFoundException {
        String val = xmlAttributes.getValue(xmlAttrName);
        if (val == null) {
            throw new XmlAttributeNotFoundException(xmlAttrName);
        }
        return val;
    }

    public static String getAttrValue(String xmlAttrName, Attributes xmlAttributes, String defaultValue) {
        String val = xmlAttributes.getValue(xmlAttrName);
        if (val == null) {
            val = defaultValue;
        }
        return val;
    }

    public static boolean getBooleanAttrValue(String xmlAttrName, Attributes xmlAttributes, boolean defaultValue) {
        String attrValue = getAttrValue(xmlAttrName, xmlAttributes, Boolean.toString(defaultValue));
        return Boolean.parseBoolean(attrValue);
    }

    public static int getIntAttrValue(String xmlAttrName, Attributes xmlAttributes, int defaultValue) throws NumberFormatException {
        String attrValue = getAttrValue(xmlAttrName, xmlAttributes, Integer.toString(defaultValue));
        return Integer.parseInt(attrValue);
    }

    public static double getDoubleAttrValue(String xmlAttrName, Attributes xmlAttributes, double defaultValue) throws NumberFormatException {
        String attrValue = getAttrValue(xmlAttrName, xmlAttributes, Double.toString(defaultValue));
        return Double.parseDouble(attrValue);
    }

    public static String convertEntities(String text) {
        if (text.trim().isEmpty()) {
            return "";
        }
        if (text.indexOf(AMP_CHARACTER) != -1) {
            text = text.replace(AMP_STRING, AMP_ENTITY);
        }
        if (text.indexOf(APOS_CHARACTER) != -1) {
            text = text.replace(APOS_STRING, APOS_ENTITY);
        }
        if (text.indexOf(LT_CHARACTER) != -1) {
            text = text.replace(LT_STRING, LT_ENTITY);
        }
        if (text.indexOf(GT_CHARACTER) != -1) {
            text = text.replace(GT_STRING, GT_ENTITY);
        }
        if (text.indexOf(QUOTE_CHARACTER) != -1) {
            text = text.replace(QUOTE_STRING, QUOTE_ENTITY);
        }
        return text;
    }

    public static String addInCDataValue(String xmlString) {
        return "<![CDATA[" + xmlString + "]]>";
    }

    public static String format(String input, XMLReader parser, int attributeCountOnLine) throws IOException {
        Writer output = new StringWriter();
        SaxStackParser.parse(parser,
                new XmlNodeToBuilder(new PrettyPrintRootXmlTag(output, attributeCountOnLine), null),
                new StringReader(input));
        return output.toString();
    }

    public static String format(String input, XMLReader parser, int attributeCountOnLine, Filter filter) throws IOException {
        Writer output = new StringWriter();
        BootstrapDomXmlNode rootNode = new BootstrapDomXmlNode();
        SaxStackParser.parse(parser, rootNode, new StringReader(input));
        XmlPrettyPrinter.write(output, rootNode.getChild().getBuilder(), filter, attributeCountOnLine);
        return output.toString();
    }

    public static void build(Filter filter, XmlRootBuilder rootBuilder, XmlTag root) throws IOException {
        if (filter == null) {
            filter = new FilterNone();
        }
        String tagName = rootBuilder.getTagName();
        XmlTag xmlTag = filter.enter(root, tagName);
        XmlNodeBuilder[] xmlExporterNodes = rootBuilder.process(xmlTag);
        write(xmlTag, xmlExporterNodes, filter);
        xmlTag.end();
        filter.leave();
    }

    private static void write(XmlTag parentTag, XmlNodeBuilder[] xmlWriters, Filter filter)
            throws IOException {
        for (XmlNodeBuilder xmlWriter : xmlWriters) {
            write(parentTag, xmlWriter, filter);
        }
    }

    private static void write(XmlTag parentTag, XmlNodeBuilder xmlBuilder, Filter factory) throws IOException {
        if (parentTag instanceof NullXmlTag) {
            return;
        }
        while (xmlBuilder.hasNext()) {
            String nextTagName = xmlBuilder.getNextTagName();
            XmlTag newTag = factory.enter(parentTag, nextTagName);
            write(newTag, xmlBuilder.processNext(newTag), factory);
            factory.leave();
            newTag.end();
        }
        xmlBuilder.levelDone();
    }

    public static XMLReader getXmlReader() {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            SAXParser parser = factory.newSAXParser();
            return parser.getXMLReader();
        } catch (ParserConfigurationException | SAXException e) {
            throw new ExceptionHolder(e);
        }
    }
}
