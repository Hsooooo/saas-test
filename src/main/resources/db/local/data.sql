INSERT INTO `partnership` (`idx`, `name`, `domain`, `image_url`, `image_path`, `comment`, `update_date`, `create_date`) VALUES (1, '1', '1', NULL, NULL, NULL, NULL, '2025-04-21 18:45:53');
INSERT INTO `partnership` (`idx`, `name`, `domain`, `image_url`, `image_path`, `comment`, `update_date`, `create_date`) VALUES (2, '2', '2', NULL, NULL, NULL, NULL, '2025-04-21 18:45:53');
INSERT INTO `partnership` (`idx`, `name`, `domain`, `image_url`, `image_path`, `comment`, `update_date`, `create_date`) VALUES (3, '3', '3', NULL, NULL, NULL, NULL, '2025-04-21 18:45:53');
INSERT INTO `project_category` (`idx`, `partnership_idx`, `name`, `sort`, `update_date`, `create_date`) VALUES (1, 1, '미분류', 1, now(), now());
INSERT INTO `project_category` (`idx`, `partnership_idx`, `name`, `sort`, `update_date`, `create_date`) VALUES (2, 1, '피카츄', 2, now(), now());
INSERT INTO `project_category` (`idx`, `partnership_idx`, `name`, `sort`, `update_date`, `create_date`) VALUES (3, 1, '라이츄', 3, now(), now());
INSERT INTO `project_category` (`idx`, `partnership_idx`, `name`, `sort`, `update_date`, `create_date`) VALUES (4, 1, '파이리', 4, now(), now());
# INSERT INTO `project` (`idx`, `partnership_idx`, `project_category_idx`, `title`, `description`, `status_cd`, `image_url`, `image_path`, `node_cnt`, `edge_cnt`, `update_date`, `create_date`) VALUES (1, 1, 1, 'test', 'test', NULL, NULL, NULL, 0, 0, '2025-04-23 06:36:16', '2025-04-23 06:36:16');
INSERT INTO `member` (idx, email, password, name, profile_image_url, type_cd, state_cd, comment, last_login_date, leave_date, update_date, create_date) VALUES (1, 'test@test.com', '$2a$10$3yOGt4XE/3VNv3TxgGDM1.H76.NhggSfg2XuN7GgdhZxQDrlPnNIi', '홍길동', null, 'MTP0001', 'MST0002', null, null, null, '2025-04-25 11:07:09', '2025-04-25 11:07:09');
INSERT INTO `member` (idx, email, password, name, profile_image_url, type_cd, state_cd, comment, last_login_date, leave_date, update_date, create_date) VALUES (2, 'test2@test.com', '$2a$10$3yOGt4XE/3VNv3TxgGDM1.H76.NhggSfg2XuN7GgdhZxQDrlPnNIi', '마스터원', null, 'MTP0001', 'MST0002', null, null, null, '2025-04-25 11:07:09', '2025-04-25 11:07:09');
INSERT INTO `member` (idx, email, password, name, profile_image_url, type_cd, state_cd, comment, last_login_date, leave_date, update_date, create_date) VALUES (3, 'test3@test.com', '$2a$10$3yOGt4XE/3VNv3TxgGDM1.H76.NhggSfg2XuN7GgdhZxQDrlPnNIi', '홍길동2', null, 'MTP0001', 'MST0002', null, null, null, '2025-04-25 11:07:09', '2025-04-25 11:07:09');
INSERT INTO `member` (idx, email, password, name, profile_image_url, type_cd, state_cd, comment, last_login_date, leave_date, update_date, create_date) VALUES (4, 'test4@test.com', '$2a$10$3yOGt4XE/3VNv3TxgGDM1.H76.NhggSfg2XuN7GgdhZxQDrlPnNIi', '가자미', null, 'MTP0001', 'MST0002', null, null, null, '2025-04-25 11:07:09', '2025-04-25 11:07:09');
INSERT INTO `member` (idx, email, password, name, profile_image_url, type_cd, state_cd, comment, last_login_date, leave_date, update_date, create_date) VALUES (5, 'test5@test.com', '$2a$10$3yOGt4XE/3VNv3TxgGDM1.H76.NhggSfg2XuN7GgdhZxQDrlPnNIi', '동태', null, 'MTP0001', 'MST0002', null, null, null, '2025-04-25 11:07:09', '2025-04-25 11:07:09');
INSERT INTO `member` (idx, email, password, name, profile_image_url, type_cd, state_cd, comment, last_login_date, leave_date, update_date, create_date) VALUES (6, 'test6@test.com', '$2a$10$3yOGt4XE/3VNv3TxgGDM1.H76.NhggSfg2XuN7GgdhZxQDrlPnNIi', '오징어', null, 'MTP0001', 'MST0002', null, null, null, '2025-04-25 11:07:09', '2025-04-25 11:07:09');
INSERT INTO `member` (idx, email, password, name, profile_image_url, type_cd, state_cd, comment, last_login_date, leave_date, update_date, create_date) VALUES (7, 'test7@test.com', '$2a$10$3yOGt4XE/3VNv3TxgGDM1.H76.NhggSfg2XuN7GgdhZxQDrlPnNIi', '청어', null, 'MTP0001', 'MST0002', null, null, null, '2025-04-25 11:07:09', '2025-04-25 11:07:09');
INSERT INTO `member` (idx, email, password, name, profile_image_url, type_cd, state_cd, comment, last_login_date, leave_date, update_date, create_date) VALUES (8, 'test8@test.com', '$2a$10$3yOGt4XE/3VNv3TxgGDM1.H76.NhggSfg2XuN7GgdhZxQDrlPnNIi', '갈치', null, 'MTP0001', 'MST0002', null, null, null, '2025-04-25 11:07:09', '2025-04-25 11:07:09');
INSERT INTO `member` (idx, email, password, name, profile_image_url, type_cd, state_cd, comment, last_login_date, leave_date, update_date, create_date) VALUES (9, 'anoter@anoter.com', '$2a$10$3yOGt4XE/3VNv3TxgGDM1.H76.NhggSfg2XuN7GgdhZxQDrlPnNIi', '갈치', null, 'MTP0001', 'MST0002', null, null, null, '2025-04-25 11:07:09', '2025-04-25 11:07:09');
INSERT INTO `partnership_member` (idx, manager_cd, state_cd, partnership_idx, member_idx) VALUES(1, 'PST0001', 'PMS0001', 1, 1);
INSERT INTO `partnership_member` (idx, manager_cd, state_cd, partnership_idx, member_idx) VALUES(2, 'PST0001', 'PMS0001', 1, 2);
INSERT INTO `partnership_member` (idx, manager_cd, state_cd, partnership_idx, member_idx) VALUES(3, 'PST0002', 'PMS0001', 1, 3);
INSERT INTO `partnership_member` (idx, manager_cd, state_cd, partnership_idx, member_idx) VALUES(4, 'PST0002', 'PMS0001', 1, 4);
INSERT INTO `partnership_member` (idx, manager_cd, state_cd, partnership_idx, member_idx) VALUES(5, 'PST0002', 'PMS0001', 1, 5);
INSERT INTO `partnership_member` (idx, manager_cd, state_cd, partnership_idx, member_idx) VALUES(6, 'PST0002', 'PMS0001', 1, 6);
INSERT INTO `partnership_member` (idx, manager_cd, state_cd, partnership_idx, member_idx) VALUES(7, 'PST0002', 'PMS0001', 1, 7);
INSERT INTO `partnership_member` (idx, manager_cd, state_cd, partnership_idx, member_idx) VALUES(8, 'PST0002', 'PMS0001', 1, 8);
INSERT INTO `partnership_member` (idx, manager_cd, state_cd, partnership_idx, member_idx) VALUES(9, 'PST0001', 'PMS0001', 2, 9);
INSERT INTO `partnership_member` (idx, manager_cd, state_cd, partnership_idx, member_idx) VALUES(10, 'PST0001', 'PMS0001', 2, 1);
INSERT INTO `partnership_member` (idx, manager_cd, state_cd, partnership_idx, member_idx) VALUES(11, 'PST0001', 'PMS0001', 3, 1);


