-- ===========================
-- Upgrade links
-- ===========================
ALTER TABLE link ADD COLUMN title TEXT;
ALTER TABLE link ADD COLUMN display_title TEXT;
ALTER TABLE link ADD COLUMN summary TEXT;
ALTER TABLE link ADD COLUMN published_date DATETIME DEFAULT NOW();
ALTER TABLE link ADD COLUMN type INT;
ALTER TABLE link ADD COLUMN content_key TEXT;
ALTER TABLE link ADD COLUMN content_type VARCHAR(64);
ALTER TABLE link ADD COLUMN content_length INTEGER;
ALTER TABLE link ADD COLUMN invalid_reason VARCHAR(20);
ALTER TABLE link ADD COLUMN width INT;
ALTER TABLE link ADD COLUMN height INT;
ALTER TABLE link ADD COLUMN status INT;

CREATE INDEX idx_link__status_type_published_date ON link (status, type, published_date);

-- ===========================
-- assets
-- ===========================
CREATE TABLE asset (
  id                INT         NOT NULL AUTO_INCREMENT,
  link_fk           INT         NOT NULL REFERENCES link (id),
  target_fk         INT         NOT NULL REFERENCES link (id),
  type              VARCHAR(20) NOT NULL,
  creation_datetime DATETIME             DEFAULT NOW(),

  PRIMARY KEY (id),
  UNIQUE (link_fk, target_fk, type)
) ENGINE = InnoDB;


INSERT INTO feed (id, NAME, url, logo_url, path)
  VALUE (34, "C'Koment Magazine", 'http://www.ckomentpublishing.com', 'feeds/ckoment-magazine.jpeg', NULL);

INSERT INTO feed (id, NAME, url, logo_url, path)
  VALUE (35, 'TIC Mag', 'http://www.ticmag.net', 'feeds/tic-mag.jpeg', NULL);


-- #########################
-- article -> link
-- #########################
UPDATE link l, article a
SET l.s3_key       = a.s3_key,
  l.title          = a.title,
  l.display_title  = a.display_title,
  l.summary        = a.summary,
  l.published_date = a.published_date,
  l.content_length = a.content_length,
  l.content_type   = 'text/html',
  l.invalid_reason = a.invalid_reason,
  l.status         = a.status,
  l.width          = 0,
  l.height         = 0,
  l.type           = 0
WHERE a.link_fk = l.id;

-- invalid
UPDATE link SET status = 1, invalid_reason = 'duplicate' WHERE status = 2;

-- duplicate
UPDATE link SET status = 2 WHERE status = 16;

-- published
UPDATE link SET status = 3 WHERE status = 32;


-- #########################
-- image -> link
-- #########################
INSERT INTO link (
  id,
  feed_fk,
  url,
  url_hash,
  s3_key,
  width,
  height,
  content_length,
  content_type,
  creation_datetime,
  published_date,
  STATUS,
  type
)
  SELECT
    100000 + i.id,
    l.feed_fk,
    i.url,
    MD5(i.url),
    i.s3_key,
    i.width,
    i.height,
    i.content_length,
    i.content_type,
    i.creation_datetime,
    i.creation_datetime,
    3,
    1
  FROM image i JOIN link l ON i.link_fk = l.id
  WHERE i.type = 2
ON DUPLICATE KEY UPDATE
  status = 3;

# Thumbnails image
INSERT INTO asset (link_fk, target_fk, type)
  SELECT
    i.link_fk,
    l.id,
    1
  FROM image i, link l
  WHERE i.type = 2 AND MD5(i.url) = l.url_hash
ON DUPLICATE KEY UPDATE
  asset.type = 1;


-- #########################
-- video -> link
-- #########################
INSERT INTO link (
  id,
  feed_fk,
  url,
  url_hash,
  creation_datetime,
  published_date,
  STATUS,
  type,
  content_length,
  width,
  height
)
  SELECT
    200000 + v.id,
    l.feed_fk,
    v.embed_url,
    MD5(v.embed_url),
    v.creation_datetime,
    v.creation_datetime,
    3,
    2,
    0,
    0,
    0
  FROM video v JOIN link l ON v.link_fk = l.id
ON DUPLICATE KEY UPDATE
  status = 3;


INSERT INTO asset (link_fk, target_fk, type)
  SELECT
    v.link_fk,
    l.id,
    2
  FROM video v JOIN link l
  WHERE MD5(v.embed_url) = url_hash
ON DUPLICATE KEY UPDATE
  type = 2;
