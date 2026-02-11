CREATE TABLE payment_link (
    id VARCHAR(13) PRIMARY KEY,
    tenant_id VARCHAR(13) NOT NULL COMMENT '테넌트 ID',
    dojang_id VARCHAR(13) NOT NULL COMMENT '도장 ID',
    invoice_id VARCHAR(13) NOT NULL COMMENT '청구서 ID',
    token VARCHAR(64) NOT NULL UNIQUE COMMENT '결제 링크 토큰',
    expires_at TIMESTAMP NOT NULL COMMENT '만료 시각',
    used_at TIMESTAMP NULL COMMENT '사용 시각',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    CONSTRAINT fk_payment_link_invoice FOREIGN KEY (invoice_id) REFERENCES invoice(id),
    INDEX idx_payment_link_token (token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='결제 링크 (학부모 PG 결제용)';
