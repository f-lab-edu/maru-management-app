import { useSearchParams } from 'react-router-dom';
import { cn } from '@/shared/utils';
import {
  InvoiceTab,
  UnpaidTab,
  StatsTab,
} from '@/features/payments/components';

type TabValue = 'invoices' | 'unpaid' | 'stats';

const TABS: { value: TabValue; label: string }[] = [
  { value: 'invoices', label: '청구서' },
  { value: 'unpaid', label: '미납자' },
  { value: 'stats', label: '통계' },
];

export default function BillingPage() {
  const [searchParams, setSearchParams] = useSearchParams();

  const currentTab = (searchParams.get('tab') as TabValue) || 'invoices';

  const handleTabChange = (value: TabValue) => {
    setSearchParams({ tab: value });
  };

  return (
    <div className="h-full flex flex-col gap-4 p-4 lg:gap-6 lg:p-8">
      <header className="shrink-0">
        <h1 className="text-2xl font-bold">수납 관리</h1>
        <p className="text-sm text-muted-foreground">청구서 및 수납 현황을 관리합니다</p>
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
        {currentTab === 'invoices' && <InvoiceTab />}
        {currentTab === 'unpaid' && <UnpaidTab />}
        {currentTab === 'stats' && <StatsTab />}
      </div>
    </div>
  );
}
