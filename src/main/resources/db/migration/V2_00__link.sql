ALTER TABLE link ADD COLUMN title TEXT;
ALTER TABLE link ADD COLUMN display_title TEXT;
ALTER TABLE link ADD COLUMN summary TEXT;
ALTER TABLE link ADD COLUMN published_date DATETIME DEFAULT NOW();
ALTER TABLE link ADD COLUMN type VARCHAR(20);
ALTER TABLE link ADD COLUMN content_key TEXT;
ALTER TABLE link ADD COLUMN content_type VARCHAR(64);
ALTER TABLE link ADD COLUMN content_length INTEGER;
ALTER TABLE link ADD COLUMN valid BIT;
ALTER TABLE link ADD COLUMN invalid_reason VARCHAR(20);
ALTER TABLE link ADD COLUMN width INT;
ALTER TABLE link ADD COLUMN height INT;
ALTER TABLE link ADD COLUMN published BIT;


INSERT INTO feed (id, NAME, url, logo_url, path)
  VALUE (34, "C'Koment Magazine", 'http://www.ckomentpublishing.com', 'feeds/ckoment-magazine.jpeg', NULL);

INSERT INTO feed (id, NAME, url, logo_url, path)
  VALUE (35, 'TIC Mag', 'http://www.ticmag.net', 'feeds/tic-mag.jpeg', NULL);



CREATE TABLE asset (
  id        INT         NOT NULL AUTO_INCREMENT,
  link_fk   INT         NOT NULL REFERENCES link (id),
  target_fk INT         NOT NULL REFERENCES link (id),
  type      VARCHAR(20) NOT NULL,
  creation_datetime DATETIME DEFAULT NOW(),

  PRIMARY KEY (id),
  UNIQUE (link_fk, target_fk, type)
) ENGINE = InnoDB;
