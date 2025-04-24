INSERT INTO `partnership` (`idx`, `name`, `domain`, `image_url`, `image_path`, `comment`, `update_date`, `create_date`) VALUES (1, '1', '1', NULL, NULL, NULL, NULL, '2025-04-21 18:45:53');
INSERT INTO `project_category` (`idx`, `partnership_idx`, `name`, `sort`, `update_date`, `create_date`) VALUES (1, 1, '미분류', 1, now(), now());
INSERT INTO `project_category` (`idx`, `partnership_idx`, `name`, `sort`, `update_date`, `create_date`) VALUES (2, 1, '피카츄', 2, now(), now());
INSERT INTO `project_category` (`idx`, `partnership_idx`, `name`, `sort`, `update_date`, `create_date`) VALUES (3, 1, '라이츄', 3, now(), now());
INSERT INTO `project_category` (`idx`, `partnership_idx`, `name`, `sort`, `update_date`, `create_date`) VALUES (4, 1, '파이리', 4, now(), now());
INSERT INTO `project` (`idx`, `project_category_idx`, `partnership_idx`, `title`, `description`, `status_cd`, `image_url`, `image_path`, `node_cnt`, `edge_cnt`, `update_date`, `create_date`, `delete_date`) VALUES (1, 1, 1, 'test', 'test', NULL, NULL, NULL, 0, 0, '2025-04-23 06:36:16', '2025-04-23 06:36:16', '2025-04-23 06:36:16');
