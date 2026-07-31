package org.globsframework.saxstack.bench;

import org.globsframework.saxstack.writer.SaxStackWriter;
import org.globsframework.saxstack.writer.XmlNodeBuilder;
import org.globsframework.saxstack.writer.XmlRootBuilder;
import org.globsframework.saxstack.writer.XmlTag;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class ReadWriteBigFile {

    public static void main(String[] args) throws IOException {
        XmlRootBuilder rootBuilder = new XmlRootBuilder() {
            @Override
            public String getTagName() {
                return "root";
            }

            @Override
            public XmlNodeBuilder[] process(XmlTag rootTag) throws IOException {
                return new XmlNodeBuilder[]{
                        new TestXmlNodeBuilder(6, 5)
                };
            }
        };

        File file = File.createTempFile("testBigXml", ".xml");
//    file.deleteOnExit();
        long start = System.currentTimeMillis();
        try (Writer writer = new BufferedWriter(new FileWriter(file))) {
            SaxStackWriter.write(writer, rootBuilder);
        }
        System.out.println("ReadWriteBigFile.main " + file.getAbsolutePath());
        System.out.println("ReadWriteBigFile.main " + file.length() + " in " + (System.currentTimeMillis() - start));
    }

    private static class TestXmlNodeBuilder implements XmlNodeBuilder {
        private final int level;
        private final int count;
        private final String name;
        private final XmlNodeBuilder[] childs;
        private int tmpCount;

        public TestXmlNodeBuilder(int count, int level) {
            this.count = count;
            this.tmpCount = count;
            this.level = level;
            name = "name_" + level;
            if (level > 0) {
                childs = new XmlNodeBuilder[]{
                        new TestXmlNodeBuilder(count, level - 1)
                };
            } else {
                childs = new XmlNodeBuilder[0];
            }
        }

        @Override
        public boolean hasNext() {
            return tmpCount >= 0;
        }

        @Override
        public String getNextTagName() {
            return name + "_" + tmpCount;
        }

        @Override
        public XmlNodeBuilder[] processNext(XmlTag tag) throws IOException {
            tmpCount--;
            tag.addAttribute("AnAttribute", "some attr value");
            if (childs.length == 0) {
                tag.addValue("Some value");
            }
            return childs;
        }

        @Override
        public void levelDone() {
            tmpCount = count;
        }
    }
}
