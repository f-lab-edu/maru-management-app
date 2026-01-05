-- ========================================
-- 인덱스 업데이트
-- ========================================
-- Version: V012
-- Description: Invoice 테이블 인덱스 최적화
-- Created: 2025-12-19
-- ========================================

-- INVOICE 테이블 인덱스 추가
CREATE INDEX idx_invoice_tenant_dojang_issue ON invoice (tenant_id, dojang_id, issue_date);
