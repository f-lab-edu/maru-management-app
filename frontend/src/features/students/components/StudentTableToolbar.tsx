import { Search, Trash2, X } from 'lucide-react';
import { Input } from '@/shared/components/ui/input';
import { Button } from '@/shared/components/ui/button';
import { cn } from '@/shared/utils';
import { SectionDivisionFilter } from '@/features/divisions';

type StatusFilter = 'ALL' | 'ACTIVE' | 'PAUSED';

interface StudentTableToolbarProps {
  dojangId: string | null;
  statusFilter: StatusFilter;
  onStatusFilterChange: (filter: StatusFilter) => void;
  searchQuery: string;
  onSearchQueryChange: (query: string) => void;
  selectedCount: number;
  onBulkDelete: () => void;
  stats: {
    total: number;
    activeCount: number;
    pausedCount: number;
  };
  // TODO: 백엔드 Enrollment API 구현 후 활성화
  sectionId?: string | null;
  divisionId?: string | null;
  onSectionChange?: (sectionId: string | null) => void;
  onDivisionChange?: (divisionId: string | null) => void;
}

const FILTER_TABS: { value: StatusFilter; label: string }[] = [
  { value: 'ALL', label: '전체' },
  { value: 'ACTIVE', label: '유효' },
  { value: 'PAUSED', label: '휴원' },
];

export function StudentTableToolbar({
  dojangId,
  statusFilter,
  onStatusFilterChange,
  searchQuery,
  onSearchQueryChange,
  selectedCount,
  onBulkDelete,
  stats,
  sectionId,
  divisionId,
  onSectionChange,
  onDivisionChange,
}: StudentTableToolbarProps) {
  const getTabCount = (filter: StatusFilter) => {
    switch (filter) {
      case 'ALL':
        return stats.total;
      case 'ACTIVE':
        return stats.activeCount;
      case 'PAUSED':
        return stats.pausedCount;
    }
  };

  const showDivisionFilter = dojangId && onSectionChange && onDivisionChange;

  return (
    <div className="space-y-4">
      {/* 탭 필터 + 수련부/반 필터 */}
      <div className="flex flex-col sm:flex-row sm:items-center gap-4">
        <div className="inline-flex rounded-lg bg-muted p-1">
          {FILTER_TABS.map((tab) => (
            <button
              key={tab.value}
              onClick={() => onStatusFilterChange(tab.value)}
              className={cn(
                'rounded-md px-4 py-2 text-sm font-medium transition-all',
                statusFilter === tab.value
                  ? 'bg-white text-foreground shadow-sm'
                  : 'text-muted-foreground hover:text-foreground'
              )}
            >
              {tab.label}
              <span className="ml-1.5 text-xs text-muted-foreground">
                {getTabCount(tab.value)}
              </span>
            </button>
          ))}
        </div>

        {/* 수련부/수련반 필터 - TODO: 백엔드 Enrollment API 구현 후 필터링 활성화 */}
        {showDivisionFilter && (
          <SectionDivisionFilter
            dojangId={dojangId}
            selectedSectionId={sectionId ?? null}
            selectedDivisionId={divisionId ?? null}
            onSectionChange={onSectionChange}
            onDivisionChange={onDivisionChange}
            compact
          />
        )}
      </div>

      {/* 검색 + 단체 액션 */}
      <div className="flex items-center justify-between gap-4">
        <div className="relative w-full max-w-sm">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            placeholder="이름, 연락처로 검색..."
            value={searchQuery}
            onChange={(e) => onSearchQueryChange(e.target.value)}
            className="pl-9 pr-9"
          />
          {searchQuery && (
            <button
              onClick={() => onSearchQueryChange('')}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
            >
              <X className="h-4 w-4" />
            </button>
          )}
        </div>

        {selectedCount > 0 && (
          <div className="flex items-center gap-2">
            <span className="text-sm text-muted-foreground">
              {selectedCount}명 선택됨
            </span>
            <Button
              variant="destructive"
              size="sm"
              onClick={onBulkDelete}
            >
              <Trash2 className="mr-2 h-4 w-4" />
              선택 삭제
            </Button>
          </div>
        )}
      </div>
    </div>
  );
}
