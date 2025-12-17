import { useState, useRef, memo, useEffect, useMemo } from 'react';
import {
  ColumnDef,
  SortingState,
  RowSelectionState,
  flexRender,
  getCoreRowModel,
  getSortedRowModel,
  getFilteredRowModel,
  useReactTable,
  Row,
} from '@tanstack/react-table';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';
import { Button } from '@/shared/components/ui/button';
import { Skeleton } from '@/shared/components/ui/skeleton';
import { Checkbox } from '@/shared/components/ui/checkbox';
import { getTodayISO } from '../utils/dateUtils';

const NAME_COL_WIDTH = 140;
const CHECKBOX_COL_WIDTH = 40;
const RATE_COL_WIDTH = 110;
const SUMMARY_COL_WIDTH = 140;

interface AttendanceDataTableProps<TData extends { id: number }> {
  columns: ColumnDef<TData>[];
  data: TData[];
  isLoading?: boolean;
  onRowClick?: (row: TData) => void;
  globalFilter?: string;
  rowSelection?: RowSelectionState;
  onRowSelectionChange?: (selection: RowSelectionState) => void;
}

function TableSkeleton({ columnCount, rowCount }: { columnCount: number; rowCount: number }) {
  return (
    <>
      {Array.from({ length: rowCount }).map((_, i) => (
        <TableRow key={i}>
          {Array.from({ length: columnCount }).map((_, j) => (
            <TableCell key={j}>
              <Skeleton className="h-4 w-full" />
            </TableCell>
          ))}
        </TableRow>
      ))}
    </>
  );
}

interface MemoizedRowProps<TData> {
  row: Row<TData>;
  onRowClick?: (row: TData) => void;
  columnCount: number;
  hasCheckbox: boolean;
  todayColumnId: string;
}

const MemoizedRow = memo(function MemoizedRow<TData>({
  row,
  onRowClick,
  columnCount,
  hasCheckbox,
  todayColumnId,
}: MemoizedRowProps<TData>) {
  const cells = row.getVisibleCells();
  const lastTwoStart = columnCount - 2;
  const nameColIndex = hasCheckbox ? 1 : 0;

  return (
    <TableRow
      className="cursor-pointer hover:bg-muted/50"
      onClick={() => onRowClick?.(row.original)}
      data-state={row.getIsSelected() && 'selected'}
    >
      {cells.map((cell, index) => {
        const isToday = cell.column.id === todayColumnId;
        let className = isToday ? 'bg-primary/10' : '';
        let style: React.CSSProperties = { width: cell.column.columnDef.size };

        // 체크박스 컬럼 - 왼쪽 고정
        if (hasCheckbox && index === 0) {
          className = 'sticky left-0 z-10 bg-white';
          style = { ...style, minWidth: CHECKBOX_COL_WIDTH };
        }
        // 이름 컬럼 - 왼쪽 고정 (체크박스 다음)
        else if (index === nameColIndex) {
          className = 'sticky z-10 bg-white';
          const leftOffset = hasCheckbox ? CHECKBOX_COL_WIDTH : 0;
          style = { ...style, left: leftOffset, minWidth: NAME_COL_WIDTH, boxShadow: '2px 0 0 0 hsl(var(--border))' };
        }
        // 마지막 두 컬럼 (출석률, 상세) - 오른쪽 고정
        else if (index === lastTwoStart) {
          className = 'sticky z-10 bg-white';
          style = { ...style, right: SUMMARY_COL_WIDTH, minWidth: RATE_COL_WIDTH, boxShadow: '-2px 0 0 0 hsl(var(--border))' };
        } else if (index === lastTwoStart + 1) {
          className = 'sticky z-10 bg-white';
          style = { ...style, right: 0, minWidth: SUMMARY_COL_WIDTH };
        }

        return (
          <TableCell key={cell.id} className={className} style={style}>
            {flexRender(cell.column.columnDef.cell, cell.getContext())}
          </TableCell>
        );
      })}
    </TableRow>
  );
}) as <TData>(props: MemoizedRowProps<TData>) => JSX.Element;

