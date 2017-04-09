
# Disable mboko.tv and mamaafrika
ALTER TABLE feed ADD COLUMN active BIT DEFAULT 1;
UPDATE feed SET active = TRUE;
UPDATE feed SET active = FALSE WHERE id = 14 OR id = 19;

# Drop unused fields
ALTER TABLE link DROP COLUMN shingle_key;

# Drop unused tables
DROP TABLE article;
DROP TABLE video;
DROP TABLE image;
