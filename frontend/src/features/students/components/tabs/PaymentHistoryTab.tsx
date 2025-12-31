import { useState } from 'react';
import { CreditCard, AlertCircle, CheckCircle2, Plus } from 'lucide-react';
import { Badge } from '@/shared/components/ui/badge';
import { Button } from '@/shared/components/ui/button';
import { Skeleton } from '@/shared/components/ui/skeleton';
import { useAuthStore } from '@/stores/authStore';
import { useStudentPaymentHistory } from '@/features/payments/hooks';
import { InvoiceCreateSheet } from '@/features/payments/components';
import type { InvoiceStatus, StudentInvoiceSummary } from '@/features/payments/types';

interface PaymentHistoryTabProps {
  studentId: string;
}

const STATUS_CONFIG: Record<
  InvoiceStatus,
  { label: string; className: string; icon: typeof CheckCircle2 }
> = {
  PAID: {
    label: '완납',
    className: 'bg-green-100 text-green-800',
    icon: CheckCircle2,
  },
  OPEN: {
    label: '미납',
    className: 'bg-red-100 text-red-800',
    icon: AlertCircle,
  },
  PARTIAL: {
    label: '부분납부',
    className: 'bg-yellow-100 text-yellow-800',
    icon: AlertCircle,
  },
  DRAFT: {
    label: '임시저장',
    className: 'bg-gray-100 text-gray-800',
    icon: AlertCircle,
  },
  VOID: {
    label: '무효',
    className: 'bg-slate-100 text-slate-800',
    icon: AlertCircle,
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

  const invoices = history?.invoices ?? [];

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2 text-sm text-muted-foreground">
          <CreditCard className="h-4 w-4" />
          <span>수납 내역</span>
        </div>
        <Button size="sm" variant="outline" onClick={() => setShowCreateSheet(true)}>
          <Plus className="mr-1 h-3 w-3" />
          청구서 생성
        </Button>
      </div>

      {invoices.length === 0 ? (
        <div className="text-center py-8 text-muted-foreground">
          <CreditCard className="h-8 w-8 mx-auto mb-2 opacity-50" />
          <p>수납 내역이 없습니다</p>
        </div>
      ) : (
        <div className="space-y-2">
          {invoices.map((invoice: StudentInvoiceSummary) => {
            const config = STATUS_CONFIG[invoice.status];
            const Icon = config.icon;

            return (
              <div
                key={invoice.id}
                className="flex items-center justify-between rounded-lg border p-3"
              >
                <div className="flex items-center gap-3">
                  <Icon
                    className={`h-5 w-5 ${
                      invoice.status === 'PAID' ? 'text-green-600' : 'text-red-600'
                    }`}
                  />
                  <div>
                    <p className="font-medium">
                      {invoice.billingYear}년 {invoice.billingMonth}월
                    </p>
                    <p className="text-sm text-muted-foreground">
                      {formatAmount(invoice.amount)}
                      {invoice.status === 'PARTIAL' && (
                        <span className="ml-1">
                          (납부: {formatAmount(invoice.paidAmount)})
                        </span>
                      )}
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