INSERT INTO `partnership_invited_member` (idx, email, partnership_idx, invited_by_partnership_member_idx, member_idx, partnership_member_idx, invited_date, joined_date) VALUES(1, 'test3@test.com', 1, 1, 3, 3, now(), now());
INSERT INTO `partnership_invited_member` (idx, email, partnership_idx, invited_by_partnership_member_idx, member_idx, partnership_member_idx, invited_date, joined_date) VALUES(2, 'test3@test.com', 1, 2, 4, 4, now(), now());

INSERT INTO `project` (`idx`, `project_category_idx`, `partnership_idx`, `title`, `description`, `status_cd`, `image_url`, `image_path`, `node_cnt`, `edge_cnt`, `update_date`, `create_date`, `delete_date`) VALUES (1, 1, 1, '프로젝트 제목2', '프로젝트 설명2', 'PJS0001', NULL, NULL, NULL, NULL, '2025-05-21 12:14:44', '2025-05-21 12:14:44', NULL);
INSERT INTO `project` (`idx`, `project_category_idx`, `partnership_idx`, `title`, `description`, `status_cd`, `image_url`, `image_path`, `node_cnt`, `edge_cnt`, `update_date`, `create_date`, `delete_date`) VALUES (2, 1, 1, '프로젝트 제목2', '프로젝트 설명2', 'PJS0001', NULL, NULL, NULL, NULL, '2025-05-21 12:14:45', '2025-05-21 12:14:45', NULL);
INSERT INTO `project` (`idx`, `project_category_idx`, `partnership_idx`, `title`, `description`, `status_cd`, `image_url`, `image_path`, `node_cnt`, `edge_cnt`, `update_date`, `create_date`, `delete_date`) VALUES (3, 2, 1, '프로젝트 제목2', '프로젝트 설명2', 'PJS0001', NULL, NULL, NULL, NULL, '2025-05-21 12:14:49', '2025-05-21 12:14:49', NULL);
INSERT INTO `project` (`idx`, `project_category_idx`, `partnership_idx`, `title`, `description`, `status_cd`, `image_url`, `image_path`, `node_cnt`, `edge_cnt`, `update_date`, `create_date`, `delete_date`) VALUES (4, 2, 1, '프로젝트 제목2', '프로젝트 설명2', 'PJS0001', NULL, NULL, NULL, NULL, '2025-05-21 12:14:49', '2025-05-21 12:14:49', NULL);
INSERT INTO `project` (`idx`, `project_category_idx`, `partnership_idx`, `title`, `description`, `status_cd`, `image_url`, `image_path`, `node_cnt`, `edge_cnt`, `update_date`, `create_date`, `delete_date`) VALUES (5, NULL, 1, '프로젝트 제목2', '프로젝트 설명2', 'PJS0001', NULL, NULL, NULL, NULL, '2025-05-21 12:14:54', '2025-05-21 12:14:54', NULL);
INSERT INTO `project` (`idx`, `project_category_idx`, `partnership_idx`, `title`, `description`, `status_cd`, `image_url`, `image_path`, `node_cnt`, `edge_cnt`, `update_date`, `create_date`, `delete_date`) VALUES (6, NULL, 1, '프로젝트 제목2', '프로젝트 설명2', 'PJS0001', NULL, NULL, NULL, NULL, '2025-05-21 12:14:54', '2025-05-21 12:14:54', NULL);
INSERT INTO `project_member` (`idx`, `project_idx`, `partnership_member_idx`, `type_cd`, `disable_functions`, `update_date`, `create_date`) VALUES (1, 1, 1, 'PMT0001', NULL, '2025-05-21 12:14:44', '2025-05-21 12:14:44');
INSERT INTO `project_member` (`idx`, `project_idx`, `partnership_member_idx`, `type_cd`, `disable_functions`, `update_date`, `create_date`) VALUES (2, 2, 1, 'PMT0001', NULL, '2025-05-21 12:14:45', '2025-05-21 12:14:45');
INSERT INTO `project_member` (`idx`, `project_idx`, `partnership_member_idx`, `type_cd`, `disable_functions`, `update_date`, `create_date`) VALUES (3, 3, 1, 'PMT0001', NULL, '2025-05-21 12:14:49', '2025-05-21 12:14:49');
INSERT INTO `project_member` (`idx`, `project_idx`, `partnership_member_idx`, `type_cd`, `disable_functions`, `update_date`, `create_date`) VALUES (4, 4, 1, 'PMT0001', NULL, '2025-05-21 12:14:49', '2025-05-21 12:14:49');
INSERT INTO `project_member` (`idx`, `project_idx`, `partnership_member_idx`, `type_cd`, `disable_functions`, `update_date`, `create_date`) VALUES (5, 5, 1, 'PMT0001', NULL, '2025-05-21 12:14:54', '2025-05-21 12:14:54');
INSERT INTO `project_member` (`idx`, `project_idx`, `partnership_member_idx`, `type_cd`, `disable_functions`, `update_date`, `create_date`) VALUES (6, 6, 1, 'PMT0001', NULL, '2025-05-21 12:14:54', '2025-05-21 12:14:54');
INSERT INTO `project_member` (`idx`, `project_idx`, `partnership_member_idx`, `type_cd`, `disable_functions`, `update_date`, `create_date`) VALUES (7, 2, 2, 'PMT0001', NULL, '2025-05-21 12:14:45', '2025-05-21 12:14:45');
INSERT INTO `project_member` (`idx`, `project_idx`, `partnership_member_idx`, `type_cd`, `disable_functions`, `update_date`, `create_date`) VALUES (8, 4, 2, 'PMT0001', NULL, '2025-05-21 12:14:49', '2025-05-21 12:14:49');
INSERT INTO `project_member` (`idx`, `project_idx`, `partnership_member_idx`, `type_cd`, `disable_functions`, `update_date`, `create_date`) VALUES (9, 6, 2, 'PMT0001', NULL, '2025-05-21 12:14:54', '2025-05-21 12:14:54');

