-- Employment 테이블에 입관일(joined_at), 퇴사일(ended_at) 컬럼 추가

-- 1. joined_at 컬럼 추가 (NULL 허용으로 먼저 추가)
ALTER TABLE employment ADD COLUMN joined_at TIMESTAMP NULL;

-- 2. ended_at 컬럼 추가 (NULL 허용)
ALTER TABLE employment ADD COLUMN ended_at TIMESTAMP NULL;

-- 3. 기존 데이터 보정: joined_at = created_at
UPDATE employment SET joined_at = created_at WHERE joined_at IS NULL;

-- 4. joined_at NOT NULL 제약조건 추가
ALTER TABLE employment MODIFY COLUMN joined_at TIMESTAMP NOT NULL;
