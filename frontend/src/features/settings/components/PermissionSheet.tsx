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
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '../../../shared/components/ui/alert-dialog';
import { PermissionGroupCard } from './PermissionGroupCard';
import { PERMISSION_GROUPS, DEFAULT_PERMISSIONS, TOTAL_PERMISSION_COUNT } from '../constants/permissions';
import {
  useInstructorDetail,
  useUpdatePermissions,
  useResetPermissions,
} from '../../employment/hooks/useEmployment';

interface PermissionSheetProps {
  instructorId: string | null;
  instructorName: string;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function PermissionSheet({
  instructorId,
  instructorName,
  open,
  onOpenChange,
}: PermissionSheetProps) {
  const { data: detail, isLoading } = useInstructorDetail(instructorId ?? '', open && !!instructorId);
  const updatePermissions = useUpdatePermissions();
  const resetPermissions = useResetPermissions();

  const [selectedPermissions, setSelectedPermissions] = useState<Set<string>>(new Set());
  const [showResetDialog, setShowResetDialog] = useState(false);

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
      await updatePermissions.mutateAsync({
        id: instructorId,
        permissions: Array.from(selectedPermissions),
      });
      onOpenChange(false);
    } catch (error) {
      console.error('권한 저장 실패:', error);
    }
  };

  const handleReset = async () => {
    if (!instructorId) return;
    try {
      await resetPermissions.mutateAsync(instructorId);
      setShowResetDialog(false);
    } catch (error) {
      console.error('권한 초기화 실패:', error);
    }
  };

  const hasChanges = detail?.permissions
    ? JSON.stringify([...selectedPermissions].sort()) !== JSON.stringify([...detail.permissions].sort())
    : false;

  return (
    <>
      <Sheet open={open} onOpenChange={onOpenChange}>
        <SheetContent className="w-full sm:max-w-lg overflow-y-auto">
          <SheetHeader className="space-y-1">
            <SheetTitle className="text-lg">{instructorName} 권한 설정</SheetTitle>
            <SheetDescription>
              사범에게 부여할 권한을 선택하세요.
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
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => setShowResetDialog(true)}
                  disabled={resetPermissions.isPending}
                  className="text-slate-500 hover:text-slate-700"
                >
                  <RotateCcw className="h-4 w-4 mr-1" />
                  기본값으로 초기화
                </Button>
              </div>

              {PERMISSION_GROUPS.map((group) => (
                <PermissionGroupCard
                  key={group.key}
                  group={group}
                  selectedPermissions={selectedPermissions}
                  onPermissionChange={handlePermissionChange}
                  onGroupToggle={handleGroupToggle}
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
              취소
            </Button>
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
          </SheetFooter>
        </SheetContent>
      </Sheet>

      <AlertDialog open={showResetDialog} onOpenChange={setShowResetDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>권한 초기화</AlertDialogTitle>
            <AlertDialogDescription>
              {instructorName}의 권한을 기본값으로 초기화하시겠습니까?
              <br />
              기본 권한 {DEFAULT_PERMISSIONS.length}개가 부여됩니다.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>취소</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleReset}
              disabled={resetPermissions.isPending}
            >
              {resetPermissions.isPending ? '초기화 중...' : '초기화'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}
