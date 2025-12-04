-- ========================================
-- Student/Guardian/Guardianship 제약조건 추가
-- ========================================
-- Version: V009
-- Description: 원생/보호자 관련 UNIQUE 제약 및 주 보호자 단일 제약 추가
-- Created: 2025-12-05
-- ========================================


-- ===================================
-- 1. Guardian 전화번호 UNIQUE 제약
-- ===================================

-- UK-001: Guardian phone UNIQUE
-- 용도: 전화번호로 보호자 식별 (동일 전화번호로 중복 생성 방지)
ALTER TABLE guardian ADD CONSTRAINT uk_guardian_phone UNIQUE (phone);


-- ===================================
-- 2. Student 중복 방지 UNIQUE 제약
-- ===================================

-- UK-002: Student (dojang_id, name, birth) UNIQUE
-- 용도: 동일 도장 내 이름+생년월일 중복 원생 등록 방지
ALTER TABLE student ADD CONSTRAINT uk_student_dojang_name_birth UNIQUE (dojang_id, name, birth);


-- ===================================
-- 3. 주 보호자 단일 제약
-- ===================================

-- COL-001: Guardianship primary_student_id Generated Column
-- 용도: MySQL Partial Index 대체 - is_primary=true일 때만 student_id 저장
ALTER TABLE guardianship
ADD COLUMN primary_student_id BIGINT GENERATED ALWAYS AS (IF(is_primary, student_id, NULL)) STORED;

-- UK-003: Guardianship primary_student_id UNIQUE
-- 용도: 원생당 주 보호자 1명 제한 (동시 요청 시에도 DB 레벨에서 중복 방지)
ALTER TABLE guardianship ADD CONSTRAINT uk_guardianship_primary_student UNIQUE (primary_student_id);


-- ===================================
-- 4. Guardianship relation NOT NULL
-- ===================================

-- MOD-001: Guardianship relation NOT NULL
-- 용도: 보호자 관계(부/모/조부모 등)는 필수 입력
ALTER TABLE guardianship MODIFY COLUMN relation VARCHAR(20) NOT NULL;
