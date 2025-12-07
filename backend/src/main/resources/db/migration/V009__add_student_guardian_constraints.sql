-- ========================================
-- Student/Guardian/Guardianship 제약조건 추가
-- ========================================
-- Version: V009
-- Description: 원생/보호자 관련 UNIQUE 제약 추가
-- Created: 2025-12-05
-- ========================================


-- ===================================
-- 1. Guardian 전화번호 UNIQUE 제약
-- ===================================

ALTER TABLE guardian ADD CONSTRAINT uk_guardian_phone UNIQUE (phone);


-- ===================================
-- 2. Student 중복 방지 UNIQUE 제약
-- ===================================

ALTER TABLE student ADD CONSTRAINT uk_student_dojang_name_birth UNIQUE (dojang_id, name, birth);


-- ===================================
-- 3. Guardianship relation NOT NULL
-- ===================================

ALTER TABLE guardianship MODIFY COLUMN relation VARCHAR(20) NOT NULL;