CREATE TABLE link (
  id      INT      NOT NULL AUTO_INCREMENT,
  url     TEXT,
  keyhash CHAR(32) NOT NULL UNIQUE,

  PRIMARY KEY (id)
) ENGINE = InnoDB;
