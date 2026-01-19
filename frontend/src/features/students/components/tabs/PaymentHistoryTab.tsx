import { useState } from 'react';
import { CreditCard, CheckCircle2, XCircle, Plus } from 'lucide-react';
import { Badge } from '@/shared/components/ui/badge';
import { Button } from '@/shared/components/ui/button';
import { Skeleton } from '@/shared/components/ui/skeleton';
import { useAuthStore } from '@/stores/authStore';
import { useStudentPaymentHistory } from '@/features/payments/hooks';
import { InvoiceCreateSheet } from '@/features/payments/components';
import type { PaymentHistoryItem } from '@/features/payments/types';
import { formatBillingYearMonth } from '@/features/payments/utils';
import { PAYMENT_METHOD_LABEL } from '@/features/payments/types';

interface PaymentHistoryTabProps {
  studentId: string;
}

const PAYMENT_STATUS_CONFIG: Record<
  string,
  { label: string; className: string; icon: typeof CheckCircle2 }
> = {
  COMPLETED: {
    label: '완료',
    className: 'bg-green-100 text-green-800',
    icon: CheckCircle2,
  },
  REFUNDED: {
    label: '환불',
    className: 'bg-red-100 text-red-800',
    icon: XCircle,
  },
};

export function PaymentHistoryTab({ studentId }: PaymentHistoryTabProps) {
  const { selectedDojang } = useAuthStore();
  const dojangId = selectedDojang?.dojangId ?? null;

  const [showCreateSheet, setShowCreateSheet] = useState(false);

  const { data: history, isLoading } = useStudentPaymentHistory(dojangId, studentId);

  const formatAmount = (amount: number) => {
    return new Intl.NumberFormat('ko-KR').format(amount) + '원';
  };

  const formatDate = (dateStr: string | null) => {
    if (!dateStr) return '-';
    return dateStr.split(' ')[0];
  };

  if (isLoading) {
    return (
      <div className="space-y-4">
        <div className="flex items-center gap-2 text-sm text-muted-foreground">
          <CreditCard className="h-4 w-4" />
          <span>수납 내역</span>
        </div>
        <div className="space-y-2">
          {[1, 2, 3].map((i) => (
            <Skeleton key={i} className="h-16 w-full" />
          ))}
        </div>
      </div>
    );
  }

  const payments = history?.payments ?? [];

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2 text-sm text-muted-foreground">
          <CreditCard className="h-4 w-4" />
          <span>수납 내역</span>
          {history?.totalPaidAmount !== undefined && history.totalPaidAmount > 0 && (
            <span className="text-xs">
              (총 {formatAmount(history.totalPaidAmount)})
            </span>
          )}
        </div>
        <Button size="sm" variant="outline" onClick={() => setShowCreateSheet(true)}>
          <Plus className="mr-1 h-3 w-3" />
          청구서 생성
        </Button>
      </div>

      {payments.length === 0 ? (
        <div className="text-center py-8 text-muted-foreground">
          <CreditCard className="h-8 w-8 mx-auto mb-2 opacity-50" />
          <p>수납 내역이 없습니다</p>
        </div>
      ) : (
        <div className="space-y-2">
          {payments.map((payment: PaymentHistoryItem) => {
            const config = PAYMENT_STATUS_CONFIG[payment.status] ?? PAYMENT_STATUS_CONFIG.COMPLETED;
            const Icon = config.icon;
            const methodLabel = PAYMENT_METHOD_LABEL[payment.method as keyof typeof PAYMENT_METHOD_LABEL] ?? payment.method;

            return (
              <div
                key={payment.paymentId}
                className="flex items-center justify-between rounded-lg border p-3"
              >
                <div className="flex items-center gap-3">
                  <Icon
                    className={`h-5 w-5 ${
                      payment.status === 'COMPLETED' ? 'text-green-600' : 'text-red-600'
                    }`}
                  />
                  <div>
                    <p className="font-medium">
                      {formatBillingYearMonth(payment.billingYearMonth)}
                    </p>
                    <p className="text-sm text-muted-foreground">
                      {formatAmount(payment.amount)} · {methodLabel} · {formatDate(payment.paidAt)}
                    </p>
                  </div>
                </div>
                <Badge variant="outline" className={`border-0 ${config.className}`}>
                  {config.label}
                </Badge>
              </div>
            );
          })}
        </div>
      )}

      <p className="text-center text-xs text-muted-foreground">
        전체 수납 내역은 수납 관리 메뉴에서 확인하세요.
      </p>

      <InvoiceCreateSheet
        isOpen={showCreateSheet}
        onClose={() => setShowCreateSheet(false)}
        preSelectedStudentId={studentId}
      />
    </div>
  );
}
