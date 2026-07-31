package org.globsframework.saxstack.writer;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

public class SaxStackWriterWithVariableTagNamesTest extends SaxStackWriterTestCase {
    @Test
    public void testStandardOutput() throws Exception {
        checkOutput(new RootBuilder(root),
                "<contacts>" +
                        "  <me phone='512'/>" +
                        "  <family>" +
                        "    <Grandpa phone='567'/>" +
                        "    <home>" +
                        "      <Bart phone='756'/>" +
                        "      <Homer phone='757'/>" +
                        "    </home>" +
                        "  </family>" +
                        "  <work>" +
                        "    <Ralph phone='123'/>" +
                        "  </work>" +
                        "</contacts>");
    }

    @Test
    public void testFilteredOutput() throws Exception {
        checkOutput(new RootBuilder(root),
                "contacts/family/home/Bart",
                "<contacts>" +
                        "  <family>" +
                        "    <home>" +
                        "      <Bart phone='756'/>" +
                        "    </home>" +
                        "  </family>" +
                        "</contacts>");

        checkOutput(new RootBuilder(root),
                "contacts/family/home/Homer",
                "<contacts>" +
                        "  <family>" +
                        "    <home>" +
                        "      <Homer phone='757'/>" +
                        "    </home>" +
                        "  </family>" +
                        "</contacts>");

        checkOutput(new RootBuilder(root),
                "contacts/family/home",
                "<contacts>" +
                        "  <family>" +
                        "    <home/>" +
                        "  </family>" +
                        "</contacts>");
    }

    @Test
    public void testFilterAttribut() throws Exception {
        checkOutput(new RootBuilder(root),
                "contacts/family/home/Homer[]",
                "<contacts>" +
                        "  <family>" +
                        "    <home>" +
                        "      <Homer/>" +
                        "    </home>" +
                        "  </family>" +
                        "</contacts>");
    }

    public static class RootBuilder implements XmlRootBuilder {
        private final Category root;

        public RootBuilder(Category root) {
            this.root = root;
        }

        @Override
        public String getTagName() {
            return "contacts";
        }

        @Override
        public XmlNodeBuilder[] process(XmlTag rootTag) {
            return new XmlNodeBuilder[]{
                    new ContactBuilder(root.getContacts()),
                    new CategoryBuilder(root.getSubCategories())
            };
        }
    }

    static class CategoryBuilder implements XmlNodeBuilder {
        private final Iterator<Category> categories;
        private Category current;

        public CategoryBuilder(List<Category> categories) {
            this.categories = categories.iterator();
            if (this.categories.hasNext()) {
                current = this.categories.next();
            }
        }

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public String getNextTagName() {
            return current.getName();
        }

        @Override
        public XmlNodeBuilder[] processNext(XmlTag tag) throws IOException {
            try {
                return new XmlNodeBuilder[]{
                        new ContactBuilder(current.getContacts()),
                        new CategoryBuilder(current.getSubCategories())
                };
            } finally {
                current = categories.hasNext() ? categories.next() : null;
            }
        }

        @Override
        public void levelDone() {

        }
    }

    static class ContactBuilder implements XmlNodeBuilder {
        private final Iterator<Contact> contacts;
        private Contact current;

        public ContactBuilder(List<Contact> categories) {
            this.contacts = categories.iterator();
            if (contacts.hasNext()) {
                current = contacts.next();
            }
        }

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public String getNextTagName() {
            return current.name();
        }

        @Override
        public XmlNodeBuilder[] processNext(XmlTag tag) throws IOException {
            tag.addAttribute("phone", current.phone());
            current = contacts.hasNext() ? contacts.next() : null;
            return new XmlNodeBuilder[0];
        }

        @Override
        public void levelDone() {
        }
    }
}
