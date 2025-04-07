CREATE TABLE IF NOT EXISTS `em_stock`.`code`
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
COMMENT ='코드표'
COLLATE = 'utf8mb4_general_ci'
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `em_stock`.`company_detail`
(
    idx         BIGINT(20) AUTO_INCREMENT COMMENT '기업정보 idx',
    iscd        VARCHAR(9)  NULL COMMENT '단축종목코드',
    bstp_name   VARCHAR(40) NULL COMMENT '업종명',
    create_date DATETIME    NULL COMMENT '등록일',
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `company_detail_bstp_name_index` (`bstp_name`) USING BTREE,
    INDEX `company_detail_company_info_iscd_index` (`iscd`) USING BTREE
)
COMMENT = '종목 기업 상세 데이터'
COLLATE = 'utf8mb4_general_ci'
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `em_stock`.`company_info`
(
    id                      INT(11) AUTO_INCREMENT COMMENT '고유번호',
    biz_num                 VARCHAR(13)                              NULL COMMENT '사업자등록번호',
    corporation_num         VARCHAR(13)                              NULL COMMENT '법인등록번호',
    company_name            VARCHAR(255)                             NULL COMMENT '기업명',
    real_company_name       VARCHAR(255)                             NULL COMMENT '순수 기업명',
    company_state           VARCHAR(255)                             NULL COMMENT '법인상태',
    representation_name     VARCHAR(60)                              NULL COMMENT '대표자명',
    company_type            VARCHAR(255)                             NULL COMMENT '기업유형',
    company_size            VARCHAR(255)                             NULL COMMENT '기업규모',
    employee_count          VARCHAR(10)                              NULL COMMENT '직원수',
    establishment_date      VARCHAR(20)                              NULL COMMENT '설립일',
    acct_month              VARCHAR(50)                              NULL COMMENT '재무일자',
    business_condition_code VARCHAR(1)   DEFAULT ''                  NULL COMMENT '산업코드 업태(산업코드 앞1자리 알파벳)',
    business_condition_desc VARCHAR(255) DEFAULT ''                  NULL COMMENT '산업코드 업태명',
    business_category_code  VARCHAR(10)                              NULL COMMENT '산업코드',
    business_category_desc  VARCHAR(255)                             NULL COMMENT '산업코드명',
    homepage                VARCHAR(255)                             NULL COMMENT '홈페이지 주소',
    tel                     VARCHAR(50)                              NULL COMMENT '전화번호',
    email                   VARCHAR(255) DEFAULT ''                  NULL COMMENT '대표 이메일',
    fax                     VARCHAR(20)                              NULL COMMENT '팩스',
    address                 VARCHAR(255)                             NULL COMMENT '주소',
    zip_code                VARCHAR(5)                               NULL COMMENT '우편번호',
    sales                   VARCHAR(255) DEFAULT ''                  NULL COMMENT '매출(천원)',
    sales_year              VARCHAR(255) DEFAULT ''                  NULL COMMENT '매출년도',
    major_product           VARCHAR(255)                             NULL COMMENT '대표제품',
    head_office             CHAR         DEFAULT '1'                 NULL COMMENT '본사여부',
    category                VARCHAR(255) DEFAULT ''                  NULL COMMENT '카테고리',
    keyword                 VARCHAR(500) DEFAULT ''                  NULL COMMENT '키워드',
    visible                 BIT          DEFAULT b'1'                NULL COMMENT '공개유무(1:공개, 0:비공개)',
    description             TEXT         DEFAULT ''                  NULL COMMENT '소개',
    origin_id               INT(10)                                  NULL COMMENT '0 : 바우처, 1 : ked_detail, 2 : 대전, ',
    listing_market_id       VARCHAR(3)                               NULL COMMENT '상장시장구분코드(1:상장, 2:코스닥, 3:코넥스, 4:K-OTC, 9:기타)',
    listing_market_desc     VARCHAR(10)                              NULL COMMENT '상장코드명',
    country                 VARCHAR(255)                             NULL COMMENT '기업국가',
    logo_url                VARCHAR(500)                             NULL COMMENT '로고 URL',
    illu_id                 VARCHAR(13)                              NULL,
    is_koscom_scrap_success TINYINT      DEFAULT 0                   NULL COMMENT '코스콤 수집 성공여부(0:수집성공, 1:수집실패, 2:직접입력)',
    create_date             DATE                                     NULL COMMENT '생성일',
    update_date             DATE                                     NULL COMMENT '수정일',
    company_timestamp       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP() NULL ON UPDATE CURRENT_TIMESTAMP(),
    stnd_iscd               VARCHAR(12)                              NULL COMMENT '표준종목코드',
    iscd                    VARCHAR(9)                               NULL COMMENT '단축종목코드',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_biz_cor` (`biz_num`, `corporation_num`) USING BTREE,
    INDEX `idx_illu_id` (`illu_id`) USING BTREE,
    INDEX `idx_name` (`real_company_name`) USING BTREE,
    INDEX `idx_update_date` (`update_date`) USING BTREE,
    INDEX `index_name` (`corporation_num`) USING BTREE,
    INDEX `iscd` (`iscd`) USING BTREE,
    INDEX `new_company_info_biz_num_index` (`biz_num`) USING BTREE,
    INDEX `new_company_info_business_category_code_index` (`business_category_code`) USING BTREE,
    INDEX `new_company_info_corporation_num_index` (`corporation_num`) USING BTREE,
    INDEX `new_company_info_illu_id_index` (`illu_id`) USING BTREE,
    INDEX `new_company_info_is_koscom_scrap_success_index` (`is_koscom_scrap_success`) USING BTREE,
    INDEX `new_company_info_origin_id_index` (`origin_id`) USING BTREE,
    INDEX `stnd_iscd` (`stnd_iscd`) USING BTREE
)
COMMENT = '기업정보'
COLLATE = 'utf8mb4_general_ci'
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `em_stock`.`company_logos`
(
    idx              BIGINT(11)   AUTO_INCREMENT         COMMENT '로고 idx',
    iscd             VARCHAR(9)                     NULL COMMENT '종목번호',
    kor_isnm         VARCHAR(40)                    NULL COMMENT '한글 종목명',
    file_url         VARCHAR(500)                   NULL COMMENT '로고 url',
    file_path        VARCHAR(500)                   NULL COMMENT '로고 위치',
    file_name        VARCHAR(500)                   NULL COMMENT '파일 원본이름',
    file_alias       VARCHAR(500)                   NULL COMMENT '파일 별칭(종목번호_한글종목명)',
    file_size        bigint(20)                     NULL COMMENT '파일크기',
    update_date      DATETIME                       NULL COMMENT '수정일',
    create_date      DATETIME                       NULL COMMENT '등록일',
    PRIMARY KEY (`idx`) USING BTREE,
    CONSTRAINT company_logos_jstock_jong_iscd_fk FOREIGN KEY (iscd) REFERENCES jstock_jong (iscd)
)
COMMENT = '기업 로고'
COLLATE = 'utf8mb4_general_ci'
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `em_stock`.`company_theme`
(
    idx              BIGINT(20) AUTO_INCREMENT COMMENT '종목 테마 데이터 idx',
    theme_idx        BIGINT(20)     NULL COMMENT '테마 코드 idx',
    iscd             VARCHAR(9) NULL COMMENT '단축종목코드',
    company_overview TEXT       NULL COMMENT '기업개요',
    create_date      DATETIME   NULL COMMENT '등록일',
    PRIMARY KEY (`idx`) USING BTREE,
    CONSTRAINT company_theme_theme_code_idx_fk FOREIGN KEY (theme_idx) REFERENCES theme_code (idx),
    INDEX `company_theme_iscd_index` (`iscd`) USING BTREE
)
COMMENT = '종목-테마 데이터'
COLLATE = 'utf8mb4_general_ci'
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `em_stock`.`jstock_jong`
(
    idx                    BIGINT(20) AUTO_INCREMENT COMMENT '주식종목 정보 idx',
    stnd_iscd              VARCHAR(12) DEFAULT '' NOT   NULL COMMENT '표준종목코드',
    iscd                   VARCHAR(9)                   NULL COMMENT '단축 종목코드',
    kor_isnm               VARCHAR(40)                  NULL COMMENT '한글 종목명',
    mrkt_div_cls_code      VARCHAR(1)                   NULL COMMENT '시장 분류 구분 코드',
    scrt_grp_cls_code      VARCHAR(3)                   NULL COMMENT '증권그룹구분코드',
    avls_scal_cls_code     VARCHAR(1)                   NULL COMMENT '시가총액 규모 구분코드',
    bstp_larg_div_code     VARCHAR(4)                   NULL COMMENT '업종대분류코드',
    bstp_medm_div_code     VARCHAR(4)                   NULL COMMENT '업종중분류코드',
    bstp_smal_div_code     VARCHAR(4)                   NULL COMMENT '업종소분류코드',
    mnin_cls_code          VARCHAR(2)                   NULL COMMENT '재조업구분코드',
    dvdn_nmix_issu_yn      VARCHAR(2)                   NULL COMMENT '배당지수종목여부',
    sprn_strr_sprr_yn      VARCHAR(2)                   NULL COMMENT '지배구조우량여부',
    kospi200_apnt_cls_code VARCHAR(1)                   NULL COMMENT 'KOSPI200채용구분코드',
    kospi100_issu_yn       VARCHAR(2)                   NULL COMMENT 'KOSPI100종목여부',
    sprn_strr_nmix_issu_yn VARCHAR(2)                   NULL COMMENT '지배구조지수종목여부',
    krx100_issu_yn         VARCHAR(2)                   NULL COMMENT 'KRX100종목여부',
    stac_month             VARCHAR(2)                   NULL COMMENT '결산월',
    stck_fcam              FLOAT                        NULL COMMENT '액면가',
    stck_sdpr              INT                          NULL COMMENT '주식 기준가',
    lstn_cpf               DOUBLE                       NULL COMMENT '자본금',
    lstn_stcn              BIGINT                       NULL COMMENT '상장주수',
    dvdn_ert               DOUBLE                       NULL COMMENT '배당수익율',
    crdt_rmnd_rate         FLOAT                        NULL COMMENT '신용잔고비율',
    trht_yn                VARCHAR(1)                   NULL COMMENT '거래정지여부(Y:정상, N:거래정지)',
    sltr_yn                VARCHAR(1)                   NULL COMMENT '정리매매여부(Y:정상, N:정리매매)',
    mang_issu_yn           VARCHAR(1)                   NULL COMMENT '관리 종목 여부',
    mrkt_alrm_cls_code     VARCHAR(2)                   NULL COMMENT '시장 경고 구분 코드',
    mrkt_alrm_risk_adnt_yn VARCHAR(1)                   NULL COMMENT '시장 경고 예고',
    insn_pbnt_yn           VARCHAR(1)                   NULL COMMENT '불성실공시여부',
    byps_lstn_yn           VARCHAR(1)                   NULL COMMENT '우회상장여부',
    flng_cls_code          VARCHAR(2)                   NULL COMMENT '락구분코드',
    crdt_rate              FLOAT                        NULL COMMENT '당사신용비율',
    high_risk              VARCHAR(1)                   NULL COMMENT '고위험종목 여부 1:위험 0:해당없음  2014.08.01',
    equ_rating             VARCHAR(1)                   NULL COMMENT 'Equity Rating A,B,C,D,F            2015.04.09',
    avls                   BIGINT                       NULL COMMENT '시가총액 (단위 : 원)',
    frgn_hldn_rate         DOUBLE                       NULL COMMENT '외국인보유비율',
    prst_cls_code          VARCHAR(1)                   NULL COMMENT '우선주구분코드',
    update_date            DATETIME                     NULL COMMENT '수정일',
    create_date            DATETIME                     NULL COMMENT '등록일',
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `jstock_jong_iscd` (`iscd`) USING BTREE,
    INDEX `jstock_jong_kor_isnm` (`kor_isnm`) USING BTREE,
    INDEX `jstock_jong_stnd_iscd` (`stnd_iscd`) USING BTREE
)
COMMENT = '주식종목 정보'
COLLATE = 'utf8mb4_general_ci'
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `em_stock`.`theme_original`
(
    idx              BIGINT AUTO_INCREMENT COMMENT '테마 원본 데이터 idx',
    theme_idx        BIGINT      NULL COMMENT '테마 코드 idx',
    theme_name       VARCHAR(40) NULL COMMENT '테마명',
    iscd             VARCHAR(9)  NULL COMMENT '단축종목 코드',
    kor_isnm         VARCHAR(40) NULL COMMENT '종목명',
    bstp_name        VARCHAR(40) NULL COMMENT '업종명',
    company_overview TEXT        NULL COMMENT '기업 개요',
    PRIMARY KEY (`idx`) USING BTREE,
    CONSTRAINT theme_original_theme_code_idx_fk FOREIGN KEY (theme_idx) REFERENCES theme_code (idx),
    INDEX `theme_original_theme_idx_index` (`theme_idx`) USING BTREE,
    INDEX `theme_original_theme_name_index` (`theme_name`) USING BTREE
)
COMMENT = '테마 원본 데이터'
COLLATE = 'utf8mb4_general_ci'
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `em_stock`.`theme_code`
(
    idx        BIGINT(20) AUTO_INCREMENT COMMENT '테마 idx',
    theme_name VARCHAR(100) NULL COMMENT '테마명',
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `theme_code_theme_name_index` (`theme_name`) USING BTREE
)
COMMENT = '테마 코드 테이블'
COLLATE = 'utf8mb4_general_ci'
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `em_stock`.`invest`
(
    idx                    BIGINT(20) AUTO_INCREMENT COMMENT '투자자정보 idx',
    pos                    INT                       NULL,
    mrkt_div_cls_code      VARCHAR(1) DEFAULT '' NOT NULL COMMENT 'NOT_NULL 시장 분류 구분 코드',
    bstp_cls_code          VARCHAR(3) DEFAULT '' NOT NULL COMMENT 'NOT_NULL 업종 구분 코드',
    grp_cls_code           VARCHAR(3) DEFAULT '' NOT NULL COMMENT 'NOT_NULL 그룹 구분 코드',
    prod_no                VARCHAR(3) DEFAULT '' NOT NULL COMMENT 'NOT_NULL 상품 번호',
    bsop_date              INT(11)                   NULL COMMENT '주식 영업 일자',
    bsop_hour              INT(11)                   NULL COMMENT '현재 시간',
    prpr_nmix              DOUBLE                    NULL COMMENT '업종 지수 현재가',
    prdy_vrss_sign         VARCHAR(1)                NULL COMMENT '전일 대비 부호',
    bstp_nmix_prdy_vrss    DOUBLE                    NULL COMMENT '전일 대비',
    scrt_seln_vol          BIGINT(20)                NULL COMMENT '증권 매도 거래량                   금융투자',
    scrt_shnu_vol          BIGINT(20)                NULL COMMENT '증권 매수 거래량                   금융투자',
    scrt_seln_tr_pbmn      BIGINT(20)                NULL COMMENT '증권 매도 거래대금                 금융투자',
    scrt_shnu_tr_pbmn      BIGINT(20)                NULL COMMENT '증권 매수 거래대금                 금융투자',
    insu_seln_vol          BIGINT(20)                NULL COMMENT '보험 매도 거래량',
    insu_shnu_vol          BIGINT(20)                NULL COMMENT '보험 매수 거래량',
    insu_seln_tr_pbmn      BIGINT(20)                NULL COMMENT '보험 매도 거래대금',
    insu_shnu_tr_pbmn      BIGINT(20)                NULL COMMENT '보험 매수 거래대금',
    ivtr_seln_vol          BIGINT(20)                NULL COMMENT '투신 매도 거래량',
    ivtr_shnu_vol          BIGINT(20)                NULL COMMENT '투신 매수 거래량',
    ivtr_seln_tr_pbmn      BIGINT(20)                NULL COMMENT '투신 매도 거래대금',
    ivtr_shnu_tr_pbmn      BIGINT(20)                NULL COMMENT '투신 매수 거래대금',
    pe_fund_seln_vol       BIGINT(20)                NULL COMMENT '사모펀드 매도 거래량',
    pe_fund_shnu_vol       BIGINT(20)                NULL COMMENT '사모펀드 매수 거래량',
    pe_fund_seln_tr_pbmn   BIGINT(20)                NULL COMMENT '사모펀드 매도 거래대금',
    pe_fund_shnu_tr_pbmn   BIGINT(20)                NULL COMMENT '사모펀드 매수 거래대금',
    bank_seln_vol          BIGINT(20)                NULL COMMENT '은행 매도 거래량',
    bank_shnu_vol          BIGINT(20)                NULL COMMENT '은행 매수 거래량',
    bank_seln_tr_pbmn      BIGINT(20)                NULL COMMENT '은행 매도 거래대금',
    bank_shnu_tr_pbmn      BIGINT(20)                NULL COMMENT '은행 매수 거래대금',
    mrbn_seln_vol          BIGINT(20)                NULL COMMENT '종금 매도 거래량                   기타금융',
    mrbn_shnu_vol          BIGINT(20)                NULL COMMENT '종금 매수 거래량                   기타금융',
    mrbn_seln_tr_pbmn      BIGINT(20)                NULL COMMENT '종금 매도 거래대금                 기타금융',
    mrbn_shnu_tr_pbmn      BIGINT(20)                NULL COMMENT '종금 매수 거래대금                 기타금융',
    fund_seln_vol          BIGINT(20)                NULL COMMENT '기금 매도 거래량                   연기금  ',
    fund_shnu_vol          BIGINT(20)                NULL COMMENT '기금 매수 거래량                   연기금  ',
    fund_seln_tr_pbmn      BIGINT(20)                NULL COMMENT '기금 매도 거래대금                 연기금  ',
    fund_shnu_tr_pbmn      BIGINT(20)                NULL COMMENT '기금 매수 거래대금                 연기금  ',
    etc_orgt_seln_vol      BIGINT(20)                NULL COMMENT '기타 단체 매도 거래량              국가자치',
    etc_orgt_shnu_vol      BIGINT(20)                NULL COMMENT '기타 단체 매수 거래량              국가자치',
    etc_orgt_seln_tr_pbmn  BIGINT(20)                NULL COMMENT '기타 단체 매도 거래대금            국가자치',
    etc_orgt_shnu_tr_pbmn  BIGINT(20)                NULL COMMENT '기타 단체 매수 거래대금            국가자치',
    etc_corp_seln_vol      BIGINT(20)                NULL COMMENT '기타 법인 매도 거래량 ',
    etc_corp_shnu_vol      BIGINT(20)                NULL COMMENT '기타 법인 매수 거래량 ',
    etc_corp_seln_tr_pbmn  BIGINT(20)                NULL COMMENT '기타 법인 매도 거래대금',
    etc_corp_shnu_tr_pbmn  BIGINT(20)                NULL COMMENT '기타 법인 매수 거래대금',
    etc_seln_vol           BIGINT(20)                NULL COMMENT '기타 매도 거래량',
    etc_shnu_vol           BIGINT(20)                NULL COMMENT '기타 매수 거래량',
    etc_seln_tr_pbmn       BIGINT(20)                NULL COMMENT '기타 매도 거래대금',
    etc_shnu_tr_pbmn       BIGINT(20)                NULL COMMENT '기타 매수 거래대금',
    prsn_seln_vol          BIGINT(20)                NULL COMMENT '개인 매도 거래량',
    prsn_shnu_vol          BIGINT(20)                NULL COMMENT '개인 매수 거래량',
    prsn_seln_tr_pbmn      BIGINT(20)                NULL COMMENT '개인 매도 거래대금',
    prsn_shnu_tr_pbmn      BIGINT(20)                NULL COMMENT '개인 매수 거래대금',
    frgn_reg_seln_vol      BIGINT(20)                NULL COMMENT '외국인 등록 매도 거래량            외국인',
    frgn_reg_shnu_vol      BIGINT(20)                NULL COMMENT '외국인 등록 매수 거래량            외국인',
    frgn_reg_seln_tr_pbmn  BIGINT(20)                NULL COMMENT '외국인 등록 매도 거래대금          외국인',
    frgn_reg_shnu_tr_pbmn  BIGINT(20)                NULL COMMENT '외국인 등록 매수 거래대금          외국인',
    frgn_nreg_seln_vol     BIGINT(20)                NULL COMMENT '외국인 비등록 매도 거래량          기타외인',
    frgn_nreg_shnu_vol     BIGINT(20)                NULL COMMENT '외국인 비등록 매수 거래량          기타외인',
    frgn_nreg_seln_tr_pbmn BIGINT(20)                NULL COMMENT '외국인 비등록 매도 거래대금        기타외인',
    frgn_nreg_shnu_tr_pbmn BIGINT(20)                NULL COMMENT '외국인 비등록 매수 거래대금        기타외인',
    frgn_seln_vol          BIGINT(20)                NULL COMMENT '외국인 매도 거래량                 외국인계',
    frgn_shnu_vol          BIGINT(20)                NULL COMMENT '외국인 매수 거래량                 외국인계',
    frgn_seln_tr_pbmn      BIGINT(20)                NULL COMMENT '외국인 매도 거래대금               외국인계',
    frgn_shnu_tr_pbmn      BIGINT(20)                NULL COMMENT '외국인 매수 거래대금               외국인계',
    orgn_seln_vol          BIGINT(20)                NULL COMMENT '기관계 매도 거래량',
    orgn_shnu_vol          BIGINT(20)                NULL COMMENT '기관계 매수 거래량',
    orgn_seln_tr_pbmn      BIGINT(20)                NULL COMMENT '기관계 매도 거래대금',
    orgn_shnu_tr_pbmn      BIGINT(20)                NULL COMMENT '기관계 매수 거래대금',
    whol_seln_vol          BIGINT(20)                NULL COMMENT '전체 매도 거래량',
    whol_shnu_vol          BIGINT(20)                NULL COMMENT '전체 매수 거래량',
    whol_seln_tr_pbmn      BIGINT(20)                NULL COMMENT '전체 매도 거래대금',
    whol_shnu_tr_pbmn      BIGINT(20)                NULL COMMENT '전체 매수 거래대금',
    update_date            DATETIME              NULL COMMENT '수정일',
    create_date            DATETIME              NULL COMMENT '등록일',
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX bstp_cls_code (`bstp_cls_code`) USING BTREE,
    INDEX grp_cls_code (`grp_cls_code`) USING BTREE,
    INDEX mrkt_div_cls_code (`mrkt_div_cls_code`) USING BTREE,
    INDEX prod_no (`prod_no`) USING BTREE
)
COMMENT '투자자 정보'
COLLATE = 'utf8mb4_general_ci'
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `em_stock`.`jsise`
(
    idx                          BIGINT(20) AUTO_INCREMENT COMMENT '주식 시세 idx',
    iscd                         VARCHAR(9)            NULL COMMENT '단축 종목코드',
    stnd_iscd                    VARCHAR(12)           NULL COMMENT '표준종목코드',
    prpr                         BIGINT(20)            NULL COMMENT '현재가',
    kor_isnm                     VARCHAR(40)           NULL COMMENT '한글 종목명',
    prdy_vrss                    BIGINT(20)            NULL COMMENT '전일 대비',
    prdy_vrss_sign               VARCHAR(1) DEFAULT '' NULL COMMENT '전일 대비 부호(1:상한 2:상승 3:보합 4:하한 5:하락)',
    prdy_ctrt                    DOUBLE                NULL COMMENT '전일 대비율 (단위 : %)',
    bsop_hour                    BIGINT(20)            NULL COMMENT '시간 (단위 : HHMMSS)',
    bsop_date                    BIGINT(20)            NULL COMMENT '일자 (단위 : YYYYMMDD)',
    prdy_clpr                    BIGINT(20)            NULL COMMENT '전일 종가 (단위 : 원)',
    acml_vol                     BIGINT(20)            NULL COMMENT '누적 거래량 (단위 : 주)',
    acml_tr_pbmn                 BIGINT(20)            NULL COMMENT '누적 거래 대금 (단위 : 원)',
    oprc                         BIGINT(20)            NULL COMMENT '시가',
    hgpr                         BIGINT(20)            NULL COMMENT '고가',
    lwpr                         BIGINT(20)            NULL COMMENT '저가',
    mxpr                         BIGINT(20)            NULL COMMENT '상한가',
    llam                         BIGINT(20)            NULL COMMENT '하한가',
    avls                         BIGINT(20)            NULL COMMENT '시가총액 (단위 : 원)',
    prdy_vol                     BIGINT(20)            NULL COMMENT '전일거래량 (단위 : 주)',
    askp1                        BIGINT(20)            NULL COMMENT '매도호가1',
    askp2                        BIGINT(20)            NULL COMMENT '매도호가2',
    askp3                        BIGINT(20)            NULL COMMENT '매도호가3',
    askp4                        BIGINT(20)            NULL COMMENT '매도호가4',
    askp5                        BIGINT(20)            NULL COMMENT '매도호가5',
    askp6                        BIGINT(20)            NULL COMMENT '매도호가6',
    askp7                        BIGINT(20)            NULL COMMENT '매도호가7',
    askp8                        BIGINT(20)            NULL COMMENT '매도호가8',
    askp9                        BIGINT(20)            NULL COMMENT '매도호가9',
    askp10                       BIGINT(20)            NULL COMMENT '매도호가10',
    bidp1                        BIGINT(20)            NULL COMMENT '매수호가1',
    bidp2                        BIGINT(20)            NULL COMMENT '매수호가2',
    bidp3                        BIGINT(20)            NULL COMMENT '매수호가3',
    bidp4                        BIGINT(20)            NULL COMMENT '매수호가4',
    bidp5                        BIGINT(20)            NULL COMMENT '매수호가5',
    bidp6                        BIGINT(20)            NULL COMMENT '매수호가6',
    bidp7                        BIGINT(20)            NULL COMMENT '매수호가7',
    bidp8                        BIGINT(20)            NULL COMMENT '매수호가8',
    bidp9                        BIGINT(20)            NULL COMMENT '매수호가9',
    bidp10                       BIGINT(20)            NULL COMMENT '매수호가10',
    askp_rsqn1                   BIGINT(20)            NULL COMMENT '매도호가 잔량1',
    askp_rsqn2                   BIGINT(20)            NULL COMMENT '매도호가 잔량2',
    askp_rsqn3                   BIGINT(20)            NULL COMMENT '매도호가 잔량3',
    askp_rsqn4                   BIGINT(20)            NULL COMMENT '매도호가 잔량4',
    askp_rsqn5                   BIGINT(20)            NULL COMMENT '매도호가 잔량5',
    askp_rsqn6                   BIGINT(20)            NULL COMMENT '매도호가 잔량6',
    askp_rsqn7                   BIGINT(20)            NULL COMMENT '매도호가 잔량7',
    askp_rsqn8                   BIGINT(20)            NULL COMMENT '매도호가 잔량8',
    askp_rsqn9                   BIGINT(20)            NULL COMMENT '매도호가 잔량9',
    askp_rsqn10                  BIGINT(20)            NULL COMMENT '매도호가 잔량10',
    bidp_rsqn1                   BIGINT(20)            NULL COMMENT '매수호가 잔량1',
    bidp_rsqn2                   BIGINT(20)            NULL COMMENT '매수호가 잔량2',
    bidp_rsqn3                   BIGINT(20)            NULL COMMENT '매수호가 잔량3',
    bidp_rsqn4                   BIGINT(20)            NULL COMMENT '매수호가 잔량4',
    bidp_rsqn5                   BIGINT(20)            NULL COMMENT '매수호가 잔량5',
    bidp_rsqn6                   BIGINT(20)            NULL COMMENT '매수호가 잔량6',
    bidp_rsqn7                   BIGINT(20)            NULL COMMENT '매수호가 잔량7',
    bidp_rsqn8                   BIGINT(20)            NULL COMMENT '매수호가 잔량8',
    bidp_rsqn9                   BIGINT(20)            NULL COMMENT '매수호가 잔량9',
    bidp_rsqn10                  BIGINT(20)            NULL COMMENT '매수호가 잔량10',
    w52_hgpr                     BIGINT(20)            NULL COMMENT '52주 최고가',
    w52_hgpr_date                BIGINT(20)            NULL COMMENT '52주 최고가 일자',
    w52_lwpr                     BIGINT(20)            NULL COMMENT '52주 최저가',
    w52_lwpr_date                BIGINT(20)            NULL COMMENT '52주 최저가 일자',
    fcam                         DOUBLE                NULL COMMENT '액면가',
    sspr                         BIGINT(20)            NULL COMMENT '대용가',
    sdpr                         BIGINT(20)            NULL COMMENT '기준가',
    lstn_stcn                    BIGINT(20)            NULL COMMENT '상장 주수 (단위 : 주)',
    new_mkop_cls_code            VARCHAR(2)            NULL COMMENT '신 장운영 구분 코드(첫번째비트-0: 예상 1:장개시전 2:장중 3:장종료후 4:시간외단일가/두번째비트-0:보통 1:종가 2:대량 3:바스켓, 7:정리매매)',
    frgn_hldn_rate               DOUBLE                NULL COMMENT '외국인보유비율',
    eng_isnm                     VARCHAR(40)           NULL COMMENT '영문종목명',
    flng_cls_code                VARCHAR(2)            NULL COMMENT '락구분코드',
    fcam_mod_cls_code            VARCHAR(2)            NULL COMMENT '액면가변경구분코드',
    oprc_sdpr_issu_yn            VARCHAR(1)            NULL COMMENT '시가기준가종목여부',
    revl_issu_reas_code          VARCHAR(2)            NULL COMMENT '재평가종목사유코드',
    sdpr_mod_issu_yn             VARCHAR(2)            NULL COMMENT '기준가변경종목여부',
    mrkt_alrm_cls_code           VARCHAR(2)            NULL COMMENT '시장경고구분코드',
    mang_issu_yn                 VARCHAR(1)            NULL COMMENT '관리종목여부',
    insn_pbnt_yn                 VARCHAR(1)            NULL COMMENT '불성실공시여부',
    byps_lstn_yn                 VARCHAR(1)            NULL COMMENT '우회상장여부',
    trht_yn                      VARCHAR(1)            NULL COMMENT '거래정지여부',
    bstp_larg_div_code           VARCHAR(3)            NULL COMMENT '업종대분류코드',
    bstp_medm_div_code           VARCHAR(3)            NULL COMMENT '업종중분류코드',
    bstp_smal_div_code           VARCHAR(3)            NULL COMMENT '업종소분류코드',
    kospi200_apnt_cls_code       VARCHAR(1)            NULL COMMENT 'KOSPI200 채용구분코드',
    krx100_issu_yn               VARCHAR(1)            NULL COMMENT 'KRX100 종목여부',
    kospi_issu_yn                VARCHAR(1)            NULL COMMENT 'KOSPI 종목여부',
    kospi100_issu_yn             VARCHAR(1)            NULL COMMENT 'KOSPI100 종목여부',
    kospi50_issu_yn              VARCHAR(1)            NULL COMMENT 'KOSPI50 종목여부',
    prdy_cls_code                VARCHAR(1)            NULL COMMENT '전일종가구분코드',
    hts_stck_vltn_prc            BIGINT(20)            NULL COMMENT '평가가격',
    stck_pblc_prc                BIGINT(20)            NULL COMMENT '발행가격',
    dvdn_ert                     DOUBLE                NULL COMMENT '배당수익률',
    acpr                         DOUBLE                NULL COMMENT '행사가',
    cpfn                         DOUBLE                NULL COMMENT '자본금',
    crdt_oder_able_yn            VARCHAR(1)            NULL COMMENT '신용주문가능여부',
    lmts_aspr_cond_cls_code      VARCHAR(1)            NULL COMMENT '지정가호가조건구분코드',
    mrpr_aspr_cond_cls_code      VARCHAR(1)            NULL COMMENT '시장가호가조건구분코드',
    cnlm_aspr_cond_cls_code      VARCHAR(1)            NULL COMMENT '조건부지정가호가조건구분코드',
    bslp_aspr_cond_cls_code      VARCHAR(1)            NULL COMMENT '최유리지정가호가조건구분코드',
    pmpr_lmts_aspr_cond_cls_code VARCHAR(1)            NULL COMMENT '최우선지정가호가조건구분코드',
    icic_cls_code                VARCHAR(2)            NULL COMMENT '증자구분코드',
    prst_cls_code                VARCHAR(2)            NULL COMMENT '우선주구분코드',
    ntst_yn                      VARCHAR(1)            NULL COMMENT '국민주여부',
    stck_lwst_aspr_prc           BIGINT(20)            NULL COMMENT '주식 최저 호가 가격',
    stck_hghs_aspr_prc           BIGINT(20)            NULL COMMENT '주식 최고 호가 가격',
    frml_mrkt_deal_qty_unit      BIGINT(20)            NULL COMMENT '정규시장매매수량단위',
    ovtm_mrkt_deal_qty_unit      BIGINT(20)            NULL COMMENT '시간외시장매매수량단위',
    stck_reit_cls_code           VARCHAR(1)            NULL COMMENT '리츠구분코드',
    objt_stnd_iscd               VARCHAR(12)           NULL COMMENT '목적표준종목코드',
    lp_oder_able_yn              VARCHAR(10)           NULL COMMENT 'LP주문가능여부',
    mkon_bef_ovtm_clpr_able_yn   VARCHAR(1)            NULL COMMENT '장개시전시간외종가가능여부',
    mkon_bef_ovtm_bltr_able_yn   VARCHAR(1)            NULL COMMENT '장개시전시간외대랑매매가능여부',
    mkon_bef_ovtm_bskt_able_yn   VARCHAR(1)            NULL COMMENT '장개시전시간외바스켓가능여부',
    regl_s_yn                    VARCHAR(1)            NULL COMMENT 'Regulation_S적용종목여부',
    etpr_undt_objt_co_yn         VARCHAR(1)            NULL COMMENT '기업인수목적회사여부',
    etf_crcl_stcn                BIGINT(20)            NULL COMMENT 'ETF 유통주수',
    prdy_stas_stnd_prc           DOUBLE                NULL COMMENT '전일과표기기준고가격',
    prdy_dvdn_bef_stas_stnd_prc  DOUBLE                NULL COMMENT '전일배당전과표기준가격',
    prdy_cash_dvdn_amt           DOUBLE                NULL COMMENT '전일현금배당금액',
    d2_bef_stas_stnd_proc        DOUBLE                NULL COMMENT '전전일과표기준가격',
    sspr_assm_rate               DOUBLE                NULL COMMENT '대용가사정비율',
    warn_yn                      VARCHAR(1)            NULL COMMENT '투자주의환기종목여부',
    txtn_type_code               VARCHAR(20)           NULL COMMENT '과세유형',
    update_date                  DATETIME              NULL COMMENT '수정일',
    create_date                  DATETIME              NULL COMMENT '등록일',
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX iscd (`iscd`) USING BTREE,
    INDEX kor_isnm (`kor_isnm`) USING BTREE,
    INDEX stnd_iscd (`stnd_iscd`) USING BTREE,
    UNIQUE KEY jsise_iscd_bsop_unique (`iscd`, `bsop_date`)
)
COMMENT = '주식 시세'
COLLATE = 'utf8mb4_general_ci'
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `em_stock`.`investor_trends`
(
    idx                                 BIGINT(20) AUTO_INCREMENT COMMENT '종목별 투자자 동향 idx',
    iscd                                VARCHAR(9) NOT NULL COMMENT '종목 코드',
    investor_date                       VARCHAR(8) NOT NULL COMMENT '종목별 투자자 동향 조회 날짜',
    net_foreign_buying_volume           BIGINT(20)     NULL COMMENT '외국인 순매수 수량',
    foreign_buy_volume                  BIGINT(20)     NULL COMMENT '외국인 매수(수량)',
    foreign_sell_volume                 BIGINT(20)     NULL COMMENT '외국인 매도(수량)',
    foreign_net_buy_amount              BIGINT(20)     NULL COMMENT '외국인 순매수(금액)',
    foreign_buy_amount                  BIGINT(20)     NULL COMMENT '외국인 매수(금액)',
    foreign_sell_amount                 BIGINT(20)     NULL COMMENT '외국인 매도(금액)',
    individual_net_buy_volume           BIGINT(20)     NULL COMMENT '개인 순매수(수량)',
    individual_buy_volume               BIGINT(20)     NULL COMMENT '개인 매수(수량)',
    individual_sell_volume              BIGINT(20)     NULL COMMENT '개인 매도(수량)',
    individual_net_buy_amount           BIGINT(20)     NULL COMMENT '개인 순매수(금액)',
    individual_buy_amount               BIGINT(20)     NULL COMMENT '개인 매수(금액)',
    individual_sell_amount              BIGINT(20)     NULL COMMENT '개인 매도(금액)',
    institutional_net_buy_volume        BIGINT(20)     NULL COMMENT '기관계 순매수(수량)',
    institutional_buy_volume            BIGINT(20)     NULL COMMENT '기관계 매수(수량)',
    institutional_sell_volume           BIGINT(20)     NULL COMMENT '기관계 매도(수량)',
    institutional_net_buy_amount        BIGINT(20)     NULL COMMENT '기관계 순매수(금액)',
    institutional_buy_amount            BIGINT(20)     NULL COMMENT '기관계 매수(금액)',
    institutional_sell_amount           BIGINT(20)     NULL COMMENT '기관계 매도(금액)',
    investment_trust_net_buy_volume     BIGINT(20)     NULL COMMENT '투신 순매수(수량)',
    investment_trust_buy_volume         BIGINT(20)     NULL COMMENT '투신 매수(수량)',
    investment_trust_sell_volume        BIGINT(20)     NULL COMMENT '투신 매도(수량)',
    investment_trust_net_buy_amount     BIGINT(20)     NULL COMMENT '투신 순매수(금액)',
    investment_trust_buy_amount         BIGINT(20)     NULL COMMENT '투신 매수(금액)',
    investment_trust_sell_amount        BIGINT(20)     NULL COMMENT '투신 매도(금액)',
    private_equity_net_buy_volume       BIGINT(20)     NULL COMMENT '사모펀드 순매수(수량)',
    private_equity_buy_volume           BIGINT(20)     NULL COMMENT '사모펀드 매수(수량)',
    private_equity_sell_volume          BIGINT(20)     NULL COMMENT '사모펀드 매도(수량)',
    private_equity_net_buy_amount       BIGINT(20)     NULL COMMENT '사모펀드 순매수(금액)',
    private_equity_buy_amount           BIGINT(20)     NULL COMMENT '사모펀드 매수(금액)',
    private_equity_sell_amount          BIGINT(20)     NULL COMMENT '사모펀드 매도(금액)',
    financial_investment_net_buy_volume BIGINT(20)     NULL COMMENT '금융투자 순매수(수량)',
    financial_investment_buy_volume     BIGINT(20)     NULL COMMENT '금융투자 매수(수량)',
    financial_investment_sell_volume    BIGINT(20)     NULL COMMENT '금융투자 매도(수량)',
    financial_investment_net_buy_amount BIGINT(20)     NULL COMMENT '금융투자 순매수(금액)',
    financial_investment_buy_amount     BIGINT(20)     NULL COMMENT '금융투자 매수(금액)',
    financial_investment_sell_amount    BIGINT(20)     NULL COMMENT '금융투자 매도(금액)',
    insurance_net_buy_volume            BIGINT(20)     NULL COMMENT '보험 순매수(수량)',
    insurance_buy_volume                BIGINT(20)     NULL COMMENT '보험 매수(수량)',
    insurance_sell_volume               BIGINT(20)     NULL COMMENT '보험 매도(수량)',
    insurance_net_buy_amount            BIGINT(20)     NULL COMMENT '보험 순매수(금액)',
    insurance_buy_amount                BIGINT(20)     NULL COMMENT '보험 매수(금액)',
    insurance_sell_amount               BIGINT(20)     NULL COMMENT '보험 매도(금액)',
    bank_net_buy_volume                 BIGINT(20)     NULL COMMENT '은행 순매수(수량)',
    bank_buy_volume                     BIGINT(20)     NULL COMMENT '은행 매수(수량)',
    bank_sell_volume                    BIGINT(20)     NULL COMMENT '은행 매도(수량)',
    bank_net_buy_amount                 BIGINT(20)     NULL COMMENT '은행 순매수(금액)',
    bank_buy_amount                     BIGINT(20)     NULL COMMENT '은행 매수(금액)',
    bank_sell_amount                    BIGINT(20)     NULL COMMENT '은행 매도(금액)',
    other_financial_net_buy_volume      BIGINT(20)     NULL COMMENT '기타금융 순매수(수량)',
    other_financial_buy_volume          BIGINT(20)     NULL COMMENT '기타금융 매수(수량)',
    other_financial_sell_volume         BIGINT(20)     NULL COMMENT '기타금융 매도(수량)',
    other_financial_net_buy_amount      BIGINT(20)     NULL COMMENT '기타금융 순매수(금액)',
    other_financial_buy_amount          BIGINT(20)     NULL COMMENT '기타금융 매수(금액)',
    other_financial_sell_amount         BIGINT(20)     NULL COMMENT '기타금융 매도(금액)',
    pension_funds_net_buy_volume        BIGINT(20)     NULL COMMENT '연기금 등 순매수(수량)',
    pension_funds_buy_volume            BIGINT(20)     NULL COMMENT '연기금 등 매수(수량)',
    pension_funds_sell_volume           BIGINT(20)     NULL COMMENT '연기금 등 매도(수량)',
    pension_funds_net_buy_amount        BIGINT(20)     NULL COMMENT '연기금 등 순매수(금액)',
    pension_funds_buy_amount            BIGINT(20)     NULL COMMENT '연기금 등 매수(금액)',
    pension_funds_sell_amount           BIGINT(20)     NULL COMMENT '연기금 등 매도(금액)',
    other_corporations_net_buy_volume   BIGINT(20)     NULL COMMENT '기타법인 순매수(수량)',
    other_corporations_buy_volume       BIGINT(20)     NULL COMMENT '기타법인 매수(수량)',
    other_corporations_sell_volume      BIGINT(20)     NULL COMMENT '기타법인 매도(수량)',
    other_corporations_net_buy_amount   BIGINT(20)     NULL COMMENT '기타법인 순매수(금액)',
    other_corporations_buy_amount       BIGINT(20)     NULL COMMENT '기타법인 매수(금액)',
    other_corporations_sell_amount      BIGINT(20)     NULL COMMENT '기타법인 매도(금액)',
    domestic_and_foreign_net_buy_volume BIGINT(20)     NULL COMMENT '내외국인 순매수(수량)',
    domestic_and_foreign_buy_volume     BIGINT(20)     NULL COMMENT '내외국인 매수(수량)',
    domestic_and_foreign_sell_volume    BIGINT(20)     NULL COMMENT '내외국인 매도(수량)',
    domestic_and_foreign_net_buy_amount BIGINT(20)     NULL COMMENT '내외국인 순매수(금액)',
    domestic_and_foreign_buy_amount     BIGINT(20)     NULL COMMENT '내외국인 매수(금액)',
    domestic_and_foreign_sell_amount    BIGINT(20)     NULL COMMENT '내외국인 매도(금액)',
    update_date                         DATETIME       NULL COMMENT '수정일',
    create_date                         DATETIME       NULL COMMENT '생성일',
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `investor_trends_invest_date_index` (`investor_date`) USING BTREE,
    INDEX `investor_trends_jstock_jong_iscd_fk` (`iscd`) USING BTREE
)
COMMENT = '종목별 투자자 동향 정보'
COLLATE = 'utf8mb4_general_ci'
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `em_stock`.`finance_balance_sheet`
(
    idx                        BIGINT(20) AUTO_INCREMENT COMMENT 'B/S 재무항목 idx',
    iscd                       VARCHAR(9) NULL COMMENT '단축종목코드',
    financial_reporting_period VARCHAR(2) NULL COMMENT '결산구분(13: 결산, 14: 반기, 15:분기)',
    consolidated               VARCHAR(1) NULL COMMENT '연결구분코드(1: 연결재무, 2: 별도재무)',
    fiscal_year_end            INT(11)    NULL COMMENT '결산년월',
    total_assets               INT(11)    NULL COMMENT '자산총계',
    total_equity               INT(11)    NULL COMMENT '자본총계',
    total_liabilities          INT(11)    NULL COMMENT '부채총계',
    create_date                DATETIME   NULL,
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `finance_balance_sheet_consolidated_index` (`consolidated`) USING BTREE,
    INDEX `finance_balance_sheet_financial_reporting_period_index` (`financial_reporting_period`) USING BTREE,
    INDEX `finance_balance_sheet_fiscal_year_end_index` (`fiscal_year_end`) USING BTREE,
    INDEX `finance_balance_sheet_indexes` (`iscd`, `fiscal_year_end`, `financial_reporting_period`, `consolidated`) USING BTREE,
    INDEX `finance_balance_sheet_iscd_index` (`iscd`) USING BTREE
)
COMMENT = 'B/S 재무항목'
COLLATE = 'utf8mb4_general_ci'
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `em_stock`.`finance_cash_flow`
(
    idx                        BIGINT(20) AUTO_INCREMENT COMMENT '현금흐름 재무항목 idx',
    iscd                       VARCHAR(9)           NULL COMMENT '단축종목코드',
    financial_reporting_period VARCHAR(2)           NULL COMMENT '결산구분(13: 결산, 14: 반기, 15:분기)',
    consolidated               VARCHAR(1)           NULL COMMENT '연결구분코드(1: 연결재무, 2: 별도재무)',
    fiscal_year_end            INT(11)              NULL COMMENT '결산년월',
    operating_cash_flow        INT(11)              NULL COMMENT '영업현금흐름',
    investing_cash_flow        INT(11)              NULL COMMENT '투자현금흐름',
    financing_cash_flow        INT(11)              NULL COMMENT '재무현금흐름',
    create_date                DATETIME             NULL,
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `finance_cash_flow_consolidated_index` (`consolidated`) USING BTREE,
    INDEX `finance_cash_flow_financial_reporting_period_index` (`financial_reporting_period`) USING BTREE,
    INDEX `finance_cash_flow_fiscal_year_end_index` (`fiscal_year_end`) USING BTREE,
    INDEX `finance_cash_flow_indexes` (`iscd`, `financial_reporting_period`, `consolidated`, `fiscal_year_end`) USING BTREE,
    INDEX `finance_cash_flow_iscd_index` (`iscd`) USING BTREE
)
COMMENT = '현금흐름 재무항목'
COLLATE = 'utf8mb4_general_ci'
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `em_stock`.`finance_growth_analysis`
(
    idx                           BIGINT(20) AUTO_INCREMENT COMMENT '성장성 분석 idx',
    iscd                          VARCHAR(9)           NULL COMMENT '단축종목코드',
    financial_reporting_period    VARCHAR(2)           NULL COMMENT '결산구분(13: 결산, 14: 반기, 15:분기)',
    consolidated                  VARCHAR(1)           NULL COMMENT '연결구분코드(1: 연결재무, 2: 별도재무)',
    fiscal_year_end               INT(20)              NULL COMMENT '결산년월',
    revenue_growth_rate           FLOAT                NULL COMMENT '매출액 증가율',
    operating_profit_growth_rate  FLOAT                NULL COMMENT '영업이익 증가율',
    pretax_income_growth_rate     FLOAT                NULL COMMENT '세전계속사업이익 증가율',
    net_income_growth_rate        FLOAT                NULL COMMENT '순이익 증가율',
    eps_growth_rate               FLOAT                NULL COMMENT 'EPS 증가율',
    ebitda_growth_rate            FLOAT                NULL COMMENT 'EBITDA 증가율',
    capital_change_rate           FLOAT                NULL COMMENT '자본금 증감률',
    total_asset_change_rate       FLOAT                NULL COMMENT '총자산 증감률',
    total_equity_change_rate      FLOAT                NULL COMMENT '총자본 증감률',
    total_liabilities_change_rate FLOAT                NULL COMMENT '총부채 증감률',
    profit_to_revenue_ratio       FLOAT                NULL COMMENT '매출액 이익률',
    current_assets_high           INT(11)              NULL COMMENT '유동자산 상위',
    fixed_assets_high             INT(11)              NULL COMMENT '고정자산 상위',
    current_liabilities_low       INT(11)              NULL COMMENT '유동부채 하위',
    fixed_liabilities_low         INT(11)              NULL COMMENT '고정부채 하위',
    create_date                   DATETIME             NULL,
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `finance_growth_analysis_consolidated_index` (`consolidated`) USING BTREE,
    INDEX `finance_growth_analysis_financial_reporting_period_index` (`financial_reporting_period`) USING BTREE,
    INDEX `finance_growth_analysis_fiscal_year_end_index` (`fiscal_year_end`) USING BTREE,
    INDEX `finance_growth_analysis_indexes` (`fiscal_year_end`, `iscd`, `consolidated`, `financial_reporting_period`) USING BTREE,
    INDEX `finance_growth_analysis_iscd_index` (`iscd`) USING BTREE
)
COMMENT '성장성 분석'
COLLATE = 'utf8mb4_general_ci'
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `em_stock`.`finance_other_analysis`
(
    idx                        BIGINT(20) AUTO_INCREMENT COMMENT '기타 재무 분석 idx',
    iscd                       VARCHAR(9)           NULL COMMENT '단축종목코드',
    financial_reporting_period VARCHAR(2)           NULL COMMENT '결산구분(13: 결산, 14: 반기, 15:분기)',
    consolidated               VARCHAR(1)           NULL COMMENT '연결구분코드(1: 연결재무, 2: 별도재무)',
    fiscal_year_end            INT(11)              NULL COMMENT '결산년월',
    revenue                    INT(11)              NULL COMMENT '매출액',
    gross_profit               INT(11)              NULL COMMENT '매출총(순영업)이익',
    operating_income           INT(11)              NULL COMMENT '영업이익',
    pretax_income              INT(11)              NULL COMMENT '세전계속사업이익',
    net_income                 INT(11)              NULL COMMENT '순이익',
    create_date                DATETIME             NULL,
    PRIMARY KEY (`idx`) USING BTREE,
    index `finance_other_analysis_consolidated_index` (`consolidated`) USING BTREE,
    index `finance_other_analysis_financial_reporting_period_index` (`financial_reporting_period`) USING BTREE,
    index `finance_other_analysis_fiscal_year_end_index` (`fiscal_year_end`) USING BTREE,
    index `finance_other_analysis_indexes` (`iscd`, `financial_reporting_period`, `consolidated`, `fiscal_year_end`) USING BTREE,
    index `finance_other_analysis_iscd_index` (`iscd`) USING BTREE
)
COMMENT = '기타 재무 분석'
COLLATE = 'utf8mb4_general_ci'
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `em_stock`.`finance_profit_loss`
(
    idx                        BIGINT(20) AUTO_INCREMENT COMMENT 'PL 재무항목 idx',
    iscd                       VARCHAR(9)           NULL COMMENT '단축종목코드',
    financial_reporting_period VARCHAR(2)           NULL COMMENT '결산구분(13: 결산, 14: 반기, 15:분기)',
    consolidated               VARCHAR(1)           NULL COMMENT '연결구분코드(1: 연결재무, 2: 별도재무)',
    fiscal_year_end            INT(11)              NULL COMMENT '결산년월',
    depreciation               INT(11)              NULL COMMENT '감가상각비',
    interest_expense           INT(11)              NULL COMMENT '이자비용',
    interest_income            INT(11)              NULL COMMENT '이자수익',
    equity_method_gain         INT(11)              NULL COMMENT '지분법평가이익',
    equity_method_loss         INT(11)              NULL COMMENT '지분법평가손익',
    create_date                DATETIME             NULL,
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `finance_profit_loss_consolidated_index` (`consolidated`) USING BTREE,
    INDEX `finance_profit_loss_financial_reporting_period_index` (`financial_reporting_period`) USING BTREE,
    INDEX `finance_profit_loss_fiscal_year_end_index` (`fiscal_year_end`) USING BTREE,
    INDEX `finance_profit_loss_indexes` (`iscd`, `fiscal_year_end`, `consolidated`, `financial_reporting_period`) USING BTREE,
    INDEX `finance_profit_loss_iscd_index` (`iscd`) USING BTREE
)
COMMENT = 'P/L 재무항목'
COLLATE = 'utf8mb4_general_ci'
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `em_stock`.`finance_profitability_analysis`
(
    idx                        BIGINT(20) AUTO_INCREMENT COMMENT '수익성 분석 idx',
    iscd                       VARCHAR(9)           NULL COMMENT '단축종목코드',
    financial_reporting_period VARCHAR(2)           NULL COMMENT '결산구분(13: 결산, 14: 반기, 15:분기)',
    fiscal_year_end            INT(11)              NULL COMMENT '결산년월',
    consolidated               VARCHAR(1)           NULL COMMENT '연결구분코드(1: 연결재무, 2: 별도재무)',
    operating_profit_margin    FLOAT                NULL COMMENT '영업이익률',
    pretax_income_margin       FLOAT                NULL COMMENT '세전계속사업이익률',
    net_profit_margin          FLOAT                NULL COMMENT '순이익률',
    roa                        FLOAT                NULL COMMENT '총자산이익율',
    ebitda_margin              INT(11)              NULL COMMENT 'EBITDA 마진율',
    total_asset_turnover       FLOAT                NULL COMMENT '총자산회전율',
    return_on_equity           FLOAT                NULL COMMENT '자기자본 순이익률(ROE)',
    profit_loss_reversal_rank  FLOAT                NULL COMMENT '흑자/적자 전환 순위(분기)',
    retention_ratio            FLOAT                NULL COMMENT '유보율',
    create_date                DATETIME             NULL,
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `finance_profitability_analysis_indexes` (`iscd`, `fiscal_year_end`, `financial_reporting_period`, `consolidated`) USING BTREE,
    INDEX `profitability_analysis_consolidated_index` (`consolidated`) USING BTREE,
    INDEX `profitability_analysis_financial_reporting_period_index` (`financial_reporting_period`) USING BTREE,
    INDEX `profitability_analysis_fiscal_year_end_index` (`fiscal_year_end`) USING BTREE,
    INDEX `profitability_analysis_iscd_index` (`iscd`) USING BTREE
)
COMMENT = '수익성 분석'
COLLATE = 'utf8mb4_general_ci'
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `em_stock`.`finance_stability_analysis`
(
    idx                                  BIGINT(20) AUTO_INCREMENT COMMENT '안정성 분석 idx',
    iscd                                 VARCHAR(9)           NULL COMMENT '단축종목코드',
    financial_reporting_period           VARCHAR(2)           NULL COMMENT '결산구분(13: 결산, 14: 반기, 15:분기)',
    consolidated                         VARCHAR(1)           NULL COMMENT '연결구분코드(1: 연결재무, 2: 별도재무)',
    fiscal_year_end                      INT(11)              NULL COMMENT '결산년월',
    debt_ratio                           FLOAT                NULL COMMENT '부채비율',
    financial_cost_burden_rate           FLOAT                NULL COMMENT '금융비용부담율',
    short_term_borrowing_ratio           FLOAT                NULL COMMENT '단기 차입 비율',
    debt_dependency                      FLOAT                NULL COMMENT '차입금 의존도',
    accounts_receivable_to_revenue_ratio FLOAT                NULL COMMENT '매출채권/매출액 비율',
    inventory_to_revenue_ratio           FLOAT                NULL COMMENT '재고자산/매출액 비율',
    interest_coverage_ratio              FLOAT                NULL COMMENT '이자보상 배율',
    net_debt                             INT(11)              NULL COMMENT '순차입금',
    short_term_debt                      INT(11)              NULL COMMENT '단기성 차입금',
    operating_income_to_interest_ratio   FLOAT                NULL COMMENT '영업이익 이자보상 비율',
    create_date                          DATETIME             NULL,
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `finance_stability_analysis_consolidated_index` (`consolidated`) USING BTREE,
    INDEX `finance_stability_analysis_financial_reporting_period_index` (`financial_reporting_period`) USING BTREE,
    INDEX `finance_stability_analysis_fiscal_year_end_index` (`fiscal_year_end`) USING BTREE,
    INDEX `finance_stability_analysis_indexes` (`iscd`, `fiscal_year_end`, `financial_reporting_period`, `consolidated`) USING BTREE,
    INDEX `finance_stability_analysis_iscd_index` (`iscd`) USING BTREE
)
COMMENT = '안정성 분석'
COLLATE = 'utf8mb4_general_ci'
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `em_stock`.`finance_stock_analysis`
(
    idx                        BIGINT(20) AUTO_INCREMENT COMMENT '주가분석 번호',
    financial_reporting_period VARCHAR(2)           NULL COMMENT '결산구분(13: 결산, 14: 반기, 15:분기)',
    consolidated               VARCHAR(1)           NULL COMMENT '연결구분코드(1: 연결재무, 2: 별도재무)',
    fiscal_year_end            INT(11)              NULL COMMENT '결산년월(yyyyMMdd)',
    iscd                       VARCHAR(9)           NULL COMMENT '단축종목코드',
    per                        FLOAT                NULL COMMENT 'PER',
    pbr                        FLOAT                NULL COMMENT 'PBR',
    eps                        FLOAT                NULL COMMENT '주당 순이익(지배)',
    ev_ebitda                  FLOAT                NULL COMMENT 'EV/EBITDA',
    psr                        FLOAT                NULL COMMENT 'PSR',
    pcr                        FLOAT                NULL COMMENT 'PCR',
    peg                        FLOAT                NULL COMMENT 'PEG',
    dividend_yield             FLOAT                NULL COMMENT '배당수익률',
    bps                        INT(11)              NULL COMMENT '주당 순자산(지배)',
    sps                        INT(11)              NULL COMMENT '주당 매출액',
    cfps                       INT(11)              NULL COMMENT '주당 현금흐름',
    market_cap                 BIGINT(11)           NULL COMMENT '시가총액',
    share_capital              BIGINT(11)           NULL COMMENT '자본금',
    eva                        FLOAT                NULL COMMENT 'EVA',
    create_date                DATETIME             NULL,
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `finance_stock_analysis_consolidated_index` (`consolidated`) USING BTREE,
    INDEX `finance_stock_analysis_financial_reporting_period_index` (`financial_reporting_period`) USING BTREE,
    INDEX `finance_stock_analysis_indexes` (`iscd`, `financial_reporting_period`, `consolidated`, `fiscal_year_end`) USING BTREE,
    INDEX `stock_analysis_fiscal_year_end_index` (`fiscal_year_end`) USING BTREE,
    INDEX `stock_analysis_iscd_index` (`iscd`) USING BTREE
)
COMMENT = '주가 분석'
COLLATE = 'utf8mb4_general_ci'
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `em_stock`.`theme_category`
(
    idx             BIGINT(20)          AUTO_INCREMENT  COMMENT '테마 카테고리 번호',
    theme_name      VARCHAR(20)              NOT NULL   COMMENT '테마명(대분류)',
    theme_list      TEXT                         NULL   COMMENT '테마 목록명',
    create_date     DATETIME                     NULL   COMMENT '등록일',
    update_date     DATETIME                     NULL   COMMENT '수정일',
    PRIMARY KEY (`idx`) USING BTREE
)
COMMENT = '테마 카테고리'
COLLATE = 'utf8mb4_general_ci'
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `em_stock`.`theme_logos`
(
    idx         BIGINT(11) auto_increment comment '테마 로고 idx',
    theme_idx    BIGINT(11)  NOT NULL COMMENT '테마 idx',
    file_url    VARCHAR(500)     NULL COMMENT '로고 url',
    file_path   VARCHAR(500)     NULL COMMENT '로고 위치',
    file_name   VARCHAR(500)     NULL COMMENT '파일 원본이름',
    file_alias  VARCHAR(500)     NULL COMMENT '파일 별칭(테마idx+원본파일명)',
    file_size   BIGINT           NULL COMMENT '파일크기',
    update_date DATETIME         NULL COMMENT '수정일',
    create_date DATETIME         NULL COMMENT '등록일',
    constraint theme_logos_theme_code_fk foreign key (theme_idx) references theme_code (idx),
    PRIMARY KEY (`idx`) USING BTREE
)
COMMENT = '테마 로고'
COLLATE = 'utf8mb4_general_ci'
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `em_stock`.`ksic_company`
(
    idx             BIGINT(20) auto_increment    COMMENT '연계 idx',
    company_id      BIGINT(20)          NOT NULL COMMENT '기업 id',
    ksic_idx        BIGINT(20)              NULL COMMENT '산업분류코드 idx',
    iscd            VARCHAR(12)             NULL COMMENT '단축종목코드',
    update_date     DATETIME                NULL COMMENT '수정일',
    create_date     DATETIME                NULL COMMENT '등록일',
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `ksic_company_iscd_index` (`iscd`) USING BTREE,
    UNIQUE KEY ksic_company_iscd_unique (`iscd`)
)
COMMENT = '산업분류코드-기업 정보 연계 정보'
COLLATE = 'utf8mb4_general_ci'
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `em_stock`.`jsise_real_time`
(
    iscd         VARCHAR(9)  not null comment '단축 종목코드',
    bsop_date    BIGINT(20)  null comment '일자 (단위 : YYYYMMDD)',
    bsop_hour    BIGINT(20)  null comment '시간 (단위 : HHMMSS)',
    kor_isnm     VARCHAR(40) null comment '한글 종목명',
    prpr         BIGINT(20)  null comment '현재가',
    prdy_ctrt    DOUBLE      null comment '전일대비율(단위: %)',
    acml_vol     BIGINT(20)  null comment '누적 거래량(단위: 주)',
    acml_tr_pbmn BIGINT(20)  null comment '누적 거래 대금 (단위 : 원)',
    oprc         BIGINT(20)  null comment '시가',
    hgpr         BIGINT(20)  null comment '고가',
    lwpr         BIGINT(20)  null comment '저가',
    mxpr         BIGINT(20)  null comment '상한가',
    llam         BIGINT(20)  null comment '하한가',
    avls         BIGINT(20)  null comment '시가총액 (단위 : 원)',
    prdy_vol     BIGINT(20)  null comment '전일거래량 (단위 : 주)',
    trht_yn      VARCHAR(1)  null comment '거래정지여부',
    create_date  DATETIME    null comment '생성날짜',
    update_date  DATETIME    null comment '수정일',
    PRIMARY KEY (`iscd`) USING BTREE,
    INDEX `jsise_real_time_iscd_index` (`iscd`) USING BTREE,
    INDEX `jsise_real_time_kor_isnm_index` (`kor_isnm`) USING BTREE
)
COMMENT = '실시간 시세 정보'
COLLATE = 'utf8mb4_general_ci'
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `member` (
    `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '회원번호',
    `email` VARCHAR(255) NULL DEFAULT NULL COMMENT '이메일주소' COLLATE 'utf8mb4_general_ci',
    `password` VARCHAR(100) NULL DEFAULT NULL COMMENT '회원비밀번호' COLLATE 'utf8mb4_general_ci',
    `nickname` VARCHAR(128) NULL DEFAULT NULL COMMENT '회원닉네임' COLLATE 'utf8mb4_general_ci',
    `profile_image_url` VARCHAR(255) NULL DEFAULT NULL COMMENT '프로필이미지 URL' COLLATE 'utf8mb4_general_ci',
    `type_cd` VARCHAR(7) NULL DEFAULT NULL COMMENT '회원 구분(code 테이블)' COLLATE 'utf8mb4_general_ci',
    `state_cd` VARCHAR(7) NULL DEFAULT NULL COMMENT '회원 상태(code 테이블)' COLLATE 'utf8mb4_general_ci',
    `comment` VARCHAR(100) NULL DEFAULT NULL COMMENT '간략소개(최대 100자)' COLLATE 'utf8mb4_general_ci',
    `leave_date` DATETIME NULL DEFAULT NULL COMMENT '탈퇴일',
    `update_date` DATETIME NULL DEFAULT NULL COMMENT '수정일',
    `create_date` DATETIME NULL DEFAULT NULL COMMENT '가입일',
    PRIMARY KEY (`idx`) USING BTREE,
    UNIQUE INDEX `email` (`email`) USING BTREE,
    UNIQUE INDEX `nickname` (`nickname`) USING BTREE,
    INDEX `type_cd` (`type_cd`) USING BTREE,
    INDEX `state_cd` (`state_cd`) USING BTREE
)
COMMENT='회원 정보'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `member_term` (
    `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '약관번호',
    `subject` VARCHAR(255) NULL DEFAULT NULL COMMENT '약관제목' COLLATE 'utf8mb4_general_ci',
    `content` TEXT NULL DEFAULT NULL COMMENT '약관내용' COLLATE 'utf8mb4_general_ci',
    `active` BIT(1) NULL DEFAULT b'1' COMMENT '사용여부(1:사용, 0:미사용)',
    `required` BIT(1) NULL DEFAULT b'1' COMMENT '필수여부(1:필수, 0:선택)',
    `update_date` DATETIME NULL DEFAULT NULL COMMENT '수정일',
    `create_date` DATETIME NULL DEFAULT NULL COMMENT '등록일',
    PRIMARY KEY (`idx`) USING BTREE
)
COMMENT='약관 정보'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `member_term_agree` (
    `idx` INT(11) NOT NULL AUTO_INCREMENT,
    `member_idx` INT(11) NULL DEFAULT NULL COMMENT '회원번호',
    `member_term_idx` INT(11) NULL DEFAULT NULL COMMENT '약관번호',
    `agree` BIT(1) NULL DEFAULT b'1' COMMENT '동의 여부(1:동의, 0:미동의)',
    `update_date` DATETIME NULL DEFAULT NULL COMMENT '수정일',
    `create_date` DATETIME NULL DEFAULT NULL COMMENT '등록일',
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `term_agree_member_term_idx` (`member_term_idx`) USING BTREE,
    INDEX `term_agree_member_idx` (`member_idx`) USING BTREE,
    CONSTRAINT `member_term_agree_member_idx` FOREIGN KEY (`member_idx`) REFERENCES `member` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION,
    CONSTRAINT `member_term_agree_member_term_idx` FOREIGN KEY (`member_term_idx`) REFERENCES `member_term` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COMMENT='약관 동의 정보'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `member_login_history` (
    `idx` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '로그인이력번호',
    `member_idx` INT(11) NULL DEFAULT NULL COMMENT '회원번호',
    `browser` VARCHAR(255) NULL DEFAULT NULL COMMENT '접속 브라우저' COLLATE 'utf8mb4_general_ci',
    `platform` VARCHAR(255) NULL DEFAULT NULL COMMENT '접속 환경' COLLATE 'utf8mb4_general_ci',
    `ip` VARCHAR(15) NULL DEFAULT NULL COMMENT '접속 IP' COLLATE 'utf8mb4_general_ci',
    `create_date` DATETIME NULL DEFAULT NULL COMMENT '접속일',
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `member_login_history_member_idx` (`member_idx`) USING BTREE,
    CONSTRAINT `member_login_history_member_idx` FOREIGN KEY (`member_idx`) REFERENCES `member` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COMMENT='로그인 이력 정보'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `member_email_history` (
    `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '비밀번호변경이력번호',
    `member_idx` INT(11) NULL DEFAULT NULL COMMENT '회원번호',
    `cert_data` VARCHAR(255) NULL DEFAULT NULL COMMENT '인증키' COLLATE 'utf8mb4_general_ci',
    `used` BIT(1) NULL DEFAULT b'0' COMMENT '사용여부(1:사용, 0:미사용)',
    `email_type` VARCHAR(7) NULL DEFAULT NULL COMMENT '메일구분(code 테이블)' COLLATE 'utf8mb4_general_ci',
    `expire_date` DATETIME NULL DEFAULT NULL COMMENT '만료일',
    `create_date` DATETIME NULL DEFAULT NULL COMMENT '생성일',
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `member_email_history_member_idx` (`member_idx`) USING BTREE,
    INDEX `email_type` (`email_type`) USING BTREE,
    CONSTRAINT `member_email_history_member_idx` FOREIGN KEY (`member_idx`) REFERENCES `member` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COMMENT='회원 메일 전송 이력'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `margin_trading` (
    `idx`                              INT(20)      NOT NULL AUTO_INCREMENT COMMENT '신용거래현황 idx',
    `iscd`                             VARCHAR(6) NULL COMMENT '종목번호',
    `settlement_date`                  INT        NULL COMMENT '결제일자',
    `pre_cnt`                          BIGINT(20) NULL COMMENT '신용융자 전일대비수량',
    `margin_balance`                   BIGINT(20) NULL COMMENT '신용융자 잔고주수',
    `bln_amt_ctns`                     BIGINT(20) NULL COMMENT '신용융자 잔고금액(백만)',
    `bln_amt_stcn`                     BIGINT(20) NULL COMMENT '신용융자 신규주수',
    `bln_amt_payment`                  BIGINT(20) NULL COMMENT '신용융자 상환주수',
    `bln_contribution_ratio`           FLOAT      NULL COMMENT '전체융자 공여율(%)',
    `bln_balance_ratio`                FLOAT      NULL COMMENT '신용융자 잔고율(%)',
    `trade_date`                       INT        NULL COMMENT '매매일자',
    `prpr`                             BIGINT     NULL COMMENT '현재가(원)',
    `prdy_vrss`                        BIGINT     NULL COMMENT '전일 대비(원)',
    `prdy_ctrt`                        FLOAT      NULL COMMENT '전일 대비율 (단위 : %)',
    `acml_vol`                         BIGINT(20) NULL COMMENT '누적 거래량 (단위 : 주)',
    `prdy_vrss_cnt`                    BIGINT(20) NULL COMMENT '신용대주 전일대비수량',
    `short_sale_balance_shares`        BIGINT(20) NULL COMMENT '신용대주 잔고주수',
    `short_sale_balance_amount`        BIGINT(20) NULL COMMENT '신용대주 잔고금액',
    `new_short_sale_shares`            BIGINT(20) NULL COMMENT '신용대주 신규주수',
    `short_sale_repayment_shares`      BIGINT(20) NULL COMMENT '신용대주 상환주수',
    `total_lending_contribution_ratio` FLOAT      NULL COMMENT '전체대주 공여율',
    `create_date`                      DATETIME   NULL COMMENT '생성일',
    `update_date`                      DATETIME   NULL COMMENT '수정일',
    PRIMARY KEY (`idx`) USING BTREE,
    INDEX `margin_trading_iscd` (`iscd`) USING BTREE,
    INDEX `margin_trading_settlement_date` (`settlement_date`) USING BTREE
)
COMMENT= '신용거래현황 정보'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;