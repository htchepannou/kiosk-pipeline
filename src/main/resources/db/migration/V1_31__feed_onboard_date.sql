ALTER TABLE feed ADD COLUMN onboard_date DATETIME DEFAULT NOW();

update feed SET onboard_date=NOW();
