-- ========================================
-- 초기 스키마 생성
-- ========================================
-- Version: V001
-- Description: 18개 테이블 생성 (PK, FK, UNIQUE 제약조건 포함)
-- Created: 2025-11-03
-- ========================================

-- ========================================
-- 1. 인증 및 테넌트 관련 테이블 (5개)
-- ========================================

-- 1.1 USERS (사용자)
CREATE TABLE USERS (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL COMMENT '사용자 이름',
  email VARCHAR(255) COMMENT '이메일 (선택)',
  phone VARCHAR(20) COMMENT '전화번호 (선택)',
  role VARCHAR(20) NOT NULL COMMENT '사용자 역할 (OWNER, INSTRUCTOR)',
  onboarding_step VARCHAR(30) COMMENT '온보딩 단계 (ROLE_SELECT, DOJO_INFO, APPROVAL_WAIT, COMPLETED)',
  last_login_at TIMESTAMP NULL COMMENT '마지막 로그인 시각',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
  deleted_at TIMESTAMP NULL COMMENT '소프트 삭제 시각'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='사용자 (관장, 사범)';

-- 1.2 OAUTH_ACCOUNT (OAuth 계정)
CREATE TABLE OAUTH_ACCOUNT (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '사용자 ID',
  provider VARCHAR(20) NOT NULL COMMENT 'OAuth 제공자 (GOOGLE, KAKAO)',
  provider_account_id VARCHAR(255) NOT NULL COMMENT 'OAuth 제공자의 사용자 ID',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
  UNIQUE KEY uk_oauth_provider_account (provider, provider_account_id),
  CONSTRAINT fk_oauth_account_user_id FOREIGN KEY (user_id) REFERENCES USERS (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OAuth 계정 정보';

-- 1.3 TENANT (테넌트)
CREATE TABLE TENANT (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '관장 (소유자) ID',
  slug VARCHAR(8) NOT NULL COMMENT 'URL 친화적 ID (8자리)',
  is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성 상태',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
  deleted_at TIMESTAMP NULL COMMENT '소프트 삭제 시각',
  UNIQUE KEY uk_tenant_slug (slug),
  CONSTRAINT fk_tenant_user_id FOREIGN KEY (user_id) REFERENCES USERS (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='테넌트 (독립적인 도장 운영 단위)';

-- 1.4 DOJANG (도장)
CREATE TABLE DOJANG (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL COMMENT '테넌트 ID',
  user_id BIGINT NOT NULL COMMENT '관장 ID',
  name VARCHAR(255) NOT NULL COMMENT '도장 이름',
  plan VARCHAR(20) NOT NULL DEFAULT 'FREE' COMMENT '요금제 (FREE, BASIC, PRO)',
  address VARCHAR(500) COMMENT '도장 주소',
  phone VARCHAR(20) COMMENT '도장 전화번호',
  is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성 상태',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
  deleted_at TIMESTAMP NULL COMMENT '소프트 삭제 시각',
  CONSTRAINT fk_dojang_tenant_id FOREIGN KEY (tenant_id) REFERENCES TENANT (id) ON DELETE CASCADE,
  CONSTRAINT fk_dojang_user_id FOREIGN KEY (user_id) REFERENCES USERS (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='도장 정보';

-- 1.5 EMPLOYMENT (고용)
CREATE TABLE EMPLOYMENT (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '사범 ID',
  tenant_id BIGINT NOT NULL COMMENT '테넌트 ID',
  dojang_id BIGINT NOT NULL COMMENT '도장 ID',
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '고용 상태 (PENDING, ACTIVE, SUSPENDED, REJECTED, LEFT)',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
  UNIQUE KEY uk_employment_user_dojang (user_id, dojang_id),
  CONSTRAINT fk_employment_user_id FOREIGN KEY (user_id) REFERENCES USERS (id) ON DELETE CASCADE,
  CONSTRAINT fk_employment_tenant_id FOREIGN KEY (tenant_id) REFERENCES TENANT (id) ON DELETE CASCADE,
  CONSTRAINT fk_employment_dojang_id FOREIGN KEY (dojang_id) REFERENCES DOJANG (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='사범 고용 관계';

-- ========================================
-- 2. 권한 및 이력 테이블 (2개)
-- ========================================

-- 2.1 EMPLOYMENT_HISTORY (고용 이력)
CREATE TABLE EMPLOYMENT_HISTORY (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  employment_id BIGINT NOT NULL COMMENT '고용 관계 ID',
  from_status VARCHAR(20) COMMENT '이전 상태 (NULL이면 최초 생성)',
  to_status VARCHAR(20) NOT NULL COMMENT '변경된 상태',
  user_id BIGINT COMMENT '변경한 사용자 ID (NULL이면 시스템 변경)',
  reason VARCHAR(500) COMMENT '변경 사유',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '이력 생성 시각',
  CONSTRAINT fk_employment_history_employment_id FOREIGN KEY (employment_id) REFERENCES EMPLOYMENT (id) ON DELETE CASCADE,
  CONSTRAINT fk_employment_history_user_id FOREIGN KEY (user_id) REFERENCES USERS (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='고용 상태 변경 이력';

-- 2.2 PERMISSION (권한)
CREATE TABLE PERMISSION (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  employment_id BIGINT NOT NULL COMMENT '고용 관계 ID',
  resource VARCHAR(50) NOT NULL COMMENT '리소스 (students, attendances, payments 등)',
  action VARCHAR(50) NOT NULL COMMENT '액션 (read, write, delete)',
  granted BOOLEAN NOT NULL DEFAULT TRUE COMMENT '권한 부여 여부',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
  UNIQUE KEY uk_permission_employment_resource_action (employment_id, resource, action),
  CONSTRAINT fk_permission_employment_id FOREIGN KEY (employment_id) REFERENCES EMPLOYMENT (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='사범 권한';

-- ========================================
-- 3. 원생 및 학부모 관련 테이블 (3개)
-- ========================================

-- 3.1 STUDENT (원생)
CREATE TABLE STUDENT (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL COMMENT '테넌트 ID',
  dojang_id BIGINT NOT NULL COMMENT '도장 ID',
  name VARCHAR(100) NOT NULL COMMENT '원생 이름',
  photo_url VARCHAR(500) COMMENT '프로필 사진 URL',
  phone VARCHAR(20) COMMENT '원생 전화번호',
  birth DATE COMMENT '생년월일',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '원생 상태 (ACTIVE, PAUSED, WITHDRAWN)',
  enrolled_at DATE COMMENT '입관일',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
  deleted_at TIMESTAMP NULL COMMENT '소프트 삭제 시각',
  CONSTRAINT fk_student_tenant_id FOREIGN KEY (tenant_id) REFERENCES TENANT (id) ON DELETE CASCADE,
  CONSTRAINT fk_student_dojang_id FOREIGN KEY (dojang_id) REFERENCES DOJANG (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='원생 정보';

-- 3.2 GUARDIAN (학부모)
CREATE TABLE GUARDIAN (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  phone VARCHAR(20) NOT NULL COMMENT '전화번호 (인증 수단)',
  name VARCHAR(100) NOT NULL COMMENT '학부모 이름',
  is_verified BOOLEAN DEFAULT FALSE COMMENT '전화번호 인증 여부',
  verified_at TIMESTAMP NULL COMMENT '인증 완료 시각',
  push_token VARCHAR(500) COMMENT 'FCM 푸시 토큰',
  push_token_updated_at TIMESTAMP NULL COMMENT '푸시 토큰 갱신 시각',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
  deleted_at TIMESTAMP NULL COMMENT '소프트 삭제 시각'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='학부모 정보';

-- 3.3 GUARDIANSHIP (보호자 관계)
CREATE TABLE GUARDIANSHIP (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  guardian_id BIGINT NOT NULL COMMENT '학부모 ID',
  student_id BIGINT NOT NULL COMMENT '원생 ID',
  relation VARCHAR(20) COMMENT '관계 (부, 모, 조부모 등)',
  is_primary BOOLEAN DEFAULT FALSE COMMENT '주 보호자 여부',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
  deleted_at TIMESTAMP NULL COMMENT '소프트 삭제 시각',
  UNIQUE KEY uk_guardianship_guardian_student (guardian_id, student_id),
  CONSTRAINT fk_guardianship_guardian_id FOREIGN KEY (guardian_id) REFERENCES GUARDIAN (id) ON DELETE CASCADE,
  CONSTRAINT fk_guardianship_student_id FOREIGN KEY (student_id) REFERENCES STUDENT (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='학부모-원생 관계';

-- ========================================
-- 4. 수련부 및 수련반 테이블 (3개)
-- ========================================

-- 4.1 SECTION (수련부)
CREATE TABLE SECTION (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  dojang_id BIGINT NOT NULL COMMENT '도장 ID',
  name VARCHAR(255) NOT NULL COMMENT '수련부 이름',
  is_active BOOLEAN DEFAULT TRUE COMMENT '활성 상태',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
  deleted_at TIMESTAMP NULL COMMENT '소프트 삭제 시각',
  CONSTRAINT fk_section_dojang_id FOREIGN KEY (dojang_id) REFERENCES DOJANG (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='수련부 (유아부, 초등부, 성인부 등)';

-- 4.2 CLASSES (수련반)
CREATE TABLE CLASSES (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  dojang_id BIGINT NOT NULL COMMENT '도장 ID',
  section_id BIGINT COMMENT '수련부 ID (NULL 가능)',
  day_of_week VARCHAR(10) NOT NULL COMMENT '요일 (MON, TUE, WED, THU, FRI, SAT, SUN)',
  start_time TIME NOT NULL COMMENT '수업 시작 시간',
  end_time TIME NOT NULL COMMENT '수업 종료 시간',
  name VARCHAR(255) NOT NULL COMMENT '수련반 이름',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
  deleted_at TIMESTAMP NULL COMMENT '소프트 삭제 시각',
  CONSTRAINT fk_class_dojang_id FOREIGN KEY (dojang_id) REFERENCES DOJANG (id) ON DELETE CASCADE,
  CONSTRAINT fk_class_section_id FOREIGN KEY (section_id) REFERENCES SECTION (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='수련반 (요일 + 시간 기반)';

-- 4.3 ENROLLMENT (수업 등록)
CREATE TABLE ENROLLMENT (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL COMMENT '원생 ID',
  class_id BIGINT NOT NULL COMMENT '수련반 ID',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록 시각',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
  deleted_at TIMESTAMP NULL COMMENT '소프트 삭제 시각 (등록 취소)',
  UNIQUE KEY uk_enrollment_student_class (student_id, class_id),
  CONSTRAINT fk_enrollment_student_id FOREIGN KEY (student_id) REFERENCES STUDENT (id) ON DELETE CASCADE,
  CONSTRAINT fk_enrollment_class_id FOREIGN KEY (class_id) REFERENCES CLASSES (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='원생 수련반 등록';

-- ========================================
-- 5. 출결, 승급, 결제 관련 테이블 (5개)
-- ========================================

-- 5.1 ATTENDANCE (출결)
CREATE TABLE ATTENDANCE (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL COMMENT '테넌트 ID',
  dojang_id BIGINT NOT NULL COMMENT '도장 ID',
  student_id BIGINT NOT NULL COMMENT '원생 ID',
  class_id BIGINT COMMENT '수련반 ID (NULL 가능)',
  checked_by BIGINT COMMENT '출결 처리자 ID',
  attendance_date DATE NOT NULL COMMENT '출결 날짜',
  status VARCHAR(20) NOT NULL DEFAULT 'PRESENT' COMMENT '출결 상태 (PRESENT, ABSENT, SICK, LATE)',
  method VARCHAR(20) COMMENT '체크 방법 (MANUAL, KIOSK, ADMIN_APP)',
  checkin_at TIMESTAMP NULL COMMENT '입장 시각',
  checkout_at TIMESTAMP NULL COMMENT '퇴장 시각',
  note VARCHAR(500) COMMENT '비고',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
  UNIQUE KEY uk_attendance_tenant_student_date (tenant_id, student_id, attendance_date),
  CONSTRAINT fk_attendance_tenant_id FOREIGN KEY (tenant_id) REFERENCES TENANT (id) ON DELETE CASCADE,
  CONSTRAINT fk_attendance_dojang_id FOREIGN KEY (dojang_id) REFERENCES DOJANG (id) ON DELETE CASCADE,
  CONSTRAINT fk_attendance_student_id FOREIGN KEY (student_id) REFERENCES STUDENT (id) ON DELETE CASCADE,
  CONSTRAINT fk_attendance_class_id FOREIGN KEY (class_id) REFERENCES CLASSES (id) ON DELETE SET NULL,
  CONSTRAINT fk_attendance_checked_by FOREIGN KEY (checked_by) REFERENCES USERS (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='원생 출결 기록';

-- 5.2 PROMOTION (승급)
CREATE TABLE PROMOTION (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  dojang_id BIGINT NOT NULL COMMENT '도장 ID',
  student_id BIGINT NOT NULL COMMENT '원생 ID',
  `rank` VARCHAR(50) NOT NULL COMMENT '품계 (예: 흰띠, 노란띠, 1단)',
  rank_type VARCHAR(20) COMMENT '품계 유형 (COLOR_BELT, DAN)',
  examined_by BIGINT COMMENT '심사자 ID',
  note VARCHAR(500) COMMENT '비고',
  awarded_at TIMESTAMP NOT NULL COMMENT '승급일',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
  UNIQUE KEY uk_promotion_student_rank_awarded (student_id, `rank`, awarded_at),
  CONSTRAINT fk_promotion_dojang_id FOREIGN KEY (dojang_id) REFERENCES DOJANG (id) ON DELETE CASCADE,
  CONSTRAINT fk_promotion_student_id FOREIGN KEY (student_id) REFERENCES STUDENT (id) ON DELETE CASCADE,
  CONSTRAINT fk_promotion_examined_by FOREIGN KEY (examined_by) REFERENCES USERS (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='원생 승급 이력';

-- 5.3 PROMOTION_HISTORY (승급 변경 이력)
CREATE TABLE PROMOTION_HISTORY (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  promotion_id BIGINT NOT NULL COMMENT '승급 ID',
  `from_rank` VARCHAR(50) COMMENT '이전 품계 (NULL이면 최초 생성)',
  `to_rank` VARCHAR(50) NOT NULL COMMENT '변경된 품계',
  `action` VARCHAR(20) NOT NULL COMMENT '변경 유형 (CREATED, UPDATED, DELETED)',
  changed_by BIGINT COMMENT '변경한 사용자 ID',
  reason VARCHAR(500) COMMENT '변경 사유',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '이력 생성 시각',
  CONSTRAINT fk_promotion_history_promotion_id FOREIGN KEY (promotion_id) REFERENCES PROMOTION (id) ON DELETE CASCADE,
  CONSTRAINT fk_promotion_history_changed_by FOREIGN KEY (changed_by) REFERENCES USERS (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='승급 변경 이력';

-- 5.4 INVOICE (청구서)
CREATE TABLE INVOICE (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL COMMENT '테넌트 ID',
  dojang_id BIGINT NOT NULL COMMENT '도장 ID',
  student_id BIGINT NOT NULL COMMENT '원생 ID',
  issued_by BIGINT COMMENT '발행자 ID',
  issue_date DATE NOT NULL COMMENT '발행일',
  due_date DATE NOT NULL COMMENT '납부 마감일',
  status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '청구서 상태 (DRAFT, OPEN, PARTIAL, PAID, VOID)',
  amount DECIMAL(10,2) NOT NULL COMMENT '청구 금액',
  paid_amount DECIMAL(10,2) DEFAULT 0 COMMENT '납부 금액',
  note VARCHAR(500) COMMENT '비고',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
  CONSTRAINT fk_invoice_tenant_id FOREIGN KEY (tenant_id) REFERENCES TENANT (id) ON DELETE CASCADE,
  CONSTRAINT fk_invoice_dojang_id FOREIGN KEY (dojang_id) REFERENCES DOJANG (id) ON DELETE CASCADE,
  CONSTRAINT fk_invoice_student_id FOREIGN KEY (student_id) REFERENCES STUDENT (id) ON DELETE CASCADE,
  CONSTRAINT fk_invoice_issued_by FOREIGN KEY (issued_by) REFERENCES USERS (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='원생 수납 청구서';

-- 5.5 PAYMENT (수납)
CREATE TABLE PAYMENT (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL COMMENT '테넌트 ID',
  dojang_id BIGINT NOT NULL COMMENT '도장 ID',
  invoice_id BIGINT NOT NULL COMMENT '청구서 ID',
  status VARCHAR(20) NOT NULL COMMENT '수납 상태 (PAID, REFUNDED)',
  paid_at TIMESTAMP NULL COMMENT '수납 시각',
  refunded_at TIMESTAMP NULL COMMENT '환불 시각',
  method VARCHAR(20) COMMENT '수납 방법 (CASH, CARD, TRANSFER, PG)',
  amount DECIMAL(10,2) NOT NULL COMMENT '수납 금액',
  received_by BIGINT COMMENT '수납 처리자 ID',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
  CONSTRAINT fk_payment_tenant_id FOREIGN KEY (tenant_id) REFERENCES TENANT (id) ON DELETE CASCADE,
  CONSTRAINT fk_payment_dojang_id FOREIGN KEY (dojang_id) REFERENCES DOJANG (id) ON DELETE CASCADE,
  CONSTRAINT fk_payment_invoice_id FOREIGN KEY (invoice_id) REFERENCES INVOICE (id) ON DELETE CASCADE,
  CONSTRAINT fk_payment_received_by FOREIGN KEY (received_by) REFERENCES USERS (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='청구서 수납 내역';

-- ========================================
-- 6. 알림 관련 테이블 (1개)
-- ========================================

-- 6.1 MESSAGE_QUEUE (메시지 큐)
CREATE TABLE MESSAGE_QUEUE (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL COMMENT '테넌트 ID',
  guardian_id BIGINT NOT NULL COMMENT '학부모 ID',
  student_id BIGINT COMMENT '원생 ID (NULL 가능)',
  message_type VARCHAR(30) NOT NULL COMMENT '메시지 유형 (ATTENDANCE, PAYMENT, ANNOUNCEMENT)',
  title VARCHAR(200) NOT NULL COMMENT '메시지 제목',
  body VARCHAR(500) NOT NULL COMMENT '메시지 본문',
  data_payload TEXT COMMENT '추가 데이터 (JSON)',
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '발송 상태 (PENDING, PROCESSING, SENT, FAILED)',
  scheduled_at TIMESTAMP NOT NULL COMMENT '발송 예정 시각',
  sent_at TIMESTAMP NULL COMMENT '발송 완료 시각',
  failed_count INT DEFAULT 0 COMMENT '실패 횟수',
  error_message VARCHAR(1000) COMMENT '오류 메시지',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
  CONSTRAINT fk_message_queue_tenant_id FOREIGN KEY (tenant_id) REFERENCES TENANT (id) ON DELETE CASCADE,
  CONSTRAINT fk_message_queue_guardian_id FOREIGN KEY (guardian_id) REFERENCES GUARDIAN (id) ON DELETE CASCADE,
  CONSTRAINT fk_message_queue_student_id FOREIGN KEY (student_id) REFERENCES STUDENT (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='학부모 푸시 알림 메시지 큐';
