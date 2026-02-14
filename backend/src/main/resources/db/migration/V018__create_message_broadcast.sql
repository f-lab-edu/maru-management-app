CREATE TABLE message_broadcast (
    id VARCHAR(13) PRIMARY KEY,
    tenant_id VARCHAR(13) NOT NULL,
    dojang_id VARCHAR(13) NOT NULL,
    sent_by_user_id VARCHAR(13) NOT NULL,
    title VARCHAR(100) NOT NULL,
    body VARCHAR(2000) NOT NULL,
    channel VARCHAR(20) NOT NULL DEFAULT 'SMS',
    recipient_type VARCHAR(20) NOT NULL,
    recipient_criteria JSON,
    total_count INT NOT NULL DEFAULT 0,
    skipped_count INT NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_broadcast_dojang_created (dojang_id, created_at DESC),
    INDEX idx_broadcast_status (status)
);
