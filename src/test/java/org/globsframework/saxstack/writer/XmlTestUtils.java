package org.globsframework.saxstack.writer;

import org.globsframework.saxstack.comparator.XmlComparator;
import org.globsframework.saxstack.utils.XmlUtils;
import org.junit.Assert;
import org.xml.sax.XMLReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class XmlTestUtils {

    public static void assertEquivalent(String expectedXml, String resultXml) throws Exception {
        XMLReader reader = XmlUtils.getXmlReader();
        if (!XmlComparator.areEquivalent(expectedXml, resultXml, reader)) {
            Assert.assertEquals(XmlUtils.format(expectedXml, reader, 4),
                    XmlUtils.format(resultXml, reader, 4));
        }
    }

    public static void assertEquivalent(Reader xmlA, Reader xmlB) throws Exception {
        assertEquivalent(readerToString(xmlA), readerToString(xmlB));
    }

    public static void assertEquals(String xmlA, String xmlB) throws Exception {
        XMLReader reader = XmlUtils.getXmlReader();
        if (!XmlComparator.areEqual(xmlA, xmlB, reader)) {
            Assert.assertEquals(XmlUtils.format(xmlA, reader, 4),
                    XmlUtils.format(xmlB, reader, 4));
        }
    }

    public static void assertEquals(Reader xmlA, Reader xmlB) throws Exception {
        assertEquals(readerToString(xmlA), readerToString(xmlB));
    }

    static String readerToString(Reader reader) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(reader);
        String readingString = bufferedReader.readLine();
        while (readingString != null) {
            builder.append(readingString);
            readingString = bufferedReader.readLine();
        }
        return builder.toString();
    }
}
