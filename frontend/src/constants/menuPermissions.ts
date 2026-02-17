export const MENU_PERMISSIONS: Record<string, string[]> = {
  '/dashboard': ['STATS_VIEW_DASHBOARD'],
  '/students': ['STUDENT_VIEW'],
  '/attendance': ['ATTENDANCE_VIEW'],
  '/billing': ['PAYMENT_VIEW'],
  '/messages': ['MESSAGE_SEND'],
  '/settings': [],
};

export const getMenuRequiredPermissions = (path: string): string[] => {
  const normalizedPath = '/' + path.split('/').filter(Boolean)[0];
  return MENU_PERMISSIONS[normalizedPath] ?? [];
};
