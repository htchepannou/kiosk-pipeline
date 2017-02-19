UPDATE link SET url_hash = IF(RIGHT(url, 1) = '/', LEFT(url, LENGTH(url)-1), url);
UPDATE link SET url_hash = MD5(url);
