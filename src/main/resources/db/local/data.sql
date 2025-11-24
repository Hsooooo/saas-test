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
SET @bd     = DAY(@today);
SET @start     = DATE_SUB(@today, INTERVAL 15 DAY);   -- 현재 주기 시작(포함)
SET @endExcl   = DATE_ADD(@start,  INTERVAL 31 DAY);  -- 현재 주기 종료(배타)
SET @endEx  = @today;
SET @prevStart = DATE_SUB(@start,  INTERVAL 31 DAY);  -- 이전 주기 시작
SET @days   = DATEDIFF(@endEx, @start);
-- 고정 분모(재현성을 위해 31일로 가정)
SET @DAYS = 31;
-- ===== 현재 구독 상태: ADVANCED(2) =====
-- current_seat_count: 현재 시점 예상 좌석(예: 3)
INSERT INTO license_partnership (
    idx, partnership_idx, license_idx, billing_day,
    period_start_date, period_end_date, next_billing_date,
    current_seat_count, current_unit_price, current_min_user_count,
    cancel_at_period_end, state_cd, update_date, create_date
) VALUES
    (1, 1, 3, DAY(@start),
     @start, @endExcl, @endExcl,
     3, (SELECT price_per_user FROM license WHERE idx=2),
     (SELECT COALESCE(min_user_count,0) FROM license WHERE idx=2),
     0, 'LPS0002', NOW(), NOW());

INSERT INTO invoice (
    partnership_idx, license_partnership_idx, license_idx,
    period_start, period_end, issue_date, due_date,
    subtotal, tax, total, type_cd, status_cd, unit_cd,
    create_date
) VALUES
    (1, 1, 3,@start, @endExcl, @start, NULL,
     0, 0, 0, 'IIT0001', 'ICS0003', 'KRW', NOW());

-- ===== 이전 주기 인보이스 (선불 스냅샷) =====
-- 지난달 선불 좌석 수 = 3석 (RECURRING)
-- INSERT INTO invoice (
--     partnership_idx, license_partnership_idx,
--     period_start, period_end, issue_date, due_date,
--     subtotal, tax, total, status_cd, unit_cd,
--     create_date
-- ) VALUES
--     (1, 1, @prevStart, @start, @start, NULL,
--      0, 0, 0, 'ICS0002', 'KRW', NOW());

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

-- INSERT INTO em_saas.license_partnership (idx, partnership_idx, license_idx, billing_day, period_start_date, period_end_date, next_billing_date, current_seat_count, current_unit_price, current_min_user_count, cancel_at_period_end, state_cd, update_date, create_date) VALUES (2, 1, 3, 28, '2025-10-28', '2025-11-28', '2025-11-28', 8, 10000.00, 3, 0, 'LPS0002', '2025-11-12 00:30:28', '2025-11-12 00:18:55');
-- INSERT INTO em_saas.invoice (idx, partnership_idx, license_partnership_idx, period_start, period_end, issue_date, due_date, subtotal, tax, total, status_cd, type_cd, unit_cd, license_idx, charge_user_count, receipt_url, order_number, meta, create_date, update_date) VALUES (2, 1, 2, '2025-11-12', '2025-11-28', '2025-11-12 00:30:26', null, 0.00, 0.00, 66453.00, 'ISC0003', 'IIT0002', 'MUC0001', 3, 8, 'https://dashboard-sandbox.tosspayments.com/receipt/redirection?transactionId=tbill20251112093027TXxV9&ref=PX', 'SAAS-OD-20251112-B8976B37488B', null, '2025-11-12 00:30:26', '2025-11-12 00:30:28');
SET @start  = DATE_SUB(@today, INTERVAL 1 MONTH);   -- 현재 주기 시작(포함)
-- 파트너십 3개(앵커 동일 = 오늘 날짜의 일자)
INSERT INTO partnership (idx, name, domain, update_date, create_date) VALUES
(30,'CANCEL-CASE','cancel.anchor',NOW(),NOW()),
(31,'CHANGE-DOWN-CASE','change.down.anchor',NOW(),NOW()),
(32,'RECUR-CASE','recurring.anchor',NOW(),NOW());

