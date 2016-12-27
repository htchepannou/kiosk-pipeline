CREATE TABLE feed (
  id                INT NOT NULL AUTO_INCREMENT,

  name              VARCHAR(64),
  url               VARCHAR(255),
  path              VARCHAR(64),
  logo_url          VARCHAR(255),

  creation_datetime DATETIME     DEFAULT NOW(),

  PRIMARY KEY (id)
) ENGINE = InnoDB;

INSERT INTO feed (id, name, url, logo_url, path) VALUE (1, 'camer.be', 'http://www.camer.be', '/feeds/camer.be.png', NULL);
INSERT INTO feed (id, name, url, logo_url, path) VALUE (2, 'camer24', 'http://www.camer24.de', '/feeds/camer24.png', NULL);
INSERT INTO feed (id, name, url, logo_url, path) VALUE (3, 'Cameroun Sports', 'http://www.camerounsports.info', '/feeds/cameroun-sports.png', '*.html');
INSERT INTO feed (id, name, url, logo_url, path) VALUE (4, 'Camfoot', 'http://www.camfoot.com', '/feeds/camfoot.png', '*.html');
INSERT INTO feed (id, name, url, logo_url, path) VALUE (5, 'CulturEbene', 'http://www.culturebene.com', '/feeds/culturebene.png', '*.html');
INSERT INTO feed (id, name, url, logo_url, path) VALUE (6, 'Spark Cameroun', 'http://www.sparkcameroun.com', '/feeds/sparkcameroon.png', NULL);
INSERT INTO feed (id, name, url, logo_url, path) VALUE (7, 'Je Wanda Magazine', 'http://www.jewanda-magazine.com', '/feeds/jewanda-magazine.jpg', NULL);

ALTER TABLE link ADD COLUMN feed_fk INT REFERENCES feed (id);

UPDATE link SET feed_fk = 1 WHERE url LIKE 'http://www.camer.be/%';
UPDATE link SET feed_fk = 2 WHERE url LIKE 'http://www.camer24.de/%';
UPDATE link SET feed_fk = 3 WHERE url LIKE 'http://www.camerounsports.info/%';
UPDATE link SET feed_fk = 4 WHERE url LIKE 'http://www.camfoot.com/%';
UPDATE link SET feed_fk = 5 WHERE url LIKE 'http://www.culturebene.com/%';
UPDATE link SET feed_fk = 6 WHERE url LIKE 'http://www.sparkcameroun.com/%';
