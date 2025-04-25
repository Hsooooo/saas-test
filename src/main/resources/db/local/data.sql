INSERT INTO `partnership` (`idx`, `name`, `domain`, `image_url`, `image_path`, `comment`, `update_date`, `create_date`) VALUES (1, '1', '1', NULL, NULL, NULL, NULL, '2025-04-21 18:45:53');
INSERT INTO `project_category` (`idx`, `partnership_idx`, `name`, `sort`, `update_date`, `create_date`) VALUES (1, 1, '미분류', 1, now(), now());
INSERT INTO `project_category` (`idx`, `partnership_idx`, `name`, `sort`, `update_date`, `create_date`) VALUES (2, 1, '피카츄', 2, now(), now());
INSERT INTO `project_category` (`idx`, `partnership_idx`, `name`, `sort`, `update_date`, `create_date`) VALUES (3, 1, '라이츄', 3, now(), now());
INSERT INTO `project_category` (`idx`, `partnership_idx`, `name`, `sort`, `update_date`, `create_date`) VALUES (4, 1, '파이리', 4, now(), now());
INSERT INTO `project` (`idx`, `partnership_idx`, `project_category_idx`, `title`, `description`, `status_cd`, `image_url`, `image_path`, `node_cnt`, `edge_cnt`, `update_date`, `create_date`) VALUES (1, 1, 1, 'test', 'test', NULL, NULL, NULL, 0, 0, '2025-04-23 06:36:16', '2025-04-23 06:36:16');
INSERT INTO `member` (idx, email, password, name, profile_image_url, type_cd, state_cd, comment, last_login_date, leave_date, update_date, create_date) VALUES (1, 'test@test.com', '$2a$10$3yOGt4XE/3VNv3TxgGDM1.H76.NhggSfg2XuN7GgdhZxQDrlPnNIi', null, null, 'MTP0001', 'MST0002', null, null, null, '2025-04-25 11:07:09', '2025-04-25 11:07:09');
INSERT INTO `partnership_member` (idx, partnership_idx, member_idx) VALUES(1, 1, 1);
