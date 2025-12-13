import { useState } from 'react';
import { MoreHorizontal, CheckCircle, LogOut, Edit } from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/shared/components/ui/dropdown-menu';
import { AttendanceStatusEditDialog } from './AttendanceStatusEditDialog';
import { useAttendanceCheckout } from '../hooks/useAttendanceMutations';
import type { AttendanceStatus } from '../types';

interface AttendanceRowActionsProps {
  student: {
    id: string;
    name: string;
    status: AttendanceStatus | null;
    checkoutAt: string | null;
  };
  date: string;
  attendanceId?: string;
  dojangId: number | null;
  onCheckInClick?: () => void;
}

export function AttendanceRowActions({
  student,
  date,
  attendanceId,
  dojangId,
  onCheckInClick,
}: AttendanceRowActionsProps) {
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const { mutate: checkout, isPending: isCheckingOut } = useAttendanceCheckout(dojangId);

  const handleCheckout = () => {
    if (!attendanceId) {
      console.error('출석 ID가 없습니다');
      return;
    }

    checkout({
      id: attendanceId,
      checkoutAt: new Date().toISOString(),
    });
  };

  const handleEditClick = () => {
    setEditDialogOpen(true);
  };

  const isPresent = student.status === 'PRESENT';
  const isCheckedIn = isPresent && !student.checkoutAt;

  return (
    <>
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant="ghost" size="sm" className="h-8 w-8 p-0">
            <MoreHorizontal className="h-4 w-4" />
            <span className="sr-only">메뉴 열기</span>
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end">
          {/* 미출석: 출석 체크 버튼 */}
          {!student.status && (
            <DropdownMenuItem onClick={onCheckInClick}>
              <CheckCircle className="mr-2 h-4 w-4" />
              출석 체크
            </DropdownMenuItem>
          )}

          {/* 재관 중: 퇴관 버튼 */}
          {isCheckedIn && (
            <DropdownMenuItem onClick={handleCheckout} disabled={isCheckingOut}>
              <LogOut className="mr-2 h-4 w-4" />
              {isCheckingOut ? '퇴관 처리 중...' : '퇴관'}
            </DropdownMenuItem>
          )}

          {/* 출석/결석 상태가 있는 경우: 상태 수정 버튼 */}
          {student.status && attendanceId && (
            <DropdownMenuItem onClick={handleEditClick}>
              <Edit className="mr-2 h-4 w-4" />
              상태 수정
            </DropdownMenuItem>
          )}
        </DropdownMenuContent>
      </DropdownMenu>

      {/* 상태 수정 다이얼로그 */}
      {student.status && attendanceId && (
        <AttendanceStatusEditDialog
          attendance={
            editDialogOpen
              ? {
                  id: attendanceId,
                  studentName: student.name,
                  date,
                  currentStatus: student.status,
                }
              : null
          }
          open={editDialogOpen}
          onOpenChange={setEditDialogOpen}
          dojangId={dojangId}
        />
      )}
    </>
  );
}
