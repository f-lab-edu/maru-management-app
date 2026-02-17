export type MessageChannel = 'SMS' | 'KAKAO' | 'EXPO_PUSH';
export type RecipientType = 'ALL' | 'SECTION' | 'DIVISION' | 'INDIVIDUAL';
export type MessageStatus = 'PENDING' | 'PROCESSING' | 'ACCEPTED' | 'DEAD';
export type BroadcastStatus = 'CREATED' | 'DISPATCHING' | 'COMPLETED' | 'PARTIAL_FAILED';

export interface BroadcastCreateRequest {
  title: string;
  body: string;
  channel?: MessageChannel;
  recipientType: RecipientType;
  sectionIds?: string[];
  divisionIds?: string[];
  studentIds?: string[];
}

export interface BroadcastCreateResponse {
  broadcastId: string;
  totalCount: number;
  skippedCount: number;
  status: string;
}

export interface BroadcastSummary {
  id: string;
  title: string;
  channel: MessageChannel;
  recipientType: RecipientType;
  totalCount: number;
  skippedCount: number;
  acceptedCount: number;
  failedCount: number;
  pendingCount: number;
  status: BroadcastStatus;
  createdAt: string;
}

export interface BroadcastDetail {
  id: string;
  title: string;
  body: string;
  channel: MessageChannel;
  recipientType: RecipientType;
  totalCount: number;
  skippedCount: number;
  acceptedCount: number;
  failedCount: number;
  pendingCount: number;
  status: BroadcastStatus;
  sentByUserName: string | null;
  createdAt: string;
}

export interface ResendFailedResponse {
  resendCount: number;
}

export interface RecipientPreviewRequest {
  recipientType: RecipientType;
  sectionIds?: string[];
  divisionIds?: string[];
  studentIds?: string[];
}

export interface RecipientPreview {
  totalStudents: number;
  totalGuardians: number;
  skippedNoGuardian: number;
  skippedNoPhone: number;
  estimatedMessageCount: number;
}

export interface RecipientStatus {
  guardianId: string;
  guardianName: string | null;
  phone: string | null;
  studentName: string | null;
  messageStatus: MessageStatus;
  sentAt: string | null;
}

export interface NotificationDetail {
  id: string;
  guardianName: string | null;
  studentName: string | null;
  title: string;
  messageType: string;
  messageTypeLabel: string;
  status: string;
  createdAt: string;
  sentAt: string | null;
}

export interface MonthlyUsage {
  totalCount: number;
  autoCount: number;
  broadcastCount: number;
}

export interface PagedResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export const MESSAGE_TYPE_LABEL: Record<string, string> = {
  ATTENDANCE_CHECKIN: '출결 알림 (입실)',
  ATTENDANCE_CHECKOUT: '출결 알림 (퇴원)',
  PAYMENT: '결제 링크',
};

export const CHANNEL_LABEL: Record<MessageChannel, string> = {
  SMS: 'SMS',
  KAKAO: '카카오톡',
  EXPO_PUSH: '푸시 알림',
};

export const RECIPIENT_TYPE_LABEL: Record<RecipientType, string> = {
  ALL: '전체',
  SECTION: '반별',
  DIVISION: '부별',
  INDIVIDUAL: '개별',
};

export const STATUS_LABEL: Record<string, string> = {
  PENDING: '대기',
  PROCESSING: '처리 중',
  ACCEPTED: '성공',
  DEAD: '실패',
  DISPATCHING: '발송 중',
  COMPLETED: '완료',
  PARTIAL_FAILED: '일부 실패',
};
