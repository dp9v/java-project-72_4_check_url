INSERT INTO url
(name, created_at)
VALUES
    ('https://en.hexlet.io', '2023-07-10 12:00:01');

INSERT INTO url_check
(status_code, title, h1, description, url_id, created_at)
VALUES
    (200, 'title', 'h1', 'description', 1, '2023-07-10 12:00:01');
