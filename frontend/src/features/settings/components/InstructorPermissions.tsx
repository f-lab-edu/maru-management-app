import { useState } from 'react';
import { Loader2, Settings2, UserX, UserCheck } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../../../shared/components/ui/card';
import { Button } from '../../../shared/components/ui/button';
import { Avatar, AvatarFallback } from '../../../shared/components/ui/avatar';
import { Badge } from '../../../shared/components/ui/badge';
import { Label } from '../../../shared/components/ui/label';
import { Textarea } from '../../../shared/components/ui/textarea';
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
import { PermissionSheet } from './PermissionSheet';
import { useInstructors, useUpdateInstructorStatus } from '../../employment/hooks/useEmployment';
import { Instructor } from '../../../types/employment';

export function InstructorPermissions() {
  const { data: instructors, isLoading } = useInstructors();
  const updateStatus = useUpdateInstructorStatus();

  const [selectedInstructor, setSelectedInstructor] = useState<Instructor | null>(null);
  const [sheetOpen, setSheetOpen] = useState(false);
  const [statusDialogOpen, setStatusDialogOpen] = useState(false);
  const [statusDialogInstructor, setStatusDialogInstructor] = useState<Instructor | null>(null);
  const [statusReason, setStatusReason] = useState('');

  const handleOpenSheet = (instructor: Instructor) => {
    setSelectedInstructor(instructor);
    setSheetOpen(true);
  };

  const handleOpenStatusDialog = (instructor: Instructor) => {
    setStatusDialogInstructor(instructor);
    setStatusReason('');
    setStatusDialogOpen(true);
  };

  const handleStatusChange = async () => {
    if (!statusDialogInstructor) return;

    const newStatus = statusDialogInstructor.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';

    try {
      await updateStatus.mutateAsync({
        id: statusDialogInstructor.id,
        status: newStatus,
        reason: statusReason || undefined,
      });
      setStatusDialogOpen(false);
    } catch (error) {
      console.error('상태 변경 실패:', error);
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
            소속 사범의 권한을 개별적으로 설정할 수 있습니다.
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
                    onClick={() => handleOpenStatusDialog(instructor)}
                    disabled={updateStatus.isPending}
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
      />

      <AlertDialog open={statusDialogOpen} onOpenChange={setStatusDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>
              {statusDialogInstructor?.status === 'ACTIVE' ? '사범 정지' : '사범 활성화'}
            </AlertDialogTitle>
            <AlertDialogDescription>
              {statusDialogInstructor?.name} 사범을{' '}
              {statusDialogInstructor?.status === 'ACTIVE' ? '정지' : '활성화'}하시겠습니까?
            </AlertDialogDescription>
          </AlertDialogHeader>
          <div className="py-4">
            <Label htmlFor="reason" className="text-sm text-slate-700">
              사유 (선택사항)
            </Label>
            <Textarea
              id="reason"
              placeholder="사유를 입력하세요..."
              value={statusReason}
              onChange={(e) => setStatusReason(e.target.value)}
              className="mt-2"
              rows={3}
            />
          </div>
          <AlertDialogFooter>
            <AlertDialogCancel>취소</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleStatusChange}
              disabled={updateStatus.isPending}
              className={
                statusDialogInstructor?.status === 'ACTIVE'
                  ? 'bg-red-600 hover:bg-red-700'
                  : 'bg-green-600 hover:bg-green-700'
              }
            >
              {updateStatus.isPending
                ? '처리 중...'
                : statusDialogInstructor?.status === 'ACTIVE'
                  ? '정지'
                  : '활성화'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}