INSERT INTO `member_term` (`idx`, `subject`, `content`, `active`, `required`, `update_date`, `create_date`) VALUES (1, '첫번째약관(필수)', '내용', true, true, now(), now());
INSERT INTO `member_term` (`idx`, `subject`, `content`, `active`, `required`, `update_date`, `create_date`) VALUES (2, '두번째약관(선택)', '내용', true, false, now(), now());

INSERT INTO em_saas.project_table (idx, project_idx, title, data_count, type_cd, update_date, create_date) VALUES (1, 1, 'Company', 5000, 'PTT0001', '2025-07-23 07:13:21', '2025-07-23 07:13:21');
INSERT INTO em_saas.project_table (idx, project_idx, title, data_count, type_cd, update_date, create_date) VALUES (2, 1, 'Company-Company', 826, 'PTT0002', '2025-07-23 07:13:23', '2025-07-23 07:13:23');

INSERT INTO `license` (`idx`, `plan_cd`, `name`, `description`, `price_per_user`, `min_user_count`, `data_total_limit`, `project_count_limit`, `period_month`, `active`, `update_date`, `create_date`)
VALUES
    (1, 'PLC0001', 'Basic Plan', 'basic license', 0, 1,  1000, 1, 0, 1, NOW(), NOW()),
    (2, 'PLC0002', 'Advanced Plan', 'advanced license', 10000, 3,  200000, 50, 1, 1, NOW(), NOW()),
    (3, 'PLC0003', 'Premium Plan', 'premium license', 20000, 5,  500000, 100, 1, 1, NOW(), NOW());

