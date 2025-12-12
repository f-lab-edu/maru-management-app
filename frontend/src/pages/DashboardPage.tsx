import { useQuery } from '@tanstack/react-query';
import { DashboardBanner } from '../features/dashboard/components/DashboardBanner';
import { StatsGrid } from '../features/dashboard/components/StatsGrid';
import { NotificationList } from '../features/dashboard/components/NotificationList';
import { DashboardCalendar } from '../features/dashboard/components/DashboardCalendar';
import { DetailPanel } from '../features/dashboard/components/DetailPanel';
import { useDashboardCalendar } from '../features/dashboard/hooks/useDashboardCalendar';
import { EVENTS, NOTIFICATIONS, APPLICANTS, MOCK_DASHBOARD_STATS } from '../features/dashboard/constants';
import { DashboardStats } from '../features/dashboard/types';
import { Loader2 } from 'lucide-react';

const fetchDashboardStats = async (): Promise<DashboardStats> => {
  // TODO: 실제 API 연동 시 아래 코드로 교체
  // return apiClient.get('/dashboard/stats').then(res => res.data);

  await new Promise(resolve => setTimeout(resolve, 500));
  return MOCK_DASHBOARD_STATS;
};

export default function DashboardPage() {
  const { date, selectedDateDetails, handleDateSelect, resetSelection } = useDashboardCalendar();

  const { data: stats, isLoading } = useQuery({
    queryKey: ['dashboardStats'],
    queryFn: fetchDashboardStats,
  });

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-full">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    );
  }

  if (!stats) return null;

  return (
    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 h-full p-4 lg:p-8">
      {/* Left Column (2/3) */}
      <div className="lg:col-span-2 flex flex-col gap-6 h-full min-h-0">
        <DashboardBanner activeStudents={stats.activeStudents} />
        <StatsGrid stats={stats} />
        <NotificationList notifications={NOTIFICATIONS} />
      </div>

      {/* Right Column (1/3) */}
      <div className="flex flex-col gap-6 h-full min-h-0">
        <DashboardCalendar
          date={date}
          onSelect={handleDateSelect}
          events={EVENTS}
        />
        <DetailPanel
          selectedDate={date}
          isDetailsView={selectedDateDetails}
          onBack={resetSelection}
          applicants={APPLICANTS}
        />
      </div>
    </div>
  );
}
