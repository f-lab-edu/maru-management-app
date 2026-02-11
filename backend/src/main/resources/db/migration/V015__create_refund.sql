CREATE TABLE refund (
    id VARCHAR(13) PRIMARY KEY,
    tenant_id VARCHAR(13) NOT NULL COMMENT '테넌트 ID',
    dojang_id VARCHAR(13) NOT NULL COMMENT '도장 ID',
    payment_id VARCHAR(13) NOT NULL COMMENT '수납 ID',
    idempotency_key VARCHAR(64) NOT NULL UNIQUE COMMENT '멱등키',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '환불 상태 (PENDING, COMPLETED, FAILED)',
    amount DECIMAL(10,2) NOT NULL COMMENT '환불 금액',
    reason VARCHAR(200) COMMENT '환불 사유',
    pg_refund_key VARCHAR(200) COMMENT 'PG 환불 키',
    response_data JSON COMMENT 'PG 환불 응답 원본',
    completed_at TIMESTAMP NULL COMMENT '환불 완료 시각',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    CONSTRAINT fk_refund_payment FOREIGN KEY (payment_id) REFERENCES payment(id),
    INDEX idx_refund_payment_id (payment_id),
    INDEX idx_refund_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='환불 내역';