-- -- 오늘 기준으로 청구일 세팅
-- -- SET @today = CURDATE();
-- SET @today = DATE_ADD(CURDATE(), INTERVAL +15 DAY);
-- SET @start = DATE_SUB(@today, INTERVAL 1 MONTH);
--
-- INSERT INTO license_partnership (
--     idx, partnership_idx, license_idx, billing_day,
--     period_start_date, period_end_date, next_billing_date,
--     current_seat_count, current_unit_price, current_min_user_count,
--     cancel_at_period_end, state_cd, update_date, create_date
-- ) VALUES (
--              1, 1, 2, DAY(@today),
--              @start, @today, @today,
--              3, 100000, 3,
--              0, 'LPS0002', NOW(), NOW()
-- );
--
-- -- 이벤트 여러 개 (전 달부터 오늘 사이)
-- INSERT INTO subscription_change_event (license_partnership_idx, occurred_date, type_cd, qty_delta, note, create_date)
-- VALUES
--     (1, DATE_ADD(@start, INTERVAL 3 DAY), 'CET0001', 1, '좌석 추가 +1', NOW()),
--     (1, DATE_ADD(@start, INTERVAL 10 DAY), 'CET0002', -1, '좌석 감소 -1', NOW()),
--     (1, DATE_ADD(@start, INTERVAL 15 DAY), 'CET0001', 1, '좌석 추가 +1', NOW()),
SET @today     = CURDATE();                      -- 업그레이드(NOW) 평가 시점
SET @start     = DATE_SUB(@today, INTERVAL 15 DAY);   -- 현재 주기 시작(포함)
SET @endExcl   = DATE_ADD(@start,  INTERVAL 31 DAY);  -- 현재 주기 종료(배타)
SET @prevStart = DATE_SUB(@start,  INTERVAL 31 DAY);  -- 이전 주기 시작
-- 고정 분모(재현성을 위해 31일로 가정)
SET @DAYS = 31;
-- ===== 현재 구독 상태: ADVANCED(2) =====
-- current_seat_count: 현재 시점 예상 좌석(예: 5)
INSERT INTO license_partnership (
    idx, partnership_idx, license_idx, billing_day,
    period_start_date, period_end_date, next_billing_date,
    current_seat_count, current_unit_price, current_min_user_count,
    cancel_at_period_end, state_cd, update_date, create_date
) VALUES
    (1, 1, 2, DAY(@start),
     @start, @endExcl, @endExcl,
     5, (SELECT price_per_user FROM license WHERE idx=2),
     (SELECT COALESCE(min_user_count,0) FROM license WHERE idx=2),
     0, 'LPS0002', NOW(), NOW());

