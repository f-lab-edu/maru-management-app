import { useMemo } from 'react';
import { useAuthStore } from '../stores/authStore';
import { ALL_PERMISSIONS } from '../features/settings/constants/permissions';

export function usePermissions() {
  const selectedDojang = useAuthStore((s) => s.selectedDojang);

  const permissions = useMemo(() => {
    if (!selectedDojang) return [];
    return selectedDojang.permissions ?? [];
  }, [selectedDojang]);

  const isOwner = selectedDojang?.role === 'OWNER';

  const hasPermission = (permission: string): boolean => {
    if (isOwner) return true;
    return permissions.includes(permission);
  };

  const hasAnyPermission = (requiredPermissions: string[]): boolean => {
    if (isOwner) return true;
    if (requiredPermissions.length === 0) return true;
    return requiredPermissions.some((p) => permissions.includes(p));
  };

  const hasAllPermissions = (requiredPermissions: string[]): boolean => {
    if (isOwner) return true;
    if (requiredPermissions.length === 0) return true;
    return requiredPermissions.every((p) => permissions.includes(p));
  };

  const grantedCount = isOwner ? ALL_PERMISSIONS.length : permissions.length;
  const totalCount = ALL_PERMISSIONS.length;

  return {
    permissions,
    isOwner,
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    grantedCount,
    totalCount,
  };
}
