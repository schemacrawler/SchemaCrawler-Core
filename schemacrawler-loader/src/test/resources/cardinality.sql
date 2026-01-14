CREATE TABLE Parent
(
  Id INTEGER NOT NULL,
  CONSTRAINT PK_Parent PRIMARY KEY (Id)
)
;

-- zero_one: Unique, Nullable
CREATE TABLE ZeroOneChild
(
  Id INTEGER NOT NULL,
  ParentId INTEGER,
  CONSTRAINT PK_ZeroOneChild PRIMARY KEY (Id),
  CONSTRAINT FK_ZeroOneChild_Parent FOREIGN KEY (ParentId) REFERENCES Parent (Id),
  CONSTRAINT U_ZeroOneChild_Parent UNIQUE (ParentId)
)
;

-- one_one: Unique, Not Null
CREATE TABLE OneOneChild
(
  Id INTEGER NOT NULL,
  ParentId INTEGER NOT NULL,
  CONSTRAINT PK_OneOneChild PRIMARY KEY (Id),
  CONSTRAINT FK_OneOneChild_Parent FOREIGN KEY (ParentId) REFERENCES Parent (Id),
  CONSTRAINT U_OneOneChild_Parent UNIQUE (ParentId)
)
;

-- zero_many: Not Unique, Nullable
CREATE TABLE ZeroManyChild
(
  Id INTEGER NOT NULL,
  ParentId INTEGER,
  CONSTRAINT PK_ZeroManyChild PRIMARY KEY (Id),
  CONSTRAINT FK_ZeroManyChild_Parent FOREIGN KEY (ParentId) REFERENCES Parent (Id)
)
;

-- one_many: Not Unique, Not Null
CREATE TABLE OneManyChild
(
  Id INTEGER NOT NULL,
  ParentId INTEGER NOT NULL,
  CONSTRAINT PK_OneManyChild PRIMARY KEY (Id),
  CONSTRAINT FK_OneManyChild_Parent FOREIGN KEY (ParentId) REFERENCES Parent (Id)
)
;
