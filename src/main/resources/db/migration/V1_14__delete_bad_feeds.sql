-- cameroon-tribune, crtv, cameroon-info.net, aucunlait.net
DELETE FROM article WHERE link_fk IN (SELECT id FROM link WHERE feed_fk IN (10,11,12,16));
DELETE FROM feed WHERE id IN (10,11,12,16);

-- hotjem
UPDATE feed set url='http://thehotjem.com' WHERE id=18;

-- cameroon-online
UPDATE feed set name='CameroonOnline.org' WHERE id=15;