package schemacrawler.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;
import schemacrawler.schema.NamedObjectKey;

public class NamedObjectKeyTest {

  @Test
  public void comparisonPreservesExactComponents() {
    final NamedObjectKey uppercase = new NamedObjectKey("schema", "AB");
    final NamedObjectKey lowercase = new NamedObjectKey("schema", "ab");
    final NamedObjectKey punctuated = new NamedObjectKey("schema", "a-b");

    assertThat(uppercase.compareTo(lowercase), lessThan(0));
    assertThat(lowercase.compareTo(uppercase), greaterThan(0));
    assertThat(punctuated.compareTo(lowercase), lessThan(0));
    assertThat(lowercase.compareTo(punctuated), greaterThan(0));
    assertThat(
        new NamedObjectKey("a.b", "c").compareTo(new NamedObjectKey("a", "b.c")), greaterThan(0));
    assertThat(
        new NamedObjectKey("schema", "").compareTo(new NamedObjectKey("schema", null)),
        greaterThan(0));
    assertThat(
        new NamedObjectKey("schema", null).compareTo(new NamedObjectKey("schema", "")),
        lessThan(0));
    assertThat(uppercase.compareTo(new NamedObjectKey("schema", "AB")), is(0));
  }

  @Test
  public void startsWithMatchesWholeComponentsAndPreservesIdentity() {
    final NamedObjectKey key = new NamedObjectKey(null, "", "a.b", "\"quoted\"", "specific");
    assertThat(key.startsWith(new NamedObjectKey()), is(true));
    assertThat(key.startsWith(null), is(false));
    assertThat(key.startsWith(key), is(true));
    assertThat(key.startsWith(new NamedObjectKey(null, "")), is(true));
    assertThat(key.startsWith(new NamedObjectKey(null, "", "a.b", "\"quoted\"")), is(true));
    assertThat(key.startsWith(new NamedObjectKey("", "")), is(false));
    assertThat(key.startsWith(new NamedObjectKey(null, null)), is(false));
    assertThat(key.startsWith(new NamedObjectKey(null, "", "a")), is(false));
    assertThat(key.startsWith(new NamedObjectKey(null, "", "a", "b")), is(false));
    assertThat(key.startsWith(new NamedObjectKey(null, "", "A.B")), is(false));
    assertThat(key.startsWith(new NamedObjectKey(null, "", "a.b", "quoted")), is(false));
    assertThat(key.startsWith(key.with("longer")), is(false));
    assertThat(new NamedObjectKey().startsWith(new NamedObjectKey()), is(true));
    assertThat(new NamedObjectKey().startsWith(new NamedObjectKey("part")), is(false));
    assertThat(new NamedObjectKey().startsWith(null), is(false));
  }

  @Test
  public void startsWithRetainsKeyContractsAfterSerialization() {
    final NamedObjectKey key = new NamedObjectKey(null, "", "a.b", "\"quoted\"");
    final NamedObjectKey copy = SerializationUtils.clone(key);
    assertThat(copy, is(key));
    assertThat(copy.hashCode(), is(key.hashCode()));
    assertThat(copy.join(), is(key.join()));
    assertThat(copy.withoutLast().with("\"quoted\""), is(key));
    assertThat(copy.startsWith(key.withoutLast()), is(true));
    assertThat(key.startsWith(copy), is(true));
  }
}
