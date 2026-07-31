package org.globsframework.saxstack.comparator;

import org.globsframework.saxstack.parser.SaxStackParser;
import org.globsframework.saxstack.parser.XmlNode;
import org.globsframework.saxstack.utils.BootstrapDomXmlNode;
import org.globsframework.saxstack.utils.NodeType;
import org.globsframework.saxstack.utils.NodeTypeFilter;
import org.globsframework.saxstack.utils.XmlUtils;
import org.globsframework.saxstack.writer.SaxStackWriter;
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Performs comparisons between XML streams, using several diff policies.
 * Three policies are available:
 * <ul>
 * <li>The {@link #areEqual} methods check that the two streams are strictly identical, with tags in the same order</li>
 * <li>The {@link #areEquivalent} methods check that the two streams are identical but without taking the order
 * of tags into account</li>
 * <li>The {@link #computeDiff} method performs a comparison in which the expected stream is used
 * as a reference for identifying which XML attributes are relevant for the comparison.</li>
 * </ul>
 */
public class XmlComparator {
    public static final String SPECIAL_CHARS = "< > ' \" &";
    public static final String ESCAPED_SPECIAL_CHARS = "&lt; &gt; &apos; &quot; &amp;";

    private XmlComparator() {
        // Static class
    }

    public static boolean areEquivalent(String expectedXml, String resultXml, XMLReader parser) throws Exception {
        if (expectedXml.equals(resultXml)) {
            return true;
        }
        XmlComparableNode aNode = createXmlEquivalentComparableNode(new StringReader(expectedXml), parser);
        XmlComparableNode bNode = createXmlEquivalentComparableNode(new StringReader(resultXml), parser);
        return aNode.equals(bNode);
    }

    /**
     * Represents the result of a comparison in which the expected stream is used as a filter
     * for identifying which XML attributes are relevant.
     */
    public static class Diff {
        private final String expected;
        private final String filteredActual;

        public Diff(String expected, String filteredActual) {
            this.filteredActual = filteredActual;
            this.expected = expected;
        }

        /**
         * Returns the actual stream filtered to show only attributes which are present in the XML stream
         */
        public String getFilteredActual() {
            return filteredActual;
        }

        public String getExpected() {
            return expected;
        }
    }

    public static Diff computeDiff(String expectedXml, String actualXml, XMLReader parser) throws Exception {
        if (expectedXml.equals(actualXml)) {
            return null;
        }
        BootstrapDomXmlNode expectedBootstrap = new BootstrapDomXmlNode();
        BootstrapDomXmlNode actualBootstrap = new BootstrapDomXmlNode();
        SaxStackParser.parse(parser, expectedBootstrap, new StringReader(expectedXml));
        SaxStackParser.parse(parser, actualBootstrap, new StringReader(actualXml));
        if (!expectedBootstrap.contains(actualBootstrap)) {
            NodeType bootstrapNodeType = new NodeType("", List.of());
            expectedBootstrap.getChild().populateNodeType(bootstrapNodeType);
            StringWriter writer = new StringWriter();
            SaxStackWriter.write(writer, actualBootstrap.getChild().getBuilder(), new NodeTypeFilter(bootstrapNodeType));
            return new Diff(expectedXml, writer.toString());
        }
        return null;
    }

    public static boolean areEquivalent(Reader xmlA, Reader xmlB, XMLReader parser) throws Exception {
        return areEquivalent(getReader(xmlA), getReader(xmlB), parser);
    }

    public static boolean areEqual(String xmlA, String xmlB, XMLReader parser) throws Exception {
        XmlComparableNode aNode = createXmlEqualComparableNode(new StringReader(xmlA), parser);
        XmlComparableNode bNode = createXmlEqualComparableNode(new StringReader(xmlB), parser);
        return aNode.equals(bNode);
    }

    public static boolean areEqual(Reader xmlA, Reader xmlB, XMLReader parser) throws Exception {
        return createXmlEqualComparableNode(xmlA, parser).equals(createXmlEqualComparableNode(xmlB, parser));
    }

    static String getReader(Reader reader) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(reader);
        String readingString = bufferedReader.readLine();
        while (readingString != null) {
            builder.append(readingString);
            readingString = bufferedReader.readLine();
        }
        return builder.toString();
    }

    private static XmlComparableNode createXmlEqualComparableNode(Reader reader, XMLReader parser) throws Exception {
        XmlEqualComparator comparableNode = new XmlEqualComparator();
        SaxStackParser.parse(parser, comparableNode, reader);
        return comparableNode;
    }

    private static XmlComparableNode createXmlEquivalentComparableNode(Reader reader, XMLReader parser) throws Exception {
        XmlEquivalentComparator comparableNode = new XmlEquivalentComparator();
        SaxStackParser.parse(parser, comparableNode, reader);
        return comparableNode;
    }

    abstract static class XmlComparableNode implements XmlNode {
        protected String tag;
        protected Map<String, String> attributes;
        protected Map<XmlComparableNode, Integer> childrenOccurences = new HashMap<>();
        protected List<XmlComparableNode> children = new ArrayList<>();
        protected String text = "";

        protected abstract boolean comparator(XmlComparableNode other);

        public XmlComparableNode(String tag, Attributes attributes) {
            this.tag = tag;
            this.attributes = map(attributes);
        }

        @Override
        public String toString() {
            if ("root".equals(tag)) {
                return childrenString();
            } else if (!childrenOccurences.isEmpty() || !text.isEmpty()) {
                return "<" + tag + attributeString() + ">" + childrenString() + text + "</" + tag + ">";
            } else {
                return "<" + tag + attributeString() + "/>";
            }
        }

        public String getCurrentTagName() {
            return null;
        }

        @Override
        public void setValue(String value) {
            text = XmlUtils.convertEntities(value
                    .replace("\n", "")
                    .replace(XmlUtils.LINE_SEPARATOR, "")
                    .replaceAll(" +", " ")
                    .trim());
        }

        @Override
        public void complete() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof XmlComparableNode comparableXml)) {
                return false;
            }
            if (!Objects.equals(attributes, comparableXml.attributes)) {
                return false;
            }
            if (!Objects.equals(tag, comparableXml.tag)) {
                return false;
            }
            if (!Objects.equals(text, comparableXml.text)) {
                return false;
            }

            return comparator(comparableXml);
        }

        @Override
        public int hashCode() {
            int result = tag.hashCode();
            result = 31 * result + attributes.hashCode();
            return result;
        }

        private Map<String, String> map(Attributes xmlAttrs) {
            Map<String, String> map = new HashMap<>();
            if (xmlAttrs == null) {
                return map;
            }
            for (int i = 0; i < xmlAttrs.getLength(); i++) {
                map.put(xmlAttrs.getLocalName(i), xmlAttrs.getValue(i));
            }
            return map;
        }

        protected void addChild(XmlComparableNode child) {
            childrenOccurences.merge(child, 1, Integer::sum);
            children.add(child);
        }

        private String childrenString() {
            StringBuilder sb = new StringBuilder();
            for (XmlComparableNode node : childrenOccurences.keySet()) {
                sb.append(node);
                sb.append("\n");
            }
            return sb.toString();
        }

        private String attributeString() {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> e : attributes.entrySet()) {
                sb.append(" ").append(e.getKey()).append("=\"").append(e.getValue()).append('"');
            }
            return sb.toString();
        }
    }

    static class XmlEquivalentComparator extends XmlComparableNode {
        public XmlEquivalentComparator(String tag, Attributes attributes) {
            super(tag, attributes);
        }

        public XmlEquivalentComparator() {
            super("root", null);
        }

        @Override
        public XmlNode getSubNode(String childName, Attributes xmlAttrs, String uri, String fullName) {
            XmlComparableNode o = new XmlEquivalentComparator(childName, xmlAttrs);
            addChild(o);
            return o;
        }

        @Override
        protected boolean comparator(XmlComparableNode other) {
            return Objects.equals(childrenOccurences, other.childrenOccurences);
        }
    }

    static class XmlEqualComparator extends XmlComparableNode {
        public XmlEqualComparator(String tag, Attributes attributes) {
            super(tag, attributes);
        }

        public XmlEqualComparator() {
            super("root", null);
        }

        @Override
        public XmlNode getSubNode(String childName, Attributes xmlAttrs, String uri, String fullName) {
            XmlComparableNode o = new XmlEqualComparator(childName, xmlAttrs);
            addChild(o);
            return o;
        }

        @Override
        protected boolean comparator(XmlComparableNode other) {
            return Objects.equals(children, other.children);
        }
    }

}
