/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static us.fatehi.utility.Utility.commonPrefix;
import static us.fatehi.utility.Utility.convertForComparison;
import static us.fatehi.utility.Utility.hasNoUpperCase;
import static us.fatehi.utility.Utility.hash;
import static us.fatehi.utility.Utility.isBlank;
import static us.fatehi.utility.Utility.isIntegral;
import static us.fatehi.utility.Utility.join;
import static us.fatehi.utility.Utility.toCamelCase;
import static us.fatehi.utility.Utility.toKebabCase;
import static us.fatehi.utility.Utility.toSnakeCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class UtilityTest {

  @Test
  public void commonPrefixTest() {
    assertThat(commonPrefix("preTest", null), is(""));
    assertThat(commonPrefix(null, "preCompile"), is(""));
    assertThat(commonPrefix("preTest", "preCompile"), is("pre"));
    assertThat(commonPrefix("something", "nothing"), is(""));
    assertThat(commonPrefix("preTest", ""), is(""));
    assertThat(commonPrefix("12345", "12345"), is(""));
  }

  @Test
  public void convertForComparisonTest() {
    assertThat(convertForComparison(null), is(""));
    assertThat(convertForComparison(""), is(""));
    assertThat(convertForComparison("ABC123"), is("abc123"));
    assertThat(convertForComparison("ABC_123"), is("abc_123"));
    assertThat(convertForComparison("ABC.123"), is("abc.123"));
    assertThat(convertForComparison("ABC!@#123"), is("abc123"));
    assertThat(convertForComparison("ABC_123.DEF"), is("abc_123.def"));
    assertThat(convertForComparison("ABC 123"), is("abc123"));
  }

  @Test
  public void hasNoUpperCaseTest() {
    assertThat(hasNoUpperCase(null), is(false));
    assertThat(hasNoUpperCase("A"), is(false));
    assertThat(hasNoUpperCase("Aa"), is(false));
    assertThat(hasNoUpperCase("A a"), is(false));

    assertThat(hasNoUpperCase(""), is(true));
    assertThat(hasNoUpperCase(" "), is(true));
    assertThat(hasNoUpperCase("a"), is(true));
    assertThat(hasNoUpperCase("aa"), is(true));
    assertThat(hasNoUpperCase("a s"), is(true));
    assertThat(hasNoUpperCase("1.0"), is(true));
  }

  @Test
  public void hashTest() {
    assertThat(hash(null), is(nullValue()));
    assertThat(
        hash(
            new Object() {
              @Override
              public String toString() {
                return "";
              }
            }),
        is(nullValue()));
    assertThat(hash("abc"), is("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"));
  }

  @Test
  public void isBlankTest() {
    assertThat(isBlank(null), is(true));
    assertThat(isBlank(""), is(true));
    assertThat(isBlank(" "), is(true));
    assertThat(isBlank("   "), is(true));
    assertThat(isBlank("\t"), is(true));
    assertThat(isBlank("\n"), is(true));
    assertThat(isBlank("\r"), is(true));
    assertThat(isBlank(" \t "), is(true));
    assertThat(isBlank("\t\t"), is(true));

    assertThat(!isBlank("a"), is(true));
    assertThat(!isBlank("©"), is(true));
    assertThat(!isBlank(" a"), is(true));
    assertThat(!isBlank("a "), is(true));
    assertThat(!isBlank("a b"), is(true));
  }

  @Test
  public void isIntegralTest() {
    assertThat(isIntegral(null), is(false));
    assertThat(isIntegral(""), is(false));
    assertThat(isIntegral(" "), is(false));
    assertThat(isIntegral("1.0"), is(false));
    assertThat(isIntegral("-0.3"), is(false));
    assertThat(isIntegral("a"), is(false));

    assertThat(isIntegral("1"), is(true));
    assertThat(isIntegral("+1"), is(true));
    assertThat(isIntegral("-1"), is(true));
  }

  @Test
  public void joinCollectionTest() {
    assertThat(join((Collection) null, ","), nullValue());
    assertThat(join(new ArrayList<>(), ","), nullValue());

    assertThat(join(Arrays.asList("abc"), ","), is("abc"));
    assertThat(join(Arrays.asList(new String[] {null}), ","), is("null"));
    assertThat(join(Arrays.asList("abc", "bcd"), ","), is("abc,bcd"));
    assertThat(join(Arrays.asList("abc", null), ","), is("abc,null"));
  }

  @Test
  public void joinMapTest() {
    assertThat(join((Map<String, String>) null, ","), nullValue());
    assertThat(join(new HashMap<>(), ","), nullValue());

    final Map<String, String> map = new LinkedHashMap<>();
    map.put("RED", null);
    map.put(null, "#00FF00");
    map.put("BLUE", "#0000FF");

    assertThat(join(map, ","), is("RED=null,null=#00FF00,BLUE=#0000FF"));
  }

  @Test
  public void toCamelCaseTest() {
    assertThat(toCamelCase(null), nullValue());
    assertThat(toCamelCase(""), equalTo(""));
    assertThat(toCamelCase(" "), equalTo(" "));

    assertThat(toCamelCase("alreadycamel"), equalTo("alreadycamel"));
    assertThat(toCamelCase("ALREADYCAMEL"), equalTo("alreadycamel"));
    assertThat(toCamelCase("snake_case_identifier"), equalTo("snakeCaseIdentifier"));
    assertThat(toCamelCase("kebab-case-identifier"), equalTo("kebabCaseIdentifier"));
    assertThat(toCamelCase("MIXED_case-Identifier"), equalTo("mixedCaseIdentifier"));

    assertThat(toCamelCase("_leading_separator"), equalTo("LeadingSeparator"));
    assertThat(toCamelCase("trailing_separator_"), equalTo("trailingSeparator"));
    assertThat(toCamelCase("multiple__separators"), equalTo("multipleSeparators"));
  }

  @Test
  public void toKebabCaseTest() {
    assertThat(toKebabCase(null), nullValue());
    assertThat(toKebabCase(""), equalTo(""));
    assertThat(toKebabCase(" "), equalTo(" "));

    assertThat(toKebabCase("camelCaseIdentifier"), equalTo("camel-case-identifier"));
    assertThat(toKebabCase("PascalCaseIdentifier"), equalTo("pascal-case-identifier"));
    assertThat(toKebabCase("snake_case_identifier"), equalTo("snake-case-identifier"));
    assertThat(toKebabCase("already-kebab"), equalTo("already-kebab"));
    assertThat(toKebabCase("trailing-kebab-"), equalTo("trailing-kebab"));
    assertThat(toKebabCase("contains spaces"), equalTo("contains-spaces"));
    assertThat(toKebabCase("ABC"), equalTo("abc"));
  }

  @Test
  public void toSnakeCaseTest() {
    assertThat(toSnakeCase(null), nullValue());
    assertThat(toSnakeCase(""), equalTo(""));
    assertThat(toSnakeCase(" "), equalTo(" "));

    assertThat(toSnakeCase("a b"), equalTo("a_b"));
    assertThat(toSnakeCase("ab"), equalTo("ab"));
    assertThat(toSnakeCase("abI"), equalTo("ab_i"));
    assertThat(toSnakeCase("Ab"), equalTo("ab"));
    assertThat(toSnakeCase("abIj"), equalTo("ab_ij"));
    assertThat(toSnakeCase("ABC"), equalTo("abc"));
    assertThat(toSnakeCase("ABC_"), equalTo("abc"));
    assertThat(toSnakeCase("kebab-case"), equalTo("kebab_case"));
    assertThat(toSnakeCase("kebab-Case"), equalTo("kebab_case"));
    assertThat(toSnakeCase("-leading-kebab"), equalTo("leading_kebab"));
    assertThat(toSnakeCase("trailing-kebab-"), equalTo("trailing_kebab"));
  }
}
