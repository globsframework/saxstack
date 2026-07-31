package org.globsframework.saxstack.parser;

import org.globsframework.saxstack.utils.XmlUtils;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.Attributes;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocReadSample {

    @Test
    public void testSample() throws Exception {
        AnyXmlNode node = new AnyXmlNode();
        SaxStackParser.parse(XmlUtils.getXmlReader(), node, new StringReader(DATA));
        Assert.assertEquals(1, node.serverXmlNodeList.size());
        ServerXmlNode first = node.serverXmlNodeList.get(0);
        Assert.assertEquals("my_login", first.config.get("username"));
    }

    private static class ExtractValueXmlNode extends DefaultXmlNode {
        String data;

        @Override
        public void setValue(String value) {
            data = value;
        }
    }

    private static class AnyXmlNode extends DefaultXmlNode {
        List<ServerXmlNode> serverXmlNodeList = new ArrayList<>();

        @Override
        public XmlNode getSubNode(String childName, Attributes xmlAttrs, String uri, String fullName) {
            if (childName.equals("server")) {
                ServerXmlNode node = new ServerXmlNode();
                serverXmlNodeList.add(node);
                return node;
            } else {
                return this;
            }
        }
    }

    private static class ServerXmlNode extends DefaultXmlNode {
        Map<String, String> config = new HashMap<>();
        String current;

        ExtractValueXmlNode extractValueXmlNode = new ExtractValueXmlNode() {
            @Override
            public void complete() {
                config.put(current, data);
            }
        };

        @Override
        public XmlNode getSubNode(String childName, Attributes xmlAttrs, String uri, String fullName) {
            current = childName;
            return extractValueXmlNode;
        }
    }

    public final static String DATA =
            "<servers>\n" +
                    "    <server>\n" +
                    "      <id>server001</id>\n" +
                    "      <username>my_login</username>\n" +
                    "      <password>my_password</password>\n" +
                    "      <privateKey>${user.home}/.ssh/id_dsa</privateKey>\n" +
                    "      <passphrase>some_passphrase</passphrase>\n" +
                    "      <filePermissions>664</filePermissions>\n" +
                    "      <directoryPermissions>775</directoryPermissions>\n" +
                    "      <configuration></configuration>\n" +
                    "    </server>\n" +
                    "  </servers>";

}
