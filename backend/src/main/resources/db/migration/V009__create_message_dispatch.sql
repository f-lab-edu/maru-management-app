-- ========================================
-- V009: message_queue → message_dispatch 전환
-- ========================================
-- 1. 필드 확장 (channel, thin outbox, claim 지원)
-- 2. message_dispatch로 리네이밍
-- 3. message_dispatch_attempt 테이블 생성
-- ========================================

-- 1. 필드 추가
ALTER TABLE message_queue
    ADD COLUMN channel VARCHAR(30) NOT NULL DEFAULT 'SMS' COMMENT '발송 채널 (SMS, KAKAO_ALIMTALK, FCM_PUSH)' AFTER message_type,
    ADD COLUMN ref_type VARCHAR(30) COMMENT '참조 타입 (ATTENDANCE 등)' AFTER student_id,
    ADD COLUMN ref_id VARCHAR(13) COMMENT '참조 ID' AFTER ref_type,
    ADD COLUMN next_retry_at TIMESTAMP NULL COMMENT '다음 재시도 시각' AFTER sent_at,
    ADD COLUMN processing_owner VARCHAR(100) NULL COMMENT '처리 중인 워커 인스턴스 ID' AFTER next_retry_at,
    ADD COLUMN processing_started_at TIMESTAMP NULL COMMENT '처리 시작 시각' AFTER processing_owner,
    ADD COLUMN vendor_message_id VARCHAR(100) NULL COMMENT '벤더가 부여한 메시지 ID' AFTER processing_started_at,
    ADD COLUMN accepted_at TIMESTAMP NULL COMMENT '벤더 접수 시각' AFTER vendor_message_id;

-- 2. title, body를 nullable로 변경
ALTER TABLE message_queue
    MODIFY COLUMN title VARCHAR(200) NULL COMMENT '메시지 제목',
    MODIFY COLUMN body VARCHAR(500) NULL COMMENT '메시지 본문';

-- 3. 기존 데이터 마이그레이션
UPDATE message_queue SET next_retry_at = scheduled_at WHERE status = 'PENDING';
UPDATE message_queue SET accepted_at = sent_at WHERE status = 'SENT';
UPDATE message_queue SET status = 'ACCEPTED' WHERE status = 'SENT';
UPDATE message_queue SET status = 'DEAD' WHERE status = 'FAILED';

-- 4. student_id FK 및 컬럼 삭제
ALTER TABLE message_queue DROP FOREIGN KEY fk_message_queue_student_id;
ALTER TABLE message_queue DROP COLUMN student_id;

-- 5. 테이블 리네이밍
RENAME TABLE message_queue TO message_dispatch;

-- 6. 인덱스 재구성
DROP INDEX idx_message_queue_status_scheduled_at ON message_dispatch;
CREATE INDEX idx_md_claim ON message_dispatch (status, next_retry_at, id);
CREATE INDEX idx_md_guardian_status ON message_dispatch (guardian_id, status, created_at);
CREATE INDEX idx_md_ref ON message_dispatch (ref_type, ref_id);

-- 7. message_dispatch_attempt 테이블 생성
CREATE TABLE message_dispatch_attempt (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dispatch_id VARCHAR(13) NOT NULL COMMENT '메시지 발송 ID',
    attempt_number INT NOT NULL COMMENT '시도 번호 (1부터 시작)',
    channel VARCHAR(30) NOT NULL COMMENT '발송 채널',
    status VARCHAR(20) NOT NULL COMMENT '시도 상태 (IN_PROGRESS, SUCCESS, FAILED)',
    processing_owner VARCHAR(100) COMMENT '처리한 워커 인스턴스 ID',
    vendor_message_id VARCHAR(100) COMMENT '벤더가 부여한 메시지 ID',
    error_code VARCHAR(50) COMMENT '에러 코드 (RENDER_FAIL, SEND_EXCEPTION 등)',
    error_message VARCHAR(2000) COMMENT '에러 메시지',
    started_at TIMESTAMP NOT NULL COMMENT '시도 시작 시각',
    completed_at TIMESTAMP NULL COMMENT '시도 완료 시각',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '레코드 생성 시각',

    UNIQUE KEY uk_dispatch_attempt (dispatch_id, attempt_number),
    INDEX idx_mda_dispatch_id (dispatch_id),
    INDEX idx_mda_status_created (status, created_at),
    CONSTRAINT fk_mda_dispatch_id FOREIGN KEY (dispatch_id) REFERENCES message_dispatch(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='메시지 발송 시도 히스토리';
