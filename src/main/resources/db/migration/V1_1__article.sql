CREATE TABLE article(
  id        INT      NOT NULL AUTO_INCREMENT,
  link_fk   INT NOT NULL REFERENCES link(id),

  s3_key    TEXT NOT NULL,

  title           TEXT,
  display_title   TEXT,
  summary         TEXT,
  status          INT         DEFAULT 0,
  published_date  DATETIME    DEFAULT NOW(),

  creation_datetime DATETIME DEFAULT NOW(),


  PRIMARY KEY (id)
) ENGINE = InnoDB;
