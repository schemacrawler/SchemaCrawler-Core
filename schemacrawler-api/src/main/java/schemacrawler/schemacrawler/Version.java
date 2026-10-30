/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.schemacrawler;

import java.io.Serial;
import us.fatehi.utility.property.BaseProductVersion;

/**
 * Version information for this product. Has methods to obtain information about the product, as
 * well as a main method, so it can be called from the command-line.
 */
public final class Version extends BaseProductVersion {

  @Serial private static final long serialVersionUID = 1143606778430634288L;

  private static final String PRODUCT_VERSION = "18.0.1";

  private static final String ABOUT =
      """
      SchemaCrawler %s
      Database schema discovery and comprehension tool
      https://www.schemacrawler.com/
      Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.

      You can search for database schema objects using regular expressions,
      and output the schema and data in a readable text format. You can find
      potential schema design issues with lint. The output serves for
      database documentation is designed to be diff-ed against other database
      schemas. SchemaCrawler also generates schema diagrams.
      """
          .stripIndent()
          .formatted(PRODUCT_VERSION);

  private static final Version VERSION = new Version("SchemaCrawler", PRODUCT_VERSION);

  /**
   * Information about this product.
   *
   * @return Information about this product.
   */
  public static String about() {
    return ABOUT;
  }

  public static Version version() {
    return VERSION;
  }

  private Version(final String productName, final String productVersion) {
    super(productName, productVersion);
  }
}
