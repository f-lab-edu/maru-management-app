-- ========================================
-- 도장 검색용 Full-Text 인덱스 추가
-- ========================================
-- Version: V008
-- Description: 도장 검색 성능 향상을 위한 Full-Text 인덱스 (ngram 파서)
-- Created: 2025-12-04
-- ========================================

ALTER TABLE dojang ADD FULLTEXT INDEX idx_dojang_fulltext (name, address) WITH PARSER ngram;
