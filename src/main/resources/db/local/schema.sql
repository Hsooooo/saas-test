CREATE TABLE IF NOT EXISTS `em_saas`.`code`
(
    code        VARCHAR(7)   NOT NULL COMMENT '코드',
    first_code  VARCHAR(3)   NULL COMMENT '첫번째 코드',
    second_code VARCHAR(2)   NULL COMMENT '두번째 코드',
    third_code  VARCHAR(2)   NULL COMMENT '세번째 코드',
    code_value   VARCHAR(100) NULL COMMENT '코드 값',
    seq         INT(11)          NULL COMMENT '코드 순서',
    PRIMARY KEY (`code`) USING BTREE,
    INDEX `idx_code_first_code_index_first_code` (`first_code`) USING BTREE,
    INDEX `idx_code_first_code_index_second_code` (`second_code`) USING BTREE,
    INDEX `idx_code_first_code_index_third_code` (`third_code`) USING BTREE
)
    COMMENT ='코드표';

create table if not exists em_saas.member
(
    idx               int auto_increment comment '회원번호'
    primary key,
    email             varchar(255)                         not null comment '이메일주소',
    password          varchar(100)                         null comment '회원 비밀번호',
    name              varchar(50)                          null comment '회원 이름',
    profile_image_url varchar(255)                         null comment '프로필 이미지 url',
    type_cd           varchar(7)                           null comment '회원 구분(code 테이블)',
    state_cd          varchar(7)                           null comment '회원 상태(code 테이블)',
    comment           varchar(100)                         null comment '간략 소개',
    last_login_date   datetime                             null comment '마지막 로그인 일자',
    leave_date        datetime                             null comment '탈퇴일',
    update_date       datetime                             null comment '수정일',
    create_date       datetime default current_timestamp() not null comment '가입일',
    constraint member_email_uindex
    unique (email)
    )
    comment '회원 정보';

