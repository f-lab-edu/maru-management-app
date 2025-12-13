import { UserCheck, UserX, Users, TrendingUp } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';

interface AttendanceStatsCardsProps {
  summary: {
    totalStudents: number;
    present: number;
    absent: number;
    sick?: number;
    excused?: number;
    currentlyPresent?: number;
  };
  periodLabel?: string;
}

export function AttendanceStatsCards({ summary, periodLabel = '오늘' }: AttendanceStatsCardsProps) {
  const { totalStudents, present, absent, sick = 0, excused = 0 } = summary;
  const totalAbsent = absent + sick + excused;
  const attendanceRate = totalStudents > 0 ? Math.round((present / totalStudents) * 100) : 0;

  return (
    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium">전체 원생</CardTitle>
          <Users className="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold">{totalStudents}명</div>
          <p className="text-xs text-muted-foreground">{periodLabel} 기준</p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium">출석</CardTitle>
          <UserCheck className="h-4 w-4 text-green-600" />
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold text-green-600">{present}명</div>
          <p className="text-xs text-muted-foreground">
            전체 {totalStudents}명 중
          </p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium">미출석</CardTitle>
          <UserX className="h-4 w-4 text-red-600" />
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold text-red-600">{totalAbsent}명</div>
          <p className="text-xs text-muted-foreground">
            결석 {absent} / 병결 {sick} / 공결 {excused}
          </p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium">출석률</CardTitle>
          <TrendingUp className="h-4 w-4 text-blue-600" />
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold text-blue-600">{attendanceRate}%</div>
          <p className="text-xs text-muted-foreground">
            {periodLabel} 출석률
          </p>
        </CardContent>
      </Card>
    </div>
  );
}
