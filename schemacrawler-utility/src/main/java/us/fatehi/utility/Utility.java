/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.regex.Pattern;

@UtilityMarker
public final class Utility {

  private static final Pattern camelCaseBoundaryPattern = Pattern.compile("([a-z0-9])([A-Z])");
  private static final Pattern acronymBoundaryPattern = Pattern.compile("([A-Z]+)([A-Z][a-z])");
  private static final Pattern repeatedUnderscorePattern = Pattern.compile("_+");

  public static String commonPrefix(final String string1, final String string2) {
    if (string1 == null || string2 == null) {
      return "";
    }
    final int index = indexOfDifference(string1, string2);
    if (index < 1) {
      return "";
    }
    return string1.substring(0, index).toLowerCase(Locale.ROOT);
  }

  /**
   * Produces a lossy, locale-independent comparison form of a database object name. Retains only
   * characters recognized by {@link Character#isLetterOrDigit(char)}, underscores, and dots,
   * lowercasing retained characters with {@link Character#toLowerCase(char)}. Quotes, other
   * punctuation, and whitespace are discarded, so distinct identifiers can produce the same result.
   *
   * <p>This method does not perform Unicode normalization or full case folding. Supplementary
   * letters and combining marks are discarded; canonically equivalent strings can produce different
   * results. This comparison form must not be used as an object identity.
   *
   * @param text Text to convert
   * @return Lossy comparison text, or an empty string for null or empty input
   */
  public static String convertForComparison(final String text) {
    if (text == null || text.length() == 0) {
      return "";
    }

    final StringBuilder result = new StringBuilder();
    text.codePoints()
        .forEach(
            cp -> {
              if (Character.isLetterOrDigit(cp) || cp == '_' || cp == '.') {
                result.appendCodePoint(Character.toLowerCase(cp));
              }
            });

    final String identifier = result.toString();
    return identifier;
  }

  /**
   * Checks if the text is all lowercase.
   *
   * @param text Text to check.
   * @return Whether the string is all lowercase.
   */
  public static boolean hasNoUpperCase(final String text) {
    if (text == null) {
      return false;
    }
    return text.chars().noneMatch(Character::isUpperCase);
  }

  public static String hash(final Object object) {
    if (object == null) {
      return null;
    }
    final String text = object.toString();
    if (text.isEmpty()) {
      return null;
    }
    try {
      final MessageDigest digest = MessageDigest.getInstance("SHA-256");
      final byte[] hashed = digest.digest(text.getBytes(UTF_8));
      return HexFormat.of().formatHex(hashed);
    } catch (final NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  /**
   * Checks if the text is null or empty.
   *
   * @param text Text to check.
   * @return Whether the string is blank.
   */
  public static boolean isBlank(final String text) {
    return text == null || text.isBlank();
  }

  /**
   * Checks if the text contains an integer only.
   *
   * @param text Text to check.
   * @return Whether the string is an integer.
   */
  public static boolean isIntegral(final CharSequence text) {
    if (text == null || text.length() == 0) {
      return false;
    }

    for (int i = 0; i < text.length(); i++) {
      final char ch = text.charAt(i);
      if (!Character.isDigit(ch) && ch != '+' && ch != '-') {
        return false;
      }
    }
    return true;
  }

  public static String join(final Collection<String> collection, final String separator) {
    if (collection == null || collection.isEmpty()) {
      return null;
    }

    final StringJoiner joiner = new StringJoiner(separator);
    joiner.setEmptyValue("");
    for (final String string : collection) {
      joiner.add(string);
    }

    return joiner.toString();
  }

  public static String join(final Map<?, ?> map, final String separator) {
    if (map == null || map.isEmpty()) {
      return null;
    }

    final StringJoiner joiner = new StringJoiner(separator);
    for (final Entry<?, ?> entry : map.entrySet()) {
      joiner.add("%s=%s".formatted(entry.getKey(), entry.getValue()));
    }

    return joiner.toString();
  }

  /**
   * Checks if the text is null or empty, and throws an exception if it is.
   *
   * @param text Text to check.
   * @return Provided string, if not blank.
   * @throws IllegalArgumentException If the provided string is blank
   */
  public static String requireNotBlank(final String text, final String message) {
    if (isBlank(text)) {
      throw new IllegalArgumentException(message);
    }
    return text;
  }

  public static String toCamelCase(final String identifier) {
    if (isBlank(identifier)) {
      return identifier;
    }

    // Normalize all separators to a single delimiter
    final String normalized = identifier.replace('-', '_').toLowerCase(Locale.ROOT);

    final String[] parts = normalized.split("_");
    if (parts.length == 0) {
      return normalized;
    }

    final StringBuilder camel = new StringBuilder(parts[0]);
    for (int i = 1; i < parts.length; i++) {
      final String part = parts[i];
      if (!part.isEmpty()) {
        camel.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
      }
    }

    return camel.toString();
  }

  public static String toKebabCase(final String identifier) {
    if (isBlank(identifier)) {
      return identifier;
    }
    return toSnakeCase(identifier).replace('_', '-');
  }

  public static String toSnakeCase(final String identifier) {
    if (isBlank(identifier)) {
      return identifier;
    }
    String normalizedIdentifier = identifier.replace('-', '_').replace(' ', '_');
    normalizedIdentifier = acronymBoundaryPattern.matcher(normalizedIdentifier).replaceAll("$1_$2");
    normalizedIdentifier =
        camelCaseBoundaryPattern.matcher(normalizedIdentifier).replaceAll("$1_$2");
    normalizedIdentifier = normalizedIdentifier.toLowerCase(Locale.ROOT);
    normalizedIdentifier = repeatedUnderscorePattern.matcher(normalizedIdentifier).replaceAll("_");
    return normalizedIdentifier.replaceAll("^_+|_+$", "");
  }

  public static String trimToEmpty(final String text) {
    if (text == null) {
      return "";
    }
    return text.strip();
  }

  private static int indexOfDifference(final String string1, final String string2) {
    if (string1 == null || string2 == null) {
      return 0;
    }
    int i;
    for (i = 0; i < string1.length() && i < string2.length(); ++i) {
      if (string1.charAt(i) != string2.charAt(i)) {
        break;
      }
    }
    if (i < string2.length() || i < string1.length()) {
      return i;
    }
    return -1;
  }

  private Utility() {
    // Prevent instantiation
  }
}
