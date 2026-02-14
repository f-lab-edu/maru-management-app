import { useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { cn } from '@/shared/utils';
import { Button } from '@/shared/components/ui/button';
import { Plus } from 'lucide-react';
import { BroadcastList } from '@/features/messages/components/BroadcastList';
import { NotificationSummaryList } from '@/features/messages/components/NotificationSummaryList';
import { BroadcastDetailSheet } from '@/features/messages/components/BroadcastDetailSheet';
import { MessageCreateSheet } from '@/features/messages/components/MessageCreateSheet';

type TabValue = 'broadcasts' | 'notifications';

const TABS: { value: TabValue; label: string }[] = [
  { value: 'broadcasts', label: '단체 문자' },
  { value: 'notifications', label: '자동 알림' },
];

export default function MessagePage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [selectedBroadcastId, setSelectedBroadcastId] = useState<string | null>(null);
  const [isCreateOpen, setIsCreateOpen] = useState(false);

  const currentTab = (searchParams.get('tab') as TabValue) || 'broadcasts';

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
