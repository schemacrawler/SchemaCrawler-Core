-- SchemaCrawler
-- http://www.schemacrawler.com
-- Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
-- All rights reserved.
-- SPDX-License-Identifier: EPL-2.0

-- H2 syntax
-- Stored procedures
CREATE ALIAS NEW_PUBLISHER_FORCE_VALUE AS '
String newPublisherForceValue() {
  return "New Publisher";
}
'
;

CREATE ALIAS NEW_PUBLISHER AS '
String newPublisher(String newPublisher) {
  return newPublisher;
}
'
;
