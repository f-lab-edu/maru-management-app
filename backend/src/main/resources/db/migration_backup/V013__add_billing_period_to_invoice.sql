-- ========================================
-- Invoice 테이블 billingYear/billingMonth 필드 추가
-- ========================================
-- Version: V013
-- Description: 청구 연도/월 필드 추가 및 중복 청구서 방지
-- Created: 2025-12-27
-- ========================================

-- 1. issue_date NULL 허용 (DRAFT 상태에서는 null)
ALTER TABLE invoice MODIFY COLUMN issue_date DATE NULL COMMENT '발행일 (DRAFT 상태에서는 NULL)';

-- 2. billing_year, billing_month 컬럼 추가
ALTER TABLE invoice
    ADD COLUMN billing_year INT NOT NULL COMMENT '청구 연도' AFTER student_id,
    ADD COLUMN billing_month INT NOT NULL COMMENT '청구 월 (1-12)' AFTER billing_year;

-- 3. 복합 유니크 제약 (한 달에 청구서 단 하나)
ALTER TABLE invoice
    ADD CONSTRAINT uk_invoice_billing
    UNIQUE (tenant_id, dojang_id, student_id, billing_year, billing_month);

-- 4. 조회 성능용 인덱스
CREATE INDEX idx_invoice_billing
    ON invoice (tenant_id, dojang_id, billing_year, billing_month);
