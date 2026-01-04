-- section 테이블에서 deleted_at 컬럼 제거 (soft delete 불필요)
ALTER TABLE section DROP COLUMN deleted_at;

-- division 테이블에서 deleted_at 컬럼 제거 (soft delete 불필요)
ALTER TABLE division DROP COLUMN deleted_at;