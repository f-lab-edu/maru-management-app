-- V002__add_indexes.sql
-- Description: 성능 최적화를 위한 인덱스 생성
-- Created: 2025-11-03
-- Author: Maru Management System

-- ===================================
-- 1. 인증 및 테넌트 인덱스
-- ===================================

-- INDEX-001: USER 테이블 인덱스
-- 용도: 이메일 검색, 전화번호 검색, 역할별 조회
CREATE INDEX idx_user_email ON USER (email);
CREATE INDEX idx_user_phone ON USER (phone);
CREATE INDEX idx_user_role ON USER (role);

-- INDEX-002: OAUTH_ACCOUNT 테이블 인덱스
-- 용도: OAuth 계정 조회 시 사용자 ID로 검색
CREATE INDEX idx_oauth_account_user_id ON OAUTH_ACCOUNT (user_id);

-- INDEX-003: TENANT 테이블 인덱스
-- 용도: 테넌트 소유자 조회
CREATE INDEX idx_tenant_user_id ON TENANT (user_id);

-- INDEX-004: DOJANG 테이블 인덱스
-- 용도: 테넌트별 도장 조회, 소유자별 도장 조회, 플랜별 도장 조회
CREATE INDEX idx_dojang_tenant_id ON DOJANG (tenant_id);
CREATE INDEX idx_dojang_user_id ON DOJANG (user_id);
CREATE INDEX idx_dojang_plan ON DOJANG (plan);

-- INDEX-005: EMPLOYMENT 테이블 인덱스
-- 용도: 테넌트별 고용 상태 조회, 도장별 고용 상태 조회
CREATE INDEX idx_employment_tenant_id_status ON EMPLOYMENT (tenant_id, status);
CREATE INDEX idx_employment_dojang_id_status ON EMPLOYMENT (dojang_id, status);

-- ===================================
-- 2. 권한 및 이력 인덱스
-- ===================================

-- INDEX-006: EMPLOYMENT_HISTORY 테이블 인덱스
-- 용도: 고용 이력 조회 (시간순), 상태 전환 이력 조회
CREATE INDEX idx_employment_history_employment_id_created_at ON EMPLOYMENT_HISTORY (employment_id, created_at);
CREATE INDEX idx_employment_history_to_status_created_at ON EMPLOYMENT_HISTORY (to_status, created_at);

-- INDEX-007: PERMISSION 테이블 인덱스
-- 용도: 고용별 권한 조회
CREATE INDEX idx_permission_employment_id ON PERMISSION (employment_id);

-- ===================================
-- 3. 원생 및 학부모 인덱스
-- ===================================

-- INDEX-008: STUDENT 테이블 인덱스
-- 용도: 테넌트별 원생 상태 조회, 도장별 원생 상태 조회, 테넌트별 원생 이름 검색
CREATE INDEX idx_student_tenant_id_status ON STUDENT (tenant_id, status);
CREATE INDEX idx_student_dojang_id_status ON STUDENT (dojang_id, status);
CREATE INDEX idx_student_tenant_id_name ON STUDENT (tenant_id, name);

-- INDEX-009: GUARDIAN 테이블 인덱스
-- 용도: 전화번호로 학부모 검색, 인증 여부 필터링
CREATE INDEX idx_guardian_phone ON GUARDIAN (phone);
CREATE INDEX idx_guardian_is_verified ON GUARDIAN (is_verified);

-- INDEX-010: GUARDIANSHIP 테이블 인덱스
-- 용도: 학부모별 원생 조회, 원생별 학부모 조회
CREATE INDEX idx_guardianship_guardian_id ON GUARDIANSHIP (guardian_id);
CREATE INDEX idx_guardianship_student_id ON GUARDIANSHIP (student_id);

-- ===================================
-- 4. 수련부 및 수련반 인덱스
-- ===================================

-- INDEX-011: SECTION 테이블 인덱스
-- 용도: 도장별 활성 수련부 조회
CREATE INDEX idx_section_dojang_id_is_active ON SECTION (dojang_id, is_active);

