package org.globsframework.saxstack.utils;

@FunctionalInterface
public interface AttributeOrderProvider {
    AttributeOrderProvider NULL_ATTRIBUTE = xmlTagName -> null;

    String getOrderedAttribute(String xmlTagName);
}
