ALTER TABLE article ADD content_length INT DEFAULT 0;

UPDATE feed SET logo_url = SUBSTR(logo_url, 2);