CREATE TABLE IF NOT EXISTS `em_saas`.`member_term` (
    `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '약관번호',
    `subject` VARCHAR(255) NULL DEFAULT NULL COMMENT '약관 제목',
    `content` TEXT NULL DEFAULT NULL COMMENT '약관 내용',
    `active` BIT(1) NULL DEFAULT NULL COMMENT '활성화여부(1:활성화, 0:비활성화)',
    `required` BIT(1) NULL DEFAULT NULL COMMENT '필수 여부(1:필수, 0:선택)',
    `update_date` DATETIME NULL DEFAULT NULL COMMENT '수정일',
    `create_date` DATETIME NULL DEFAULT NULL COMMENT '생성일',
    PRIMARY KEY (`idx`) USING BTREE
)
COMMENT='약관 정보'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `em_saas`.`member_term_agree` (
    `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '약관 동의 정보 번호',
    `member_idx` INT(11) NULL DEFAULT NULL COMMENT '회원번호',
    `member_term_idx` INT(11) NULL DEFAULT NULL COMMENT '약관번호',
    `agree` BIT(1) NULL DEFAULT NULL COMMENT '동의여부(1:동의, 0:미동의)',
    `update_date` DATETIME NULL DEFAULT NULL COMMENT '수정일',
    `create_date` DATETIME NULL DEFAULT NULL COMMENT '생성일',
    PRIMARY KEY (`idx`) USING BTREE,
    CONSTRAINT `fk_member_term_agree_member_idx` FOREIGN KEY (`member_idx`) REFERENCES `member` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION,
    CONSTRAINT `fk_member_term_agree_member_term_idx` FOREIGN KEY (`member_term_idx`) REFERENCES `member_term` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COMMENT='약관 동의 정보'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

create table if not exists em_saas.member_login_history
(
    idx         int auto_increment comment '로그인이력번호'
    primary key,
    member_idx  int                                  not null comment '회원번호',
    browser     varchar(255)                         null comment '접속 브라우저',
    platform    varchar(255)                         null comment '접속 환경',
    ip          varchar(15)                          null comment '접속 ip',
    create_date datetime default current_timestamp() not null comment '접속일',
    constraint member_login_history_member_idx_fk
    foreign key (member_idx) references em_saas.member (idx)
    on delete cascade
    )
    comment '로그인 이력 정보';

CREATE TABLE IF NOT EXISTS `em_saas`.`member_email_history` (
    `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '이메일전송이력번호',
    `member_idx` INT(11) NULL DEFAULT NULL COMMENT '회원번호',
    `cert_data` VARCHAR(500) NULL DEFAULT NULL COMMENT '인증키' COLLATE 'utf8mb4_general_ci',
    `used` BIT(1) NULL DEFAULT NULL COMMENT '인증여부(1:인증, 0:미인증)',
    `email_type` VARCHAR(7) NULL DEFAULT NULL COMMENT '메일구분(code)' COLLATE 'utf8mb4_general_ci',
    `expire_date` DATETIME NULL DEFAULT NULL COMMENT '만료일',
    `create_date` DATETIME NULL DEFAULT NULL COMMENT '생성일',
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `email_type` (`email_type`) USING BTREE,
    INDEX `fk_project_file_member_idx` (`member_idx`) USING BTREE,
    CONSTRAINT `fk_project_file_member_idx` FOREIGN KEY (`member_idx`) REFERENCES `member` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COMMENT='회원 이메일 전송 이력 정보'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

create table if not exists em_saas.partnership
(
    idx         int auto_increment comment '파트너쉽 번호'
    primary key,
    name        varchar(255)                         not null comment '파트너쉽명',
    domain      varchar(255)                         not null comment '파트너쉽도메인',
    image_url   varchar(255)                         null comment '파트너쉽 이미지 url',
    image_path  varchar(255)                         null comment '파트너쉽 이미지 위치',
    comment     varchar(100)                         null comment '파트너쉽 간략 소개',
    update_date datetime                             null comment '수정일',
    create_date datetime default current_timestamp() not null comment '생성일',
    constraint partnership_domain_uindex
    unique (domain)
    )
    comment '파트너쉽 정보';

CREATE TABLE IF NOT EXISTS `em_saas`.`license` (
    `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '라이센스 번호',
    `plan_cd` VARCHAR(30) NOT NULL COMMENT '플랜 코드 (BASIC, ADVANCED, PREMIUM)',
    `name` VARCHAR(50) NULL DEFAULT NULL COMMENT '라이센스 명' COLLATE 'utf8mb4_general_ci',
    `description` VARCHAR(255) NULL DEFAULT NULL COMMENT '라이센스 설명' COLLATE 'utf8mb4_general_ci',
    `price_per_user` DECIMAL(12, 2) NULL DEFAULT NULL COMMENT '사용자당 가격',
    `min_user_count` INT(11) NULL DEFAULT NULL COMMENT '최소 사용자 수',
    `data_total_limit` INT(11) NULL DEFAULT NULL COMMENT '데이터 총량 제한(ROW 수)',
    `project_count_limit` INT(11) NULL DEFAULT NULL COMMENT '프로젝트 개수 제한',
    `period_month` INT(11) NULL DEFAULT 1 COMMENT '결제 주기 개월수 (기본 1개월)',
    `version_no` INT(11) DEFAULT 1 COMMENT '요금제 버전',
    `active` TINYINT(1) NULL DEFAULT 1 COMMENT '활성화 여부(1:활성, 0:비활성)',
    `update_date` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    `create_date` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    INDEX `license_plan_cd` (`plan_cd`) USING BTREE,
    PRIMARY KEY (`idx`) USING BTREE
)
COMMENT='라이센스 정보'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `em_saas`.`license_partnership` (
  `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '구독 ID',
  `partnership_idx` INT(11) NOT NULL COMMENT '파트너십 ID',
  `license_idx` INT(11) NOT NULL COMMENT '현재 적용 라이선스(플랜)',
  `billing_day` TINYINT NOT NULL COMMENT '매월 결제 anchor(1~28 권장)',
  `period_start_date` DATE NOT NULL COMMENT '현재 청구주기 시작(포함, inclusive)',
  `period_end_date` DATE NOT NULL COMMENT '현재 청구주기 종료(배타, exclusive)',
  `next_billing_date` DATE NOT NULL COMMENT '다음 결제 예정일(= period_end_date)',
  `current_seat_count` INT(11) NOT NULL COMMENT '현재 과금 기준 좌석 수',
  `current_unit_price` DECIMAL(12,2) NOT NULL COMMENT '현재 단가 스냅샷(license.price_per_user)',
  `current_min_user_count` INT(11) NOT NULL COMMENT '현재 최소유저수 스냅샷(license.min_user_count)',
  `cancel_at_period_end` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '다음 결제일에 해지(1) 예약',
  `state_cd` VARCHAR(7) NOT NULL COMMENT '파트너쉽 라이센스 상태 코드 (LPS0000)' COLLATE 'utf8mb4_general_ci',
  `active_uniquer` INT
    GENERATED ALWAYS AS (
        IF(`state_cd` IN ('LPS0002', 'LPS0003'), `partnership_idx`, NULL)
    ) STORED COMMENT '활성 구독 유니크 강제용 컬럼',

  `update_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (`idx`) USING BTREE,
  INDEX `fk_license_partnership_partnership_idx` (`partnership_idx`) USING BTREE,
  INDEX `fk_license_partnership_license_idx` (`license_idx`) USING BTREE,
  INDEX `license_partnership_next_billing_date` (`next_billing_date`) USING BTREE,
  UNIQUE KEY `ui_license_partnership_active_uniquer` (`active_uniquer`),
  CONSTRAINT `fk_license_partnership_partnership_idx` FOREIGN KEY (`partnership_idx`) REFERENCES `partnership`(`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT `fk_license_partnership_license_idx` FOREIGN KEY (`license_idx`) REFERENCES `license`(`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COMMENT='파트너쉽 라이센스 정보'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `em_saas`.`subscription_change_event` (
  `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '구독 변경 이벤트 번호',
  `license_partnership_idx` INT(11) NOT NULL COMMENT '파트너쉽 라이센스 번호',
  `occurred_date` DATETIME NOT NULL COMMENT '이벤트 발생 시각',
  `type_cd` VARCHAR(7) NOT NULL COMMENT '구독 변경 타입 코드 (CET0000)' COLLATE 'utf8mb4_general_ci',
  `qty_delta` INT NOT NULL DEFAULT 0 COMMENT '좌석 변화량(+증가/-감소), 플랜 변경 시 0 가능',
  `from_license_idx` INT NULL COMMENT '플랜 변경 전',
  `to_license_idx` INT NULL COMMENT '플랜 변경 후',
  `note` VARCHAR(255) NULL COMMENT '비고(요청자, 사유 등)' COLLATE 'utf8mb4_general_ci',
  `meta` JSON NULL COMMENT '요청/계산 스냅샷(옵션)' COLLATE 'utf8mb4_general_ci',
  `create_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`idx`),
  INDEX `fk_subscription_change_event_from_license_idx` (`from_license_idx`) USING BTREE,
  INDEX `fk_subscription_change_event_to_license_idx` (`to_license_idx`) USING BTREE,
  CONSTRAINT `fk_subscription_change_event_license_partnership_idx` FOREIGN KEY (`license_partnership_idx`) REFERENCES `license_partnership`(`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT `fk_subscription_change_event_from_license_idx` FOREIGN KEY (`from_license_idx`) REFERENCES `license`(`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT `fk_subscription_change_event_to_license_idx` FOREIGN KEY (`to_license_idx`) REFERENCES `license`(`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COMMENT='구독 변경 이벤트 정보'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `em_saas`.`invoice` (
  `idx` INT NOT NULL AUTO_INCREMENT COMMENT '인보이스 번호',
  `partnership_idx` INT NOT NULL COMMENT '파트너쉽 번호',
  `license_partnership_idx` INT NOT NULL COMMENT '라이센스 파트너쉽 번호',
  `period_start` DATE NOT NULL COMMENT '청구기간 시작(포함, inclusive)',
  `period_end`   DATE NOT NULL COMMENT '청구기간 종료(배타, exclusive)',
  `issue_date`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '발행일',
  `due_date`     DATETIME NULL COMMENT '납기일',
  `subtotal`     DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '세전 합계',
  `tax`          DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '세액',
  `total`        DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '총액(=subtotal+tax)',
  `status_cd`    VARCHAR(7) NOT NULL COMMENT '인보이스 상태 코드 (ISC0000)' COLLATE 'utf8mb4_general_ci',
  `unit_cd`      VARCHAR(7) NOT NULL DEFAULT 'MUC0001' COMMENT '화폐단위 (MUC0000)' COLLATE 'utf8mb4_general_ci',
  `license_idx`  INT NULL COMMENT '청구 당시 플랜(라이센스) 번호',
  `charge_user_count` INT NOT NULL DEFAULT 0 COMMENT '청구 당시 과금 기준 사용자 수',
  `receipt_url`  VARCHAR(500) NULL COMMENT '결제전표' COLLATE 'utf8mb4_general_ci',
  `order_number`  VARCHAR(500) NULL COMMENT '주문번호' COLLATE 'utf8mb4_general_ci',
  `meta` JSON NULL COMMENT '스냅샷(시점의 단가/최소과금/분모기준/플랜버전 등)' COLLATE 'utf8mb4_general_ci',
  -- 운영
  `create_date`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_date`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  -- 동일 기간 중 "활성 인보이스" 중복 방지용 생성 컬럼(VOID는 중복 허용)
  `active_invoice_uniquer` INT
    GENERATED ALWAYS AS (
      IF(`status_cd` IN ('ICS0004'), NULL, `license_partnership_idx`)
    ) STORED COMMENT '활성 인보이스 유니크 강제용(VOID는 NULL)',

  PRIMARY KEY (`idx`),
  INDEX `fk_invoice_partnership_idx` (`partnership_idx`) USING BTREE,
  INDEX `fk_invoice_license_partnership_idx` (`license_partnership_idx`) USING BTREE,
  INDEX `invoice_issue_date` (`issue_date`) USING BTREE,
  INDEX `invoice_period_start_period_end` (`period_start`, `period_end`) USING BTREE,
  -- 동일 LP + 동일 기간에 활성 인보이스 1건만 허용(VOID는 예외)
  UNIQUE KEY `ui_invoice_period_active_invoice_uniquer` (`period_start`, `period_end`, `active_invoice_uniquer`),
  CONSTRAINT `fk_invoice_partnership_idx` FOREIGN KEY (`partnership_idx`) REFERENCES `partnership`(`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT `fk_invoice_license_partnership_idx` FOREIGN KEY (`license_partnership_idx`) REFERENCES `license_partnership`(`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
ENGINE=InnoDB
COLLATE='utf8mb4_general_ci'
COMMENT='청구서';

CREATE TABLE IF NOT EXISTS `em_saas`.`invoice_item` (
  `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '청구 항목 번호',
  `invoice_idx` INT(11) NOT NULL COMMENT '인보이스 번호',
  `item_type_cd` VARCHAR(7) NOT NULL COMMENT '청구항목유형 (ITC0000)' COLLATE 'utf8mb4_general_ci',
  `description`  VARCHAR(255) NULL COMMENT '항목 설명' COLLATE 'utf8mb4_general_ci',

  `quantity`   INT NOT NULL DEFAULT 1 COMMENT '수량(좌석 수 등)',
  `unit_price` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '단가 스냅샷',
  `days`       INT NULL COMMENT '일할 일수(분자, 일할계산때 사용)',
  `amount`     DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '금액(마이너스 허용: CREDIT)',

  `related_event_idx` INT(11) NULL COMMENT 'subscription_change_event 참조(월중 이벤트 기반)',
  `meta` JSON NULL COMMENT '분모/분자/단가/좌석증감/플랜버전 등 재현 스냅샷',
  `create_date`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (`idx`),
  INDEX `fk_invoice_item_invoice_idx` (`invoice_idx`) USING BTREE,
  INDEX `fk_invoice_item_related_event_idx` (`related_event_idx`) USING BTREE,
  CONSTRAINT `fk_invoice_item_invoice_idx` FOREIGN KEY (`invoice_idx`) REFERENCES `invoice`(`idx`) ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT `fk_invoice_item_related_event_idx` FOREIGN KEY (`related_event_idx`) REFERENCES `subscription_change_event`(`idx`) ON UPDATE NO ACTION ON DELETE SET NULL
)
ENGINE=InnoDB
COLLATE=utf8mb4_general_ci
COMMENT='청구 항목';

CREATE TABLE IF NOT EXISTS `em_saas`.`license_payment_history` (
  `idx` INT NOT NULL AUTO_INCREMENT COMMENT '결제 내역 번호',
  `invoice_idx` INT NOT NULL COMMENT '인보이스 번호',
  `provider_cd` VARCHAR(7) NOT NULL COMMENT 'PG사 코드 (PGP0000)' COLLATE 'utf8mb4_general_ci',
  `order_number` VARCHAR(100) NOT NULL COMMENT 'PG 거래 ID(고유)' COLLATE 'utf8mb4_general_ci',
  `amount` DECIMAL(12,2) NOT NULL COMMENT '수납 금액(양수)',
  `unit_cd` VARCHAR(7) NOT NULL DEFAULT 'MUC0001' COMMENT '화폐단위 (MUC0000)' COLLATE 'utf8mb4_general_ci',
  `meta` JSON NULL COMMENT 'PG 원천 응답/영수증 스냅샷(JSON)',
  `paid_date` DATETIME NOT NULL COMMENT '결제 완료 시각(수납 시각)',
  `create_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',

  PRIMARY KEY (`idx`),
  -- 동일 거래 중복 방지(프로바이더+거래ID)
  UNIQUE KEY `ui_license_payment_history_provider_cd_order_number` (`provider_cd`, `order_number`),

  INDEX `fk_license_payment_history_invoice_idx` (`invoice_idx`),
  INDEX `license_payment_history_paid_date` (`paid_date`),

  CONSTRAINT `fk_license_payment_history_invoice_idx` FOREIGN KEY (`invoice_idx`) REFERENCES `invoice`(`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION,

  -- 금액 유효성(0 초과)
  CONSTRAINT `ck_payment_amount_positive` CHECK (`amount` > 0)
)
ENGINE=InnoDB
COLLATE=utf8mb4_general_ci
COMMENT='결제 내역(성공 수납 기록)';

CREATE TABLE IF NOT EXISTS `em_saas`.`partnership_payment_method` (
  `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '결제수단 번호',
  `partnership_idx` INT(11) NOT NULL COMMENT '파트너십 번호',
  `method_type_cd` VARCHAR(7) NOT NULL COMMENT '결제수단 유형 (PMC0000: CARD/BANK 등)' COLLATE 'utf8mb4_general_ci',
  `brand` VARCHAR(7) NULL COMMENT '카드 브랜드 (CBC0000: VISA/MASTER 등)' COLLATE 'utf8mb4_general_ci',
  `last4` VARCHAR(4) NULL COMMENT '카드 끝 4자리' COLLATE 'utf8mb4_general_ci',
  `exp_year` VARCHAR(4) NULL COMMENT '유효년(YYYY)' COLLATE 'utf8mb4_general_ci',
  `exp_month` VARCHAR(2) NULL COMMENT '유효월(1~12)' COLLATE 'utf8mb4_general_ci',
  `customer_key` VARCHAR(200) NOT NULL COMMENT 'PG 고객 토큰/키' COLLATE 'utf8mb4_general_ci',
  `auth_key` VARCHAR(200) NOT NULL COMMENT 'PG 결제수단 토큰/키' COLLATE 'utf8mb4_general_ci',
  `holder_name` VARCHAR(100) NULL COLLATE 'utf8mb4_general_ci',

  `state_cd` VARCHAR(7) NOT NULL DEFAULT 'PMS0001' COMMENT '수단 상태(PSC0001=ACTIVE, PSC0002=INACTIVE, PSC0003=DELETED 등)' COLLATE 'utf8mb4_general_ci',
  `is_default` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '기본 결제수단 여부',

  `default_uniquer` INT
    GENERATED ALWAYS AS (IF(`is_default`=1 AND `state_cd`='PSC0001', `partnership_idx`, NULL))
    STORED COMMENT '활성 기본수단 1건 강제',

  `delete_date` DATETIME NULL COMMENT '논리 삭제일',
  `update_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (`idx`),
  INDEX `fk_partnership_payment_method_partnership_idx` (`partnership_idx`) USING BTREE,
  INDEX `partnership_payment_method_state_cd` (`state_cd`) USING BTREE,
  UNIQUE KEY `ui_payment_method_default_per_partnership` (`default_uniquer`),

  CONSTRAINT `fk_partnership_payment_method_partnership_idx` FOREIGN KEY (`partnership_idx`) REFERENCES `partnership`(`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
ENGINE=InnoDB
COLLATE='utf8mb4_general_ci'
COMMENT='파트너십 결제수단(카드/계좌/토큰)';

CREATE TABLE IF NOT EXISTS `em_saas`.`payment_mandate` (
  `idx` INT NOT NULL AUTO_INCREMENT COMMENT '정기결제정보 번호',
  `partnership_idx` INT NOT NULL COMMENT '파트너십 ID',
  `payment_method_idx` INT NOT NULL COMMENT '연결 결제수단',
  `provider_cd` VARCHAR(7) NOT NULL COMMENT 'PG사 코드' COLLATE 'utf8mb4_general_ci',

  `mandate_id` VARCHAR(100) NOT NULL COMMENT 'PG 정기결제 ID' COLLATE 'utf8mb4_general_ci',
  `status_cd` VARCHAR(7) NOT NULL COMMENT '활성/해지 상태(MDS0000)' COLLATE 'utf8mb4_general_ci',

  `agree_date` DATETIME NOT NULL COMMENT '동의(체결) 시각',
  `revoke_date` DATETIME NULL COMMENT '철회 시각',
  `meta` JSON NULL COMMENT '약관 버전, 동의 IP/UA, 증빙 URL 등 스냅샷',

  `update_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (`idx`),
  INDEX `fk_payment_mandate_partnership_idx` (`partnership_idx`) USING BTREE,
  INDEX `fk_payment_mandate_payment_method_idx` (`payment_method_idx`) USING BTREE,
  INDEX `payment_mandate_mandate_id` (`mandate_id`) USING BTREE,

  CONSTRAINT `fk_payment_mandate_partnership_idx` FOREIGN KEY (`partnership_idx`) REFERENCES `partnership`(`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT `fk_payment_mandate_payment_method_idx` FOREIGN KEY (`payment_method_idx`) REFERENCES `partnership_payment_method`(`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
ENGINE=InnoDB
COMMENT='정기결제 위임/동의';

CREATE TABLE IF NOT EXISTS `em_saas`.`payment_attempt` (
  `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '결제시도 번호',
  `invoice_idx` INT(11) NOT NULL COMMENT '인보이스 번호',
  `partnership_idx` INT(11) NOT NULL COMMENT '파트너십 번호',
  `provider_cd` VARCHAR(7) NOT NULL COMMENT 'PG사 코드' COLLATE 'utf8mb4_general_ci',
  `payment_method_idx` INT NULL COMMENT '사용한 결제수단',
  `payment_mandate_idx` INT NULL COMMENT '사용한 정기결제번호',

  `attempt_no` INT NOT NULL DEFAULT 1 COMMENT '동일 인보이스 내 시도 순번',
  `amount` DECIMAL(12,2) NOT NULL COMMENT '시도 금액',
  `unit_cd` VARCHAR(7) NOT NULL DEFAULT 'MUC0001' COMMENT '화폐단위' COLLATE 'utf8mb4_general_ci',
  `status_cd` VARCHAR(7) NOT NULL COMMENT '시도 상태(PAS0000)' COLLATE 'utf8mb4_general_ci',

  `order_number` VARCHAR(100) NULL COMMENT 'PG 거래ID' COLLATE 'utf8mb4_general_ci',
  `failure_code` VARCHAR(50) NULL COMMENT '실패 코드',
  `failure_message` VARCHAR(255) NULL COMMENT '실패 메시지' COLLATE 'utf8mb4_general_ci',

  `request_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '시도 요청 시각',
  `respond_date` DATETIME NULL COMMENT 'PG 응답 시각',
  `meta` JSON NULL COMMENT '원천 응답 전문/파라미터/3DS 결과 등',

  PRIMARY KEY (`idx`),
  INDEX `fk_payment_attempt_invoice_idx` (`invoice_idx`) USING BTREE,
  INDEX `fk_payment_attempt_partnership_idx` (`partnership_idx`) USING BTREE,
  INDEX `fk_payment_attempt_method_idx` (`payment_method_idx`) USING BTREE,
  INDEX `fk_payment_attempt_mandate_idx` (`payment_mandate_idx`) USING BTREE,

  CONSTRAINT `fk_payment_attempt_invoice_idx` FOREIGN KEY (`invoice_idx`) REFERENCES `invoice`(`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT `fk_payment_attempt_partnership_idx` FOREIGN KEY (`partnership_idx`) REFERENCES `partnership`(`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT `fk_payment_attempt_method_idx` FOREIGN KEY (`payment_method_idx`) REFERENCES `partnership_payment_method`(`idx`) ON UPDATE NO ACTION ON DELETE SET NULL,
  CONSTRAINT `fk_payment_attempt_mandate_idx` FOREIGN KEY (`payment_mandate_idx`) REFERENCES `payment_mandate`(`idx`) ON UPDATE NO ACTION ON DELETE SET NULL
)
ENGINE=InnoDB
COLLATE='utf8mb4_general_ci'
COMMENT='결제 시도/재시도 로그(성공/실패 포함)';

create table if not exists em_saas.partnership_position
(
    idx             int auto_increment comment '파트너쉽 직급 번호'
    primary key,
    partnership_idx int                                  not null comment '파트너쉽 번호',
    name            varchar(50)                          null comment '직급명',
    sort_level      int                                  null comment '정렬 레벨(1~10)',
    update_date     datetime                             null comment '수정일',
    create_date     datetime default current_timestamp() not null comment '생성일',
    constraint partnership_position_partnership_idx_fk
    foreign key (partnership_idx) references em_saas.partnership (idx)
    on delete cascade
    )
    comment '파트너십 직급 정보';

create table if not exists em_saas.partnership_team
(
    idx             int auto_increment comment '파트너쉽 팀번호'
    primary key,
    partnership_idx int                                  null comment '파트너쉽 번호',
    name            varchar(255)                         null comment '팀 이름',
    team_image_url  varchar(255)                         null comment '팀 이미지 url',
    team_image_path varchar(255)                         null comment '팀 이미지 경로',
    update_date     datetime                             null comment '수정일',
    create_date     datetime default current_timestamp() not null comment '생성일',
    constraint partnership_team_partnership_idx_fk
    foreign key (partnership_idx) references em_saas.partnership (idx)
    on delete cascade
    )
    comment '파트너쉽 팀정보';

create table if not exists em_saas.partnership_member
(
    idx                      int auto_increment comment '파트너쉽 회원번호'
    primary key,
    partnership_idx          int                                  not null comment '파트너쉽 번호',
    partnership_team_idx     int                                  null comment '파트너쉽 팀 번호',
    member_idx               int                                  not null comment '회원 번호',
    partnership_position_idx int                                  null comment '파트너쉽 직급번호',
    manager_cd               varchar(7)                           null comment '파트너쉽 관리구분(code 테이블)',
    state_cd                 varchar(7)                           null comment '파트너쉽 회원상태(code 테이블)',
    profile_image_url        varchar(255)                         null comment '프로필 이미지 url',
    profile_image_path       varchar(255)                         null comment '프로필 이미지 경로',
    phone                    varchar(255)                         null comment '사용자 휴대폰 번호',
    disable_functions        varchar(1024)                        null comment '사용제한 라이센스 기능들(code 쉼표 구분)',
    update_date              datetime                             null comment '수정일',
    create_date              datetime default current_timestamp() not null comment '생성일',
    constraint partnership_member_uindx
    unique (partnership_idx, partnership_team_idx, member_idx),
    constraint partnership_member_member_idx_fk
    foreign key (member_idx) references em_saas.member (idx)
    on delete cascade,
    constraint partnership_member_partnership_idx_fk
    foreign key (partnership_idx) references em_saas.partnership (idx)
    on delete cascade,
    constraint partnership_member_partnership_position_idx_fk
    foreign key (partnership_position_idx) references em_saas.partnership_position (idx)
    on delete cascade,
    constraint partnership_member_partnership_team_idx_fk
    foreign key (partnership_team_idx) references em_saas.partnership_team (idx)
    on delete cascade,
    INDEX `manager_cd` (`manager_cd`) USING BTREE,
    INDEX `state_cd` (`state_cd`) USING BTREE
    )
    comment '파트너쉽 회원정보';


CREATE TABLE IF NOT EXISTS `em_saas`.`partnership_invite_link` (
     `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '파트너십 초대 링크 번호',
     `partnership_idx` INT(11) NULL DEFAULT NULL COMMENT '파트너십 번호',
     `created_by_partnership_member_idx` INT(11) NULL DEFAULT NULL COMMENT '초대링크 생성자 파트너십 회원 번호',
     `invite_token_hash` VARCHAR(255) NULL DEFAULT NULL COMMENT '링크 토큰' COLLATE 'utf8mb4_general_ci',
     `used_count` INT(11) NULL DEFAULT 0 COMMENT '사용된 횟수',
     `state_cd` VARCHAR(7) NULL DEFAULT NULL COMMENT '링크 상태 코드' COLLATE 'utf8mb4_general_ci',
     `invite_info_json` JSON NULL DEFAULT NULL COMMENT '초대 정보 json' COLLATE 'utf8mb4_general_ci',
     `expire_date` DATETIME NULL DEFAULT NULL COMMENT '링크 만료일',
     `create_date` DATETIME NULL DEFAULT NULL COMMENT '생성일',
     PRIMARY KEY (`idx`) USING BTREE,
     INDEX `fk_partnership_invite_link_partnership_idx` (`partnership_idx`) USING BTREE,
     INDEX `fk_partnership_invite_link_created_by_partnership_member_idx` (`created_by_partnership_member_idx`) USING BTREE,
     UNIQUE KEY `ui_partnership_invite_link_invite_token_hash` (`invite_token_hash`) USING BTREE,
     CONSTRAINT `fk_partnership_invite_link_partnership_idx` FOREIGN KEY (`partnership_idx`) REFERENCES `partnership` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION,
     CONSTRAINT `fk_partnership_invite_link_created_by_partnership_member_idx` FOREIGN KEY (`created_by_partnership_member_idx`) REFERENCES `partnership_member` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COMMENT='파트너쉽 초대 링크 정보'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `em_saas`.`partnership_member_product_grant` (
     `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '파트너십 회원 제품 권한 번호',
     `partnership_member_idx` INT(11) NULL DEFAULT NULL COMMENT '파트너십 회원 번호',
     `product_code` VARCHAR(7) NULL DEFAULT NULL COMMENT '제품 코드' COLLATE 'utf8mb4_general_ci',
     `permission_code` VARCHAR(7) NULL DEFAULT NULL COMMENT '제품 권한 코드' COLLATE 'utf8mb4_general_ci',
     `update_date` DATETIME NULL DEFAULT NULL COMMENT '수정일',
     `create_date` DATETIME NULL DEFAULT NULL COMMENT '생성일',
     PRIMARY KEY (`idx`) USING BTREE,
     INDEX `fk_partnership_member_product_grant_partnership_member_idx` (`partnership_member_idx`) USING BTREE,
     CONSTRAINT `fk_partnership_member_product_grant_partnership_member_idx` FOREIGN KEY (`partnership_member_idx`) REFERENCES `partnership_member` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COMMENT='파트너십 회원 제품 권한'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

create table if not exists em_saas.partnership_invited_member
(
    idx                               int auto_increment
    primary key,
    email                             varchar(255)                         not null,
    partnership_idx                   int                                  not null,
    invited_by_partnership_member_idx int                                  null,
    member_idx                        int                                  null,
    partnership_member_idx            int                                  null,
    invited_date                      datetime default current_timestamp() null,
    joined_date                       datetime                             null,
    constraint partnership_invited_member_ibfk_1
    foreign key (partnership_idx) references em_saas.partnership (idx),
    constraint partnership_invited_member_ibfk_2
    foreign key (invited_by_partnership_member_idx) references em_saas.partnership_member (idx),
    constraint partnership_invited_member_ibfk_3
    foreign key (member_idx) references em_saas.member (idx),
    constraint partnership_invited_member_ibfk_4
    foreign key (partnership_member_idx) references em_saas.partnership_member (idx)
    );

create index if not exists invited_by_partnership_member_idx
    on em_saas.partnership_invited_member (invited_by_partnership_member_idx);

create index if not exists member_idx
    on em_saas.partnership_invited_member (member_idx);

create index if not exists partnership_idx
    on em_saas.partnership_invited_member (partnership_idx);

create index if not exists partnership_member_idx
    on em_saas.partnership_invited_member (partnership_member_idx);

CREATE TABLE IF NOT EXISTS `em_saas`.`partnership_additional` (
    `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '파트너쉽 부가정보 번호',
    `partnership_idx` INT(11) NULL DEFAULT NULL COMMENT '파트너쉽 번호',
    `attr_key` VARCHAR(255) NULL DEFAULT NULL COMMENT '파트너쉽 부가정보 속성 키' COLLATE 'utf8mb4_general_ci',
    `attr_value` VARCHAR(255) NULL DEFAULT NULL COMMENT '파트너쉽 부가정보 속성 값' COLLATE 'utf8mb4_general_ci',
    `update_date` DATETIME NULL DEFAULT NULL COMMENT '수정일',
    `create_date` DATETIME NULL DEFAULT NULL COMMENT '생성일',
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `fk_partnership_additional_partnership_idx` (`partnership_idx`) USING BTREE,
    CONSTRAINT `fk_partnership_additional_partnership_idx` FOREIGN KEY (`partnership_idx`) REFERENCES `partnership` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION,
    UNIQUE INDEX `partnership_idx_attr_key` (`partnership_idx`, `attr_key`) USING BTREE
)
COMMENT='파트너쉽 부가정보'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `em_saas`.`project_category` (
    `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '카테고리번호',
    `partnership_idx` INT(11) NULL DEFAULT NULL COMMENT '파트너쉽번호',
    `name` VARCHAR(255) NULL DEFAULT NULL COMMENT '카테고리명' COLLATE 'utf8mb4_general_ci',
    `sort` INT(11) NULL DEFAULT NULL COMMENT '정렬순서',
    `update_date` DATETIME NULL DEFAULT NULL COMMENT '수정일',
    `create_date` DATETIME NULL DEFAULT NULL COMMENT '생성일',
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `fk_project_category_partnership_idx` (`partnership_idx`) USING BTREE,
    CONSTRAINT `fk_project_category_partnership_idx` FOREIGN KEY (`partnership_idx`) REFERENCES `partnership` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COMMENT='프로젝트 카테고리 정보'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `em_saas`.`project` (
    `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '프로젝트번호',
    `project_category_idx` INT(11) NULL DEFAULT NULL COMMENT '프로젝트 카테고리번호',
    `partnership_idx` INT(11) NULL DEFAULT NULL COMMENT '파트너쉽 번호',
    `title` VARCHAR(50) NULL DEFAULT NULL COMMENT '제목' COLLATE 'utf8mb4_general_ci',
    `description` VARCHAR(200) NULL DEFAULT NULL COMMENT '내용(최대 200자)' COLLATE 'utf8mb4_general_ci',
    `status_cd` VARCHAR(7) NULL DEFAULT NULL COMMENT '프로젝트상태(code테이블)' COLLATE 'utf8mb4_general_ci',
    `image_url` VARCHAR(255) NULL DEFAULT NULL COMMENT '프로젝트 이미지 URL' COLLATE 'utf8mb4_general_ci',
    `image_path` VARCHAR(255) NULL DEFAULT NULL COMMENT '프로젝트 이미지 경로' COLLATE 'utf8mb4_general_ci',
    `node_cnt` INT(11) NULL DEFAULT '0' COMMENT '노드 개수',
    `edge_cnt` INT(11) NULL DEFAULT '0' COMMENT '엣지 개수',
    `update_date` DATETIME NULL DEFAULT NULL COMMENT '수정일',
    `create_date` DATETIME NULL DEFAULT NULL COMMENT '생성일',
    `delete_date` DATETIME NULL DEFAULT NULL COMMENT '삭제일',
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `status_cd` (`status_cd`) USING BTREE,
    INDEX `fk_project_project_category_idx` (`project_category_idx`) USING BTREE,
    CONSTRAINT `fk_project_project_category_idx` FOREIGN KEY (`project_category_idx`) REFERENCES `project_category` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COMMENT='프로젝트 정보'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `em_saas`.`project_member` (
    `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '프로제거트 구성원 번호',
    `project_idx` INT(11) NULL DEFAULT NULL COMMENT '프로젝트 번호',
    `partnership_member_idx` INT(11) NULL DEFAULT NULL COMMENT '파트너쉽 회원번호',
    `type_cd` VARCHAR(7) NULL DEFAULT NULL COMMENT '사용자 구분(code테이블)' COLLATE 'utf8mb4_general_ci',
    `disable_functions` VARCHAR(1024) NULL DEFAULT NULL COMMENT '사용제한 프로젝트 기능들(code 쉼표 구분)' COLLATE 'utf8mb4_general_ci',
    `active_flag` TINYINT(1) AS (CASE WHEN `delete_date` IS NULL THEN 1 ELSE 0 END) STORED COMMENT '활성여부(1:활성, 0:비활성)',
    `update_date` DATETIME NULL DEFAULT NULL COMMENT '수정일',
    `create_date` DATETIME NULL DEFAULT NULL COMMENT '생성일',
    `delete_date` DATETIME NULL DEFAULT NULL COMMENT '삭제일',
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `type_cd` (`type_cd`) USING BTREE,
    INDEX `fk_project_member_project_idx` (`project_idx`) USING BTREE,
    INDEX `fk_project_member_partnership_member_idx` (`partnership_member_idx`) USING BTREE,
    UNIQUE INDEX `ui_project_member_project_idx_partnership_member_idx_active_flag` (`project_idx`, `partnership_member_idx`, `delete_date`) USING BTREE,
    CONSTRAINT `fk_project_member_partnership_member_idx` FOREIGN KEY (`partnership_member_idx`) REFERENCES `partnership_member` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION,
    CONSTRAINT `fk_project_member_project_idx` FOREIGN KEY (`project_idx`) REFERENCES `project` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COMMENT='프로젝트 구성원'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `em_saas`.`project_file` (
    `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '프로젝트 파일번호',
    `project_idx` INT(11) NULL DEFAULT NULL COMMENT '프로젝트 번호',
    `file_name` VARCHAR(255) NULL DEFAULT NULL COMMENT '파일명' COLLATE 'utf8mb4_general_ci',
    `file_url` VARCHAR(512) NULL DEFAULT NULL COMMENT '파일URL' COLLATE 'utf8mb4_general_ci',
    `file_path` VARCHAR(512) NULL DEFAULT NULL COMMENT '파일경로' COLLATE 'utf8mb4_general_ci',
    `file_size` BIGINT(20) NULL DEFAULT NULL COMMENT '파일크기',
    `file_cd` VARCHAR(7) NULL DEFAULT NULL COMMENT '파일 구분(code)' COLLATE 'utf8mb4_general_ci',
    `update_date` DATETIME NULL DEFAULT NULL COMMENT '수정일',
    `create_date` DATETIME NULL DEFAULT NULL COMMENT '생성일',
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `file_cd` (`file_cd`) USING BTREE,
    INDEX `fk_project_file_project_idx` (`project_idx`) USING BTREE,
    CONSTRAINT `fk_project_file_project_idx` FOREIGN KEY (`project_idx`) REFERENCES `project` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COMMENT='프로젝트 업로드 파일'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `em_saas`.`project_table` (
    `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '프로젝트 테이블번호',
    `project_idx` INT(11) NULL DEFAULT NULL COMMENT '프로젝트 번호',
    `title` VARCHAR(512) NULL DEFAULT NULL COMMENT '테이블명' COLLATE 'utf8mb4_general_ci',
    `data_count` INT(11) NULL DEFAULT NULL COMMENT '데이터개수',
    `type_cd` VARCHAR(512) NULL DEFAULT NULL COMMENT '테이블 구분(code)' COLLATE 'utf8mb4_general_ci',
    `update_date` DATETIME NULL DEFAULT NULL COMMENT '수정일',
    `create_date` DATETIME NULL DEFAULT NULL COMMENT '생성일',
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `fk_project_table_project_idx` (`project_idx`) USING BTREE,
    CONSTRAINT `fk_project_table_project_idx` FOREIGN KEY (`project_idx`) REFERENCES `project` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COMMENT='프로젝트 테이블 정보'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `em_saas`.`project_table_auth` (
    `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '프로젝트 테이블 사용자 권한 번호',
    `project_table_idx` INT(11) NULL DEFAULT NULL COMMENT '프로젝트 테이블 번호',
    `partnership_member_idx` INT(11) NULL DEFAULT NULL COMMENT '파트너십 회원번호',
    `auth_cd` VARCHAR(7) NULL DEFAULT NULL COMMENT '권한 구분(code)' COLLATE 'utf8mb4_general_ci',
    `update_date` DATETIME NULL DEFAULT NULL COMMENT '수정일',
    `create_date` DATETIME NULL DEFAULT NULL COMMENT '생성일',
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `fk_project_table_auth_project_table_idx` (`project_table_idx`) USING BTREE,
    INDEX `fk_project_table_auth_partnership_member_idx` (`partnership_member_idx`) USING BTREE,
    CONSTRAINT `fk_project_table_auth_project_table_idx` FOREIGN KEY (`project_table_idx`) REFERENCES `project_table` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION,
    CONSTRAINT `fk_project_table_auth_partnership_member_idx` FOREIGN KEY (`partnership_member_idx`) REFERENCES `partnership_member` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COMMENT='프로젝트 테이블 사용자 권한 정보'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `em_saas`.`project_query_category` (
    `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '프로젝트 쿼리 카테고리 번호',
    `partnership_member_idx` INT(11) NULL DEFAULT NULL COMMENT '파트너십 회원번호',
    `project_idx` INT(11) NULL DEFAULT NULL COMMENT '프로젝트 번호',
    `name` VARCHAR(512) NULL DEFAULT NULL COMMENT '쿼리 카테고리명' COLLATE 'utf8mb4_general_ci',
    `sort` INT(11) NULL DEFAULT NULL COMMENT '정렬순서',
    `update_date` DATETIME NULL DEFAULT NULL COMMENT '수정일',
    `create_date` DATETIME NULL DEFAULT NULL COMMENT '생성일',
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `fk_project_query_category_partnership_member_idx` (`partnership_member_idx`) USING BTREE,
    INDEX `fk_project_query_category_project_idx` (`project_idx`) USING BTREE,
    CONSTRAINT `fk_project_query_category_partnership_member_idx` FOREIGN KEY (`partnership_member_idx`) REFERENCES `partnership_member` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION,
    CONSTRAINT `fk_project_query_category_project_idx` FOREIGN KEY (`project_idx`) REFERENCES `project` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COMMENT='프로젝트 쿼리 카테고리 정보'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `em_saas`.`project_query` (
    `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '프로젝트 쿼리 번호',
    `partnership_member_idx` INT(11) NULL DEFAULT NULL COMMENT '파트너십 회원번호',
    `project_idx` INT(11) NULL DEFAULT NULL COMMENT '프로젝트 번호',
    `project_query_category_idx` INT(11) NULL DEFAULT NULL COMMENT '프로젝트 쿼리 카테고리 번호',
    `title` VARCHAR(512) NULL DEFAULT NULL COMMENT '쿼리 타이틀' COLLATE 'utf8mb4_general_ci',
    `raw_query` TEXT NULL DEFAULT NULL COMMENT '쿼리' COLLATE 'utf8mb4_general_ci',
    `type_cd` VARCHAr(7) NULL DEFAULT NULL COMMENT '쿼리 종류' COLLATE 'utf8mb4_general_ci',
    `update_date` DATETIME NULL DEFAULT NULL COMMENT '수정일',
    `create_date` DATETIME NULL DEFAULT NULL COMMENT '생성일',
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `fk_project_query_partnership_member_idx` (`partnership_member_idx`) USING BTREE,
    INDEX `fk_project_query_project_idx` (`project_idx`) USING BTREE,
    INDEX `fk_project_query_project_query_category_idx` (`project_query_category_idx`) USING BTREE,
    CONSTRAINT `fk_project_query_partnership_member_idx` FOREIGN KEY (`partnership_member_idx`) REFERENCES `partnership_member` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION,
    CONSTRAINT `fk_project_query_project_idx` FOREIGN KEY (`project_idx`) REFERENCES `project` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION,
    CONSTRAINT `fk_project_query_project_query_category_idx` FOREIGN KEY (`project_query_category_idx`) REFERENCES `project_query_category` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COMMENT='프로젝트 쿼리 정보'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `em_saas`.`chat_room` (
    `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '채팅방 번호',
    `partnership_member_idx` INT(11) NULL DEFAULT NULL COMMENT '파트너십 회원번호',
    `title` VARCHAR(255) NULL DEFAULT NULL COMMENT '채팅방 제목' COLLATE 'utf8mb4_general_ci',
    `delete_date` DATETIME NULL DEFAULT NULL COMMENT '삭제일',
    `update_date` DATETIME NULL DEFAULT NULL COMMENT '수정일',
    `create_date` DATETIME NULL DEFAULT NULL COMMENT '생성일',
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `fk_chat_room_partnership_member_idx` (`partnership_member_idx`) USING BTREE,
    CONSTRAINT `fk_chat_room_partnership_member_idx` FOREIGN KEY (`partnership_member_idx`) REFERENCES `partnership_member` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COMMENT='LLM 채팅방 정보'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `em_saas`.`chat_history` (
    `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '채팅 이력 번호',
    `chat_room_idx` INT(11) NULL DEFAULT NULL COMMENT '채팅방 번호',
    `message` LONGTEXT NULL DEFAULT NULL COMMENT '채팅 메세지' COLLATE 'utf8mb4_general_ci',
    `sender_type` VARCHAR(7) NULL DEFAULT NULL COMMENT '보낸이 타입' COLLATE 'utf8mb4_general_ci',
    `category_type` VARCHAR(7) NULL DEFAULT NULL COMMENT '카테고리 타입' COLLATE 'utf8mb4_general_ci',
    `update_date` DATETIME NULL DEFAULT NULL COMMENT '수정일',
    `create_date` DATETIME NULL DEFAULT NULL COMMENT '생성일',
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `fk_chat_history_chat_room_idx` (`chat_room_idx`) USING BTREE,
    CONSTRAINT `fk_chat_history_chat_room_idx` FOREIGN KEY (`chat_room_idx`) REFERENCES `chat_room` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COMMENT='LLM 채팅 이력'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `em_saas`.`chat_tool_result` (
    `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '채팅 외부 도구 결과 번호',
    `chat_history_idx` INT(11) NULL DEFAULT NULL COMMENT '채팅 이력 번호',
    `tool_type` VARCHAR(7) NULL DEFAULT NULL COMMENT '채팅 도구 타입' COLLATE 'utf8mb4_general_ci',
    `title` VARCHAR(255) NULL DEFAULT NULL COMMENT '검색 결과 제목' COLLATE 'utf8mb4_general_ci',
    `url` VARCHAR(500) NULL DEFAULT NULL COMMENT '검색 결과 url' COLLATE 'utf8mb4_general_ci',
    `update_date` DATETIME NULL DEFAULT NULL COMMENT '수정일',
    `create_date` DATETIME NULL DEFAULT NULL COMMENT '생성일',
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `fk_chat_tool_result_chat_history_idx` (`chat_history_idx`) USING BTREE,
    CONSTRAINT `fk_chat_tool_result_chat_history_idx` FOREIGN KEY (`chat_history_idx`) REFERENCES `chat_history` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COMMENT='LLM 채팅 외부 도구 결과'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `em_saas`.`chat_file` (
    `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '채팅 파일번호',
    `chat_history_idx` INT(11) NULL DEFAULT NULL COMMENT '채팅 이력 번호',
    `file_name` VARCHAR(255) NULL DEFAULT NULL COMMENT '파일명' COLLATE 'utf8mb4_general_ci',
    `file_url` VARCHAR(512) NULL DEFAULT NULL COMMENT '파일URL' COLLATE 'utf8mb4_general_ci',
    `file_path` VARCHAR(512) NULL DEFAULT NULL COMMENT '파일경로' COLLATE 'utf8mb4_general_ci',
    `file_size` BIGINT(20) NULL DEFAULT NULL COMMENT '파일크기',
    `file_cd` VARCHAR(7) NULL DEFAULT NULL COMMENT '파일 구분(code)' COLLATE 'utf8mb4_general_ci',
    `update_date` DATETIME NULL DEFAULT NULL COMMENT '수정일',
    `create_date` DATETIME NULL DEFAULT NULL COMMENT '생성일',
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `file_cd` (`file_cd`) USING BTREE,
    INDEX `fk_chat_file_chat_history_idx` (`chat_history_idx`) USING BTREE,
    CONSTRAINT `fk_chat_file_chat_history_idx` FOREIGN KEY (`chat_history_idx`) REFERENCES `chat_history` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COMMENT='채팅 관련 파일'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `em_saas`.`chat_file_slide` (
     `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '채팅 ppt 슬라이드 번호',
     `chat_file_idx` INT(11) NULL DEFAULT NULL COMMENT '채팅 파일 번호',
     `content` LONGTEXT NULL DEFAULT NULL COMMENT '슬라이드 내용' COLLATE 'utf8mb4_general_ci',
     `page` INT(11) NULL DEFAULT NULL COMMENT '페이지번호',
     `update_date` DATETIME NULL DEFAULT NULL COMMENT '수정일',
     `create_date` DATETIME NULL DEFAULT NULL COMMENT '생성일',
     PRIMARY KEY (`idx`) USING BTREE,
     INDEX `fk_chat_file_slide_chat_file_idx` (`chat_file_idx`) USING BTREE,
     CONSTRAINT `fk_chat_file_slide_chat_file_idx` FOREIGN KEY (`chat_file_idx`) REFERENCES `chat_file` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COMMENT='채팅 파일 슬라이드 정보'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `em_saas`.`chat_network` (
    `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '채팅 관계망 번호',
    `chat_history_idx` INT(11) NULL DEFAULT NULL COMMENT '채팅 이력 번호',
    `title` VARCHAR(255) NULL DEFAULT NULL COMMENT '관계망 제목' COLLATE 'utf8mb4_general_ci',
    `update_date` DATETIME NULL DEFAULT NULL COMMENT '수정일',
    `create_date` DATETIME NULL DEFAULT NULL COMMENT '생성일',
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `fk_chat_network_chat_history_idx` (`chat_history_idx`) USING BTREE,
    CONSTRAINT `fk_chat_network_chat_history_idx` FOREIGN KEY (`chat_history_idx`) REFERENCES `chat_history` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COMMENT='LLM 채팅 관계망 정보'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `em_saas`.`chat_node` (
    `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '채팅 노드 번호',
    `chat_network_idx` INT(11) NULL DEFAULT NULL COMMENT '채팅 관계망 번호',
    `id` VARCHAR(255) NULL DEFAULT NULL COMMENT '노드 아이디' COLLATE 'utf8mb4_general_ci',
    `labels` VARCHAR(50) NULL DEFAULT NULL COMMENT '노드 라벨' COLLATE 'utf8mb4_general_ci',
    `properties` JSON NULL DEFAULT NULL COMMENT '노드 속성' COLLATE 'utf8mb4_general_ci',
    `update_date` DATETIME NULL DEFAULT NULL COMMENT '수정일',
    `create_date` DATETIME NULL DEFAULT NULL COMMENT '생성일',
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `fk_chat_node_chat_network_idx` (`chat_network_idx`) USING BTREE,
    CONSTRAINT `fk_chat_node_chat_network_idx` FOREIGN KEY (`chat_network_idx`) REFERENCES `chat_network` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COMMENT='LLM 채팅 노드 정보'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `em_saas`.`chat_link` (
     `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '채팅 링크 번호',
     `chat_network_idx` INT(11) NULL DEFAULT NULL COMMENT '채팅 관계망 번호',
     `type` VARCHAR(255) NULL DEFAULT NULL COMMENT '링크 유형' COLLATE 'utf8mb4_general_ci',
     `start` VARCHAR(255) NULL DEFAULT NULL COMMENT '시작' COLLATE 'utf8mb4_general_ci',
     `end` VARCHAR(255) NULL DEFAULT NULL COMMENT '끝' COLLATE 'utf8mb4_general_ci',
     `properties` JSON NULL DEFAULT NULL COMMENT '링크 속성' COLLATE 'utf8mb4_general_ci',
     `update_date` DATETIME NULL DEFAULT NULL COMMENT '수정일',
     `create_date` DATETIME NULL DEFAULT NULL COMMENT '생성일',
     PRIMARY KEY (`idx`) USING BTREE,
     INDEX `fk_chat_link_chat_network_idx` (`chat_network_idx`) USING BTREE,
     CONSTRAINT `fk_chat_link_chat_network_idx` FOREIGN KEY (`chat_network_idx`) REFERENCES `chat_network` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COMMENT='LLM 채팅 노드 정보'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;


CREATE TABLE IF NOT EXISTS `em_saas`.`chat_mcp` (
     `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '채팅 링크 번호',
     `chat_history_idx` INT(11) NULL DEFAULT NULL COMMENT '채팅 관계망 번호',
     `name` VARCHAR(255) NULL DEFAULT NULL COMMENT '링크 유형' COLLATE 'utf8mb4_general_ci',
     `update_date` DATETIME NULL DEFAULT NULL COMMENT '수정일',
     `create_date` DATETIME NULL DEFAULT NULL COMMENT '생성일',
     PRIMARY KEY (`idx`) USING BTREE,
     INDEX `fk_chat_mcp_chat_history_idx` (`chat_history_idx`) USING BTREE,
     CONSTRAINT `fk_chat_mcp_chat_history_idx` FOREIGN KEY (`chat_history_idx`) REFERENCES `chat_history` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COMMENT='LLM 채팅 MCP 정보'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;