-- ===== 이전 주기 인보이스 (선불 스냅샷) =====
-- 지난달 선불 좌석 수 = 3석 (RECURRING)
INSERT INTO invoice (
    partnership_idx, license_partnership_idx,
    period_start, period_end, issue_date, due_date,
    subtotal, tax, total, status_cd, unit_cd,
    create_date
) VALUES
    (1, 1, @prevStart, @start, @start, NULL,
     0, 0, 0, 'ICS0002', 'KRW', NOW());

INSERT INTO invoice_item (
    invoice_idx, item_type_cd, description,
    quantity, unit_price, days, amount,
    related_event_idx, meta, create_date
) VALUES
    (LAST_INSERT_ID(), 'ITC0001', '정기결제 선불 스냅샷(ADVANCED)', 3,
     (SELECT price_per_user FROM license WHERE idx=2), @DAYS, 0, NULL,
     JSON_OBJECT('planIdx',2,'planCd',(SELECT plan_cd FROM license WHERE idx=2),'snapshot','prev_recurring'), NOW());

-- ===== 현재 주기 좌석 이벤트 (ADD/REMOVE만, 업그레이드 이벤트 없음) =====
-- 구간: @start ~ @today 사이에만 존재하도록 구성
-- 예시 흐름(최종 today 시점 seats=5를 만들 의도):
--   day+3  : ADD +2  => 3선불 → 5
--   day+9  : REMOVE -1 => 4
--   day+12 : ADD +1  => 5
INSERT INTO subscription_change_event (license_partnership_idx, occurred_date, type_cd, qty_delta, from_license_idx, to_license_idx, note, create_date) VALUES
                                                                                                                                                            (1, DATE_ADD(@start, INTERVAL 3  DAY), 'CET0001',  2, NULL, NULL, 'ADD +2', NOW()),
                                                                                                                                                            (1, DATE_ADD(@start, INTERVAL 9  DAY), 'CET0002', -1, NULL, NULL, 'REMOVE -1', NOW()),
                                                                                                                                                            (1, DATE_ADD(@start, INTERVAL 12 DAY), 'CET0001',  4, NULL, NULL, 'ADD +4', NOW());

