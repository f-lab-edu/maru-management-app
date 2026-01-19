import { useState, useMemo } from 'react';
import { useAuthStore } from '@/stores/authStore';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { Wallet, TrendingUp, TrendingDown } from 'lucide-react';
import { usePaymentStatistics, useYearlyStatistics } from '../hooks';
import { PaymentRatePieChart } from './charts/PaymentRatePieChart';
import { PaymentTrendChart } from './charts/PaymentTrendChart';
import { PAYMENT_COLORS } from '../constants/chartColors';

type ViewMode = 'monthly' | 'yearly';

export function StatsTab() {
  const { selectedDojang } = useAuthStore();
  const dojangId = selectedDojang?.dojangId ?? null;

  const now = new Date();
  const [viewMode, setViewMode] = useState<ViewMode>('monthly');
  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth() + 1);

  const { data: monthlyStats, isLoading: isMonthlyLoading } = usePaymentStatistics(
    dojangId,
    year,
    month
  );
  const { data: yearlyStats, isLoading: isYearlyLoading } = useYearlyStatistics(dojangId, year);

  const isLoading = viewMode === 'monthly' ? isMonthlyLoading : isYearlyLoading;

  const yearOptions = useMemo(() => {
    const currentYear = now.getFullYear();
    return Array.from({ length: 5 }, (_, i) => currentYear - 2 + i);
  }, []);

  const monthOptions = Array.from({ length: 12 }, (_, i) => i + 1);

  const formatAmount = (amount: number = 0) => {
    return new Intl.NumberFormat('ko-KR').format(amount);
  };

  const stats = viewMode === 'monthly' ? monthlyStats : yearlyStats;

  const totalAmount = stats
    ? (stats.totalPaidAmount ?? 0) + (stats.totalUnpaidAmount ?? 0)
    : 0;

  const paymentRate = useMemo(() => {
    if (!stats) return 0;
    const total = stats.totalPaidAmount + stats.totalUnpaidAmount;
    if (total === 0) return 0;
    return Math.round((stats.totalPaidAmount / total) * 100);
  }, [stats]);

  const periodLabel =
    viewMode === 'monthly' ? `${year}년 ${month}월` : `${year}년`;

  return (
    <div className="flex flex-col gap-6 flex-1 overflow-auto">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <span className="text-sm font-medium text-muted-foreground">조회 기간</span>
          <Select value={year.toString()} onValueChange={(v) => setYear(parseInt(v))}>
            <SelectTrigger className="w-[100px]">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {yearOptions.map((y) => (
                <SelectItem key={y} value={y.toString()}>
                  {y}년
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          {viewMode === 'monthly' && (
            <Select value={month.toString()} onValueChange={(v) => setMonth(parseInt(v))}>
              <SelectTrigger className="w-[80px]">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {monthOptions.map((m) => (
                  <SelectItem key={m} value={m.toString()}>
                    {m}월
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          )}
        </div>

        <div className="flex gap-1 p-1 bg-muted rounded-lg">
          <Button
            variant={viewMode === 'monthly' ? 'default' : 'ghost'}
            size="default"
            className="px-5"
            onClick={() => setViewMode('monthly')}
          >
            월간
          </Button>
          <Button
            variant={viewMode === 'yearly' ? 'default' : 'ghost'}
            size="default"
            className="px-5"
            onClick={() => setViewMode('yearly')}
          >
            연간
          </Button>
        </div>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center py-12 text-muted-foreground">
          로딩 중...
        </div>
      ) : (
        <>
          <div className="grid gap-4 grid-cols-1 md:grid-cols-3">
            <Card className="border shadow-sm hover:shadow-md transition-shadow">
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  총 청구액
                </CardTitle>
                <div className="p-2 rounded-full bg-blue-50">
                  <Wallet className="h-4 w-4 text-blue-500" />
                </div>
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">₩{formatAmount(totalAmount)}</div>
                <p className="text-xs text-muted-foreground mt-1">
                  {periodLabel} 기준
                </p>
              </CardContent>
            </Card>

            <Card className="border shadow-sm hover:shadow-md transition-shadow">
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  수납 완료
                </CardTitle>
                <div className="p-2 rounded-full" style={{ backgroundColor: PAYMENT_COLORS.paid.light }}>
                  <TrendingUp className="h-4 w-4" style={{ color: PAYMENT_COLORS.paid.main }} />
                </div>
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold" style={{ color: PAYMENT_COLORS.paid.dark }}>
                  ₩{formatAmount(stats?.totalPaidAmount)}
                </div>
                <p className="text-xs text-muted-foreground mt-1">
                  {stats?.paidInvoiceCount ?? 0}건 완납
                </p>
              </CardContent>
            </Card>

            <Card className="border shadow-sm hover:shadow-md transition-shadow">
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  미수납
                </CardTitle>
                <div className="p-2 rounded-full" style={{ backgroundColor: PAYMENT_COLORS.unpaid.light }}>
                  <TrendingDown className="h-4 w-4" style={{ color: PAYMENT_COLORS.unpaid.main }} />
                </div>
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold" style={{ color: PAYMENT_COLORS.unpaid.dark }}>
                  ₩{formatAmount(stats?.totalUnpaidAmount)}
                </div>
                <p className="text-xs text-muted-foreground mt-1">
                  {stats?.unpaidInvoiceCount ?? 0}건 미납
                </p>
              </CardContent>
            </Card>
          </div>

          <div className="grid gap-4 grid-cols-1 lg:grid-cols-3 flex-1 min-h-[300px]">
            <div className="lg:col-span-2 min-h-[300px]">
              {viewMode === 'yearly' && yearlyStats ? (
                <PaymentTrendChart
                  data={yearlyStats.monthlyData}
                  title={`${year}년 월별 수납 추이`}
                />
              ) : (
                <Card className="border shadow-sm h-full flex flex-col">
                  <CardHeader className="pb-2">
                    <CardTitle className="text-sm font-medium text-muted-foreground">
                      수납 진행률
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="flex-1 flex flex-col justify-center">
                    <div className="space-y-4">
                      <div className="flex items-center justify-between">
                        <span className="text-sm font-medium">진행률</span>
                        <span className="text-2xl font-bold" style={{ color: PAYMENT_COLORS.paid.main }}>
                          {paymentRate}%
                        </span>
                      </div>
                      <div className="h-4 bg-gray-100 rounded-full overflow-hidden">
                        <div
                          className="h-full transition-all duration-500 rounded-full"
                          style={{
                            width: `${paymentRate}%`,
                            backgroundColor: PAYMENT_COLORS.paid.main,
                          }}
                        />
                      </div>
                      <div className="flex justify-between text-sm">
                        <div className="flex items-center gap-2">
                          <div
                            className="w-3 h-3 rounded-full"
                            style={{ backgroundColor: PAYMENT_COLORS.paid.main }}
                          />
                          <span className="text-muted-foreground">수납</span>
                          <span className="font-medium">₩{formatAmount(stats?.totalPaidAmount)}</span>
                        </div>
                        <div className="flex items-center gap-2">
                          <div
                            className="w-3 h-3 rounded-full"
                            style={{ backgroundColor: PAYMENT_COLORS.unpaid.main }}
                          />
                          <span className="text-muted-foreground">미수납</span>
                          <span className="font-medium">₩{formatAmount(stats?.totalUnpaidAmount)}</span>
                        </div>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              )}
            </div>

            <div className="min-h-[300px]">
              <PaymentRatePieChart
                paidCount={stats?.paidInvoiceCount ?? 0}
                partialCount={stats?.partialInvoiceCount ?? 0}
                unpaidCount={stats?.unpaidInvoiceCount ?? 0}
                title={`${periodLabel} 수납 현황`}
              />
            </div>
          </div>
        </>
      )}
    </div>
  );
}
