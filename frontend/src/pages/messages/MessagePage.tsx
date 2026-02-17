import { useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { cn } from '@/shared/utils';
import { Button } from '@/shared/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';
import { Skeleton } from '@/shared/components/ui/skeleton';
import { Plus, ChevronLeft, ChevronRight, Send, Bell, MessageSquare } from 'lucide-react';
import { BroadcastList } from '@/features/messages/components/BroadcastList';
import { NotificationSummaryList } from '@/features/messages/components/NotificationSummaryList';
import { BroadcastDetailSheet } from '@/features/messages/components/BroadcastDetailSheet';
import { MessageCreateSheet } from '@/features/messages/components/MessageCreateSheet';
import { useMonthlyUsage } from '@/features/messages/hooks/useNotifications';
import { useAuthStore } from '@/stores/authStore';

type TabValue = 'broadcasts' | 'notifications';

const TABS: { value: TabValue; label: string }[] = [
  { value: 'broadcasts', label: '단체 문자' },
  { value: 'notifications', label: '자동 알림' },
];

const formatYearMonth = (date: Date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  return `${year}-${month}`;
};

const formatDisplayMonth = (yearMonth: string) => {
  const [year, month] = yearMonth.split('-');
  return `${year}년 ${Number(month)}월`;
};

const shiftMonth = (yearMonth: string, delta: number) => {
  const [year, month] = yearMonth.split('-').map(Number);
  const date = new Date(year, month - 1 + delta, 1);
  return formatYearMonth(date);
};

export default function MessagePage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [selectedBroadcastId, setSelectedBroadcastId] = useState<string | null>(null);
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [selectedMonth, setSelectedMonth] = useState(() => formatYearMonth(new Date()));

  const { selectedDojang } = useAuthStore();
  const dojangId = selectedDojang?.dojangId ?? '';
  const { data: usage, isLoading: isUsageLoading } = useMonthlyUsage(dojangId, selectedMonth);

  const currentTab = (searchParams.get('tab') as TabValue) || 'broadcasts';

  const currentYearMonth = formatYearMonth(new Date());
  const isCurrentMonth = selectedMonth === currentYearMonth;

  const handleTabChange = (value: TabValue) => {
    setSearchParams({ tab: value });
  };

  return (
    <div className="h-full flex flex-col gap-4 p-4 lg:gap-6 lg:p-8">
      <header className="shrink-0 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">메시지</h1>
          <p className="text-sm text-muted-foreground">단체 문자 발송 및 자동 알림 내역을 관리합니다</p>
        </div>
        {currentTab === 'broadcasts' && (
          <Button onClick={() => { setSelectedBroadcastId(null); setIsCreateOpen(true); }}>
            <Plus className="h-4 w-4 mr-2" />
            새 문자 발송
          </Button>
        )}
      </header>

      {/* 월별 통계 카드 */}
      <section className="shrink-0 space-y-3">
        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="icon"
            className="h-8 w-8"
            onClick={() => setSelectedMonth(shiftMonth(selectedMonth, -1))}
          >
            <ChevronLeft className="h-4 w-4" />
          </Button>
          <span className="text-sm font-medium min-w-[100px] text-center">
            {formatDisplayMonth(selectedMonth)}
          </span>
          <Button
            variant="outline"
            size="icon"
            className="h-8 w-8"
            onClick={() => setSelectedMonth(shiftMonth(selectedMonth, 1))}
            disabled={isCurrentMonth}
          >
            <ChevronRight className="h-4 w-4" />
          </Button>
        </div>

        <div className="grid grid-cols-3 gap-3">
          {isUsageLoading ? (
            Array.from({ length: 3 }).map((_, i) => (
              <Card key={i} className="border shadow-sm">
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-1 px-4 pt-3">
                  <Skeleton className="h-3 w-16" />
                  <Skeleton className="h-3.5 w-3.5 rounded" />
                </CardHeader>
                <CardContent className="px-4 pb-3 pt-0">
                  <Skeleton className="h-7 w-12" />
                </CardContent>
              </Card>
            ))
          ) : (
            <>
              <Card className="border shadow-sm">
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-1 px-4 pt-3">
                  <CardTitle className="text-xs font-medium text-muted-foreground">이번 달 발송</CardTitle>
                  <Send className="h-3.5 w-3.5 text-muted-foreground" />
                </CardHeader>
                <CardContent className="px-4 pb-3 pt-0">
                  <div className="text-2xl font-bold">{usage?.totalCount ?? 0}</div>
                  <p className="text-[10px] text-muted-foreground mt-0.5">건</p>
                </CardContent>
              </Card>
              <Card className="border shadow-sm">
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-1 px-4 pt-3">
                  <CardTitle className="text-xs font-medium text-muted-foreground">자동알림</CardTitle>
                  <Bell className="h-3.5 w-3.5 text-muted-foreground" />
                </CardHeader>
                <CardContent className="px-4 pb-3 pt-0">
                  <div className="text-2xl font-bold">{usage?.autoCount ?? 0}</div>
                  <p className="text-[10px] text-muted-foreground mt-0.5">건</p>
                </CardContent>
              </Card>
              <Card className="border shadow-sm">
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-1 px-4 pt-3">
                  <CardTitle className="text-xs font-medium text-muted-foreground">단체문자</CardTitle>
                  <MessageSquare className="h-3.5 w-3.5 text-muted-foreground" />
                </CardHeader>
                <CardContent className="px-4 pb-3 pt-0">
                  <div className="text-2xl font-bold">{usage?.broadcastCount ?? 0}</div>
                  <p className="text-[10px] text-muted-foreground mt-0.5">건</p>
                </CardContent>
              </Card>
            </>
          )}
        </div>
      </section>

      <div className="inline-flex rounded-lg bg-muted p-1 shrink-0 self-start">
        {TABS.map((tab) => (
          <button
            key={tab.value}
            onClick={() => handleTabChange(tab.value)}
            className={cn(
              'rounded-md px-4 py-2 text-sm font-medium transition-all',
              currentTab === tab.value
                ? 'bg-white text-foreground shadow-sm'
                : 'text-muted-foreground hover:text-foreground'
            )}
          >
            {tab.label}
          </button>
        ))}
      </div>

      <div className="flex-1 min-h-0 flex flex-col">
        {currentTab === 'broadcasts' && (
          <BroadcastList
            onRowClick={(id) => { setIsCreateOpen(false); setSelectedBroadcastId(id); }}
          />
        )}
        {currentTab === 'notifications' && <NotificationSummaryList />}
      </div>

      {/* Sheets */}
      <BroadcastDetailSheet
        broadcastId={selectedBroadcastId}
        isOpen={selectedBroadcastId !== null}
        onClose={() => setSelectedBroadcastId(null)}
      />
      <MessageCreateSheet
        isOpen={isCreateOpen}
        onClose={() => setIsCreateOpen(false)}
      />
    </div>
  );
}
