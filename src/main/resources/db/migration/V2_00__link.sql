ALTER TABLE link ADD COLUMN title           TEXT;
ALTER TABLE link ADD COLUMN display_title   TEXT;
ALTER TABLE link ADD COLUMN summary         TEXT;
ALTER TABLE link ADD COLUMN published_date  DATETIME    DEFAULT NOW();
ALTER TABLE link ADD COLUMN type            VARCHAR(20);
ALTER TABLE link ADD COLUMN content_key     TEXT;
ALTER TABLE link ADD COLUMN content_type    VARCHAR(64);
ALTER TABLE link ADD COLUMN content_length  INTEGER;