INSERT INTO em_saas.partnership_payment_method (idx, partnership_idx, method_type_cd, brand, last4, exp_year, exp_month, customer_key, auth_key, holder_name, state_cd, is_default, delete_date, update_date, create_date)
VALUES (5, 30, 'PMC0001', '신한', '', null, null, 'SAAS-CK-20251111-A6D289F9FD4B', 'bln_xya1xQwwARM', null, 'PSC0001', 1, null, '2025-11-11 07:27:39', '2025-11-11 07:27:39');
INSERT INTO em_saas.payment_mandate (idx, partnership_idx, payment_method_idx, provider_cd, mandate_id, status_cd, agree_date, revoke_date, meta, update_date, create_date)
VALUES (5, 30, 5, 'PGC0001', 'zZOQHTcgq8tOJrZGXrHgPoV4Iuf-i2O0APsFWJTSeYA=', 'MDS0001', '2025-11-11 07:27:46', null, null, '2025-11-11 07:27:46', '2025-11-11 07:27:46');
INSERT INTO em_saas.partnership_payment_method (idx, partnership_idx, method_type_cd, brand, last4, exp_year, exp_month, customer_key, auth_key, holder_name, state_cd, is_default, delete_date, update_date, create_date)
VALUES (6, 31, 'PMC0001', '신한', '', null, null, 'SAAS-CK-20251111-A6D289F9FD4B', 'bln_xya1xQwwARM', null, 'PSC0001', 1, null, '2025-11-11 07:27:39', '2025-11-11 07:27:39');
INSERT INTO em_saas.payment_mandate (idx, partnership_idx, payment_method_idx, provider_cd, mandate_id, status_cd, agree_date, revoke_date, meta, update_date, create_date)
VALUES (6, 31, 6, 'PGC0001', 'zZOQHTcgq8tOJrZGXrHgPoV4Iuf-i2O0APsFWJTSeYA=', 'MDS0001', '2025-11-11 07:27:46', null, null, '2025-11-11 07:27:46', '2025-11-11 07:27:46');
INSERT INTO em_saas.partnership_payment_method (idx, partnership_idx, method_type_cd, brand, last4, exp_year, exp_month, customer_key, auth_key, holder_name, state_cd, is_default, delete_date, update_date, create_date)
VALUES (7, 32, 'PMC0001', '신한', '', null, null, 'SAAS-CK-20251111-A6D289F9FD4B', 'bln_xya1xQwwARM', null, 'PSC0001', 1, null, '2025-11-11 07:27:39', '2025-11-11 07:27:39');
INSERT INTO em_saas.payment_mandate (idx, partnership_idx, payment_method_idx, provider_cd, mandate_id, status_cd, agree_date, revoke_date, meta, update_date, create_date)
VALUES (7, 32, 7, 'PGC0001', 'zZOQHTcgq8tOJrZGXrHgPoV4Iuf-i2O0APsFWJTSeYA=', 'MDS0001', '2025-11-11 07:27:46', null, null, '2025-11-11 07:27:46', '2025-11-11 07:27:46');

/* =========================================================
 * A) 해지 예약: 다음 결제일(=오늘) 해지. 신규 인보이스 생성 금지, 좌석변동(A)만 정산
 * =======================================================*/
INSERT INTO license_partnership (
  idx, partnership_idx, license_idx, billing_day,
  period_start_date, period_end_date, next_billing_date,
  current_seat_count, current_unit_price, current_min_user_count,
  cancel_at_period_end, state_cd,
  update_date, create_date
) VALUES (
  301, 30, 2, @bd,
  @start, @endEx, @endEx,
  4,
  (SELECT price_per_user FROM license WHERE idx=2),
  (SELECT COALESCE(min_user_count,0) FROM license WHERE idx=2),
  1, 'LPS0004',
  NOW(), NOW()
);

