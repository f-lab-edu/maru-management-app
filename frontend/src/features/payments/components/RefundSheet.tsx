import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useAuthStore } from '@/stores/authStore';
import { useAlert } from '@/hooks';
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from '@/shared/components/ui/sheet';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import { Textarea } from '@/shared/components/ui/textarea';
import { Badge } from '@/shared/components/ui/badge';
import { useRefundPayment } from '../hooks';
import type { PaymentRes } from '../types';
import { PAYMENT_METHOD_LABEL } from '../types';

const REFUND_REASON_MAX_LENGTH = 200;

const refundSchema = z.object({
  amount: z.number().positive('환불 금액은 0보다 커야 합니다'),
  reason: z
    .string()
    .min(1, '환불 사유를 입력해주세요')
    .max(REFUND_REASON_MAX_LENGTH, `환불 사유는 ${REFUND_REASON_MAX_LENGTH}자 이내여야 합니다`),
});

type RefundFormData = z.infer<typeof refundSchema>;

interface RefundSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  payment: PaymentRes;
  onSuccess: () => void;
}

export function RefundSheet({ open, onOpenChange, payment, onSuccess }: RefundSheetProps) {
  const { selectedDojang } = useAuthStore();
  const dojangId = selectedDojang?.dojangId ?? null;

  const { showSuccess, showError } = useAlert();
  const { mutateAsync: refundPayment, isPending } = useRefundPayment(dojangId);

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm<RefundFormData>({
    resolver: zodResolver(refundSchema),
    defaultValues: {
      amount: payment.amount,
      reason: '',
    },
  });

  const currentAmount = watch('amount');

  const formatAmount = (amount: number) => {
    return new Intl.NumberFormat('ko-KR').format(amount);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const onSubmit = async (data: RefundFormData) => {
    if (data.amount > payment.amount) {
      showError('환불 금액이 결제 금액을 초과할 수 없습니다');
      return;
    }

    try {
      await refundPayment({
        paymentId: payment.id,
        request: { amount: data.amount, reason: data.reason },
      });
      showSuccess('환불이 완료되었습니다');
      onSuccess();
      onOpenChange(false);
    } catch {
      showError('환불 처리에 실패했습니다');
    }
  };

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent
        side="right"
        className="w-[360px] sm:w-[400px]"
        aria-describedby={undefined}
      >
        <SheetHeader>
          <SheetTitle>환불</SheetTitle>
        </SheetHeader>

        <div className="mt-6 space-y-6">
          <div className="rounded-lg border p-4 space-y-2">
            <h4 className="text-sm font-medium text-muted-foreground">결제 정보</h4>
            <div className="grid grid-cols-2 gap-2 text-sm">
              <span className="text-muted-foreground">결제 금액</span>
              <span className="font-medium">₩{formatAmount(payment.amount)}</span>
              <span className="text-muted-foreground">결제 수단</span>
              <span>{PAYMENT_METHOD_LABEL[payment.method] ?? payment.method}</span>
              <span className="text-muted-foreground">결제일</span>
              <span>{formatDate(payment.paidAt)}</span>
              <span className="text-muted-foreground">채널</span>
              <span>
                {payment.channel === 'PG' ? (
                  <Badge className="bg-blue-100 text-blue-800 hover:bg-blue-100">PG결제</Badge>
                ) : (
                  '현장결제'
                )}
              </span>
            </div>
          </div>

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <Label htmlFor="refund-amount">환불 금액</Label>
                <span className="text-xs text-muted-foreground">
                  최대: ₩{formatAmount(payment.amount)}
                </span>
              </div>
              <Input
                id="refund-amount"
                type="number"
                {...register('amount', { valueAsNumber: true })}
              />
              {errors.amount && (
                <p className="text-sm text-destructive">{errors.amount.message}</p>
              )}
              {currentAmount > payment.amount && (
                <p className="text-sm text-destructive">결제 금액을 초과할 수 없습니다</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="refund-reason">환불 사유 *</Label>
              <Textarea
                id="refund-reason"
                placeholder="환불 사유를 입력해주세요"
                {...register('reason')}
                rows={3}
              />
              {errors.reason && (
                <p className="text-sm text-destructive">{errors.reason.message}</p>
              )}
            </div>

            <div className="flex gap-2 pt-2">
              <Button
                type="button"
                variant="outline"
                onClick={() => onOpenChange(false)}
                className="flex-1"
              >
                취소
              </Button>
              <Button
                type="submit"
                variant="destructive"
                disabled={isPending || currentAmount > payment.amount}
                className="flex-1"
              >
                {isPending ? '처리 중...' : '환불하기'}
              </Button>
            </div>
          </form>
        </div>
      </SheetContent>
    </Sheet>
  );
}
