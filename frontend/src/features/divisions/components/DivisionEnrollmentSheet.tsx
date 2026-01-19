import { useState } from 'react';
import { Loader2, UserMinus, UserPlus } from 'lucide-react';
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetDescription,
} from '../../../shared/components/ui/sheet';
import { Button } from '../../../shared/components/ui/button';
import { ScrollArea } from '../../../shared/components/ui/scroll-area';
import { useConfirm } from '../../../hooks/useSweetAlert/useConfirm';
import { useStudentsByDivision, useUnenrollStudent } from '../hooks';
import { StudentSelectDialog } from './StudentSelectDialog';
import type { DivisionRes } from '../types';

interface DivisionEnrollmentSheetProps {
  dojangId: string;
  division: DivisionRes | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function DivisionEnrollmentSheet({
  dojangId,
  division,
  open,
  onOpenChange,
}: DivisionEnrollmentSheetProps) {
  const [studentSelectOpen, setStudentSelectOpen] = useState(false);
  const { confirmDelete } = useConfirm();
  const { data: studentsData, isLoading } = useStudentsByDivision(
    dojangId,
    division?.id ?? null
  );
  const unenrollMutation = useUnenrollStudent(dojangId, division?.id ?? null);

  const students = studentsData?.students ?? [];
  const enrolledStudentIds = students.map((s) => s.studentId);

  const handleUnenroll = async (studentId: string, studentName: string) => {
    const { isConfirmed } = await confirmDelete({
      title: '원생 해제',
      text: `${studentName} 원생을 수련반에서 해제하시겠습니까?`,
      confirmText: '해제',
    });
    if (isConfirmed) {
      unenrollMutation.mutate(studentId);
    }
  };

  const handleOpenChange = (open: boolean) => {
    if (!open) {
      const swalContainer = document.querySelector('.swal2-container');
      if (swalContainer) return;
    }
    onOpenChange(open);
  };

  const handleInteractOutside = (e: Event) => {
    const target = e.target as HTMLElement;
    if (
      target.closest('[role="dialog"]') ||
      target.closest('[data-radix-select-content]') ||
      target.closest('[data-radix-popper-content-wrapper]') ||
      target.closest('.swal2-container')
    ) {
      e.preventDefault();
    }
  };

  return (
    <>
      <Sheet open={open} onOpenChange={handleOpenChange} modal={false}>
        <SheetContent
          className="w-full sm:max-w-lg"
          hideOverlay
          onInteractOutside={handleInteractOutside}
          onPointerDownOutside={handleInteractOutside}
        >
          <SheetHeader>
            <SheetTitle>{division?.name} 원생 관리</SheetTitle>
            <SheetDescription>
              {division?.name}에 등록된 원생 목록입니다.
            </SheetDescription>
          </SheetHeader>

          <div className="mt-6 space-y-4">
            <div className="flex items-center justify-between">
              <span className="text-sm text-slate-500">
                등록된 원생: {students.length}명
              </span>
              <Button size="sm" onClick={() => setStudentSelectOpen(true)}>
                <UserPlus className="h-4 w-4 mr-1" />
                원생 추가
              </Button>
            </div>

            {isLoading ? (
              <div className="flex items-center justify-center h-32">
                <Loader2 className="w-6 h-6 animate-spin text-primary" />
              </div>
            ) : students.length > 0 ? (
              <ScrollArea className="h-[calc(100vh-240px)]">
                <div className="space-y-2 pr-4">
                  {students.map((student) => (
                    <div
                      key={student.studentId}
                      className="flex items-center justify-between p-3 rounded-lg border border-slate-100 bg-white"
                    >
                      <div className="flex items-center gap-3">
                        <div>
                          <p className="font-medium text-slate-900">{student.studentName}</p>
                        </div>
                      </div>
                      <Button
                        size="sm"
                        variant="ghost"
                        className="text-red-500 hover:text-red-600 hover:bg-red-50"
                        onClick={() => handleUnenroll(student.studentId, student.studentName)}
                        disabled={unenrollMutation.isPending}
                      >
                        <UserMinus className="h-4 w-4 mr-1" />
                        해제
                      </Button>
                    </div>
                  ))}
                </div>
              </ScrollArea>
            ) : (
              <div className="text-center py-12 text-slate-500">
                등록된 원생이 없습니다.
              </div>
            )}
          </div>
        </SheetContent>
      </Sheet>

      {division && (
        <StudentSelectDialog
          dojangId={dojangId}
          divisionId={division.id}
          divisionName={division.name}
          enrolledStudentIds={enrolledStudentIds}
          open={studentSelectOpen}
          onOpenChange={setStudentSelectOpen}
        />
      )}
    </>
  );
}
