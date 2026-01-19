export interface PermissionItem {
  key: string;
  label: string;
  description: string;
  defaultGranted: boolean;
}

export interface PermissionGroup {
  key: string;
  label: string;
  permissions: PermissionItem[];
}

export const PERMISSION_GROUPS: PermissionGroup[] = [
  {
    key: 'student',
    label: '원생 관리',
    permissions: [
      { key: 'STUDENT_VIEW', label: '원생 정보 조회', description: '원생 목록 및 상세 정보 조회', defaultGranted: true },
      { key: 'STUDENT_CREATE', label: '원생 등록', description: '새 원생 등록', defaultGranted: true },
      { key: 'STUDENT_UPDATE', label: '원생 정보 수정', description: '원생 정보 수정', defaultGranted: true },
      { key: 'STUDENT_DELETE', label: '원생 삭제', description: '원생 삭제', defaultGranted: false },
    ],
  },
  {
    key: 'attendance',
    label: '출석 관리',
    permissions: [
      { key: 'ATTENDANCE_VIEW', label: '출석 조회', description: '출석 현황 조회', defaultGranted: true },
      { key: 'ATTENDANCE_UPDATE', label: '출석 체크/수정', description: '출석 체크 및 수정', defaultGranted: true },
    ],
  },
  {
    key: 'payment',
    label: '수납 관리',
    permissions: [
      { key: 'PAYMENT_VIEW', label: '수납 조회', description: '청구서 및 수납 현황 조회', defaultGranted: true },
      { key: 'PAYMENT_UPDATE', label: '청구서/수납 관리', description: '청구서 발행 및 수납 처리', defaultGranted: true },
    ],
  },
  {
    key: 'dojang',
    label: '도장 관리',
    permissions: [
      { key: 'DOJANG_UPDATE_INFO', label: '도장 정보 수정', description: '도장 기본 정보 수정', defaultGranted: false },
      { key: 'DOJANG_MANAGE_CLASS', label: '수련반 관리', description: '수련반 생성 및 관리', defaultGranted: true },
      { key: 'STATS_VIEW_DASHBOARD', label: '대시보드 조회', description: '통계 대시보드 조회', defaultGranted: true },
    ],
  },
];

export const ALL_PERMISSIONS = PERMISSION_GROUPS.flatMap((group) => group.permissions);

export const TOTAL_PERMISSION_COUNT = ALL_PERMISSIONS.length;

export const DEFAULT_PERMISSIONS = ALL_PERMISSIONS
  .filter((p) => p.defaultGranted)
  .map((p) => p.key);

export const REQUIRED_PERMISSIONS = ['STUDENT_VIEW'] as const;

export const getPermissionLabel = (key: string): string => {
  const permission = ALL_PERMISSIONS.find((p) => p.key === key);
  return permission?.label ?? key;
};
