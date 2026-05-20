UPDATE comment_image
SET url = CONCAT('http://localhost:8080/images/', SUBSTRING_INDEX(url, '/', -1))
WHERE url IS NOT NULL
  AND url <> ''
  AND url NOT LIKE 'http://localhost:8080/images/%';
