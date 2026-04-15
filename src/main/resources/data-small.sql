INSERT INTO category (name)
VALUES('GENERAL'),
      ('NOTICE'),
      ('QUESTION');

INSERT INTO member (login_id, password, name, birth_date, gender, email, address, created_date, last_modified_date)
VALUES
('user1', '1234', 'user1', PARSEDATETIME('2000-03-03', 'yyyy-MM-dd'), 'MALE', 'user1@test.com', '주소',  NOW(), NOW()),
('user2', '1234', 'user2', PARSEDATETIME('2001-04-04', 'yyyy-MM-dd'), 'FEMALE', 'user2@test.com', '주소', NOW(), NOW()),
('user3', '1234', 'user3', PARSEDATETIME('2002-05-05', 'yyyy-MM-dd'), 'MALE', 'user3@test.com', '주소',  NOW(), NOW()),
('user4', '1234', 'user4', PARSEDATETIME('2003-06-06', 'yyyy-MM-dd'), 'FEMALE', 'user4@test.com', '주소', NOW(), NOW()),
('user5', '1234', 'user5', PARSEDATETIME('2004-07-07', 'yyyy-MM-dd'), 'MALE', 'user5@test.com', '주소',  NOW(), NOW()),
('user6', '1234', 'user6', PARSEDATETIME('2005-08-08', 'yyyy-MM-dd'), 'FEMALE', 'user6@test.com', '주소', NOW(), NOW()),
('user7', '1234', 'user7', PARSEDATETIME('2006-09-09', 'yyyy-MM-dd'), 'MALE', 'user7@test.com', '주소',  NOW(), NOW()),
('user8', '1234', 'user8', PARSEDATETIME('2007-10-10', 'yyyy-MM-dd'), 'FEMALE', 'user8@test.com', '주소', NOW(), NOW()),
('user9', '1234', 'user9', PARSEDATETIME('2008-11-11', 'yyyy-MM-dd'), 'MALE', 'user9@test.com', '주소',  NOW(), NOW()),
('user10', '1234', 'user10', PARSEDATETIME('2009-12-12', 'yyyy-MM-dd'), 'FEMALE', 'user10@test.com','주소', NOW(), NOW());


