import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from '@/shared/components/ui/sheet';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/shared/components/ui/tabs';
import { Skeleton } from '@/shared/components/ui/skeleton';
import { StatusBadge } from './StatusBadge';
import { StudentInfoTab } from './tabs/StudentInfoTab';
import { PaymentHistoryTab } from './tabs/PaymentHistoryTab';
import { AttendanceHistoryTab } from './tabs/AttendanceHistoryTab';
import type { Student } from '@/types/student';

interface StudentDetailDrawerProps {
  student: Student | null;
  isLoading: boolean;
  isOpen: boolean;
  onClose: () => void;
}

function DrawerSkeleton() {
  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Skeleton className="h-16 w-16 rounded-full" />
        <div className="space-y-2">
          <Skeleton className="h-6 w-32" />
          <Skeleton className="h-4 w-24" />
        </div>
      </div>
      <div className="space-y-4">
        <Skeleton className="h-10 w-full" />
        <Skeleton className="h-32 w-full" />
      </div>
    </div>
  );
}

export function StudentDetailDrawer({
  student,
  isLoading,
  isOpen,
  onClose,
}: StudentDetailDrawerProps) {
  const handleOpenChange = (open: boolean) => {
    if (!open) {
      // SweetAlert2가 열려있으면 Sheet를 닫지 않음
      const swalContainer = document.querySelector('.swal2-container');
      if (swalContainer) return;
      onClose();
    }
  };

  const handleInteractOutside = (e: Event) => {
    const target = e.target as HTMLElement;
    if (
      target.closest('tr') ||
      target.closest('button') ||
      target.closest('[role="dialog"]') ||
      target.closest('[data-radix-select-content]') ||
      target.closest('[data-radix-popper-content-wrapper]')
    ) {
      e.preventDefault();
    }
  };

  return (
    <Sheet open={isOpen} onOpenChange={handleOpenChange} modal={false}>
      <SheetContent
        side="right"
        className="w-[400px] overflow-y-auto sm:w-[540px]"
        aria-describedby={undefined}
        onInteractOutside={handleInteractOutside}
        onPointerDownOutside={handleInteractOutside}
      >
        <SheetHeader className="space-y-1 pt-4">
          <div className="flex items-center justify-between">
            <SheetTitle className="text-xl">
              {isLoading || !student ? '원생 정보' : student.name}
            </SheetTitle>
            {student && <StatusBadge status={student.status} />}
          </div>
        </SheetHeader>

        {isLoading || !student ? (
          <div className="mt-6">
            <DrawerSkeleton />
          </div>
        ) : (
          <div className="mt-6">
            <Tabs defaultValue="info">
              <TabsList className="grid w-full grid-cols-3">
                <TabsTrigger value="info">기본정보</TabsTrigger>
                <TabsTrigger value="payments">수납내역</TabsTrigger>
                <TabsTrigger value="attendance">출결이력</TabsTrigger>
              </TabsList>

              <TabsContent value="info" className="mt-4">
                <StudentInfoTab student={student} />
              </TabsContent>

              <TabsContent value="payments" className="mt-4">
                <PaymentHistoryTab studentId={student.id} />
              </TabsContent>

              <TabsContent value="attendance" className="mt-4">
                <AttendanceHistoryTab studentId={student.id} />
              </TabsContent>
            </Tabs>
          </div>
        )}
      </SheetContent>
    </Sheet>
  );
}
