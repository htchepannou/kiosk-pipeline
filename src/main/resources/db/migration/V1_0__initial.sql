CREATE TABLE link (
  id       INT      NOT NULL AUTO_INCREMENT,
  url      TEXT,
  s3_key   TEXT,
  url_hash CHAR(32) NOT NULL UNIQUE,
  creation_datetime DATETIME DEFAULT NOW(),

  PRIMARY KEY (id)
) ENGINE = InnoDB;
