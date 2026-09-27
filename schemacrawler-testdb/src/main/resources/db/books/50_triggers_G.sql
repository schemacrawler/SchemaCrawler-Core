-- SchemaCrawler
-- http://www.schemacrawler.com
-- Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
-- All rights reserved.
-- SPDX-License-Identifier: EPL-2.0

-- Triggers
-- H2
CREATE TRIGGER TRG_Authors
AFTER DELETE ON Authors
FOR EACH ROW
AS $$
org.h2.api.Trigger create() {
  return new org.h2.api.Trigger() {
    @Override
    public void fire(
        java.sql.Connection conn,
        Object[] oldRow,
        Object[] newRow
    ) throws java.sql.SQLException {
      try (java.sql.PreparedStatement stmt = conn.prepareStatement(
          "UPDATE Publishers " +
          "SET Publisher = ? " +
          "WHERE Publisher = ?")) {
        stmt.setString(1, "Jacob");
        stmt.setString(2, "John");
        stmt.executeUpdate();
      }
    }
  };
}
$$
;
