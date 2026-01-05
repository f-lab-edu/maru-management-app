-- enrollment 테이블에서 deleted_at 컬럼 제거 (soft delete 불필요)
ALTER TABLE enrollment DROP COLUMN deleted_at;
