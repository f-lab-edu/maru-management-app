import { useState, useCallback, useEffect } from 'react';
import { format } from 'date-fns';
import { formatDistanceToNow } from 'date-fns';
import { ko } from 'date-fns/locale';
import { Loader2 } from 'lucide-react';
import { DashboardBanner } from '../features/dashboard/components/DashboardBanner';
import { StatsGrid } from '../features/dashboard/components/StatsGrid';
import { NotificationList } from '../features/dashboard/components/NotificationList';
import { DashboardCalendar } from '../features/dashboard/components/DashboardCalendar';
import { DetailPanel } from '../features/dashboard/components/DetailPanel';
import { useDashboardCalendar } from '../features/dashboard/hooks/useDashboardCalendar';
import { useDashboardSummary, useDashboardNotifications, useRecentStudents } from '../features/dashboard/hooks/useDashboard';
import type { DashboardStats, NotificationItem, RecentStudent } from '../features/dashboard/types';
import { usePermissions } from '@/hooks';
import { useAuthStore } from '@/stores/authStore';

const PAGE_SIZE = 10;

export default function DashboardPage() {
  const { date, selectedDateDetails, resetSelection } = useDashboardCalendar();
  const { hasPermission } = usePermissions();
  const canViewStats = hasPermission('STATS_VIEW_DASHBOARD');
  const selectedDojang = useAuthStore((state) => state.selectedDojang);
  const dojangId = selectedDojang?.dojangId ?? null;

  const [notificationOffset, setNotificationOffset] = useState(0);
  const [studentOffset, setStudentOffset] = useState(0);
  const [accumulatedNotifications, setAccumulatedNotifications] = useState<NotificationItem[]>([]);
  const [accumulatedStudents, setAccumulatedStudents] = useState<RecentStudent[]>([]);

  const { data: summaryData, isLoading: isSummaryLoading } = useDashboardSummary(dojangId, canViewStats);
  const { data: notificationsData, isLoading: isNotificationsLoading } = useDashboardNotifications(dojangId, PAGE_SIZE, notificationOffset);
  const { data: recentStudentsData, isLoading: isRecentStudentsLoading } = useRecentStudents(dojangId, PAGE_SIZE, studentOffset);

  useEffect(() => {
    if (notificationsData?.items) {
      const newItems: NotificationItem[] = notificationsData.items.map((item) => ({
        title: item.title,
        desc: item.description,
        time: formatDistanceToNow(new Date(item.occurredAt), { addSuffix: true, locale: ko }),
        type: item.type === 'PAYMENT' ? 'payment' : item.type === 'INSTRUCTOR' ? 'alert' : 'info',
      }));

      if (notificationOffset === 0) {
        setAccumulatedNotifications(newItems);
      } else {
        setAccumulatedNotifications((prev) => [...prev, ...newItems]);
      }
    }
  }, [notificationsData, notificationOffset]);

  useEffect(() => {
    if (recentStudentsData?.students) {
      const newStudents: RecentStudent[] = recentStudentsData.students.map((student) => ({
        id: student.id,
        name: student.name,
        age: student.age,
        enrolledAt: format(new Date(student.enrolledAt), 'M월 d일', { locale: ko }),
        photoUrl: student.photoUrl,
      }));

      if (studentOffset === 0) {
        setAccumulatedStudents(newStudents);
      } else {
        setAccumulatedStudents((prev) => [...prev, ...newStudents]);
      }
    }
  }, [recentStudentsData, studentOffset]);

  const handleLoadMoreNotifications = useCallback(() => {
    setNotificationOffset((prev) => prev + PAGE_SIZE);
  }, []);

  const handleLoadMoreStudents = useCallback(() => {
    setStudentOffset((prev) => prev + PAGE_SIZE);
  }, []);

  const isInitialLoading = (canViewStats && isSummaryLoading) ||
    (notificationOffset === 0 && isNotificationsLoading) ||
    (studentOffset === 0 && isRecentStudentsLoading);

  if (isInitialLoading) {
    return (
      <div className="flex items-center justify-center h-full">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    );
  }

  const stats: DashboardStats | undefined = summaryData ? {
    totalStudents: summaryData.totalStudents,
    studentsDiff: summaryData.studentsDiff,
    attendanceRate: summaryData.attendanceRate,
    attendanceRateDiff: summaryData.attendanceRateDiff,
    monthlyRevenue: summaryData.monthlyRevenue,
    revenueTarget: summaryData.revenueTarget,
    activeStudents: summaryData.activeStudents,
    pausedStudents: summaryData.pausedStudents,
  } : undefined;

  return (
    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 h-full p-4 lg:p-8">
      {/* Left Column (2/3) */}
      <div className="lg:col-span-2 flex flex-col gap-6 h-full min-h-0">
        {canViewStats && stats && (
          <>
            <DashboardBanner activeStudents={summaryData?.currentlyAttendingCount ?? 0} />
            <StatsGrid stats={stats} />
          </>
        )}
        <NotificationList
          notifications={accumulatedNotifications}
          hasMore={notificationsData?.hasMore ?? false}
          onLoadMore={handleLoadMoreNotifications}
          isLoading={isNotificationsLoading && notificationOffset > 0}
        />
      </div>

      {/* Right Column (1/3) */}
      <div className="flex flex-col gap-6 h-full min-h-0">
        <DashboardCalendar
          date={date}
          onSelect={() => {}}
          events={[]}
        />
        <DetailPanel
          selectedDate={date}
          isDetailsView={selectedDateDetails}
          onBack={resetSelection}
          students={accumulatedStudents}
          hasMore={recentStudentsData?.hasMore ?? false}
          onLoadMore={handleLoadMoreStudents}
          isLoading={isRecentStudentsLoading && studentOffset > 0}
        />
      </div>
    </div>
  );
}
