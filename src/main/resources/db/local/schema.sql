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

create table if not exists em_saas.member_login_history
(
    idx         int auto_increment comment '로그인이력번호'
    primary key,
    member_idx  int                                  not null comment '회원번호',
    browser     varchar(100)                         null comment '접속 브라우저',
    platform    varchar(100)                         null comment '접속 환경',
    ip          varchar(15)                          null comment '접속 ip',
    create_date datetime default current_timestamp() not null comment '접속일',
    constraint member_login_history_member_idx_fk
    foreign key (member_idx) references em_saas.member (idx)
    on delete cascade
    )
    comment '로그인 이력 정보';

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
    on delete cascade
    )
    comment '파트너쉽 회원정보';

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

