/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility.crawl;

import static us.fatehi.utility.Utility.isBlank;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import schemacrawler.schema.AttributedObject;
import schemacrawler.schema.DescribedObject;

abstract class AbstractLightNamedObject implements AttributedObject, DescribedObject, Serializable {

  @Serial private static final long serialVersionUID = 1L;

  private final Map<String, Object> attributes = new HashMap<>();
  private String remarks;

  @Override
  public final <T> T getAttribute(final String name) {
    return (T) attributes.get(name);
  }

  @Override
  public final <T> T getAttribute(final String name, final T defaultValue)
      throws ClassCastException {
    if (hasAttribute(name)) {
      return getAttribute(name);
    }
    return defaultValue;
  }

  @Override
  public final Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public String getRemarks() {
    return remarks == null ? "" : remarks;
  }

  @Override
  public final boolean hasAttribute(final String name) {
    return attributes.containsKey(name);
  }

  @Override
  public final boolean hasRemarks() {
    return !isBlank(remarks);
  }

  @Override
  public final <T> Optional<T> lookupAttribute(final String name) {
    return Optional.ofNullable(getAttribute(name));
  }

  @Override
  public final void removeAttribute(final String name) {
    attributes.remove(name);
  }

  @Override
  public final <T> void setAttribute(final String name, final T value) {
    attributes.put(name, value);
  }

  @Override
  public final void setRemarks(final String remarks) {
    this.remarks = remarks;
  }
}
