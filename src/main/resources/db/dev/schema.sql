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

create table if not exists em_saas.license
(
    idx          int auto_increment comment '라이센스번호'
    primary key,
    name         varchar(50)                          null comment '라이센스명',
    description  varchar(255)                         null comment '라이센스 설명',
    price        decimal(20, 10)                      null comment '라이센스 가격',
    unit_cd      varchar(7)                           null comment '화페 단위(code 테이블)',
    period_month int                                  null comment '라이센스 기간(월)',
    active       tinyint                              null comment '활성화 여부',
    update_date  datetime                             null comment '수정일',
    create_date  datetime default current_timestamp() not null comment '등록일'
    )
    comment '라이센스 정보';

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

create table if not exists em_saas.license_partnership
(
    idx             int auto_increment comment '파트너쉽 라이센스 번호'
    primary key,
    license_idx     int                                  not null comment '라이센스 번호',
    partnership_idx int                                  not null comment '파트너쉽 번호',
    state_cd        varchar(7)                           null comment '라이센스 상태(code 테이블)',
    start_date      datetime                             null comment '라이센스 시작일',
    end_date        datetime                             null comment '라이센스 만료일',
    pause_date      datetime                             null comment '정지일',
    update_date     datetime                             null comment '수정일',
    create_date     datetime default current_timestamp() not null comment '등록일',
    constraint license_partnership_license_idx_fk
    foreign key (license_idx) references em_saas.license (idx)
    on delete cascade,
    constraint license_partnership_partnership_idx_fk
    foreign key (partnership_idx) references em_saas.partnership (idx)
    on delete cascade
    )
    comment '파트너쉽 라이센스 정보';

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
    `update_date` DATETIME NULL DEFAULT NULL COMMENT '수정일',
    `create_date` DATETIME NULL DEFAULT NULL COMMENT '생성일',
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `type_cd` (`type_cd`) USING BTREE,
    INDEX `fk_project_member_project_idx` (`project_idx`) USING BTREE,
    INDEX `fk_project_member_partnership_member_idx` (`partnership_member_idx`) USING BTREE,
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

CREATE TABLE `em_saas`.`chat_tool_result` (
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

