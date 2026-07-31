package org.globsframework.saxstack.writer;

import java.util.Collection;
import java.util.Iterator;

/**
 * Base class for the builders that emit one tag per element of a collection.
 *
 * @param <T> the type of the items being written
 */
public abstract class IteratorBasedXmlNodeBuilder<T> implements XmlNodeBuilder {
    private final String tagName;
    private final Iterator<? extends T> iterator;

    protected IteratorBasedXmlNodeBuilder(String tagName, Iterator<? extends T> iterator) {
        this.tagName = tagName;
        this.iterator = iterator;
    }

    protected IteratorBasedXmlNodeBuilder(String tagName, Collection<? extends T> collection) {
        this(tagName, collection.iterator());
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public String getNextTagName() {
        return tagName;
    }

    protected T getNextItem() {
        return iterator.next();
    }
}
