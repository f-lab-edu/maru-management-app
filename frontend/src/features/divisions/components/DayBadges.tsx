import { cn } from '@/shared/utils';
import { getDayLabel, getAllDays } from '../utils';
import type { DayOfWeek } from '../types';

interface DayBadgesProps {
  selectedDays: DayOfWeek[];
  className?: string;
}

export function DayBadges({ selectedDays, className }: DayBadgesProps) {
  const allDays = getAllDays();

  return (
    <div className={cn('flex items-center gap-1', className)}>
      {allDays.map((day) => {
        const isSelected = selectedDays.includes(day);
        return (
          <div
            key={day}
            className={cn(
              'flex items-center justify-center w-6 h-6 rounded-full text-xs font-medium transition-colors',
              isSelected
                ? 'bg-primary text-primary-foreground'
                : 'bg-slate-100 text-slate-400'
            )}
          >
            {getDayLabel(day)}
          </div>
        );
      })}
    </div>
  );
}