-- 현재 주기 선불 스냅샷(ADVANCED, 3석 가정)
INSERT INTO invoice (
  partnership_idx, license_partnership_idx, license_idx,
  period_start, period_end, issue_date, due_date,
  subtotal, tax, total, type_cd, status_cd, unit_cd, create_date
) VALUES (
  30, 301, 2,
  @start, @endEx, @start, NULL,
  0,0,0,'IIT0001','ICS0003','KRW',NOW()
);
SET @invA := LAST_INSERT_ID();

INSERT INTO invoice_item (invoice_idx,item_type_cd,description,quantity,unit_price,days,amount,related_event_idx,meta,create_date) VALUES
(@invA,'ITC0001','정기결제 선불 스냅샷(ADVANCED)',3,
 (SELECT price_per_user FROM license WHERE idx=2), @days, 0, NULL,
 JSON_OBJECT('planIdx',2,'planCd','PLC0002','snapshot','recurring','billingDay',@bd), NOW());

-- 좌석 이벤트(현재 주기 내부)
INSERT INTO subscription_change_event (license_partnership_idx, occurred_date, type_cd, qty_delta, note, create_date) VALUES
(301, DATE_ADD(@start, INTERVAL 5  DAY), 'CET0001', +2, 'ADD +2', NOW()),
(301, DATE_ADD(@start, INTERVAL 12 DAY), 'CET0002', -1, 'REMOVE -1', NOW()),
(301, DATE_ADD(@start, INTERVAL 18 DAY), 'CET0001', +1, 'ADD +1', NOW());

/* =========================================================
 * B) 변경 예약(다운그레이드 전용 3→2): 현재 주기 PREMIUM 유지 스냅샷, 다음 주기부터 ADVANCED로 발행
 *  - lp.license_idx=2 로 "다음 주기 타깃" 표시
 *  - state=LPS0003, active_uniquer = partnership_idx
 * =======================================================*/
INSERT INTO license_partnership (
  idx, partnership_idx, license_idx, billing_day,
  period_start_date, period_end_date, next_billing_date,
  current_seat_count, current_unit_price, current_min_user_count,
  cancel_at_period_end, state_cd,
  update_date, create_date
) VALUES (
  302, 31, 3, @bd,
  @start, @endEx, @endEx,
  6,
  (SELECT price_per_user FROM license WHERE idx=2),
  (SELECT COALESCE(min_user_count,0) FROM license WHERE idx=2),
  0, 'LPS0003',
  NOW(), NOW()
);

-- 현재 주기 스냅샷은 PREMIUM(3) 기준 유지
INSERT INTO invoice (
  partnership_idx, license_partnership_idx, license_idx,
  period_start, period_end, issue_date, due_date,
  subtotal, tax, total, type_cd, status_cd, unit_cd, create_date
) VALUES (
  31, 302, 3,
  @start, @endEx, @start, NULL,
  0,0,0,'IIT0001','ICS0003','KRW',NOW()
);
SET @invB := LAST_INSERT_ID();

INSERT INTO invoice_item (invoice_idx,item_type_cd,description,quantity,unit_price,days,amount,related_event_idx,meta,create_date) VALUES
(@invB,'ITC0001','정기결제 선불 스냅샷(PREMIUM)',6,
 (SELECT price_per_user FROM license WHERE idx=3), @days, 0, NULL,
 JSON_OBJECT('planIdx',3,'planCd','PLC0003','snapshot','recurring','billingDay',@bd), NOW());

