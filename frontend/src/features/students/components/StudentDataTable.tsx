import { useState, useEffect, useRef, memo } from 'react';
import {
  ColumnDef,
  SortingState,
  RowSelectionState,
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
import { useResponsivePageSize, PageSizeConfig } from '@/hooks/useResponsivePageSize';

const PAGE_SIZE_CONFIG: PageSizeConfig = {
  rowHeight: 70,
  tableHeaderHeight: 48,
  paginationHeight: 56,
  bottomPadding: 32,
  minPageSize: 5,
  maxPageSize: 25,
};

interface StudentDataTableProps<TData> {
  columns: ColumnDef<TData>[];
  data: TData[];
  isLoading?: boolean;
  onRowClick?: (row: TData) => void;
  rowSelection: RowSelectionState;
  onRowSelectionChange: (selection: RowSelectionState) => void;
  globalFilter: string;
}

function TableSkeleton({ columnCount }: { columnCount: number }) {
  return (
    <>
      {Array.from({ length: 5 }).map((_, i) => (
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
}

const MemoizedRow = memo(function MemoizedRow<TData>({
  row,
  onRowClick,
}: MemoizedRowProps<TData>) {
  return (
    <TableRow
      data-state={row.getIsSelected() && 'selected'}
      className="cursor-pointer hover:bg-muted/50"
      onClick={() => onRowClick?.(row.original)}
    >
      {row.getVisibleCells().map((cell, index) => (
        <TableCell
          key={cell.id}
          className={index < 2 ? 'bg-muted/50 hover:bg-muted' : ''}
        >
          {flexRender(cell.column.columnDef.cell, cell.getContext())}
        </TableCell>
      ))}
    </TableRow>
  );
}) as <TData>(props: MemoizedRowProps<TData>) => JSX.Element;

export function StudentDataTable<TData>({
  columns,
  data,
  isLoading,
  onRowClick,
  rowSelection,
  onRowSelectionChange,
  globalFilter,
}: StudentDataTableProps<TData>) {
  const containerRef = useRef<HTMLDivElement>(null);
  const [sorting, setSorting] = useState<SortingState>([]);
  const pageSize = useResponsivePageSize(containerRef, PAGE_SIZE_CONFIG);
  const [pagination, setPagination] = useState<PaginationState>({
    pageIndex: 0,
    pageSize,
  });

  // pageSize 변경 시 첫 페이지로 이동
  useEffect(() => {
    setPagination((prev) => {
      if (prev.pageSize === pageSize) return prev;
      return { pageIndex: 0, pageSize };
    });
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
      rowSelection,
      globalFilter,
      pagination,
    },
    enableRowSelection: true,
    autoResetPageIndex: false,
    onSortingChange: setSorting,
    onPaginationChange: setPagination,
    onRowSelectionChange: (updater) => {
      const newSelection = typeof updater === 'function' ? updater(rowSelection) : updater;
      onRowSelectionChange(newSelection);
    },
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
  });

  return (
    <div ref={containerRef} className="rounded-xl border bg-white shadow-sm">
      <Table>
        <TableHeader className="bg-muted">
          {table.getHeaderGroups().map((headerGroup) => (
            <TableRow key={headerGroup.id}>
              {headerGroup.headers.map((header) => (
                <TableHead key={header.id}>
                  {header.isPlaceholder
                    ? null
                    : flexRender(header.column.columnDef.header, header.getContext())}
                </TableHead>
              ))}
            </TableRow>
          ))}
        </TableHeader>
        <TableBody>
          {isLoading ? (
            <TableSkeleton columnCount={columns.length} />
          ) : table.getRowModel().rows?.length ? (
            table.getRowModel().rows.map((row) => (
              <MemoizedRow key={row.id} row={row} onRowClick={onRowClick} />
            ))
          ) : (
            <TableRow>
              <TableCell colSpan={columns.length} className="h-32 text-center">
                <p className="text-muted-foreground">등록된 수련생이 없습니다.</p>
              </TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>

      {/* 페이지네이션 */}
      {!isLoading && table.getFilteredRowModel().rows.length > 0 && (
        <div className="flex items-center justify-center gap-2 border-t px-4 py-3">
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
            {table.getState().pagination.pageIndex + 1} / {table.getPageCount()}
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
