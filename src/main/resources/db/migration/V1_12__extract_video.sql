CREATE TABLE video(
  id        INT      NOT NULL AUTO_INCREMENT,
  link_fk   INT NOT NULL REFERENCES link(id),

  embed_url TEXT NOT NULL,

  creation_datetime DATETIME DEFAULT NOW(),

  PRIMARY KEY (id)
) ENGINE = InnoDB;

INSERT INTO feed (id, name, url, logo_url, path) VALUE (14, 'Mboko TV', 'http://www.mbokotv.com/', 'feeds/mbokotv.jpg', null);