export function AttendanceDataTable<TData extends { id: number }>({
  columns,
  data,
  isLoading,
  onRowClick,
  globalFilter = '',
  rowSelection = {},
  onRowSelectionChange,
}: AttendanceDataTableProps<TData>) {
  const scrollRef = useRef<HTMLDivElement>(null);
  const [sorting, setSorting] = useState<SortingState>([]);
  const [internalRowSelection, setInternalRowSelection] = useState<RowSelectionState>({});

  const hasCheckbox = !!onRowSelectionChange;
  const effectiveRowSelection = onRowSelectionChange ? rowSelection : internalRowSelection;

  // 외부 rowSelection이 변경되면 내부 상태도 동기화
  useEffect(() => {
    if (onRowSelectionChange) {
      setInternalRowSelection(rowSelection);
    }
  }, [rowSelection, onRowSelectionChange]);

  // 체크박스 컬럼 추가
  const columnsWithCheckbox: ColumnDef<TData>[] = hasCheckbox
    ? [
        {
          id: 'select',
          header: ({ table }) => (
            <div className="flex items-center justify-center" onClick={(e) => e.stopPropagation()}>
              <Checkbox
                checked={
                  table.getIsAllPageRowsSelected() ||
                  (table.getIsSomePageRowsSelected() && 'indeterminate')
                }
                onCheckedChange={(value) => table.toggleAllPageRowsSelected(!!value)}
                aria-label="전체 선택"
              />
            </div>
          ),
          cell: ({ row }) => (
            <div className="flex items-center justify-center" onClick={(e) => e.stopPropagation()}>
              <Checkbox
                checked={row.getIsSelected()}
                onCheckedChange={(value) => row.toggleSelected(!!value)}
                aria-label="행 선택"
              />
            </div>
          ),
          size: CHECKBOX_COL_WIDTH,
          minSize: CHECKBOX_COL_WIDTH,
          maxSize: CHECKBOX_COL_WIDTH,
          enableSorting: false,
          enableHiding: false,
        },
        ...columns,
      ]
    : columns;

  const table = useReactTable({
    data,
    columns: columnsWithCheckbox,
    state: {
      sorting,
      globalFilter,
      rowSelection: effectiveRowSelection,
    },
    enableRowSelection: hasCheckbox,
    onSortingChange: setSorting,
    onRowSelectionChange: (updater) => {
      const newSelection = typeof updater === 'function' ? updater(effectiveRowSelection) : updater;
      if (onRowSelectionChange) {
        onRowSelectionChange(newSelection);
      } else {
        setInternalRowSelection(newSelection);
      }
    },
    getRowId: (row) => String(row.id),
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
  });

  const handleScrollLeft = () => {
    scrollRef.current?.scrollBy({ left: -200, behavior: 'smooth' });
  };

  const handleScrollRight = () => {
    scrollRef.current?.scrollBy({ left: 200, behavior: 'smooth' });
  };

  const columnCount = columnsWithCheckbox.length;
  const lastTwoStart = columnCount - 2;
  const nameColIndex = hasCheckbox ? 1 : 0;

  // 오늘 날짜 컬럼 ID
  const todayColumnId = useMemo(() => `date-${getTodayISO()}`, []);

  // 컬럼 size 합계로 테이블 최소 너비 계산
  const tableMinWidth = columnsWithCheckbox.reduce((sum, col) => {
    const colDef = col as { size?: number; minSize?: number };
    return sum + (colDef.size ?? colDef.minSize ?? 50);
  }, 0);

  return (
    <div className="h-full flex flex-col rounded-xl border bg-white shadow-sm">
      {/* 스크롤 버튼 */}
      <div className="relative z-30 flex items-center justify-center gap-2 py-1 border-b bg-muted/30">
        <Button
          type="button"
          variant="ghost"
          size="sm"
          className="h-6 px-2 text-xs hover:bg-muted"
          onClick={handleScrollLeft}
        >
          <ChevronLeft className="h-3 w-3 mr-1" />
          이전
        </Button>
        <span className="text-xs text-muted-foreground">날짜 스크롤</span>
        <Button
          type="button"
          variant="ghost"
          size="sm"
          className="h-6 px-2 text-xs hover:bg-muted"
          onClick={handleScrollRight}
        >
          다음
          <ChevronRight className="h-3 w-3 ml-1" />
        </Button>
      </div>

      <div className="flex-1 min-h-0">
        <Table
          wrapperRef={scrollRef}
          wrapperClassName="h-full overflow-x-auto overflow-y-auto"
          style={{ minWidth: `${tableMinWidth}px` }}
        >
          <TableHeader className="sticky top-0 z-20 bg-muted/50">
            {table.getHeaderGroups().map((headerGroup) => (
              <TableRow key={headerGroup.id}>
                {headerGroup.headers.map((header, index) => {
                  const isToday = header.column.id === todayColumnId;
                  let className = isToday ? 'bg-primary/10' : 'bg-muted/50';
                  let style: React.CSSProperties = { width: header.column.columnDef.size };

                  // 체크박스 컬럼 - 왼쪽 고정
                  if (hasCheckbox && index === 0) {
                    className = 'sticky left-0 z-30 bg-muted/50';
                    style = { ...style, minWidth: CHECKBOX_COL_WIDTH };
                  }
                  // 이름 컬럼 - 왼쪽 고정
                  else if (index === nameColIndex) {
                    className = 'sticky z-30 bg-muted/50';
                    const leftOffset = hasCheckbox ? CHECKBOX_COL_WIDTH : 0;
                    style = { ...style, left: leftOffset, minWidth: NAME_COL_WIDTH, boxShadow: '2px 0 0 0 hsl(var(--border))' };
                  }
                  // 마지막 두 컬럼 (출석률, 상세) - 오른쪽 고정
                  else if (index === lastTwoStart) {
                    className = 'sticky z-30 bg-muted/50';
                    style = { ...style, right: SUMMARY_COL_WIDTH, minWidth: RATE_COL_WIDTH, boxShadow: '-2px 0 0 0 hsl(var(--border))' };
                  } else if (index === lastTwoStart + 1) {
                    className = 'sticky z-30 bg-muted/50';
                    style = { ...style, right: 0, minWidth: SUMMARY_COL_WIDTH };
                  }

                  return (
                    <TableHead key={header.id} className={className} style={style}>
                      {header.isPlaceholder
                        ? null
                        : flexRender(header.column.columnDef.header, header.getContext())}
                    </TableHead>
                  );
                })}
              </TableRow>
            ))}
          </TableHeader>
          <TableBody>
            {isLoading ? (
              <TableSkeleton columnCount={columnCount} rowCount={10} />
            ) : table.getRowModel().rows?.length ? (
              table.getRowModel().rows.map((row) => (
                <MemoizedRow
                  key={row.id}
                  row={row}
                  onRowClick={onRowClick}
                  columnCount={columnCount}
                  hasCheckbox={hasCheckbox}
                  todayColumnId={todayColumnId}
                />
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={columnCount} className="h-32 text-center">
                  <p className="text-muted-foreground">출석 데이터가 없습니다.</p>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>

      {/* 선택 정보 표시 */}
      {hasCheckbox && Object.keys(effectiveRowSelection).length > 0 && (
        <div className="flex items-center justify-between border-t px-4 py-2 bg-muted/30 shrink-0">
          <span className="text-sm text-muted-foreground">
            {Object.keys(effectiveRowSelection).length}명 선택됨
          </span>
        </div>
      )}
    </div>
  );
}
