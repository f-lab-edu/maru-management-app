import { Users, CalendarCheck, CreditCard, TrendingUp } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '../../../shared/components/ui/card';
import { DashboardStats } from '../types';

interface StatsGridProps {
  stats: DashboardStats;
}

export function StatsGrid({ stats }: StatsGridProps) {
  return (
    <div className="grid grid-cols-2 gap-4 shrink-0">
      <Card className="border-none shadow-sm bg-white hover:shadow-md transition-all duration-200">
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium text-slate-500">총 원생 수</CardTitle>
          <div className="h-8 w-8 rounded-lg bg-blue-50 flex items-center justify-center">
            <Users className="h-4 w-4 text-blue-600" />
          </div>
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold text-slate-900">{stats.totalStudents}명</div>
          <p className="text-xs text-slate-500 mt-1">
            <span className="text-emerald-600 font-medium">↑ 2명</span> 지난달 대비
          </p>
        </CardContent>
      </Card>
      <Card className="border-none shadow-sm bg-white hover:shadow-md transition-all duration-200">
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium text-slate-500">출석률</CardTitle>
          <div className="h-8 w-8 rounded-lg bg-emerald-50 flex items-center justify-center">
            <CalendarCheck className="h-4 w-4 text-emerald-600" />
          </div>
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold text-slate-900">{stats.attendanceRate}%</div>
          <p className="text-xs text-slate-500 mt-1">
            <span className="text-emerald-600 font-medium">↑ 5%</span> 어제 대비
          </p>
        </CardContent>
      </Card>
      <Card className="border-none shadow-sm bg-white hover:shadow-md transition-all duration-200">
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium text-slate-500">이번 달 수납</CardTitle>
          <div className="h-8 w-8 rounded-lg bg-violet-50 flex items-center justify-center">
            <CreditCard className="h-4 w-4 text-violet-600" />
          </div>
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold text-slate-900">₩{stats.monthlyRevenue.toLocaleString()}</div>
          <p className="text-xs text-slate-500 mt-1">
            목표 달성률 <span className="text-slate-900 font-medium">{(stats.monthlyRevenue / stats.revenueTarget * 100).toFixed(1)}%</span>
          </p>
        </CardContent>
      </Card>
      <Card className="border-none shadow-sm bg-white hover:shadow-md transition-all duration-200">
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium text-slate-500">활성 원생</CardTitle>
          <div className="h-8 w-8 rounded-lg bg-amber-50 flex items-center justify-center">
            <TrendingUp className="h-4 w-4 text-amber-600" />
          </div>
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold text-slate-900">{stats.activeStudents}명</div>
          <p className="text-xs text-slate-500 mt-1">
            휴원 <span className="text-slate-900 font-medium">{stats.totalStudents - stats.activeStudents}명</span>
          </p>
        </CardContent>
      </Card>
    </div>
  );
}
