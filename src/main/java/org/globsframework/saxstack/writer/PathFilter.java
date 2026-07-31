package org.globsframework.saxstack.writer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PathFilter implements Filter {
    private final PathInfo[] path;
    private int index;

    public PathFilter(String path) {
        if (path == null) {
            this.path = null;
        } else {
            List<PathInfo> pathTmp = new ArrayList<>();
            for (String token : path.split("/")) {
                if (token.isEmpty()) {
                    continue;
                }
                int attrIndex = token.indexOf('[');
                PathInfo pathInfo;
                if (attrIndex != -1) {
                    pathInfo = new PathInfo(token.substring(0, attrIndex));
                    pathInfo.createEmpty();
                    for (String attr : token.substring(attrIndex + 1, token.length() - 1).split(",")) {
                        String trimmed = attr.trim();
                        if (!trimmed.isEmpty()) {
                            pathInfo.add(trimmed);
                        }
                    }
                } else {
                    pathInfo = new PathInfo(token);
                }
                pathTmp.add(pathInfo);
            }
            this.path = pathTmp.toArray(new PathInfo[0]);
        }
    }

    @Override
    public XmlTag enter(XmlTag parent, String tagName) throws IOException {
        try {
            if (path == null) {
                return parent.createChildTag(tagName);
            }
            if (index < path.length) {
                if (path[index].shouldEnter(tagName)) {
                    return new FilteredXmlTag(parent.createChildTag(tagName));
                } else {
                    return new NullXmlTag(parent, tagName);
                }
            }
            if (path.length != 0 && path[path.length - 1].isAllSubTree()) {
                return new FilteredXmlTag(parent.createChildTag(tagName));
            }
            return new NullXmlTag(parent, tagName);
        } finally {
            index++;
        }
    }

    @Override
    public void leave() {
        index--;
    }

    static class PathInfo {
        private final String directoryName;
        private final boolean allSubTree;
        private List<String> attrs;

        public PathInfo(String directoryName) {
            this.directoryName = directoryName;
            this.allSubTree = directoryName.equals("*");
        }

        void add(String attr) {
            if (attrs == null) {
                attrs = new ArrayList<>();
            }
            attrs.add(attr);
        }

        public void createEmpty() {
            attrs = new ArrayList<>();
        }

        public boolean accept(String attrName) {
            if (allSubTree) {
                return true;
            }
            if (attrs == null) {
                return true;
            }
            return attrs.contains(attrName);
        }

        boolean shouldEnter(String directory) {
            if (allSubTree) {
                return true;
            }
            return directoryName.equals(directory);
        }

        public boolean isAllSubTree() {
            return allSubTree;
        }
    }

    private class FilteredXmlTag extends XmlTag {
        private final XmlTag childTag;

        public FilteredXmlTag(XmlTag childTag) {
            this.childTag = childTag;
        }

        @Override
        public String getTagName() {
            return childTag.getTagName();
        }

        @Override
        public XmlTag addAttribute(String attrName, Object attrValue) throws IOException {
            if (path == null) {
                childTag.addAttribute(attrName, attrValue);
            } else if (index <= path.length) {
                if (path[index - 1].accept(attrName)) {
                    childTag.addAttribute(attrName, attrValue);
                }
            } else {
                if (path[path.length - 1].isAllSubTree()) {
                    childTag.addAttribute(attrName, attrValue);
                }
            }
            return this;
        }

        @Override
        public XmlTag addValue(String value) throws IOException {
            childTag.addValue(value);
            return this;
        }

        @Override
        public XmlTag addCDataValue(String value) throws IOException {
            childTag.addCDataValue(value);
            return this;
        }

        @Override
        public XmlTag createChildTag(String tagName) throws IOException {
            return childTag.createChildTag(tagName);
        }

        @Override
        public XmlTag end() throws IOException {
            childTag.end();
            return this;
        }

        @Override
        public XmlTag addXmlSubtree(String xml) throws IOException {
            childTag.addXmlSubtree(xml);
            return this;
        }
    }
}
