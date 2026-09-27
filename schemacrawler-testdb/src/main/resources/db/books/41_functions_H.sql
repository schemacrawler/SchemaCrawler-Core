-- SchemaCrawler
-- http://www.schemacrawler.com
-- Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
-- All rights reserved.
-- SPDX-License-Identifier: EPL-2.0

-- H2 syntax
-- Functions
CREATE ALIAS CustomAdd AS $$
Integer customAdd(Integer one, Integer two) {
  return one == null || two == null ? null : one + two;
}
$$
;
