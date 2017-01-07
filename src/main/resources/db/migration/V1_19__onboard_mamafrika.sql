-- Add mamafrika
DELETE FROM feed where id=19;
DELETE FROM article WHERE link_fk IN (SELECT id FROM link WHERE feed_fk=19);
DELETE FROM video WHERE link_fk IN (SELECT id FROM link WHERE feed_fk=19);
DELETE FROM image WHERE link_fk IN (SELECT id FROM link WHERE feed_fk=19);

INSERT INTO feed (id, name, url, logo_url, path) VALUE (19, 'MamAfrika TV', 'http://www.mamafrika.tv', 'feeds/mamafrika.png', '*/blog/*');
