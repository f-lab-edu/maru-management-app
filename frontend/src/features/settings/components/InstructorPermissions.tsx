import { useState } from 'react';
import { Loader2, Settings2, UserX, UserCheck } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../../../shared/components/ui/card';
import { Button } from '../../../shared/components/ui/button';
import { Avatar, AvatarFallback } from '../../../shared/components/ui/avatar';
import { Badge } from '../../../shared/components/ui/badge';
import { PermissionSheet } from './PermissionSheet';
import { useInstructors, useUpdateInstructorStatus } from '../../employment/hooks/useEmployment';
import { usePermissions } from '../../../hooks/usePermissions';
import { useConfirm } from '../../../hooks/useSweetAlert/useConfirm';
import { useAlert } from '../../../hooks/useSweetAlert/useAlert';
import { getApiErrorMessage } from '../../../constants/errorMessages';
import { Instructor } from '../../../types/employment';

export function InstructorPermissions() {
  const { data: instructors, isLoading } = useInstructors();
  const updateStatus = useUpdateInstructorStatus();
  const { isOwner } = usePermissions();
  const { confirmWarning, confirmDelete } = useConfirm();
  const { showError, showSuccess } = useAlert();

  const [selectedInstructor, setSelectedInstructor] = useState<Instructor | null>(null);
  const [sheetOpen, setSheetOpen] = useState(false);

  const handleOpenSheet = (instructor: Instructor) => {
    setSelectedInstructor(instructor);
    setSheetOpen(true);
  };

  const handleStatusChange = async (instructor: Instructor) => {
    const isSuspending = instructor.status === 'ACTIVE';

    const { isConfirmed } = await (isSuspending ? confirmDelete : confirmWarning)({
      title: isSuspending ? '사범 정지' : '사범 활성화',
      text: `${instructor.name} 사범을 ${isSuspending ? '정지' : '활성화'}하시겠습니까?`,
      confirmText: isSuspending ? '정지' : '활성화',
    });

    if (!isConfirmed) return;

    try {
      await updateStatus.mutateAsync({
        id: instructor.id,
        status: isSuspending ? 'SUSPENDED' : 'ACTIVE',
      });
      showSuccess(`${instructor.name} 사범이 ${isSuspending ? '정지' : '활성화'}되었습니다`);
    } catch (error) {
      showError(getApiErrorMessage(error, '상태 변경에 실패했습니다'));
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    );
  }

  const instructorList = instructors ?? [];

  return (
    <>
      <Card className="border-none shadow-sm">
        <CardHeader>
          <CardTitle className="text-lg flex items-center gap-2">
            사범 권한 관리
          </CardTitle>
          <CardDescription>
            {isOwner
              ? '소속 사범의 권한을 개별적으로 설정할 수 있습니다.'
              : '소속 사범의 권한을 조회할 수 있습니다. (수정 권한 없음)'}
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {instructorList.map((instructor) => (
              <div
                key={instructor.id}
                className="flex items-center justify-between p-4 rounded-lg border border-slate-100 bg-white hover:border-primary/20 transition-colors"
              >
                <div className="flex items-center gap-4">
                  <Avatar className="h-10 w-10 bg-slate-100">
                    <AvatarFallback className="text-slate-600">
                      {instructor.name[0]}
                    </AvatarFallback>
                  </Avatar>
                  <div>
                    <div className="flex items-center gap-2">
                      <p className="font-medium text-slate-900">{instructor.name}</p>
                      <Badge
                        variant={instructor.status === 'ACTIVE' ? 'default' : 'destructive'}
                        className="text-xs"
                      >
                        {instructor.status === 'ACTIVE' ? '활성' : '정지'}
                      </Badge>
                    </div>
                    <p className="text-sm text-slate-500 mt-0.5">
                      {instructor.email}
                      {instructor.phone && ` • ${instructor.phone}`}
                    </p>
                    <p className="text-xs text-slate-400 mt-1">
                      가입일: {new Date(instructor.joinedAt).toLocaleDateString('ko-KR')}
                      {instructor.suspendedAt && (
                        <span className="text-red-400 ml-2">
                          정지일: {new Date(instructor.suspendedAt).toLocaleDateString('ko-KR')}
                        </span>
                      )}
                    </p>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => handleStatusChange(instructor)}
                    disabled={!isOwner || updateStatus.isPending}
                    className={
                      instructor.status === 'ACTIVE'
                        ? 'text-red-600 hover:text-red-700 hover:bg-red-50 border-red-100'
                        : 'text-green-600 hover:text-green-700 hover:bg-green-50 border-green-100'
                    }
                  >
                    {instructor.status === 'ACTIVE' ? (
                      <>
                        <UserX className="h-4 w-4 mr-1" />
                        정지
                      </>
                    ) : (
                      <>
                        <UserCheck className="h-4 w-4 mr-1" />
                        활성화
                      </>
                    )}
                  </Button>
                  <Button
                    size="sm"
                    onClick={() => handleOpenSheet(instructor)}
                    className="bg-primary hover:bg-primary/90"
                  >
                    <Settings2 className="h-4 w-4 mr-1" />
                    권한 설정
                  </Button>
                </div>
              </div>
            ))}
            {instructorList.length === 0 && (
              <div className="text-center py-8 text-slate-500">
                등록된 사범이 없습니다.
              </div>
            )}
          </div>
        </CardContent>
      </Card>

      <PermissionSheet
        instructorId={selectedInstructor?.id ?? null}
        instructorName={selectedInstructor?.name ?? ''}
        open={sheetOpen}
        onOpenChange={setSheetOpen}
        readOnly={!isOwner}
      />
    </>
  );
}
