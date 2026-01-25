/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.weakassociations;

import static java.util.Objects.requireNonNull;
import static us.fatehi.utility.CollectionsUtility.splitList;
import static us.fatehi.utility.Utility.isBlank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import us.fatehi.utility.Inflection;
import us.fatehi.utility.Multimap;
import us.fatehi.utility.string.ObjectToStringFormat;
import us.fatehi.utility.string.StringFormat;

/**
 * Derives normalized match keys by stripping common prefixes (based on a separator) and
 * singularizing the remaining token. These match keys are used by weak association inference to
 * relate tables and columns that share naming patterns across schemas.
 *
 * <p>The "signal-to-noise" balancing logic in {@code findPrefixes} ensures that only meaningful and
 * widely shared prefixes are considered. It ranks prefixes using a pair-count formula (n * (n - 1)
 * / 2), where n is the number of occurrences. This formula effectively prioritizes prefixes that
 * link more pairs of tables/columns, thereby highlighting strong naming patterns while filtering
 * out coincidental or infrequent prefixes.
 */
final class PrefixMatches {

  private static final Logger LOGGER = Logger.getLogger(PrefixMatches.class.getName());

  private static final int MAX_TOP_PREFIXES = 12;
  private static final double MIN_SHARED_PREFIX_RATIO = 0.5;

  private final String keySeparator;
  private final Multimap<String, String> keyPrefixes;

  /**
   * Creates match keys for a set of names using a token separator.
   *
   * @param keys Keys to analyze
   * @param keySeparator Separator between key tokens
   */
  PrefixMatches(final List<String> keys, final String keySeparator) {
    this.keySeparator = requireNonNull(keySeparator, "No key separator provided");
    keyPrefixes = new Multimap<>();

    analyze(keys);
  }

  /**
   * Returns normalized match keys for the supplied key.
   *
   * @param key Key to look up
   * @return Normalized match keys
   */
  public List<String> get(final String key) {
    if (keyPrefixes.containsKey(key)) {
      return keyPrefixes.get(key);
    }
    return Arrays.asList(key);
  }

  /**
   * Returns a string representation of the match key map.
   *
   * @return Match key map as a string
   */
  @Override
  public String toString() {
    return keyPrefixes.toString();
  }

  private void analyze(final List<String> keys) {
    if (keys.isEmpty()) {
      return;
    }

    final Collection<String> prefixes = findPrefixes(keys);
    mapPrefixes(keys, prefixes);

    LOGGER.log(Level.FINE, new StringFormat("Key prefixes=%s", prefixes));
    LOGGER.log(
        Level.FINE, new StringFormat("Key matches map: %s", new ObjectToStringFormat(keyPrefixes)));
  }

  private Map<String, Integer> countPrefixKeyOccurrences(final List<String> keys) {
    final Map<String, Integer> prefixKeyCounts = new TreeMap<>();
    for (final String key : keys) {
      final String[] splitKey = splitList(key, keySeparator);
      if (splitKey == null || splitKey.length <= 1) {
        continue;
      }

      // Build cumulative prefixes token-by-token: "schema_", "schema_table_", etc.
      final StringBuilder buffer = new StringBuilder(1024);
      for (final String token : splitKey) {
        buffer.append(token).append(keySeparator);
        final String prefix = buffer.toString();
        final int prevCount = prefixKeyCounts.getOrDefault(prefix, 0);
        prefixKeyCounts.put(prefix, prevCount + 1);
      }
    }
    return prefixKeyCounts;
  }

  /**
   * Finds key prefixes. Prefixes are separated by a separator character.
   *
   * @param keys Keys
   * @return Key name prefixes
   */
  private Collection<String> findPrefixes(final List<String> keys) {
    // Count how many keys share each token-boundary prefix in a single pass
    final Map<String, Integer> prefixKeyCounts = countPrefixKeyOccurrences(keys);

    // Sort prefixes by the number of keys using them, in descending order
    final List<Map.Entry<String, Integer>> prefixesList =
        new ArrayList<>(prefixKeyCounts.entrySet());
    Collections.sort(prefixesList, Comparator.comparing(Entry<String, Integer>::getValue));

    // Reduce the number of prefixes in use by keeping the top-ranked few and any
    // prefix that is still widely shared. This balances signal (popular prefixes)
    // with keeping the list small enough to avoid noisy matches.
    final List<String> prefixes = new ArrayList<>();
    for (int i = 0; i < prefixesList.size(); i++) {
      final boolean isTopPrefix = i < MAX_TOP_PREFIXES;
      final boolean isWidelyUsed =
          prefixesList.get(i).getValue() > prefixKeyCounts.size() * MIN_SHARED_PREFIX_RATIO;
      if (isTopPrefix || isWidelyUsed) {
        prefixes.add(prefixesList.get(i).getKey());
      }
    }
    // Always return the full key as a prefix to itself
    prefixes.add("");

    return prefixes;
  }

  private void mapPrefixes(final List<String> keys, final Collection<String> prefixes) {
    for (final String key : keys) {
      for (final String prefix : prefixes) {
        String matchKeyName = key.toLowerCase();
        if (matchKeyName.startsWith(prefix)) {
          matchKeyName = matchKeyName.substring(prefix.length());
          matchKeyName = Inflection.singularize(matchKeyName);
          if (!isBlank(matchKeyName)) {
            keyPrefixes.add(key, matchKeyName);
          }
        }
      }
    }
  }
}
