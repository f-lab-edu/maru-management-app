-- ========================================
-- role 컬럼을 nullable로 변경
-- ========================================
-- Version: V003
-- Description: OAuth 온보딩 플로우를 위해 role 컬럼을 nullable로 변경
-- Created: 2025-11-19
-- ========================================

ALTER TABLE users MODIFY COLUMN role VARCHAR(20) NULL COMMENT '사용자 역할 (OWNER, INSTRUCTOR)';
