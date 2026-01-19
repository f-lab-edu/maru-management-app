import { useState, useEffect } from 'react';
import { Loader2, RotateCcw } from 'lucide-react';
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetDescription,
  SheetFooter,
} from '../../../shared/components/ui/sheet';
import { Button } from '../../../shared/components/ui/button';
import { Badge } from '../../../shared/components/ui/badge';
import { PermissionGroupCard } from './PermissionGroupCard';
import { PERMISSION_GROUPS, DEFAULT_PERMISSIONS, TOTAL_PERMISSION_COUNT, REQUIRED_PERMISSIONS } from '../constants/permissions';
import {
  useInstructorDetail,
  useUpdatePermissions,
  useResetPermissions,
} from '../../employment/hooks/useEmployment';
import { useConfirm } from '../../../hooks/useSweetAlert/useConfirm';
import { useAlert } from '../../../hooks/useSweetAlert/useAlert';
import { getApiErrorMessage } from '../../../constants/errorMessages';

interface PermissionSheetProps {
  instructorId: string | null;
  instructorName: string;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  readOnly?: boolean;
}

export function PermissionSheet({
  instructorId,
  instructorName,
  open,
  onOpenChange,
  readOnly = false,
}: PermissionSheetProps) {
  const { data: detail, isLoading } = useInstructorDetail(instructorId ?? '', open && !!instructorId);
  const updatePermissions = useUpdatePermissions();
  const resetPermissions = useResetPermissions();
  const { confirmWarning } = useConfirm();
  const { showError, showSuccess } = useAlert();

  const [selectedPermissions, setSelectedPermissions] = useState<Set<string>>(new Set());

  useEffect(() => {
    if (detail?.permissions) {
      setSelectedPermissions(new Set(detail.permissions));
    }
  }, [detail]);

  const handlePermissionChange = (key: string, checked: boolean) => {
    setSelectedPermissions((prev) => {
      const next = new Set(prev);
      if (checked) {
        next.add(key);
      } else {
        next.delete(key);
      }
      return next;
    });
  };

  const handleGroupToggle = (groupKey: string, checked: boolean) => {
    const group = PERMISSION_GROUPS.find((g) => g.key === groupKey);
    if (!group) return;

    setSelectedPermissions((prev) => {
      const next = new Set(prev);
      group.permissions.forEach((p) => {
        if (checked) {
          next.add(p.key);
        } else {
          next.delete(p.key);
        }
      });
      return next;
    });
  };

  const handleSave = async () => {
    if (!instructorId) return;
    try {
      const permissionsToSave = new Set(selectedPermissions);
      REQUIRED_PERMISSIONS.forEach((p) => permissionsToSave.add(p));

      await updatePermissions.mutateAsync({
        id: instructorId,
        permissions: Array.from(permissionsToSave),
      });
      showSuccess('권한이 저장되었습니다');
      onOpenChange(false);
    } catch (error) {
      showError(getApiErrorMessage(error, '권한 저장에 실패했습니다'));
    }
  };

  const handleResetClick = async () => {
    if (!instructorId) return;

    const { isConfirmed } = await confirmWarning({
      title: '권한 초기화',
      text: `${instructorName}의 권한을 기본값으로 초기화하시겠습니까? 기본 권한 ${DEFAULT_PERMISSIONS.length}개가 부여됩니다.`,
      confirmText: '초기화',
    });

    if (!isConfirmed) return;

    try {
      await resetPermissions.mutateAsync(instructorId);
      showSuccess('권한이 기본값으로 초기화되었습니다');
    } catch (error) {
      showError(getApiErrorMessage(error, '권한 초기화에 실패했습니다'));
    }
  };

  const handleOpenChange = (isOpen: boolean) => {
    if (!isOpen) {
      const swalContainer = document.querySelector('.swal2-container');
      if (swalContainer) return;
    }
    onOpenChange(isOpen);
  };

  const handleInteractOutside = (e: Event) => {
    const target = e.target as HTMLElement;
    if (
      target.closest('[role="dialog"]') ||
      target.closest('.swal2-container')
    ) {
      e.preventDefault();
    }
  };

  const hasChanges = detail?.permissions
    ? JSON.stringify([...selectedPermissions].sort()) !== JSON.stringify([...detail.permissions].sort())
    : false;

  return (
    <Sheet open={open} onOpenChange={handleOpenChange} modal={false}>
      <SheetContent
        className="w-full sm:max-w-lg overflow-y-auto"
        hideOverlay
        onInteractOutside={handleInteractOutside}
        onPointerDownOutside={handleInteractOutside}
      >
        <SheetHeader className="space-y-1">
          <SheetTitle className="text-lg">{instructorName} 권한 설정</SheetTitle>
          <SheetDescription>
            {readOnly
              ? '권한 수정 권한이 없어 조회만 가능합니다.'
              : '사범에게 부여할 권한을 선택하세요.'}
          </SheetDescription>
        </SheetHeader>

        {isLoading ? (
          <div className="flex items-center justify-center h-64">
            <Loader2 className="w-8 h-8 animate-spin text-primary" />
          </div>
        ) : (
          <div className="mt-6 space-y-4">
            <div className="flex items-center justify-between">
              <Badge variant="secondary">
                {selectedPermissions.size} / {TOTAL_PERMISSION_COUNT} 권한 선택됨
              </Badge>
              {!readOnly && (
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={handleResetClick}
                  disabled={resetPermissions.isPending}
                  className="text-slate-500 hover:text-slate-700"
                >
                  <RotateCcw className="h-4 w-4 mr-1" />
                  기본값으로 초기화
                </Button>
              )}
            </div>

            {PERMISSION_GROUPS.map((group) => (
              <PermissionGroupCard
                key={group.key}
                group={group}
                selectedPermissions={selectedPermissions}
                onPermissionChange={handlePermissionChange}
                onGroupToggle={handleGroupToggle}
                disabled={readOnly}
              />
            ))}
          </div>
        )}

        <SheetFooter className="mt-6 flex gap-2">
          <Button
            variant="outline"
            onClick={() => onOpenChange(false)}
            className="flex-1"
          >
            {readOnly ? '닫기' : '취소'}
          </Button>
          {!readOnly && (
            <Button
              onClick={handleSave}
              disabled={!hasChanges || updatePermissions.isPending}
              className="flex-1"
            >
              {updatePermissions.isPending ? (
                <>
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                  저장 중...
                </>
              ) : (
                '저장'
              )}
            </Button>
          )}
        </SheetFooter>
      </SheetContent>
    </Sheet>
  );
}
