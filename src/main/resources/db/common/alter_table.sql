ALTER TABLE `em_stock`.`theme_code` ADD COLUMN IF NOT EXISTS `prdy_ctrt` DOUBLE DEFAULT 0 COMMENT '테마 전일대비 등락률(%)' AFTER `theme_name`;
ALTER TABLE `em_stock`.`theme_code` ADD COLUMN IF NOT EXISTS `scrap_date` DATETIME COMMENT '수집 저장시간' AFTER `prdy_ctrt`;
ALTER TABLE `em_stock`.`theme_original` ADD COLUMN IF NOT EXISTS `prdy_ctrt` DOUBLE DEFAULT 0 COMMENT '종목 전일대비 등락률(%)' AFTER `bstp_name`;
ALTER TABLE `em_stock`.`theme_original` ADD COLUMN IF NOT EXISTS `scrap_date` DATETIME COMMENT '수집 저장시간' AFTER `prdy_ctrt`;
ALTER TABLE `em_stock`.`ksic_company` ADD COLUMN IF NOT EXISTS `ksic_code` VARCHAR(10) COMMENT '산업분류코드' AFTER `ksic_idx`;
ALTER TABLE `em_stock`.`ksic_company` ADD COLUMN IF NOT EXISTS `ksic_desc` VARCHAR(100) COMMENT '산업분류코드 설명' AFTER `ksic_code`;
ALTER TABLE `em_stock`.`ksic_company` ADD COLUMN IF NOT EXISTS `kor_isnm` VARCHAR(40) COMMENT '한글 종목명' AFTER `iscd`;
ALTER TABLE `em_stock`.`ksic_company` ADD COLUMN IF NOT EXISTS `company_business_category_code` VARCHAR(10) COMMENT '기업 산업 코드' AFTER `company_id`;
ALTER TABLE `em_stock`.`ksic_company` ADD INDEX IF NOT EXISTS ksic_company_company_id_index (company_id);
ALTER TABLE `em_stock`.`ksic_category` ADD COLUMN IF NOT EXISTS `prdy_ctrt` DOUBLE DEFAULT 0 COMMENT '산업분류 전일대비 등락률(%)' AFTER `code_desc`;
ALTER TABLE `em_stock`.`ksic_category` ADD COLUMN IF NOT EXISTS `scrap_date` DATETIME COMMENT '산업분류별 전일대비 등락률(%)' AFTER `prdy_ctrt`;
ALTER TABLE `em_stock`.`ksic_company` ADD COLUMN IF NOT EXISTS `prdy_ctrt` DOUBLE DEFAULT 0 COMMENT '종목별 전일대비 등락률(%)' AFTER `kor_isnm`;
ALTER TABLE `em_stock`.`ksic_company` ADD COLUMN IF NOT EXISTS `scrap_date` DATETIME COMMENT '수집날짜' AFTER `prdy_ctrt`;
ALTER TABLE `em_stock`.`ksic_company` DROP COLUMN IF EXISTS `create_date`;
ALTER TABLE `em_stock`.`ksic_company` DROP COLUMN IF EXISTS `update_date`;
ALTER TABLE `em_stock`.`jsise_real_time` ADD COLUMN IF NOT EXISTS `update_date` DATETIME COMMENT '수정일' AFTER `create_date`;
ALTER TABLE `em_stock`.`jsise_real_time` ADD COLUMN IF NOT EXISTS `prdy_vrss` BIGINT(20) COMMENT '전일 대비' AFTER `prdy_ctrt`;
ALTER TABLE `em_stock`.`jsise_real_time` ADD COLUMN IF NOT EXISTS `prdy_vrss_sign` VARCHAR(1) COMMENT '전일 대비 부호(1:상한 2:상승 3:보합 4:하한 5:하락)' AFTER `prdy_vrss`;
ALTER TABLE `em_stock`.`jsise_real_time` ADD COLUMN IF NOT EXISTS `w52_hgpr` bigint(20) COMMENT '52주 최고가' AFTER `prdy_vrss_sign`;
ALTER TABLE `em_stock`.`jsise_real_time` ADD COLUMN IF NOT EXISTS `w52_hgpr_date` bigint(20) COMMENT '52주 최고가 날짜' AFTER `w52_hgpr`;
ALTER TABLE `em_stock`.`jsise_real_time` ADD COLUMN IF NOT EXISTS `w52_lwpr` bigint(20) COMMENT '52주 최저가' AFTER `w52_hgpr_date`;
ALTER TABLE `em_stock`.`jsise_real_time` ADD COLUMN IF NOT EXISTS `w52_lwpr_date` bigint(20) COMMENT '52주 최저가 날짜' AFTER `w52_lwpr`;
ALTER TABLE `em_stock`.`jsise_real_time` DROP COLUMN IF EXISTS `fiscal_year_end`;

-- 2024-12-06 hhs
ALTER TABLE `em_stock`.`member_login_history`
    CHANGE COLUMN IF EXISTS `platform` `platform` VARCHAR(255) NULL DEFAULT NULL COMMENT '접속 환경' COLLATE 'utf8mb4_general_ci' AFTER `browser`,
    CHANGE COLUMN IF EXISTS `browser` `browser` VARCHAR(255) NULL DEFAULT NULL COMMENT '접속 브라우저' COLLATE 'utf8mb4_general_ci' AFTER `member_idx`;
-- 2024-12-11 hhs
ALTER TABLE `member`
    ADD COLUMN IF NOT EXISTS `profile_image_path` VARCHAR(255) NULL DEFAULT NULL COMMENT '프로필이미지 path' AFTER `profile_image_url`;

-- 2024-12-10 theme_code 컬럼 추가
ALTER TABLE `em_stock`.`theme_code` ADD COLUMN IF NOT EXISTS `group_code` VARCHAR(4) NULL DEFAULT NULL COMMENT '테마 그룹 코드(하나증권 테마 그룹 코드)' AFTER `idx`;
-- 2024-12-18 theme_code에 컬럼 추가
ALTER TABLE `em_stock`.`theme_code` ADD COLUMN IF NOT EXISTS `expected_prdy_ctrt` DOUBLE NULL DEFAULT NULL COMMENT '평균예상등락률' AFTER `prdy_ctrt`;
ALTER TABLE `em_stock`.`theme_code` ADD COLUMN IF NOT EXISTS `average_price_ratio` DOUBLE NULL DEFAULT NULL COMMENT '평균가대비율' AFTER `expected_prdy_ctrt`;