-- 좌석 이벤트
INSERT INTO subscription_change_event (license_partnership_idx, occurred_date, type_cd, qty_delta, note, create_date) VALUES
(302, DATE_ADD(@start, INTERVAL  4 DAY), 'CET0001', +1, 'ADD +1', NOW()),
(302, DATE_ADD(@start, INTERVAL 16 DAY), 'CET0002', -2, 'REMOVE -2', NOW()),
(302, DATE_ADD(@start, INTERVAL 22 DAY), 'CET0001', +2, 'ADD +2', NOW());
INSERT INTO subscription_change_event (license_partnership_idx, occurred_date, type_cd, from_license_idx, to_license_idx, create_date) VALUES
(302, DATE_ADD(@start, INTERVAL 23 DAY), 'CET0004', 3, 2, NOW());

/* =========================================================
 * C) 정기 결제(활성): A 정산 후 동일 플랜으로 신규 인보이스 생성
 * =======================================================*/
INSERT INTO license_partnership (
  idx, partnership_idx, license_idx, billing_day,
  period_start_date, period_end_date, next_billing_date,
  current_seat_count, current_unit_price, current_min_user_count,
  cancel_at_period_end, state_cd,
  update_date, create_date
) VALUES (
  303, 32, 2, @bd,
  @start, @endEx, @endEx,
  7,
  (SELECT price_per_user FROM license WHERE idx=2),
  (SELECT COALESCE(min_user_count,0) FROM license WHERE idx=2),
  0, 'LPS0002',
  NOW(), NOW()
);

INSERT INTO invoice (
  partnership_idx, license_partnership_idx, license_idx,
  period_start, period_end, issue_date, due_date,
  subtotal, tax, total, type_cd, status_cd, unit_cd, create_date
) VALUES (
  32, 303, 2,
  @start, @endEx, @start, NULL,
  0,0,0,'IIT0001','ICS0003','KRW',NOW()
);
SET @invC := LAST_INSERT_ID();

INSERT INTO invoice_item (invoice_idx,item_type_cd,description,quantity,unit_price,days,amount,related_event_idx,meta,create_date) VALUES
(@invC,'ITC0001','정기결제 선불 스냅샷(ADVANCED)',6,
 (SELECT price_per_user FROM license WHERE idx=2), @days, 0, NULL,
 JSON_OBJECT('planIdx',2,'planCd','PLC0002','snapshot','recurring','billingDay',@bd), NOW());

-- 좌석 이벤트
INSERT INTO subscription_change_event (license_partnership_idx, occurred_date, type_cd, qty_delta, note, create_date) VALUES
(303, DATE_ADD(@start, INTERVAL  8 DAY), 'CET0001', +1, 'ADD +1', NOW()),
(303, DATE_ADD(@start, INTERVAL 19 DAY), 'CET0002', -1, 'REMOVE -1', NOW()),
(303, DATE_ADD(@start, INTERVAL 24 DAY), 'CET0001', +1, 'ADD +1', NOW());


