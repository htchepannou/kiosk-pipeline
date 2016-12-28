ALTER TABLE article ADD content_length INT DEFAULT 0;
ALTER TABLE article ADD invalid_reason VARCHAR(64);

UPDATE feed SET logo_url = SUBSTR(logo_url, 2);
