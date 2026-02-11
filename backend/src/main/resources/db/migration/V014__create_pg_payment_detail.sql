CREATE TABLE pg_payment_detail (
    id VARCHAR(13) PRIMARY KEY,
    tenant_id VARCHAR(13) NOT NULL COMMENT '테넌트 ID',
    dojang_id VARCHAR(13) NOT NULL COMMENT '도장 ID',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PG 결제 상태 (PENDING, CONFIRMED, ORPHANED)',
    payment_id VARCHAR(13) UNIQUE COMMENT '수납 ID (승인 후 연결)',
    payment_key VARCHAR(200) NOT NULL UNIQUE COMMENT '토스 결제 키',
    order_id VARCHAR(64) NOT NULL UNIQUE COMMENT '주문 ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '결제 금액',
    pg_method VARCHAR(50) COMMENT 'PG 결제 수단 (카드, 계좌이체 등)',
    receipt_url VARCHAR(500) COMMENT '영수증 URL',
    settlement_amount DECIMAL(10,2) COMMENT '정산 금액',
    platform_fee DECIMAL(10,2) COMMENT '플랫폼 수수료',
    response_data JSON COMMENT '토스 응답 원본',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    CONSTRAINT fk_pg_detail_payment FOREIGN KEY (payment_id) REFERENCES payment(id),
    INDEX idx_pg_detail_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='PG 결제 상세 정보';
