-- Section, Division 테이블에 display_order 컬럼 추가

ALTER TABLE section
    ADD COLUMN display_order INT DEFAULT 0 COMMENT '표시 순서' AFTER name;

ALTER TABLE division
    ADD COLUMN display_order INT DEFAULT 0 COMMENT '표시 순서' AFTER name;
