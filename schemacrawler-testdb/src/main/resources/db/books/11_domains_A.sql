-- SchemaCrawler
-- http://www.schemacrawler.com
-- Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
-- All rights reserved.
-- SPDX-License-Identifier: EPL-2.0

-- Domains
CREATE DOMAIN VALID_STRING
AS VARCHAR(20)
  DEFAULT 'NO VALUE'
  CHECK (VALUE IS NOT NULL AND CHARACTER_LENGTH(VALUE) > 2);

-- Table using domains
CREATE TABLE CustomerData
(
  CustomerId INTEGER NOT NULL,
  DataId INTEGER NOT NULL,
  Data VALID_STRING,
  CONSTRAINT PK_CustomerData PRIMARY KEY (CustomerId, DataId),
  CONSTRAINT FK_Customer FOREIGN KEY (CustomerId) REFERENCES Customers (Id)
)
;
