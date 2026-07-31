package org.globsframework.saxstack.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeType {
    private final String name;
    private final Map<String, NodeType> children = new HashMap<>();
    private final List<String> attrs;

    public NodeType(String name, Collection<String> attrs) {
        this.name = name;
        this.attrs = new ArrayList<>(attrs);
    }

    public String getName() {
        return name;
    }

    NodeType getChild(String name) {
        return children.get(name);
    }

    public NodeType getOrCreate(String name, Collection<String> attrs) {
        NodeType existing = children.get(name);
        if (existing != null) {
            existing.check(attrs);
            return existing;
        }
        NodeType type = new NodeType(name, attrs);
        children.put(name, type);
        return type;
    }

    private void check(Collection<String> attrs) {
        for (String attr : attrs) {
            if (!this.attrs.contains(attr)) {
                this.attrs.add(attr);
            }
        }
    }

    public NodeType findChild(String name) {
        return children.get(name);
    }

    public boolean containAttr(String name) {
        return attrs.contains(name);
    }
}
