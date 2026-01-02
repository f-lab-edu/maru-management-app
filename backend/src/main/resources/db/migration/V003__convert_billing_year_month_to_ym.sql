-- billing_year, billing_month를 billing_ym (YYYYMM 형식)으로 통합

-- 기존 제약/인덱스 삭제
ALTER TABLE invoice DROP INDEX uk_invoice_billing;
DROP INDEX idx_invoice_billing ON invoice;

-- 컬럼 변경
ALTER TABLE invoice ADD COLUMN billing_ym INT NOT NULL;
ALTER TABLE invoice DROP COLUMN billing_year;
ALTER TABLE invoice DROP COLUMN billing_month;

-- 새 제약/인덱스 생성
ALTER TABLE invoice ADD CONSTRAINT uk_invoice_billing
    UNIQUE (tenant_id, dojang_id, student_id, billing_ym);
CREATE INDEX idx_invoice_billing ON invoice (tenant_id, dojang_id, billing_ym);
