ALTER TABLE feed ADD COLUMN display_title_regex VARCHAR (64);
UPDATE feed SET display_title_regex=".+::(.+)::.+" WHERE id=1;
