package schemacrawler.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import schemacrawler.schema.NamedObjectKey;

public class NamedObjectKeyTest {

  @Test
  public void testComparable() {
    final NamedObjectKey key1 = new NamedObjectKey("a", "b");
    final NamedObjectKey key2 = new NamedObjectKey("a", "c");
    final NamedObjectKey key3 = new NamedObjectKey("b", "a");
    final NamedObjectKey key4 = new NamedObjectKey("a", "b", "c");
    final NamedObjectKey key1Duplicate = new NamedObjectKey("a", "b");

    assertTrue(key1 instanceof Comparable, "NamedObjectKey should implement Comparable");

    List<NamedObjectKey> keys = new ArrayList<>();
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
