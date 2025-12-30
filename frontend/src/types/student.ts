export type StudentStatus = 'ACTIVE' | 'PAUSED' | 'WITHDRAWN';
export type GuardianRelation = 'FATHER' | 'MOTHER' | 'GRANDPARENT' | 'OTHER';

export interface Guardian {
  id: string;
  name: string;
  phone: string;
  relation: GuardianRelation;
  isPrimary: boolean;
  isVerified: boolean;
}

export interface Student {
  id: string;
  name: string;
  birth: string;
  photoUrl?: string;
  phone?: string;
  enrolledAt: string;
  status: StudentStatus;
  guardians: Guardian[];
}

export interface StudentSummary {
  id: string;
  name: string;
  birth: string;
  photoUrl?: string;
  phone?: string;
  enrolledAt: string;
  status: StudentStatus;
  primaryGuardianName?: string;
  primaryGuardianPhone?: string;
}

export interface StudentListResponse {
  students: StudentSummary[];
  totalCount: number;
  returnedCount: number;
  hasMore: boolean;
}

export interface StudentCreateRequest {
  name: string;
  birth: string;
  photoUrl?: string;
  phone?: string;
  guardians?: GuardianCreateRequest[];
}

export interface StudentUpdateRequest {
  name: string;
  birth: string;
  photoUrl?: string;
  phone?: string;
  status?: StudentStatus;
  statusChangeReason?: string;
}

export interface GuardianCreateRequest {
  name: string;
  phone: string;
  relation: GuardianRelation;
  isPrimary: boolean;
}

export interface GuardianUpdateRequest {
  name?: string;
  phone?: string;
  relation?: GuardianRelation;
}

export interface StudentStats {
  activeCount: number;
  pausedCount: number;
}

export const GUARDIAN_RELATION_LABEL: Record<GuardianRelation, string> = {
  FATHER: '아버지',
  MOTHER: '어머니',
  GRANDPARENT: '조부모',
  OTHER: '기타',
};

export const STUDENT_STATUS_LABEL: Record<Exclude<StudentStatus, 'WITHDRAWN'>, string> = {
  ACTIVE: '수련중',
  PAUSED: '휴관',
};

export const STUDENT_STATUS_COLOR: Record<Exclude<StudentStatus, 'WITHDRAWN'>, string> = {
  ACTIVE: 'bg-green-100 text-green-800',
  PAUSED: 'bg-yellow-100 text-yellow-800',
};
