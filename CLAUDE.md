# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Scope

`saxstack` (`org.globsframework:saxstack`, currently `5.0-SNAPSHOT` on branch `v5`) is the lowest-level repo of
the globsframework workspace: **zero runtime dependencies**, only the JDK's `org.xml.sax` + `javax.xml.parsers`,
JUnit 4 for tests, Java 17 source/target (older than the rest of the workspace). It knows nothing about
`Glob`/`GlobType` — `globs-xml`, `globs-db` and `globsframework` depend on it, so any change to `XmlNode`,
`XmlTag`, `XmlNodeBuilder` or `Filter` ripples into those repos and must be propagated by `mvn install` +
version bump there (see the workspace `CLAUDE.md` one directory up).

## Build & test

```bash
mvn -o test                       # 44 tests, offline works once ~/.m2 is warm
mvn -o test -Dtest=XmlUtilsTest   # single class
mvn -o test -Dtest=SaxStackParserTest#testStandardParsing
```

Surefire includes `**/*Test.java` / `**/*Tests.java` and **excludes `**/*TestCase.java`** — `SaxStackWriterTestCase`
is the shared abstract fixture (a `Category`/`Contact` tree), not a test.

**Everything here is JUnit 4, migrated from JUnit 3** — nothing extends `TestCase` any more, so a `testXxx`
method without `@Test` is silently *not run* and the build still goes green. When adding to the `writer/`
tests, annotate every method and keep `setUp` as `public @Before` (JUnit 4 rejects a `protected` one).
`sample/SampleWriterWithXmlTag` is intentionally outside the surefire pattern: it is a doc sample that prints
to stdout and asserts nothing.

## Core model: a stack of node handlers

### Parsing (`parser/`)

`SaxStackParser` is a `DefaultHandler` holding a `Deque<XmlNode>`. Per element it does exactly:
`startElement` → push `stack.peek().getSubNode(local, atts, uri, qName)`; `endElement` → pop, `setValue(text)`,
`complete()`. So a handler only ever decides what its *direct* children are — recursion falls out of the stack.

Consequences that shape any handler you write:
- `setValue` is called **only when the accumulated character buffer is non-empty**, and the buffer is reset at
  every `startElement`. Mixed content (text interleaved with child tags) is therefore not preserved — text
  before a child is dropped.
- Handlers are ordinary short-lived objects created in `getSubNode`; carry state in them and publish results
  in `complete()` (or push into a collection passed by the parent, as `SampleParsingTest` does).
- `DefaultXmlNode` is the base to extend: its `getSubNode` returns `SilentXmlNode.INSTANCE`, which swallows an
  entire unknown subtree. Override `getSubNode`, handle the names you know, delegate the rest to `super`.
- `XmlBootstrapNode(root, "expectedRootName")` is the conventional stack bottom — it enforces the root tag name.
- `XmlUtils.getXmlReader()` builds the namespace-aware `XMLReader` every entry point takes as a parameter;
  the library never creates one implicitly.

Errors: `ExceptionHolder` is a `RuntimeException` wrapper used so `XmlNode` methods can throw checked causes
through SAX; `SaxStackParser` unwraps its own `SAXExceptionHolder` back into the original `ExceptionHolder`.
Parse-domain failures use the `XmlParsingException` hierarchy (`UnexpectedTagException`,
`XmlAttributeNotFoundException`).

### Writing (`writer/`) — two independent APIs

1. **Imperative `XmlTag`** — `XmlWriter.startTag(writer, "root")` then a fluent chain. `addAttribute`/`addValue`
   return `this`, `createChildTag` returns the child, **`end()` returns the parent**, so the chain must be
   balanced by hand (see `SampleWriterWithXmlTag`). Attributes must be added before any value/child: the
   opening `>` is written lazily by `closeSup()`, and adding an attribute afterwards throws.
2. **Declarative builders** — implement `XmlRootBuilder` (one root tag) plus `XmlNodeBuilder`s, which are
   *iterator-shaped*: `hasNext()` / `getNextTagName()` / `processNext(tag)` returning the child builders /
   `levelDone()`. `XmlUtils.build` drives the whole traversal. Note `getNextTagName()` is what advances the
   iterator in most implementations, so it is called exactly once per item, before `processNext`.
   `FixedXmlNodeBuilder` (single tag, optional `pruneIfEmpty`) and `IteratorBasedXmlNodeBuilder` (wrap a
   `Collection`/`Iterator`) cover most cases.

   Sink is chosen by the `SaxStackBuilder` implementation: `SaxStackWriter` (compact) or `XmlPrettyPrinter`
   (indented, wraps after N attributes per line).

**Performance invariant — do not break it:** `WriterXmlTag` and `PrettyPrintXmlTag` keep **one reusable `child`
instance per level**, re-`init`-ed on each `createChildTag`. Tag objects are mutable and recycled, so an
`XmlTag` reference is valid only until you leave its scope. Never store one in a field, a collection or a
closure that outlives the tag.

### `Filter` — the cross-cutting output hook

`Filter.enter(parent, tagName)` returns the `XmlTag` to use for that element and `leave()` pops the state; it
runs for every tag emitted by the builder API. Returning a `NullXmlTag` discards the element, and
`XmlUtils.write` checks `instanceof NullXmlTag` to prune the whole subtree without visiting it.
`FilterNone` is the identity. `PathFilter` takes a path string — `"a/b[attr1,attr2]"` selects tags along the
path and restricts which attributes are written, `*` as the last segment means "everything below".
`NodeTypeFilter` filters against a `NodeType` schema tree learned from another document.

### Bridges (`utils/`) and comparison (`comparator/`)

The parse and write sides meet through two adapters, which is how every derived feature is built:

- `XmlNodeToBuilder` is an `XmlNode` that writes straight into an `XmlTag` — parsing through it re-emits the
  document. That is all `XmlUtils.format` is: parse → pretty-print, no intermediate tree.
- `DomXmlNode` / `BootstrapDomXmlNode` build an in-memory tree instead, and `DomXmlNode.getBuilder()` hands
  back an `XmlRootBuilder` for that tree, so a parsed document can be re-emitted through any `Filter`.
  `populateNodeType` derives the `NodeType` schema used by `NodeTypeFilter`.

`XmlComparator` composes those: `areEqual` (strict, ordered), `areEquivalent` (order-insensitive, via
`DomXmlNode.contains`), and `computeDiff` (the *expected* document is turned into a `NodeType` and used as a
filter, so only attributes present in the expectation are compared). In tests, always go through
`XmlTestUtils.assertEquals` / `assertEquivalent` — on failure they re-assert on pretty-printed forms so the
JUnit diff is readable.

## Code style

The code was pre-generics and has been modernized to the Java 17 baseline: generified collections everywhere
(no raw types left), diamond, enhanced `for`, `StringBuilder`, `Deque` instead of `Stack`, `@Override` on every
override, `final` on fields that never change, lambdas where the interface is functional, `String.replace` for
literal replacements, multi-catch, try-with-resources. Keep it that way, and keep the compile warning-free
under `-Xlint:all`.

`IteratorBasedXmlNodeBuilder<T>` is the one generified *public* type — subclassing it raw still compiles, which
is why the four dependent repos needed no change. Two things stay deliberately un-modernized: the reusable-tag
invariant in `WriterXmlTag`/`PrettyPrintXmlTag` (see above), and the long concatenated XML literals in the
tests — text blocks would change the expected whitespace the assertions depend on.

`src/test/java/.../bench/ReadWriteBigFile.java` is a `main`-based throughput bench, not a test.
