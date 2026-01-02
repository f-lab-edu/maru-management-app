import { Button } from '@/shared/components/ui/button';
import { Badge } from '@/shared/components/ui/badge';
import { RotateCcw } from 'lucide-react';
import type { PaymentRes } from '../types';
import { PAYMENT_METHOD_LABEL } from '../types';

interface PaymentListProps {
  payments: PaymentRes[];
  onCancelPayment: (paymentId: string) => void;
  canCancel: boolean;
}

export function PaymentList({ payments, onCancelPayment, canCancel }: PaymentListProps) {
  if (payments.length === 0) {
    return (
      <p className="text-sm text-muted-foreground text-center py-4">
        수납 내역이 없습니다
      </p>
    );
  }

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

  return (
    <div className="space-y-3">
      {payments.map((payment) => (
        <div
          key={payment.id}
          className="flex items-center justify-between p-3 border rounded-lg"
        >
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <span className="font-medium">₩{formatAmount(payment.amount)}</span>
              <Badge variant="outline">
                {PAYMENT_METHOD_LABEL[payment.method] ?? payment.method}
              </Badge>
              {payment.status === 'REFUNDED' && (
                <Badge variant="destructive">환불됨</Badge>
              )}
            </div>
            <p className="text-xs text-muted-foreground">
              {formatDate(payment.paidAt)}
            </p>
            {payment.status === 'REFUNDED' && payment.refundedAt && (
              <p className="text-xs text-destructive">
                환불: {formatDate(payment.refundedAt)}
              </p>
            )}
          </div>

          {canCancel && payment.status !== 'REFUNDED' && (
            <Button
              variant="ghost"
              size="sm"
              onClick={() => onCancelPayment(payment.id)}
              className="text-muted-foreground hover:text-destructive"
            >
              <RotateCcw className="h-4 w-4" />
            </Button>
          )}
        </div>
      ))}
    </div>
  );
}
