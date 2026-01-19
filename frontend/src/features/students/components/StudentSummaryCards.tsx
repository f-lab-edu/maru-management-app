import { Users, UserX } from 'lucide-react';
import { cn } from '@/shared/utils';
import type { StudentStats } from '@/types/student';

type StatusFilter = 'ALL' | 'ACTIVE' | 'PAUSED';

interface StudentSummaryCardsProps {
  stats: StudentStats;
  activeFilter: StatusFilter;
  onFilterChange: (filter: StatusFilter) => void;
}

interface SummaryCardProps {
  icon: React.ReactNode;
  label: string;
  count: number;
  isActive: boolean;
  onClick: () => void;
  colorClass: string;
}

function SummaryCard({ icon, label, count, isActive, onClick, colorClass }: SummaryCardProps) {
  return (
    <button
      onClick={onClick}
      className={cn(
        'flex items-center gap-4 rounded-xl bg-white p-5 shadow-sm transition-all hover:shadow-md',
        'border-2 border-transparent',
        isActive && 'ring-2 ring-primary ring-offset-2'
      )}
    >
      <div className={cn('flex h-12 w-12 items-center justify-center rounded-lg', colorClass)}>
        {icon}
      </div>
      <div className="text-left">
        <p className="text-sm text-muted-foreground">{label}</p>
        <p className="text-3xl font-bold">{count}</p>
      </div>
    </button>
  );
}

export function StudentSummaryCards({
  stats,
  activeFilter,
  onFilterChange,
}: StudentSummaryCardsProps) {
  const handleActiveClick = () => {
    onFilterChange(activeFilter === 'ACTIVE' ? 'ALL' : 'ACTIVE');
  };

  const handlePausedClick = () => {
    onFilterChange(activeFilter === 'PAUSED' ? 'ALL' : 'PAUSED');
  };

  return (
    <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
      <SummaryCard
        icon={<Users className="h-6 w-6 text-green-600" />}
        label="유효 수련생"
        count={stats.activeCount}
        isActive={activeFilter === 'ACTIVE'}
        onClick={handleActiveClick}
        colorClass="bg-green-100"
      />
      <SummaryCard
        icon={<UserX className="h-6 w-6 text-yellow-600" />}
        label="휴관 수련생"
        count={stats.pausedCount}
        isActive={activeFilter === 'PAUSED'}
        onClick={handlePausedClick}
        colorClass="bg-yellow-100"
      />
    </div>
  );
}
