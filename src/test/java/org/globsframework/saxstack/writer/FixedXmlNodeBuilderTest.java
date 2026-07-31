package org.globsframework.saxstack.writer;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class FixedXmlNodeBuilderTest extends SaxStackWriterTestCase {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        root = new Category("contacts",
                new Contact[]{
                        new Contact("me", "123")
                },
                new Category[]{
                        new Category("empty", new Contact[0], new Category[0])
                });
    }

    @Test
    public void testPruneEmpry() throws Exception {
        checkOutput(new RootExporter(root, true),
                "<contacts>" +
                        "  <contact name='me' phone='123'/>" +
                        "  <category name='empty'/>" +
                        "</contacts>");
    }

    @Test
    public void testDoNotPruneEmpty() throws Exception {
        checkOutput(new RootExporter(root, false),
                "<contacts>" +
                        "  <contact name='me' phone='123'/>" +
                        "  <category name='empty'>" +
                        "    <contacts/>" +
                        "  </category>" +
                        "</contacts>");
    }

    public static class RootExporter implements XmlRootBuilder {
        private final Category root;
        private final boolean pruneIfEmpty;

        public RootExporter(Category root, boolean pruneIfEmpty) {
            this.root = root;
            this.pruneIfEmpty = pruneIfEmpty;
        }

        @Override
        public String getTagName() {
            return "contacts";
        }

        @Override
        public XmlNodeBuilder[] process(XmlTag rootTag) {
            return new XmlNodeBuilder[]{
                    new SaxStackWriterWithFixedTagNamesTest.ContactBuilder(root.getContacts()),
                    new CategoryBuilderWithContacts(root.getSubCategories(), pruneIfEmpty)
            };
        }
    }

    static class CategoryBuilderWithContacts extends SaxStackWriterWithFixedTagNamesTest.CategoryBuilder {
        private final boolean pruneIfEmpty;

        public CategoryBuilderWithContacts(List<Category> categories, boolean pruneIfEmpty) {
            super(categories);
            this.pruneIfEmpty = pruneIfEmpty;
        }

        @Override
        protected XmlNodeBuilder[] getChildren(Category category) {
            return new XmlNodeBuilder[]{new FixedXmlNodeBuilder("contacts",
                    super.getChildren(category), pruneIfEmpty)};
        }
    }
}
