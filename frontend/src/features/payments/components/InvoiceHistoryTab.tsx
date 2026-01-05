import { useMemo } from 'react';
import { Button } from '@/shared/components/ui/button';
import { Card, CardContent } from '@/shared/components/ui/card';
import { Badge } from '@/shared/components/ui/badge';
import { Separator } from '@/shared/components/ui/separator';
import { cn } from '@/shared/utils';
import { Calendar, Wallet, CalendarPlus } from 'lucide-react';
import type { PaymentHistoryItem, StudentPaymentHistoryRes } from '../types';
import { PAYMENT_METHOD_LABEL } from '../types';
import { formatBillingYearMonth, extractYear } from '../utils';

interface InvoiceHistoryTabProps {
  paymentHistory: StudentPaymentHistoryRes | undefined;
  isLoading: boolean;
  showPrepaidSheet: boolean;
  setShowPrepaidSheet: (v: boolean) => void;
  formatAmount: (amount: number) => string;
}

export function InvoiceHistoryTab({
  paymentHistory,
  isLoading,
  showPrepaidSheet,
  setShowPrepaidSheet,
  formatAmount,
}: InvoiceHistoryTabProps) {
  const yearlyStats = useMemo(() => {
    const payments = paymentHistory?.payments ?? [];
    const grouped: Record<number, { total: number; count: number }> = {};

    payments.forEach((p: PaymentHistoryItem) => {
      if (p.status === 'REFUNDED') return;
      const year = extractYear(p.billingYearMonth);
      if (!grouped[year]) {
        grouped[year] = { total: 0, count: 0 };
      }
      grouped[year].total += p.amount;
      grouped[year].count += 1;
    });

    return Object.entries(grouped)
      .map(([year, data]) => ({ year: Number(year), ...data }))
      .sort((a, b) => b.year - a.year);
  }, [paymentHistory?.payments]);

  const paymentsByMonth = useMemo(() => {
    const payments = paymentHistory?.payments ?? [];
    const grouped: Record<string, PaymentHistoryItem[]> = {};

    payments.forEach((p: PaymentHistoryItem) => {
      const key = p.billingYearMonth;
      if (!grouped[key]) {
        grouped[key] = [];
      }
      grouped[key].push(p);
    });

    return Object.entries(grouped)
      .sort(([a], [b]) => b.localeCompare(a))
      .map(([yearMonth, items]) => {
        const total = items
          .filter((p) => p.status !== 'REFUNDED')
          .reduce((sum, p) => sum + p.amount, 0);
        const refundTotal = items
          .filter((p) => p.status === 'REFUNDED')
          .reduce((sum, p) => sum + p.amount, 0);
        return {
          yearMonth,
          payments: items.sort((a, b) => {
            const dateA = a.refundedAt ? new Date(a.refundedAt) : new Date(a.paidAt);
            const dateB = b.refundedAt ? new Date(b.refundedAt) : new Date(b.paidAt);
            return dateB.getTime() - dateA.getTime();
          }),
          total,
          refundTotal,
        };
      });
  }, [paymentHistory?.payments]);

  const totalPaidAmount = paymentHistory?.totalPaidAmount ?? 0;

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-8">
        <span className="text-muted-foreground">로딩 중...</span>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex gap-3">
        <Card className="flex-1 border-0 bg-gradient-to-r from-emerald-50 to-teal-50">
          <CardContent className="p-4">
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-full bg-emerald-100">
                <Wallet className="h-5 w-5 text-emerald-600" />
              </div>
              <div>
                <p className="text-sm text-muted-foreground">총 납부 금액</p>
                <p className="text-xl font-bold text-emerald-700">
                  ₩{formatAmount(totalPaidAmount)}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
        <Button
          variant="outline"
          className={cn(
            "h-auto flex-col gap-1 px-4 py-3 border-primary/30 hover:bg-primary/5 transition-colors",
            showPrepaidSheet && "bg-primary/10 border-primary"
          )}
          onClick={(e) => {
            e.stopPropagation();
            setShowPrepaidSheet(!showPrepaidSheet);
          }}
        >
          <CalendarPlus className={cn(
            "h-5 w-5 text-primary",
            showPrepaidSheet && "text-primary"
          )} />
          <span className="text-xs font-medium">선납 등록</span>
        </Button>
      </div>

      {yearlyStats.length > 0 && (
        <div className="space-y-2">
          <h4 className="font-medium text-sm text-muted-foreground">연도별 납부</h4>
          <div className="grid gap-2">
            {yearlyStats.map((stat) => (
              <div
                key={stat.year}
                className="flex items-center justify-between rounded-lg border p-3"
              >
                <div className="flex items-center gap-2">
                  <Calendar className="h-4 w-4 text-muted-foreground" />
                  <span className="font-medium">{stat.year}년</span>
                  <Badge variant="secondary" className="text-xs">
                    {stat.count}건
                  </Badge>
                </div>
                <span className="font-semibold text-emerald-600">
                  ₩{formatAmount(stat.total)}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}

      <Separator />

      <div className="space-y-4">
        <h4 className="font-medium text-sm text-muted-foreground">
          월별 납부 내역
        </h4>
        {paymentsByMonth.length === 0 ? (
          <p className="text-sm text-muted-foreground py-4 text-center">
            납부 내역이 없습니다
          </p>
        ) : (
          <div className="space-y-4">
            {paymentsByMonth.map((monthData) => (
              <div key={monthData.yearMonth} className="space-y-2">
                <div className="flex items-center justify-between">
                  <span className="font-semibold text-sm">
                    {formatBillingYearMonth(monthData.yearMonth)}
                  </span>
                  <span className="text-emerald-600 font-medium text-sm">
                    ₩{formatAmount(monthData.total)}
                  </span>
                </div>
                <div className="space-y-1 pl-2 border-l-2 border-muted">
                  {monthData.payments.map((payment: PaymentHistoryItem) => {
                    const isRefunded = payment.status === 'REFUNDED';
                    return (
                      <div
                        key={payment.paymentId}
                        className={cn(
                          'flex items-center justify-between rounded-md p-2 text-sm',
                          isRefunded ? 'bg-red-50' : 'bg-muted/30'
                        )}
                      >
                        <div className="flex items-center gap-2">
                          {isRefunded ? (
                            <Badge variant="destructive" className="text-xs">
                              환불
                            </Badge>
                          ) : (
                            <Badge variant="outline" className="text-xs">
                              {PAYMENT_METHOD_LABEL[payment.method as keyof typeof PAYMENT_METHOD_LABEL] ?? payment.method}
                            </Badge>
                          )}
                          <span className="text-xs text-muted-foreground">
                            {isRefunded ? payment.refundedAt : payment.paidAt}
                          </span>
                        </div>
                        <span className={cn(
                          'font-medium',
                          isRefunded ? 'text-red-600' : 'text-emerald-600'
                        )}>
                          ₩{formatAmount(payment.amount)}
                        </span>
                      </div>
                    );
                  })}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