COMMIT;

-- ===== 검증 가이드 =====
-- calculateProrationAmount 호출 예:
-- partnershipIdx=1, action='UPGRADE', effective='NOW', licenseIdx=3
-- 기대 항목:
--  - PRORATION(좌석 추가 미청구분 / 구 플랜=ADVANCED): @start~@today 사이 세그먼트에 대해
--    * (effectiveSeats - baseSeatsPrepaid=3)+ 를 days에 곱해서 합산
--  - PRORATION(신 플랜 잔여기간 / PREMIUM): today~@endExcl 총일수
--  - CREDIT(구 플랜 잔여기간 / ADVANCED, 선불 3석): today~@endExcl 총일수


INSERT INTO em_saas.partnership_payment_method (idx, partnership_idx, method_type_cd, brand, last4, exp_year, exp_month, customer_key, auth_key, holder_name, state_cd, is_default, delete_date, update_date, create_date) VALUES (3, 2, 'PMC0001', '신한', null, null, null, 'SAAS-CK-20251030-1FDBBF6C3623', 'bln_q5Gg6m7m9OR', null, 'PSC0001', 1, null, '2025-10-30 05:51:17', '2025-10-30 05:51:17');
INSERT INTO em_saas.payment_mandate (idx, partnership_idx, payment_method_idx, provider_cd, mandate_id, status_cd, agree_date, revoke_date, meta, update_date, create_date) VALUES (3, 2, 3, 'PGC0001', 'I81alwBj7AgEaTbpBjOaYpzK2Rcju3XsFIHFVCF9FgU=', 'MDS0001', '2025-10-30 05:51:17', null, null, '2025-10-30 05:51:17', '2025-10-30 05:51:17');

INSERT INTO em_saas.partnership_payment_method (idx, partnership_idx, method_type_cd, brand, last4, exp_year, exp_month, customer_key, auth_key, holder_name, state_cd, is_default, delete_date, update_date, create_date) VALUES (4, 1, 'PMC0001', '신한', '', null, null, 'SAAS-CK-20251111-A6D289F9FD4B', 'bln_xya1xQwwARM', null, 'PSC0001', 1, null, '2025-11-11 07:27:39', '2025-11-11 07:27:39');
INSERT INTO em_saas.payment_mandate (idx, partnership_idx, payment_method_idx, provider_cd, mandate_id, status_cd, agree_date, revoke_date, meta, update_date, create_date) VALUES (4, 1, 4, 'PGC0001', 'zZOQHTcgq8tOJrZGXrHgPoV4Iuf-i2O0APsFWJTSeYA=', 'MDS0001', '2025-11-11 07:27:46', null, null, '2025-11-11 07:27:46', '2025-11-11 07:27:46');
