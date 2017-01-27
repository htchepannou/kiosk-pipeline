DELETE FROM article WHERE link_fk IN (SELECT id FROM link WHERE feed_fk = 25);
DELETE FROM link WHERE feed_fk = 25;

INSERT INTO feed (id, NAME, url, logo_url, path)
  VALUE (26, 'cameroun-online.com', 'http://www.cameroun-online.com', 'feeds/cameroun-online.jpg', '*/fr/*');
