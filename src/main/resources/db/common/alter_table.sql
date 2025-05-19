ALTER TABLE `em_saas`.`project`
    ADD COLUMN IF NOT EXISTS `partnership_idx` INT(11) NULL DEFAULT NULL COMMENT '프로젝트 카테고리번호' AFTER `idx`;
ALTER TABLE `em_saas`.`project`
    ADD COLUMN IF NOT EXISTS `delete_date` DATETIME NULL DEFAULT NULL COMMENT '프로젝트 삭제일' AFTER `create_date`;
ALTER TABLE `em_saas`.`member_login_history`
    MODIFY COLUMN `browser` VARCHAR(255) NULL DEFAULT NULL COMMENT '접속 브라우저',
    MODIFY COLUMN `platform` VARCHAR(255) NULL DEFAULT NULL COMMENT '접속 환경';

