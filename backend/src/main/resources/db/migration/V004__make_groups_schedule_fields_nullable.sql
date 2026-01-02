-- groups 테이블의 시간표 관련 필드를 nullable로 변경
-- 부/반 개념으로만 사용할 수 있도록 시간 정보를 선택 사항으로 변경

ALTER TABLE groups
    MODIFY COLUMN day_of_week VARCHAR(10) NULL COMMENT '요일 (MON, TUE, WED, THU, FRI, SAT, SUN)',
    MODIFY COLUMN start_time TIME NULL COMMENT '수업 시작 시간',
    MODIFY COLUMN end_time TIME NULL COMMENT '수업 종료 시간';
