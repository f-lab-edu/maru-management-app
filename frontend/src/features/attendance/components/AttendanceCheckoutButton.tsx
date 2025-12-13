import { Button } from '@/shared/components/ui/button';
import { useAttendanceCheckout } from '../hooks/useAttendanceMutations';

interface AttendanceCheckoutButtonProps {
  attendanceId: string;
  studentName: string;
  onSuccess?: () => void;
  dojangId?: number | null;
}

export function AttendanceCheckoutButton({
  attendanceId,
  studentName,
  onSuccess,
  dojangId = null,
}: AttendanceCheckoutButtonProps) {
  const { mutate: checkout, isPending } = useAttendanceCheckout(dojangId);

  const handleCheckout = () => {
    checkout(
      {
        id: attendanceId,
        checkoutAt: new Date().toISOString(),
      },
      {
        onSuccess: () => {
          console.log(`${studentName} 퇴관 완료`);
          onSuccess?.();
        },
      }
    );
  };

  return (
    <Button
      variant="outline"
      size="sm"
      onClick={handleCheckout}
      disabled={isPending}
    >
      {isPending ? '처리 중...' : '퇴관'}
    </Button>
  );
}
