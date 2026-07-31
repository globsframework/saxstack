package org.globsframework.saxstack.sample;

import org.globsframework.saxstack.parser.DefaultXmlNode;
import org.globsframework.saxstack.parser.SaxStackParser;
import org.globsframework.saxstack.parser.XmlBootstrapNode;
import org.globsframework.saxstack.parser.XmlNode;
import org.globsframework.saxstack.utils.XmlUtils;
import org.junit.Test;
import org.xml.sax.Attributes;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SampleParsingTest {

    @Test
    public void test() throws Exception {
        String input =
                "<contacts>" +
                        "  <contact firstName='Homer' lastName='Simpson'>" +
                        "    <email address='dad@simpsons.net'/>'" +
                        "    <email address='homer@simpsons.net'/>'" +
                        "  </contact>" +
                        "  <contact firstName='Bart' lastName='Simpson'>" +
                        "    <email address='bart@simpsons.net'/>'" +
                        "  </contact>" +
                        "</contacts>";

        List<Contact> contacts = new ArrayList<>();
        ContactsNode root = new ContactsNode(contacts);
        SaxStackParser.parse(XmlUtils.getXmlReader(), new XmlBootstrapNode(root, "contacts"), new StringReader(input));

        assertEquals(2, contacts.size());
        assertEquals("Homer Simpson [dad@simpsons.net, homer@simpsons.net]", contacts.get(0).toString());
        assertEquals("Bart Simpson [bart@simpsons.net]", contacts.get(1).toString());
    }

    private static class Contact {
        private final String firstName;
        private final String lastName;
        private final List<String> emails = new ArrayList<>();

        public Contact(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public void addEmail(String email) {
            emails.add(email);
        }

        public List<String> getEmails() {
            return emails;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        @Override
        public String toString() {
            return firstName + " " + lastName + " " + emails;
        }
    }

    private static class ContactsNode extends DefaultXmlNode {
        private final List<Contact> contacts;

        public ContactsNode(List<Contact> contacts) {
            this.contacts = contacts;
        }

        @Override
        public XmlNode getSubNode(String childName, Attributes xmlAttrs, String uri, String fullName) {
            if (childName.equals("contact")) {
                return new ContactNode(xmlAttrs, contacts);
            }
            return super.getSubNode(childName, xmlAttrs, uri, fullName);
        }
    }

    private static class ContactNode extends DefaultXmlNode {
        private final Contact contact;

        public ContactNode(Attributes xmlAttrs, List<Contact> contacts) {
            String firstName = XmlUtils.getAttrValue("firstName", xmlAttrs, "");
            String lastName = XmlUtils.getAttrValue("lastName", xmlAttrs, "");
            contact = new Contact(firstName, lastName);
            contacts.add(contact);
        }

        @Override
        public XmlNode getSubNode(String childName, Attributes xmlAttrs, String uri, String fullName) {
            if (childName.equals("email")) {
                return new EmailNode(xmlAttrs, contact);
            }
            return super.getSubNode(childName, xmlAttrs, uri, fullName);
        }
    }

    private static class EmailNode extends DefaultXmlNode {
        public EmailNode(Attributes xmlAttrs, Contact contact) {
            String email = XmlUtils.getAttrValue("address", xmlAttrs);
            contact.addEmail(email);
        }
    }
}
