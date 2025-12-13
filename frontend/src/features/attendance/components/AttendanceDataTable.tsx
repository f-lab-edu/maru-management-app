import { useState, useEffect, useRef, memo } from 'react';
import {
  ColumnDef,
  SortingState,
  PaginationState,
  flexRender,
  getCoreRowModel,
  getSortedRowModel,
  getFilteredRowModel,
  getPaginationRowModel,
  useReactTable,
  Row,
} from '@tanstack/react-table';
import {
  ChevronLeft,
  ChevronRight,
  ChevronsLeft,
  ChevronsRight,
} from 'lucide-react';
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

const ROW_HEIGHT = 53;
const TABLE_HEADER_HEIGHT = 49;
const PAGINATION_HEIGHT = 57;
const MIN_PAGE_SIZE = 3;
const MAX_PAGE_SIZE = 20;

const NAME_COL_WIDTH = 140;
const RATE_COL_WIDTH = 110;
const SUMMARY_COL_WIDTH = 140;

interface AttendanceDataTableProps<TData> {
  columns: ColumnDef<TData>[];
  data: TData[];
  isLoading?: boolean;
  onRowClick?: (row: TData) => void;
  globalFilter?: string;
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
}

const MemoizedRow = memo(function MemoizedRow<TData>({
  row,
  onRowClick,
  columnCount,
}: MemoizedRowProps<TData>) {
  const cells = row.getVisibleCells();
  const lastTwoStart = columnCount - 2;

  return (
    <TableRow
      className="cursor-pointer hover:bg-muted/50"
      onClick={() => onRowClick?.(row.original)}
    >
      {cells.map((cell, index) => {
        let className = '';
        let style: React.CSSProperties = { width: cell.column.columnDef.size };

        // 첫 번째 컬럼 (이름) - 왼쪽 고정
        if (index === 0) {
          className = 'sticky left-0 z-10 bg-white';
          style = { ...style, minWidth: NAME_COL_WIDTH, boxShadow: '2px 0 0 0 hsl(var(--border))' };
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

export function AttendanceDataTable<TData>({
  columns,
  data,
  isLoading,
  onRowClick,
  globalFilter = '',
}: AttendanceDataTableProps<TData>) {
  const containerRef = useRef<HTMLDivElement>(null);
  const scrollRef = useRef<HTMLDivElement>(null);
  const [sorting, setSorting] = useState<SortingState>([]);
  const [pageSize, setPageSize] = useState(MIN_PAGE_SIZE);
  const [pagination, setPagination] = useState<PaginationState>({
    pageIndex: 0,
    pageSize: MIN_PAGE_SIZE,
  });

  // 컨테이너 높이 기반 pageSize 계산
  useEffect(() => {
    const calculatePageSize = () => {
      if (!containerRef.current) return;

      const containerHeight = containerRef.current.clientHeight;
      const availableHeight = containerHeight - TABLE_HEADER_HEIGHT - PAGINATION_HEIGHT;
      const calculated = Math.floor(availableHeight / ROW_HEIGHT);
      const newPageSize = Math.max(MIN_PAGE_SIZE, Math.min(MAX_PAGE_SIZE, calculated));

      if (newPageSize !== pageSize) {
        setPageSize(newPageSize);
        setPagination({ pageIndex: 0, pageSize: newPageSize });
      }
    };

    calculatePageSize();

    const resizeObserver = new ResizeObserver(calculatePageSize);
    if (containerRef.current) {
      resizeObserver.observe(containerRef.current);
    }

    return () => resizeObserver.disconnect();
  }, [pageSize]);

  // 필터 변경 시 첫 페이지로 이동
  useEffect(() => {
    setPagination((prev) => ({ ...prev, pageIndex: 0 }));
  }, [globalFilter]);

  const table = useReactTable({
    data,
    columns,
    state: {
      sorting,
      globalFilter,
      pagination,
    },
    autoResetPageIndex: false,
    onSortingChange: setSorting,
    onPaginationChange: setPagination,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
  });

  const handleScrollLeft = () => {
    scrollRef.current?.scrollBy({ left: -200, behavior: 'smooth' });
  };

  const handleScrollRight = () => {
    scrollRef.current?.scrollBy({ left: 200, behavior: 'smooth' });
  };

  const columnCount = columns.length;
  const lastTwoStart = columnCount - 2;

  // 컬럼 size 합계로 테이블 최소 너비 계산
  const tableMinWidth = columns.reduce((sum, col) => {
    const colDef = col as { size?: number; minSize?: number };
    return sum + (colDef.size ?? colDef.minSize ?? 50);
  }, 0);

  return (
    <div ref={containerRef} className="h-full flex flex-col rounded-xl border bg-white shadow-sm">
      {/* 스크롤 버튼 */}
      <div className="relative z-30 flex items-center justify-center gap-2 py-1 border-b bg-muted/30 pointer-events-auto">
        <Button
          type="button"
          variant="ghost"
          size="sm"
          className="h-6 px-2 text-xs hover:bg-muted pointer-events-auto"
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
          className="h-6 px-2 text-xs hover:bg-muted pointer-events-auto"
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
          <TableHeader>
            {table.getHeaderGroups().map((headerGroup) => (
              <TableRow key={headerGroup.id}>
                {headerGroup.headers.map((header, index) => {
                  let className = '';
                  let style: React.CSSProperties = { width: header.column.columnDef.size };

                  // 첫 번째 컬럼 (이름) - 왼쪽 고정
                  if (index === 0) {
                    className = 'sticky left-0 z-20 bg-muted/50';
                    style = { ...style, minWidth: NAME_COL_WIDTH, boxShadow: '2px 0 0 0 hsl(var(--border))' };
                  }
                  // 마지막 두 컬럼 (출석률, 상세) - 오른쪽 고정
                  else if (index === lastTwoStart) {
                    className = 'sticky z-20 bg-muted/50';
                    style = { ...style, right: SUMMARY_COL_WIDTH, minWidth: RATE_COL_WIDTH, boxShadow: '-2px 0 0 0 hsl(var(--border))' };
                  } else if (index === lastTwoStart + 1) {
                    className = 'sticky z-20 bg-muted/50';
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
              <TableSkeleton columnCount={columnCount} rowCount={pageSize} />
            ) : table.getRowModel().rows?.length ? (
              table.getRowModel().rows.map((row) => (
                <MemoizedRow
                  key={row.id}
                  row={row}
                  onRowClick={onRowClick}
                  columnCount={columnCount}
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

      {/* 페이지네이션 */}
      {!isLoading && table.getFilteredRowModel().rows.length > 0 && (
        <div className="flex items-center justify-center gap-2 border-t px-4 py-3 shrink-0">
          <Button
            variant="outline"
            size="icon"
            className="h-8 w-8"
            onClick={() => table.firstPage()}
            disabled={!table.getCanPreviousPage()}
          >
            <ChevronsLeft className="h-4 w-4" />
          </Button>
          <Button
            variant="outline"
            size="icon"
            className="h-8 w-8"
            onClick={() => table.previousPage()}
            disabled={!table.getCanPreviousPage()}
          >
            <ChevronLeft className="h-4 w-4" />
          </Button>

          <span className="text-sm text-muted-foreground px-2">
            {table.getState().pagination.pageIndex + 1} / {table.getPageCount() || 1}
          </span>

          <Button
            variant="outline"
            size="icon"
            className="h-8 w-8"
            onClick={() => table.nextPage()}
            disabled={!table.getCanNextPage()}
          >
            <ChevronRight className="h-4 w-4" />
          </Button>
          <Button
            variant="outline"
            size="icon"
            className="h-8 w-8"
            onClick={() => table.lastPage()}
            disabled={!table.getCanNextPage()}
          >
            <ChevronsRight className="h-4 w-4" />
          </Button>
        </div>
      )}
    </div>
  );
}
