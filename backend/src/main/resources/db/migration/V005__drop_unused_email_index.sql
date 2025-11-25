-- ========================================
-- 미사용 인덱스 제거
-- ========================================
-- Version: V005
-- Description: email 필드가 필수가 아니고 현재 검색에 사용되지 않아 인덱스 제거
-- Created: 2025-11-25
-- ========================================

-- email 인덱스 제거
-- 사유:
--   1. email은 선택 필드 (NULL 가능)
--   2. Kakao OAuth에서 email이 없을 수 있음
--   3. 현재 UserRepository에 email 검색 메서드 없음
--   4. OAuth 인증은 oauth_account 테이블로 처리됨
DROP INDEX idx_user_email ON users;
