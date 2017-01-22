DELETE FROM article WHERE link_fk IN (SELECT id FROM link WHERE feed_fk=23);
DELETE FROM link WHERE feed_fk=23;
DELETE FROM feed WHERE id=23;

INSERT INTO feed (id, name, url, logo_url, path) VALUE (23, 'Mamy Muna', 'http://mamymuna.com', 'feeds/mamymuna.jpg', NULL);