-- INDEX-012: CLASS 테이블 인덱스
-- 용도: 도장별 요일별 수련반 조회, 수련부별 수련반 조회
CREATE INDEX idx_class_dojang_id_day_of_week ON CLASS (dojang_id, day_of_week);
CREATE INDEX idx_class_section_id ON CLASS (section_id);

-- INDEX-013: ENROLLMENT 테이블 인덱스
-- 용도: 원생별 등록 수련반 조회, 수련반별 등록 원생 조회
CREATE INDEX idx_enrollment_student_id ON ENROLLMENT (student_id);
CREATE INDEX idx_enrollment_class_id ON ENROLLMENT (class_id);

-- ===================================
-- 5. 출결, 승급, 결제 인덱스
-- ===================================

-- INDEX-014: ATTENDANCE 테이블 인덱스
-- 용도: 테넌트별 날짜별 출결 조회, 원생별 날짜별 출결 조회, 도장별 날짜별 출결 조회, 수련반별 날짜별 출결 조회
CREATE INDEX idx_attendance_tenant_id_attendance_date ON ATTENDANCE (tenant_id, attendance_date);
CREATE INDEX idx_attendance_student_id_attendance_date ON ATTENDANCE (student_id, attendance_date);
CREATE INDEX idx_attendance_dojang_id_attendance_date ON ATTENDANCE (dojang_id, attendance_date);
CREATE INDEX idx_attendance_class_id_attendance_date ON ATTENDANCE (class_id, attendance_date);

-- INDEX-015: PROMOTION 테이블 인덱스
-- 용도: 원생별 승급 이력 조회 (시간순), 도장별 승급 이력 조회 (시간순)
CREATE INDEX idx_promotion_student_id_awarded_at ON PROMOTION (student_id, awarded_at);
CREATE INDEX idx_promotion_dojang_id_awarded_at ON PROMOTION (dojang_id, awarded_at);

-- INDEX-016: PROMOTION_HISTORY 테이블 인덱스
-- 용도: 승급별 변경 이력 조회 (시간순), 액션별 변경 이력 조회 (시간순)
CREATE INDEX idx_promotion_history_promotion_id_created_at ON PROMOTION_HISTORY (promotion_id, created_at);
CREATE INDEX idx_promotion_history_action_created_at ON PROMOTION_HISTORY (action, created_at);

-- INDEX-017: INVOICE 테이블 인덱스
-- 용도: 테넌트별 상태별 마감일 기준 청구서 조회, 원생별 상태별 청구서 조회, 도장별 발행일 기준 청구서 조회
CREATE INDEX idx_invoice_tenant_id_status_due_date ON INVOICE (tenant_id, status, due_date);
CREATE INDEX idx_invoice_student_id_status ON INVOICE (student_id, status);
CREATE INDEX idx_invoice_dojang_id_issue_date ON INVOICE (dojang_id, issue_date);

-- INDEX-018: PAYMENT 테이블 인덱스
-- 용도: 테넌트별 결제일 기준 결제 조회, 청구서별 결제 조회, 상태별 결제 조회
CREATE INDEX idx_payment_tenant_id_paid_at ON PAYMENT (tenant_id, paid_at);
CREATE INDEX idx_payment_invoice_id ON PAYMENT (invoice_id);
CREATE INDEX idx_payment_status ON PAYMENT (status);

-- ===================================
-- 6. 알림 인덱스
-- ===================================

-- INDEX-019: MESSAGE_QUEUE 테이블 인덱스
-- 용도: 상태별 예약 시간 기준 메시지 조회, 테넌트별 생성 시간 기준 메시지 조회, 학부모별 메시지 조회, 원생별 메시지 조회
CREATE INDEX idx_message_queue_status_scheduled_at ON MESSAGE_QUEUE (status, scheduled_at);
CREATE INDEX idx_message_queue_tenant_id_created_at ON MESSAGE_QUEUE (tenant_id, created_at);
CREATE INDEX idx_message_queue_guardian_id ON MESSAGE_QUEUE (guardian_id);
CREATE INDEX idx_message_queue_student_id ON MESSAGE_QUEUE (student_id);
