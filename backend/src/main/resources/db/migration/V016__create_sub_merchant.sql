CREATE TABLE sub_merchant (
    id VARCHAR(13) PRIMARY KEY,
    tenant_id VARCHAR(13) NOT NULL COMMENT '테넌트 ID',
    dojang_id VARCHAR(13) NOT NULL UNIQUE COMMENT '도장 ID',
    toss_seller_id VARCHAR(100) UNIQUE COMMENT '토스 셀러 ID',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '서브몰 상태 (PENDING, ACTIVE, REJECTED, SUSPENDED)',
    fee_rate DECIMAL(5,4) COMMENT '수수료율',
    bank_code VARCHAR(10) COMMENT '은행 코드',
    account_number_enc VARCHAR(200) COMMENT '암호화된 계좌번호',
    account_holder VARCHAR(50) COMMENT '예금주',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    CONSTRAINT fk_sub_merchant_dojang FOREIGN KEY (dojang_id) REFERENCES dojang(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='서브몰 (지급대행 대상 도장)';
