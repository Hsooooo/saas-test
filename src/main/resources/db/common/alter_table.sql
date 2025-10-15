ALTER TABLE `em_saas`.`project`
    ADD COLUMN IF NOT EXISTS `partnership_idx` INT(11) NULL DEFAULT NULL COMMENT '프로젝트 카테고리번호' AFTER `idx`;
ALTER TABLE `em_saas`.`project`
    ADD COLUMN IF NOT EXISTS `delete_date` DATETIME NULL DEFAULT NULL COMMENT '프로젝트 삭제일' AFTER `create_date`;