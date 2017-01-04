ALTER TABLE video MODIFY embed_url VARCHAR(100);

-- remove pollution
DELETE FROM video WHERE embed_url = 'https://www.youtube.com/embed/1ST4VM9vSBg';
DELETE FROM video WHERE embed_url = 'https://www.youtube.com/embed/9xVd_1WEMOA';

-- new feed
INSERT INTO feed (id, name, url, logo_url, path) VALUE (19, 'MamAfrika TV', 'http://www.mamafrika.tv', 'feeds/mamafrika.png', NULL);
INSERT INTO feed (id, name, url, logo_url, path) VALUE (20, 'CamerNews', 'http://www.camernews.com', 'feeds/camernews.jpg', NULL);
