-- ========================================
-- Permission 테이블 → Employment JSON 컬럼 전환
-- ========================================
-- Version: V007
-- Description: Permission 별도 테이블을 Employment의 JSON 컬럼으로 전환
-- Created: 2025-12-01
-- ========================================

-- Employment 테이블에 permissions JSON 컬럼 추가
ALTER TABLE employment ADD COLUMN permissions JSON COMMENT '권한 목록 (JSON 배열)';

-- 기존 데이터에 빈 배열 설정
UPDATE employment SET permissions = '[]' WHERE permissions IS NULL;

-- Permission 테이블 삭제 (기존 데이터 없음)
DROP TABLE IF EXISTS permission;