INSERT INTO board (member_id, title, content, board_open, category_id, view_count, created_date, last_modified_date)
VALUES
(1, '게시글 제목1', '게시글 내용1', 'ALL', 1, 100, PARSEDATETIME('2026-01-01', 'yyyy-MM-dd'), PARSEDATETIME('2026-01-01', 'yyyy-MM-dd')),
(1, '게시글 제목2', '게시글 내용2', 'ALL', 1, 200, PARSEDATETIME('2026-02-02', 'yyyy-MM-dd'), PARSEDATETIME('2026-02-02', 'yyyy-MM-dd')),
(2, '게시글 제목3', '게시글 내용3', 'ALL', 1, 300, PARSEDATETIME('2026-03-03', 'yyyy-MM-dd'), PARSEDATETIME('2026-03-03', 'yyyy-MM-dd')),
(2, '게시글 제목4', '게시글 내용4', 'ALL', 1, 400, PARSEDATETIME('2026-04-04', 'yyyy-MM-dd'), PARSEDATETIME('2026-04-04', 'yyyy-MM-dd')),
(3, '게시글 제목5', '게시글 내용5', 'ALL', 1, 500, PARSEDATETIME('2025-05-05', 'yyyy-MM-dd'), PARSEDATETIME('2025-05-05', 'yyyy-MM-dd')),
(3, '게시글 제목6', '게시글 내용6', 'ALL', 1, 600, PARSEDATETIME('2025-06-06', 'yyyy-MM-dd'), PARSEDATETIME('2025-06-06', 'yyyy-MM-dd')),
(4, '게시글 제목7', '게시글 내용7', 'ALL', 1, 700, PARSEDATETIME('2025-07-07', 'yyyy-MM-dd'), PARSEDATETIME('2025-07-07', 'yyyy-MM-dd')),
(4, '게시글 제목8', '게시글 내용8', 'ALL', 1, 800, PARSEDATETIME('2025-08-08', 'yyyy-MM-dd'), PARSEDATETIME('2025-08-08', 'yyyy-MM-dd')),
(5, '게시글 제목9', '게시글 내용9', 'ALL', 1, 900, PARSEDATETIME('2025-09-09', 'yyyy-MM-dd'), PARSEDATETIME('2025-09-09', 'yyyy-MM-dd')),
(5, '게시글 제목10', '게시글 내용10', 'ALL', 1, 101, PARSEDATETIME('2025-10-10', 'yyyy-MM-dd'), PARSEDATETIME('2025-10-10', 'yyyy-MM-dd')),
(6, '게시글 제목11', '게시글 내용11', 'ALL', 1, 202, PARSEDATETIME('2025-11-11', 'yyyy-MM-dd'), PARSEDATETIME('2025-11-11', 'yyyy-MM-dd')),
(6, '게시글 제목12', '게시글 내용12', 'ALL', 1, 303, PARSEDATETIME('2025-12-12', 'yyyy-MM-dd'), PARSEDATETIME('2025-12-12', 'yyyy-MM-dd')),
(7, '게시글 제목13', '게시글 내용13', 'ALL', 1, 404, PARSEDATETIME('2026-01-02', 'yyyy-MM-dd'), PARSEDATETIME('2026-01-02', 'yyyy-MM-dd')),
(7, '게시글 제목14', '게시글 내용14', 'ALL', 1, 505, PARSEDATETIME('2026-02-03', 'yyyy-MM-dd'), PARSEDATETIME('2026-02-03', 'yyyy-MM-dd')),
(8, '게시글 제목15', '게시글 내용15', 'ALL', 1, 606, PARSEDATETIME('2026-03-04', 'yyyy-MM-dd'), PARSEDATETIME('2026-03-04', 'yyyy-MM-dd')),
(8, '게시글 제목16', '게시글 내용16', 'ALL', 1, 707, PARSEDATETIME('2026-04-05', 'yyyy-MM-dd'), PARSEDATETIME('2026-04-05', 'yyyy-MM-dd')),
(9, '게시글 제목17', '게시글 내용17', 'ALL', 1, 808, PARSEDATETIME('2025-05-06', 'yyyy-MM-dd'), PARSEDATETIME('2025-05-06', 'yyyy-MM-dd')),
(9, '게시글 제목18', '게시글 내용18', 'ALL', 1, 909, PARSEDATETIME('2025-06-07', 'yyyy-MM-dd'), PARSEDATETIME('2025-06-07', 'yyyy-MM-dd')),
(10, '게시글 제목19', '게시글 내용19', 'ALL', 1, 102, PARSEDATETIME('2025-07-08', 'yyyy-MM-dd'), PARSEDATETIME('2025-07-08', 'yyyy-MM-dd')),
(10, '게시글 제목20', '게시글 내용20', 'ALL', 1, 203, PARSEDATETIME('2025-08-09', 'yyyy-MM-dd'), PARSEDATETIME('2025-08-09', 'yyyy-MM-dd'));

INSERT INTO likes (member_id, board_id)
VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11), (1, 12), (1, 13), (1, 14), (1, 15),
(2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7), (2, 8), (2, 9), (2, 10), (2, 11), (2, 12),
(3, 1), (3, 2), (3, 3), (3, 4), (3, 5), (3, 6), (3, 7), (3, 8), (3, 9),
(4, 1), (4, 2),(4, 3), (4, 4), (4, 5), (4, 6), (4, 7), (4, 8), (4, 9), (4, 10), (4, 11), (4, 12), (4, 13), (4, 14), (4, 15), (4, 16),
(5, 1), (5, 2), (5, 3), (5, 4), (5, 5), (5, 6), (5, 7),
(6, 1), (6, 2), (6, 3), (6, 4), (6, 5), (6, 6), (6, 7), (6, 8), (6, 9), (6, 10),
(7, 1), (7, 2), (7, 3), (7, 4), (7, 5),
(8, 1), (8, 2), (8, 3), (8, 4),
(9, 1), (9, 2), (9, 3),
(10, 1), (10, 2);




