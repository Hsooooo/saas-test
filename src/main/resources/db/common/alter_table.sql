ALTER TABLE `em_saas`.`project`
    ADD COLUMN IF NOT EXISTS `partnership_idx` INT(11) NULL DEFAULT NULL COMMENT '프로젝트 카테고리번호' AFTER `idx`;
ALTER TABLE `em_saas`.`project`
    ADD COLUMN IF NOT EXISTS `delete_date` DATETIME NULL DEFAULT NULL COMMENT '프로젝트 삭제일' AFTER `create_date`;
ALTER TABLE `em_saas`.`project_member`
    ADD COLUMN IF NOT EXISTS `delete_date` DATETIME NULL DEFAULT NULL COMMENT '삭제일' AFTER `create_date`;
-- 2) 활성여부 생성 컬럼(저장형) 추가
ALTER TABLE `em_saas`.`project_member`
    ADD COLUMN IF NOT EXISTS `active_flag` TINYINT(1)
    AS (CASE WHEN `delete_date` IS NULL THEN 1 ELSE 0 END) STORED;
CREATE UNIQUE INDEX ui_project_member_project_idx_partnership_member_idx_active_flag
    ON project_member (project_idx, partnership_member_idx, active_flag);
