import { useMemo } from 'react';
import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from 'recharts';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';
import { ATTENDANCE_STATUS_LABELS } from '../constants';

interface ChartData {
  present: number;
  absent: number;
  sick: number;
  excused: number;
}

interface AttendanceChartCardProps {
  data: ChartData;
  periodLabel: string;
}

const COLORS = {
  present: '#059669', // emerald-600
  absent: '#ef4444',  // red-500
  sick: '#f59e0b',    // amber-500
  excused: '#3b82f6', // blue-500
};

export function AttendanceChartCard({ data, periodLabel }: AttendanceChartCardProps) {
  const chartData = useMemo(() => {
    const items = [
      { name: ATTENDANCE_STATUS_LABELS.PRESENT, value: data.present, key: 'present' },
      { name: ATTENDANCE_STATUS_LABELS.ABSENT, value: data.absent, key: 'absent' },
      { name: ATTENDANCE_STATUS_LABELS.SICK, value: data.sick, key: 'sick' },
      { name: ATTENDANCE_STATUS_LABELS.EXCUSED, value: data.excused, key: 'excused' },
    ];
    return items.filter((item) => item.value > 0);
  }, [data]);

  const total = data.present + data.absent + data.sick + data.excused;
  const attendanceRate = total > 0 ? Math.round((data.present / total) * 100) : 0;

  if (total === 0) {
    return (
      <Card className="border shadow-sm h-full flex flex-col">
        <CardHeader className="pb-2 shrink-0">
          <CardTitle className="text-xs font-medium text-muted-foreground">
            {periodLabel} 출석 현황
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
          <CardTitle className="text-xs font-medium text-muted-foreground">
            {periodLabel} 출석 현황
          </CardTitle>
          <span className="text-lg font-bold">{attendanceRate}%</span>
        </div>
      </CardHeader>
      <CardContent className="pt-0 flex-1 min-h-0">
        <div className="h-full">
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie
                data={chartData}
                cx="50%"
                cy="50%"
                innerRadius={45}
                outerRadius={65}
                paddingAngle={2}
                dataKey="value"
              >
                {chartData.map((entry) => (
                  <Cell
                    key={entry.key}
                    fill={COLORS[entry.key as keyof typeof COLORS]}
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
                iconSize={6}
                formatter={(value: string) => (
                  <span className="text-[10px] text-muted-foreground">{value}</span>
                )}
              />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </CardContent>
    </Card>
  );
}
