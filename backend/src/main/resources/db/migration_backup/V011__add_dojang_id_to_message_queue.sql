-- MESSAGE_QUEUE 테이블에 dojang_id 컬럼 추가

ALTER TABLE message_queue
ADD COLUMN dojang_id BIGINT NOT NULL COMMENT '도장 ID' AFTER tenant_id;

ALTER TABLE message_queue
ADD CONSTRAINT fk_message_queue_dojang_id FOREIGN KEY (dojang_id) REFERENCES dojang (id) ON DELETE CASCADE;
