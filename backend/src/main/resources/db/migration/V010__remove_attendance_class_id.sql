-- V010: attendance 테이블에서 class_id 컬럼 제거
-- 도메인 설계 변경: 출석은 수련반과 독립적인 도메인

-- 1. 인덱스 제거
DROP INDEX idx_attendance_class_id_attendance_date ON attendance;

-- 2. FK 제거
ALTER TABLE attendance DROP FOREIGN KEY fk_attendance_class_id;

-- 3. 컬럼 제거
ALTER TABLE attendance DROP COLUMN class_id;
