-- Section, Classes 테이블에 이름 중복 방지 UNIQUE 제약 추가

ALTER TABLE section
    ADD CONSTRAINT uk_section_dojang_name UNIQUE (dojang_id, name);

ALTER TABLE classes
    ADD CONSTRAINT uk_classes_section_name UNIQUE (section_id, name);
