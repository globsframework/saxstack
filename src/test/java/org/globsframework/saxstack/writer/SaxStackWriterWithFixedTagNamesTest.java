package org.globsframework.saxstack.writer;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

public class SaxStackWriterWithFixedTagNamesTest extends SaxStackWriterTestCase {
    @Test
    public void testStandardOutput() throws Exception {
        checkOutput(new RootBuilder(root),
                "<contacts>" +
                        "  <contact name='me' phone='512'/>" +
                        "  <category name='family'>" +
                        "    <contact name='Grandpa' phone='567'/>" +
                        "    <category name='home'>" +
                        "      <contact name='Bart' phone='756'/>" +
                        "      <contact name='Homer' phone='757'/>" +
                        "    </category>" +
                        "  </category>" +
                        "  <category name='work'>" +
                        "    <contact name='Ralph' phone='123'/>" +
                        "  </category>" +
                        "</contacts>");
    }

    @Test
    public void testFilteredOutput() throws Exception {
        checkOutput(new RootBuilder(root),
                "contacts/category/contact",
                "<contacts>" +
                        "  <category name='family'>" +
                        "    <contact name='Grandpa' phone='567'/>" +
                        "  </category>" +
                        "  <category name='work'>" +
                        "    <contact name='Ralph' phone='123'/>" +
                        "  </category>" +
                        "</contacts>");
    }

    @Test
    public void testFilteredOutputWithAllSubTree() throws Exception {
        checkOutput(new RootBuilder(root),
                "contacts/category/*",
                "<contacts>" +
                        "  <category name='family'>" +
                        "    <contact name='Grandpa' phone='567'/>" +
                        "    <category name='home'>" +
                        "      <contact name='Bart' phone='756'/>" +
                        "      <contact name='Homer' phone='757'/>" +
                        "    </category>" +
                        "  </category>" +
                        "  <category name='work'>" +
                        "    <contact name='Ralph' phone='123'/>" +
                        "  </category>" +
                        "</contacts>");
    }

    @Test
    public void testFilterAttr() throws Exception {
        checkOutput(new RootBuilder(root),
                "contacts/category[name]/contact[name,phone]",
                "<contacts>" +
                        "  <category name='family'>" +
                        "    <contact name='Grandpa' phone='567'/>" +
                        "  </category>" +
                        "  <category name='work'>" +
                        "    <contact name='Ralph' phone='123'/>" +
                        "  </category>" +
                        "</contacts>");

        checkOutput(new RootBuilder(root),
                "contacts/category[]/contact[phone]",
                "<contacts>" +
                        "  <category>" +
                        "    <contact phone='567'/>" +
                        "  </category>" +
                        "  <category>" +
                        "    <contact phone='123'/>" +
                        "  </category>" +
                        "</contacts>");
    }

    @Test
    public void testRootTagError() throws Exception {
        checkOutputIsEmpty(new RootBuilder(root), "unknown");
    }

    @Test
    public void testEmptyFilter() throws Exception {
        checkOutputIsEmpty(new RootBuilder(root), "");
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

    public static class CategoryBuilder extends IteratorBasedXmlNodeBuilder<Category> {
        public CategoryBuilder(List<Category> categories) {
            super("category", categories);
        }

        @Override
        public XmlNodeBuilder[] processNext(XmlTag tag) throws IOException {
            Category category = getNextItem();
            tag.addAttribute("name", category.getName());
            if (category.getAdditionalInfo() != null) {
                tag.addValue(category.getAdditionalInfo());
            }
            return getChildren(category);
        }

        @Override
        public void levelDone() {
        }

        protected XmlNodeBuilder[] getChildren(Category category) {
            return new XmlNodeBuilder[]{
                    new ContactBuilder(category.getContacts()),
                    new CategoryBuilder(category.getSubCategories())
            };
        }
    }

    public static class ContactBuilder extends IteratorBasedXmlNodeBuilder<Contact> {

        public ContactBuilder(List<Contact> categories) {
            super("contact", categories);
        }

        @Override
        public XmlNodeBuilder[] processNext(XmlTag tag) throws IOException {
            Contact contact = getNextItem();
            tag.addAttribute("name", contact.name());
            tag.addAttribute("phone", contact.phone());
            return new XmlNodeBuilder[0];
        }

        @Override
        public void levelDone() {
        }
    }
}
