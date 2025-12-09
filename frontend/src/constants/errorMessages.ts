export const ERROR_MESSAGES: Record<string, string> = {
  // 인증 관련 (AUTH_XXX)
  AUTH_001: '인증이 필요합니다',
  AUTH_002: '유효하지 않은 토큰입니다',
  AUTH_003: '접근 권한이 없습니다',
  AUTH_004: '토큰이 만료되었습니다',
  AUTH_005: '세션이 만료되었습니다. 다시 로그인해주세요',
  AUTH_006: '유효하지 않은 세션입니다',
  AUTH_007: '로그인이 필요합니다',
  AUTH_008: '소셜 로그인에 실패했습니다',
  AUTH_009: '유효하지 않은 인가 코드입니다',
  AUTH_010: '사용자 정보를 가져오지 못했습니다',

  // 사용자 관련 (USER_XXX)
  USER_001: '사용자를 찾을 수 없습니다',
  USER_002: '유효하지 않은 역할입니다',

  // 온보딩 관련 (ONBOARDING_XXX)
  ONBOARDING_001: '현재 단계에서 수행할 수 없는 작업입니다',
  ONBOARDING_002: '전화번호 인증을 완료해주세요',

  // SMS 인증 관련 (SMS_XXX)
  SMS_001: '인증번호가 일치하지 않습니다',
  SMS_002: '인증번호가 만료되었습니다',
  SMS_003: '인증 요청 내역이 없습니다',
  SMS_004: 'SMS 발송에 실패했습니다',
  SMS_005: '잠시 후 다시 시도해주세요',
  SMS_006: '인증 시도 횟수를 초과했습니다',
  SMS_007: '본인 인증만 가능합니다',

  // 고용/승인 관련 (EMPLOYMENT_XXX)
  EMPLOYMENT_001: '승인 요청을 찾을 수 없습니다',
  EMPLOYMENT_002: '이미 승인 요청이 존재합니다',
  EMPLOYMENT_003: '대기 상태의 요청만 처리할 수 있습니다',
  EMPLOYMENT_004: '관장만 승인/거절할 수 있습니다',
  EMPLOYMENT_005: '본인의 요청만 취소할 수 있습니다',

  // 도장 관련 (DOJANG_XXX)
  DOJANG_001: '도장을 찾을 수 없습니다',
  DOJANG_002: '해당 도장에 접근 권한이 없습니다',

  // 원생 관련 (STUDENT_XXX)
  STUDENT_001: '원생을 찾을 수 없습니다',
  STUDENT_002: '이미 등록된 원생입니다',

  // 보호자 관련 (GUARDIAN_XXX)
  GUARDIAN_001: '보호자를 찾을 수 없습니다',
  GUARDIAN_002: '이미 주 보호자가 설정되어 있습니다',
  GUARDIAN_003: '이미 연결된 보호자입니다',

  // 일반 에러 (COMMON_XXX)
  COMMON_001: '잘못된 요청입니다',
  COMMON_002: '요청한 정보를 찾을 수 없습니다',
  COMMON_003: '서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요',
  COMMON_004: '준비 중인 기능입니다',
};

export const getErrorMessage = (code: string): string => {
  return ERROR_MESSAGES[code] || '알 수 없는 오류가 발생했습니다';
};
