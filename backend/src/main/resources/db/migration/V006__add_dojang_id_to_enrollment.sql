-- enrollment 테이블에 dojang_id 컬럼 추가
ALTER TABLE enrollment ADD COLUMN dojang_id VARCHAR(13) NOT NULL AFTER id;

-- 외래키 제약조건 추가
ALTER TABLE enrollment ADD CONSTRAINT fk_enrollment_dojang_id FOREIGN KEY (dojang_id) REFERENCES dojang (id);

-- 인덱스 추가
CREATE INDEX idx_enrollment_dojang_id ON enrollment (dojang_id);