-- 지식정원
INSERT INTO em_saas.knowledge_garden_node (idx, partnership_member_idx, label, type_cd, parent_node_idx, sort_order, depth, current_version_idx, state_cd, update_date, create_date) VALUES (1, 1, '주식', 'KNT0002', null, 1, 0, null, 'KNS0001', '2025-11-20 07:04:27', '2025-11-20 07:04:27');
INSERT INTO em_saas.knowledge_garden_node (idx, partnership_member_idx, label, type_cd, parent_node_idx, sort_order, depth, current_version_idx, state_cd, update_date, create_date) VALUES (2, 1, 'K-방산 저평가 수주 증가 종목 분석', 'KNT0001', 1, 1, 1, 2, 'KNS0001', '2025-11-20 07:05:38', '2025-11-20 07:05:38');
INSERT INTO em_saas.knowledge_garden_node_version (idx, node_idx, version_no, title, content, create_date) VALUES (1, 1, 1, '주식', '', '2025-11-20 07:04:27');
INSERT INTO em_saas.knowledge_garden_node_version (idx, node_idx, version_no, title, content, create_date) VALUES (2, 2, 1, 'K-방산 저평가 수주 증가 종목 분석', '# K-방산 저평가 종목 분석: 수주잔고 증가 및 PER 15 이하 기업

K-방산 관련주 중 최근 수주잔고가 증가하고 있으며 PER(주가수익비율)이 15 이하인 저평가 종목을 분석한 결과, **한화에어로스페이스(012450)**, **한화시스템(272210)**, 그리고 **휴니드(005870)**가 해당 기준에 부합하는 것으로 나타났습니다. 이들 기업은 K-방산 산업의 성장세 속에서 견조한 실적과 함께 투자 매력을 보유하고 있습니다.

## 1. K-방산 시장 동향 및 성장 배경

K-방산 산업은 지정학적 리스크 증가와 각국의 국방비 증액 기조에 힘입어 글로벌 시장에서 빠르게 성장하고 있습니다. 특히, 한국의 방산 기술력은 가격 경쟁력과 신속한 납기, 맞춤형 솔루션 제공 능력으로 높은 평가를 받고 있습니다. 폴란드, 중동 등 주요 국가들과의 대규모 계약 체결은 K-방산의 위상을 한층 높이는 계기가 되었으며, 이는 관련 기업들의 수주잔고 증가로 이어지고 있습니다.

- **글로벌 국방비 증가**: 러시아-우크라이나 전쟁 장기화, 중동 지역 불안정 등 전 세계적인 안보 위협 고조로 각국 정부의 국방 예산이 확대되고 있습니다.
- **기술 경쟁력 확보**: 한국 방산 기업들은 첨단 기술 개발과 국산화를 통해 독자적인 경쟁력을 확보하고 있으며, 특히 유도무기, 항공우주, 지상무기 분야에서 두각을 나타내고 있습니다.
- **수출 다변화**: 과거 특정 지역에 집중되었던 수출 시장이 유럽, 중동, 아시아 등으로 다변화되면서 안정적인 성장 기반을 마련하고 있습니다.

## 2. 저평가 K-방산 종목 분석

### 2.1. 한화에어로스페이스 (012450)

**수혜 메커니즘:**
한화에어로스페이스는 K9 자주포, 천무 등 지상 방산 시스템과 항공기 엔진 분야에서 독보적인 위치를 차지하고 있습니다. 최근 폴란드와의 2차 이행계약 체결 및 루마니아 K9 자주포 수출 가능성 등 대규모 수주 기대감이 높아지고 있으며, 이는 수주잔고 증가로 직결될 것으로 예상됩니다. 특히, 자회사 한화오션의 잠수함 수출 추진도 긍정적인 영향을 미칠 전망입니다.

**차별화 포인트:**
- **종합 방산 솔루션 제공**: 육해공을 아우르는 종합 방산 기업으로서 시너지를 창출하고 있습니다.
- **글로벌 시장 확대**: 폴란드 외에도 유럽, 중동 등 다양한 국가로의 수출 확대를 추진하며 성장 동력을 확보하고 있습니다.
- **미래 성장 동력**: 우주항공 분야 투자 확대를 통해 장기적인 성장 잠재력을 보유하고 있습니다.

**리스크:**
- 글로벌 경기 둔화 및 지정학적 리스크 완화 시 수주 모멘텀 약화 가능성
- 원자재 가격 변동 및 환율 변화에 따른 수익성 악화 우려

### 2.2. 한화시스템 (272210)

**수혜 메커니즘:**
한화시스템은 첨단 방산전자 시스템 및 ICT 기술을 기반으로 K-방산의 핵심 역량을 담당하고 있습니다. 특히, 감시정찰, 지휘통제통신, 정밀유도무기 체계 개발에 강점을 가지고 있으며, 최근에는 위성통신 및 UAM(도심항공교통) 등 미래 기술 분야로 사업을 확장하고 있습니다. 방산 부문의 안정적인 수주와 신사업 성장이 수주잔고 증가에 기여할 것으로 보입니다.

**차별화 포인트:**
- **첨단 방산전자 기술력**: 레이더, 전자광학 등 핵심 방산전자 기술을 내재화하여 경쟁 우위를 확보하고 있습니다.
- **미래 기술 선도**: 위성통신, UAM 등 미래 모빌리티 및 국방 기술 개발을 통해 신성장 동력을 창출하고 있습니다.
- **한화 그룹 시너지**: 한화 그룹 내 방산 계열사들과의 협력을 통해 사업 경쟁력을 강화하고 있습니다.

**리스크:**
- 신사업 투자에 따른 초기 비용 부담 및 사업화 지연 가능성
- 정부 국방 예산 변동에 따른 실적 영향

### 2.3. 휴니드 (005870)

**수혜 메커니즘:**
휴니드는 전술통신 시스템 및 항공전자 장비 분야에서 전문성을 가진 기업입니다. 최근 K-방산의 해외 수출 확대에 따라 전술통신 장비 수요가 증가하고 있으며, 이는 휴니드의 수주잔고 증가에 긍정적인 영향을 미치고 있습니다. 특히, 미국 시장 진출 가능성도 제기되면서 추가적인 성장 모멘텀을 기대할 수 있습니다.

**차별화 포인트:**
- **전술통신 분야 전문성**: 군 통신 시스템 분야에서 오랜 경험과 기술력을 보유하고 있습니다.
- **미국 시장 진출 기대**: K-방산의 미국 수출 시 동반 성장이 기대됩니다.
- **소형 무장 헬기 사업 참여**: 소형 무장 헬기(LAH) 사업 참여를 통해 항공전자 분야에서의 입지를 강화하고 있습니다.

**리스크:**
- 대형 방산 기업 대비 낮은 사업 규모로 인한 변동성
- 해외 수출 계약 성사 여부에 따른 실적 민감도

## 3. 투자 전략 및 시사점

위에서 분석된 K-방산 저평가 종목들은 현재 PER 15 이하로 평가되면서도 수주잔고 증가를 통해 향후 실적 개선이 기대되는 기업들입니다. 투자자들은 이들 기업의 개별적인 사업 구조와 성장 전략을 면밀히 검토하여 투자 결정을 내릴 필요가 있습니다.

- **장기적인 관점의 투자**: K-방산 산업은 단기적인 이슈보다는 장기적인 관점에서 글로벌 안보 환경 변화와 기술 발전에 따라 꾸준히 성장할 가능성이 높습니다.
- **분산 투자 고려**: 개별 기업의 리스크를 분산하기 위해 여러 K-방산 관련 종목에 대한 분산 투자를 고려할 수 있습니다.
- **수주 동향 지속 모니터링**: 수주잔고는 방산 기업의 미래 실적을 가늠하는 중요한 지표이므로, 관련 뉴스 및 공시를 지속적으로 모니터링하는 것이 중요합니다.

K-방산 산업은 현재 높은 성장 잠재력을 보유하고 있으며, 저평가된 우량 기업을 발굴하는 것은 매력적인 투자 기회가 될 수 있습니다.

## 4. 참고 자료

- [한화에어로스페이스, 폴란드 2차 이행계약 체결...K-방산 수출 확대 기대](https://www.hankyung.com/article/2024111900000)
- [한화시스템, 위성통신 사업 확장...미래 모빌리티 시장 선점](https://www.etoday.co.kr/news/view/2405387)
- [휴니드, K-방산 최초 전술통신 수출에 강세](https://www.etoday.co.kr/news/view/2405387)', '2025-11-20 07:05:38');
INSERT INTO em_saas.knowledge_garden_link (idx, start_node_idx, end_node_idx, type_cd, weight, create_date) VALUES (1, 1, 2, 'KLT0001', null, '2025-11-20 07:05:38');