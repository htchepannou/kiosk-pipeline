CREATE TABLE tag (
  id       INT      NOT NULL AUTO_INCREMENT,
  name     VARCHAR(64),
  creation_datetime DATETIME DEFAULT NOW(),

  PRIMARY KEY (id),
  UNIQUE(name)
) ENGINE = InnoDB;

CREATE TABLE link_tag (
  id                INT         NOT NULL AUTO_INCREMENT,
  link_fk           INT         NOT NULL REFERENCES link (id),
  tag_fk            INT         NOT NULL REFERENCES tag (id),
  creation_datetime DATETIME    DEFAULT NOW(),

  PRIMARY KEY (id),
  UNIQUE (link_fk, tag_fk)
) ENGINE = InnoDB;
