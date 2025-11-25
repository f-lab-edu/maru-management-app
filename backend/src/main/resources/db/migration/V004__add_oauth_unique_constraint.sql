-- ========================================
-- oauth_account 의 provider, provider_id 컬럼에 유니크 제약조건 추가
-- ========================================
-- Version: V004
-- Description: OAuth 제공자와 제공자ID의 조합은 유일해야 한다. (중복 가입 방지)
-- Created: 2025-11-25
-- ========================================

ALTER TABLE oauth_account ADD CONSTRAINT uk_oauth_account_provider_id UNIQUE (provider, provider_id);
