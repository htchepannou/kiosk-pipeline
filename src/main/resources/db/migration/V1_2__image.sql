CREATE TABLE image(
  id        INT      NOT NULL AUTO_INCREMENT,
  link_fk   INT NOT NULL REFERENCES link(id),

  url       TEXT NOT NULL,
  s3_key    TEXT NOT NULL,
  type      INT NOT NULL,

  width     INT,
  height    INT,

  creation_datetime DATETIME DEFAULT NOW(),


  PRIMARY KEY (id)
) ENGINE = InnoDB;
