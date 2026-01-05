import { useMemo } from 'react';
import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from 'recharts';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';
import { PAYMENT_COLORS } from '../../constants/chartColors';

interface PaymentRatePieChartProps {
  paidCount: number;
  partialCount: number;
  unpaidCount: number;
  title?: string;
}

const CHART_COLORS = {
  paid: PAYMENT_COLORS.paid.main,
  partial: PAYMENT_COLORS.partial.main,
  unpaid: PAYMENT_COLORS.unpaid.main,
};

const STATUS_LABELS = {
  paid: '완납',
  partial: '부분납부',
  unpaid: '미납',
};

export function PaymentRatePieChart({
  paidCount,
  partialCount,
  unpaidCount,
  title = '수납 현황',
}: PaymentRatePieChartProps) {
  const chartData = useMemo(() => {
    const items = [
      { name: STATUS_LABELS.paid, value: paidCount, key: 'paid' },
      { name: STATUS_LABELS.partial, value: partialCount, key: 'partial' },
      { name: STATUS_LABELS.unpaid, value: unpaidCount, key: 'unpaid' },
    ];
    return items.filter((item) => item.value > 0);
  }, [paidCount, partialCount, unpaidCount]);

  const total = paidCount + partialCount + unpaidCount;
  const paidRate = total > 0 ? Math.round((paidCount / total) * 100) : 0;

  if (total === 0) {
    return (
      <Card className="border shadow-sm h-full flex flex-col">
        <CardHeader className="pb-2 shrink-0">
          <CardTitle className="text-sm font-medium text-muted-foreground">
            {title}
          </CardTitle>
        </CardHeader>
        <CardContent className="flex-1 flex items-center justify-center">
          <p className="text-sm text-muted-foreground">데이터가 없습니다</p>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className="border shadow-sm h-full flex flex-col">
      <CardHeader className="pb-2 shrink-0">
        <div className="flex items-center justify-between">
          <CardTitle className="text-sm font-medium text-muted-foreground">
            {title}
          </CardTitle>
          <span className="text-lg font-bold text-emerald-600">{paidRate}%</span>
        </div>
      </CardHeader>
      <CardContent className="pt-0 flex-1 min-h-0">
        <div className="h-full min-h-[200px]">
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie
                data={chartData}
                cx="50%"
                cy="50%"
                innerRadius={50}
                outerRadius={70}
                paddingAngle={2}
                dataKey="value"
              >
                {chartData.map((entry) => (
                  <Cell
                    key={entry.key}
                    fill={CHART_COLORS[entry.key as keyof typeof CHART_COLORS]}
                  />
                ))}
              </Pie>
              <Tooltip
                formatter={(value: number, name: string) => [`${value}건`, name]}
                contentStyle={{
                  backgroundColor: 'white',
                  border: '1px solid hsl(var(--border))',
                  borderRadius: '8px',
                  fontSize: '12px',
                }}
              />
              <Legend
                layout="vertical"
                align="right"
                verticalAlign="middle"
                iconType="circle"
                iconSize={8}
                formatter={(value: string) => (
                  <span className="text-xs text-muted-foreground ml-1">{value}</span>
                )}
              />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </CardContent>
    </Card>
  );
}
