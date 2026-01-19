import {
  ComposedChart,
  Bar,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';
import { CHART_COLORS, PAYMENT_COLORS } from '../../constants/chartColors';
import type { MonthlyStatisticsData } from '../../types';

interface PaymentTrendChartProps {
  data: MonthlyStatisticsData[];
  title?: string;
}

const formatAmount = (value: number) => {
  if (value >= 1000000) {
    return `${(value / 1000000).toFixed(1)}M`;
  }
  if (value >= 1000) {
    return `${(value / 1000).toFixed(0)}K`;
  }
  return value.toString();
};

const formatTooltipAmount = (value: number) => {
  return new Intl.NumberFormat('ko-KR').format(value);
};

export function PaymentTrendChart({ data, title = '월별 수납 추이' }: PaymentTrendChartProps) {
  const chartData = data.map((item) => ({
    name: `${item.month}월`,
    청구액: item.totalAmount,
    수납액: item.paidAmount,
  }));

  const hasData = data.some((d) => d.totalAmount > 0 || d.paidAmount > 0);

  if (!hasData) {
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
        <CardTitle className="text-sm font-medium text-muted-foreground">
          {title}
        </CardTitle>
      </CardHeader>
      <CardContent className="pt-0 flex-1 min-h-0">
        <div className="h-full min-h-[250px]">
          <ResponsiveContainer width="100%" height="100%">
            <ComposedChart data={chartData} margin={{ top: 10, right: 10, left: -10, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke={CHART_COLORS.grid} />
              <XAxis
                dataKey="name"
                tick={{ fontSize: 11, fill: CHART_COLORS.axis }}
                axisLine={{ stroke: CHART_COLORS.grid }}
              />
              <YAxis
                tick={{ fontSize: 11, fill: CHART_COLORS.axis }}
                tickFormatter={formatAmount}
                axisLine={{ stroke: CHART_COLORS.grid }}
              />
              <Tooltip
                formatter={(value: number, name: string) => [
                  `₩${formatTooltipAmount(value)}`,
                  name,
                ]}
                contentStyle={{
                  backgroundColor: 'white',
                  border: '1px solid hsl(var(--border))',
                  borderRadius: '8px',
                  fontSize: '12px',
                }}
              />
              <Legend
                iconType="circle"
                iconSize={8}
                wrapperStyle={{ fontSize: '12px' }}
              />
              <Bar
                dataKey="청구액"
                fill={CHART_COLORS.amount}
                opacity={0.3}
                radius={[4, 4, 0, 0]}
              />
              <Line
                type="monotone"
                dataKey="수납액"
                stroke={PAYMENT_COLORS.paid.main}
                strokeWidth={2}
                dot={{ fill: PAYMENT_COLORS.paid.main, strokeWidth: 0, r: 4 }}
                activeDot={{ r: 6 }}
              />
            </ComposedChart>
          </ResponsiveContainer>
        </div>
      </CardContent>
    </Card>
  );
}
