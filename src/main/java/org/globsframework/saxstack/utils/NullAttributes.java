package org.globsframework.saxstack.utils;

import org.xml.sax.Attributes;

/**
 * Empty implementation of the XML {@link Attributes} interface
 */
public class NullAttributes implements Attributes {
    @Override
    public int getLength() {
        return 0;
    }

    @Override
    public String getLocalName(int index) {
        return null;
    }

    @Override
    public String getQName(int index) {
        return null;
    }

    @Override
    public String getType(int index) {
        return null;
    }

    @Override
    public String getURI(int index) {
        return null;
    }

    @Override
    public String getValue(int index) {
        return null;
    }

    @Override
    public int getIndex(String qName) {
        return 0;
    }

    @Override
    public String getType(String qName) {
        return null;
    }

    @Override
    public String getValue(String qName) {
        return null;
    }

    @Override
    public int getIndex(String uri, String localName) {
        return 0;
    }

    @Override
    public String getType(String uri, String localName) {
        return null;
    }

    @Override
    public String getValue(String uri, String localName) {
        return null;
    }
}
