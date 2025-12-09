interface ConfirmMessage {
  title: string;
  text?: string;
  confirmText?: string;
}

export const CONFIRM_MESSAGES = {
  STUDENT: {
    DELETE: {
      title: '원생을 삭제하시겠습니까?',
      text: '삭제된 원생 정보는 복구할 수 없습니다.',
      confirmText: '삭제',
    },
    BULK_DELETE: {
      title: '선택한 원생을 삭제하시겠습니까?',
      text: '삭제된 원생 정보는 복구할 수 없습니다.',
      confirmText: '삭제',
    },
    STATUS_CHANGE: {
      title: '상태를 변경하시겠습니까?',
      confirmText: '변경',
    },
  },

  GUARDIAN: {
    DELETE: {
      title: '보호자를 삭제하시겠습니까?',
      text: '해당 보호자와의 연결이 해제됩니다.',
      confirmText: '삭제',
    },
    SET_PRIMARY: {
      title: '주 보호자로 설정하시겠습니까?',
      confirmText: '설정',
    },
    UNSET_PRIMARY: {
      title: '주 보호자를 해제하시겠습니까?',
      confirmText: '해제',
    },
  },

  DOJANG: {
    SWITCH: {
      title: '도장을 변경하시겠습니까?',
      confirmText: '변경',
    },
  },

  AUTH: {
    LOGOUT: {
      title: '로그아웃 하시겠습니까?',
      confirmText: '로그아웃',
    },
  },
} as const satisfies Record<string, Record<string, ConfirmMessage>>;

export type ConfirmMessageType = typeof CONFIRM_MESSAGES;
