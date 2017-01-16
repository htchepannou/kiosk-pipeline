INSERT INTO feed (id, name, url, logo_url, path) VALUE (22, 'OkabolMedia', 'http://okabolmedia.com/', 'feeds/okabolmedia.jpg', NULL);

-- remove article from LeFilmCamerounais
DELETE FROM article WHERE link_fk IN (SELECT id FROM link WHERE feed_fk=21);
DELETE FROM link WHERE feed_fk=21;
