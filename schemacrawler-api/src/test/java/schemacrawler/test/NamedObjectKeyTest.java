package schemacrawler.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;
import schemacrawler.schema.NamedObjectKey;

public class NamedObjectKeyTest {

  @Test
  public void nullKeysAndComponentsSortLastWithoutCollapsingEmptyComponents() {
    final NamedObjectKey absent = new NamedObjectKey("catalog", null);
    final NamedObjectKey empty = new NamedObjectKey("catalog", "");
    final NamedObjectKey named = new NamedObjectKey("catalog", "schema");
    assertTrue(named.compareTo(null) < 0);
    assertTrue(empty.compareTo(absent) < 0);
    assertTrue(absent.compareTo(empty) > 0);
    assertTrue(named.compareTo(absent) < 0);
    assertTrue(named.compareTo(new NamedObjectKey(null, "schema")) < 0);
    assertTrue(new NamedObjectKey(null, "").compareTo(new NamedObjectKey(null, null)) < 0);
    assertEquals(0, absent.compareTo(new NamedObjectKey("catalog", null)));
    final List<NamedObjectKey> keys = new ArrayList<>(List.of(absent, named, empty));
    Collections.sort(keys);
    assertEquals(List.of(empty, named, absent), keys);
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

  @Test
  public void testComparable() {
    final NamedObjectKey key1 = new NamedObjectKey("a", "b");
    final NamedObjectKey key2 = new NamedObjectKey("a", "c");
    final NamedObjectKey key3 = new NamedObjectKey("b", "a");
    final NamedObjectKey key4 = new NamedObjectKey("a", "b", "c");
    final NamedObjectKey key1Duplicate = new NamedObjectKey("a", "b");

    assertTrue(key1 instanceof Comparable, "NamedObjectKey should implement Comparable");

    final List<NamedObjectKey> keys = new ArrayList<>();
    keys.add(key3);
    keys.add(key2);
    keys.add(key4);
    keys.add(key1);

    Collections.sort(keys);

    assertEquals(key1, keys.get(0));
    assertEquals(key4, keys.get(1));
    assertEquals(key2, keys.get(2));
    assertEquals(key3, keys.get(3));

    assertEquals(0, key1.compareTo(key1Duplicate), "compareTo should return 0 for equal objects");
    assertTrue(key1.compareTo(key2) < 0);
    assertTrue(key2.compareTo(key1) > 0);
    assertTrue(key1.compareTo(key4) < 0);
    assertTrue(key4.compareTo(key1) > 0);
  }
}
