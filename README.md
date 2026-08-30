# SaxStack

A small library that makes SAX usable. Both parsing and generation are modelled as a **stack of node
handlers**: you write one handler per XML node, it only sees its own tag, and the library pushes and pops
them for you. Nothing is held in memory but the branch being read or written, so a document of any size
costs the same.

It is the XML layer under [globs-xml](https://github.com/globsframework/globs-xml), but it does not depend on
Globs and can be used on its own.

## Requirements

Java 17, no dependency beyond the JDK's SAX parser (JUnit 4 for the tests).

## Installation

```xml
<dependency>
    <groupId>org.globsframework</groupId>
    <artifactId>saxstack</artifactId>
    <version>5.0.0</version>
</dependency>
```

## Parsing

An `XmlNode` handles one tag: it is asked for the handler of each child (`getSubNode`), for the text it
contains (`setValue`), and told when the tag closes (`complete`). Returning `this` from `getSubNode` keeps the
current handler for the child; `DefaultXmlNode` implements all three as no-ops, so a handler only overrides
what it cares about.

```java
public class ServerXmlNode extends DefaultXmlNode {
    Map<String, String> config = new HashMap<>();
    String current;

    public XmlNode getSubNode(String childName, Attributes attrs, String uri, String fullName) {
        current = childName;
        return extractValueXmlNode;      // a node whose complete() stores config.put(current, data)
    }
}

SaxStackParser.parse(XmlUtils.getXmlReader(), rootNode, new StringReader(xml));
```

`parse` also takes a `File` or an `EntityResolver`. Errors raised from a handler travel out as an
`ExceptionHolder` (`UnexpectedTagException`, `XmlAttributeNotFoundException`, `XmlParsingException`), not as
a SAX exception, so the stack trace points at the handler.

`XmlUtils` has the attribute helpers that go with it — `getAttrValue`, `getIntAttrValue`,
`getBooleanAttrValue`, `getDoubleAttrValue`, each with a default — plus `convertEntities` and
`getXmlReader()`, which returns a namespace-aware, non-validating `XMLReader`.

## Generating

Two ways in, depending on what you have.

**Directly, tag by tag** — `XmlTag` is a cursor that writes as you call it:

```java
XmlTag root = XmlWriter.startTag(writer, "contacts");
root.createChildTag("contact")
        .addAttribute("name", "me")
        .addAttribute("phone", 512)
        .end();
root.end();
```

`addAttribute` skips a null value, and has `int`/`long`/`float`/`double` overloads. `addValue`,
`addCDataValue` and `addXmlSubtree` write the tag's content.

**From a model** — a `XmlRootBuilder` returns the `XmlNodeBuilder`s that produce its children, and each of
those behaves like an iterator (`hasNext`, `getNextTagName`, `processNext`), so one builder can emit several
sibling tags with different names. `IteratorBasedXmlNodeBuilder<T>` covers the common case of "one tag per
element of a list":

```java
public class CategoryBuilder extends IteratorBasedXmlNodeBuilder<Category> {
    public CategoryBuilder(List<Category> categories) { super("category", categories); }

    public XmlNodeBuilder[] processNext(XmlTag tag) throws IOException {
        Category category = getNextItem();
        tag.addAttribute("name", category.getName());
        return new XmlNodeBuilder[]{new ContactBuilder(category.getContacts()),
                                    new CategoryBuilder(category.getSubCategories())};
    }
}

new SaxStackWriter(writer).write(new RootBuilder(root));
```

`FixedXmlNodeBuilder` is the fixed-name variant.

### Filters

`write(rootBuilder, filter)` takes a `PathFilter` describing what to keep, so the same builders serve a full
document and a projection of it:

| Filter | Keeps |
| --- | --- |
| `contacts/category/contact` | those tags only, and the attributes of all of them |
| `contacts/category/*` | the whole subtree under `category` |
| `contacts/category[name]/contact[name,phone]` | the tags, restricted to the listed attributes |
| `contacts/category[]/contact[phone]` | `category` with no attribute at all |

## Pretty printing and comparing

- `XmlPrettyPrinter` / `PrettyPrintRootXmlTag` — the same writers, indented
- `XmlUtils.format(input, parser, attributeCountOnLine)` — reformat an existing document, optionally through
  a filter
- `XmlComparator` — compare two documents structurally, ignoring formatting
- `XmlNodeToBuilder`, `DomXmlNode` — bridges to and from a DOM when a whole document really is wanted in
  memory

## Building

```bash
mvn test
```

`ReadWriteBigFile` in the test sources is the throughput check on a large document.

## License

Apache License 2.0 — see <https://www.apache.org/licenses/LICENSE-2.0.txt>.

## Links

- [Globs Framework](https://globsframework.org)
- [GitHub repository](https://github.com/globsframework/saxstack)
