import { useState, useMemo } from 'react';
import { useAuthStore } from '@/stores/authStore';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';
import { Input } from '@/shared/components/ui/input';
import { Button } from '@/shared/components/ui/button';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';
import { Checkbox } from '@/shared/components/ui/checkbox';
import { FileText, CheckCircle, AlertCircle, Send, Plus, Pencil } from 'lucide-react';
import { useInvoices, useBulkIssueInvoices } from '../hooks';
import { InvoiceStatusBadge } from './InvoiceStatusBadge';
import { InvoiceDetailSheet } from './InvoiceDetailSheet';
import { InvoiceCreateSheet } from './InvoiceCreateSheet';
import { InvoiceBulkUpdateSheet } from './InvoiceBulkUpdateSheet';
import { PAYMENT_COLORS } from '../constants/chartColors';
import { useAlert } from '@/hooks';
import type { InvoiceStatus, InvoiceListRes } from '../types';
import { formatBillingYearMonth, extractYear, extractMonth } from '../utils';

const getStatusBorderColor = (status: InvoiceStatus) => {
  switch (status) {
    case 'PAID':
      return PAYMENT_COLORS.paid.main;
    case 'PARTIAL':
      return PAYMENT_COLORS.partial.main;
    case 'OPEN':
      return PAYMENT_COLORS.unpaid.main;
    case 'DRAFT':
      return PAYMENT_COLORS.draft.main;
    case 'VOID':
      return PAYMENT_COLORS.void.main;
    default:
      return 'transparent';
  }
};

const STATUS_OPTIONS: { value: InvoiceStatus | 'ALL'; label: string }[] = [
  { value: 'ALL', label: '전체 상태' },
  { value: 'DRAFT', label: '임시저장' },
  { value: 'OPEN', label: '미납' },
  { value: 'PARTIAL', label: '부분납부' },
  { value: 'PAID', label: '완납' },
  { value: 'VOID', label: '무효' },
];

export function InvoiceTab() {
  const { selectedDojang } = useAuthStore();
  const dojangId = selectedDojang?.dojangId ?? null;

  const now = new Date();
  const [statusFilter, setStatusFilter] = useState<InvoiceStatus | 'ALL'>('ALL');
  const [yearFilter, setYearFilter] = useState<number | 'ALL'>(now.getFullYear());
  const [monthFilter, setMonthFilter] = useState<number | 'ALL'>(now.getMonth() + 1);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedIds, setSelectedIds] = useState<string[]>([]);
  const [selectedInvoiceId, setSelectedInvoiceId] = useState<string | null>(null);
  const [showCreateSheet, setShowCreateSheet] = useState(false);
  const [showBulkUpdateSheet, setShowBulkUpdateSheet] = useState(false);

  const { showSuccess, showError } = useAlert();

  const { data: invoices = [], isLoading } = useInvoices(
    dojangId,
    statusFilter === 'ALL' ? undefined : statusFilter
  );

  const { mutateAsync: bulkIssue, isPending: isIssuing } = useBulkIssueInvoices(dojangId);

  const yearOptions = useMemo(() => {
    const currentYear = now.getFullYear();
    const years: number[] = [];
    for (let year = currentYear - 1; year <= currentYear + 1; year++) {
      years.push(year);
    }
    return years;
  }, []);

  const monthOptions = useMemo(() => {
    return [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12];
  }, []);

  const filteredInvoices = useMemo(() => {
    let result = invoices;

    if (yearFilter !== 'ALL') {
      result = result.filter((inv) => extractYear(inv.billingYearMonth) === yearFilter);
    }

    if (monthFilter !== 'ALL') {
      result = result.filter((inv) => extractMonth(inv.billingYearMonth) === monthFilter);
    }

    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      result = result.filter((inv) => inv.studentName.toLowerCase().includes(query));
    }

    return result;
  }, [invoices, yearFilter, monthFilter, searchQuery]);

  const statistics = useMemo(() => {
    const total = filteredInvoices.reduce((sum, inv) => sum + inv.amount, 0);
    const paid = filteredInvoices.reduce((sum, inv) => sum + inv.paidAmount, 0);
    const unpaid = total - paid;
    const paidCount = filteredInvoices.filter((inv) => inv.status === 'PAID').length;
    const rate = total > 0 ? Math.round((paid / total) * 100) : 0;
    return { total, paid, unpaid, paidCount, count: filteredInvoices.length, rate };
  }, [filteredInvoices]);

  const draftInvoices = useMemo(
    () => filteredInvoices.filter((inv) => inv.status === 'DRAFT'),
    [filteredInvoices]
  );

  const formatAmount = (amount: number) => {
    return new Intl.NumberFormat('ko-KR').format(amount);
  };

  const handleSelectAll = (checked: boolean) => {
    if (checked) {
      setSelectedIds(draftInvoices.map((inv) => inv.id));
    } else {
      setSelectedIds([]);
    }
  };

  const handleSelectOne = (id: string, checked: boolean) => {
    if (checked) {
      setSelectedIds((prev) => [...prev, id]);
    } else {
      setSelectedIds((prev) => prev.filter((i) => i !== id));
    }
  };

  const handleBulkIssue = async () => {
    if (selectedIds.length === 0) return;

    try {
      const result = await bulkIssue({ invoiceIds: selectedIds });
      showSuccess(`${result.issuedCount}건 발행 완료 (${result.failedCount}건 실패)`);
      setSelectedIds([]);
    } catch {
      showError('일괄 발행에 실패했습니다');
    }
  };

  const handleRowClick = (invoice: InvoiceListRes) => {
    if (selectedInvoiceId === invoice.id) {
      setSelectedInvoiceId(null);
    } else {
      setSelectedInvoiceId(invoice.id);
    }
  };

  const periodLabel = useMemo(() => {
    if (yearFilter === 'ALL' && monthFilter === 'ALL') return '전체 기간';
    if (yearFilter !== 'ALL' && monthFilter === 'ALL') return `${yearFilter}년`;
    if (yearFilter === 'ALL' && monthFilter !== 'ALL') return `${monthFilter}월`;
    return `${yearFilter}년 ${monthFilter}월`;
  }, [yearFilter, monthFilter]);

  return (
    <div className="flex flex-col gap-4 flex-1 min-h-0">
      <div className="grid gap-4 grid-cols-4 shrink-0">
        <Card className="border shadow-sm hover:shadow-md transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-1 px-4 pt-3">
            <CardTitle className="text-xs font-medium text-muted-foreground">총 청구</CardTitle>
            <div className="p-1.5 rounded-full bg-blue-50">
              <FileText className="h-3.5 w-3.5 text-blue-500" />
            </div>
          </CardHeader>
          <CardContent className="px-4 pb-3 pt-0">
            <div className="text-2xl font-bold">₩{formatAmount(statistics.total)}</div>
            <p className="text-[10px] text-muted-foreground mt-0.5">
              {periodLabel} · {statistics.count}건
            </p>
          </CardContent>
        </Card>

        <Card className="border shadow-sm hover:shadow-md transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-1 px-4 pt-3">
            <CardTitle className="text-xs font-medium text-muted-foreground">수납 완료</CardTitle>
            <div className="p-1.5 rounded-full" style={{ backgroundColor: PAYMENT_COLORS.paid.light }}>
              <CheckCircle className="h-3.5 w-3.5" style={{ color: PAYMENT_COLORS.paid.main }} />
            </div>
          </CardHeader>
          <CardContent className="px-4 pb-3 pt-0">
            <div className="text-2xl font-bold" style={{ color: PAYMENT_COLORS.paid.dark }}>
              ₩{formatAmount(statistics.paid)}
            </div>
            <p className="text-[10px] text-muted-foreground mt-0.5">{statistics.paidCount}건 완납</p>
          </CardContent>
        </Card>

        <Card className="border shadow-sm hover:shadow-md transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-1 px-4 pt-3">
            <CardTitle className="text-xs font-medium text-muted-foreground">미수납</CardTitle>
            <div className="p-1.5 rounded-full" style={{ backgroundColor: PAYMENT_COLORS.unpaid.light }}>
              <AlertCircle className="h-3.5 w-3.5" style={{ color: PAYMENT_COLORS.unpaid.main }} />
            </div>
          </CardHeader>
          <CardContent className="px-4 pb-3 pt-0">
            <div className="text-2xl font-bold" style={{ color: PAYMENT_COLORS.unpaid.dark }}>
              ₩{formatAmount(statistics.unpaid)}
            </div>
          </CardContent>
        </Card>

        <Card className="border shadow-sm hover:shadow-md transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-1 px-4 pt-3">
            <CardTitle className="text-xs font-medium text-muted-foreground">수납률</CardTitle>
            <div className="p-1.5 rounded-full" style={{ backgroundColor: PAYMENT_COLORS.paid.light }}>
              <CheckCircle className="h-3.5 w-3.5" style={{ color: PAYMENT_COLORS.paid.main }} />
            </div>
          </CardHeader>
          <CardContent className="px-4 pb-3 pt-0">
            <div className="text-2xl font-bold" style={{ color: PAYMENT_COLORS.paid.main }}>
              {statistics.rate}%
            </div>
          </CardContent>
        </Card>
      </div>

      <div className="flex items-center justify-between gap-4 shrink-0">
        <div className="flex items-center gap-3">
          <Select
            value={yearFilter.toString()}
            onValueChange={(v) => setYearFilter(v === 'ALL' ? 'ALL' : parseInt(v, 10))}
          >
            <SelectTrigger className="w-[110px]">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">전체</SelectItem>
              {yearOptions.map((year) => (
                <SelectItem key={year} value={year.toString()}>
                  {year}년
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          <Select
            value={monthFilter.toString()}
            onValueChange={(v) => setMonthFilter(v === 'ALL' ? 'ALL' : parseInt(v, 10))}
          >
            <SelectTrigger className="w-[100px]">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">전체</SelectItem>
              {monthOptions.map((month) => (
                <SelectItem key={month} value={month.toString()}>
                  {month}월
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          <Select
            value={statusFilter}
            onValueChange={(v) => setStatusFilter(v as InvoiceStatus | 'ALL')}
          >
            <SelectTrigger className="w-[120px]">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {STATUS_OPTIONS.map((opt) => (
                <SelectItem key={opt.value} value={opt.value}>
                  {opt.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          <Input
            placeholder="원생 이름 검색..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-[200px]"
          />
        </div>

        <div className="flex items-center gap-2">
          {selectedIds.length > 0 && (
            <>
              <Button
                variant="outline"
                onClick={() => setShowBulkUpdateSheet(true)}
              >
                <Pencil className="mr-2 h-4 w-4" />
                {selectedIds.length}건 일괄 수정
              </Button>
              <Button onClick={handleBulkIssue} disabled={isIssuing}>
                <Send className="mr-2 h-4 w-4" />
                {isIssuing ? '발행 중...' : `${selectedIds.length}건 일괄 발행`}
              </Button>
            </>
          )}
          <Button onClick={() => setShowCreateSheet(true)}>
            <Plus className="mr-2 h-4 w-4" />
            청구서 생성
          </Button>
        </div>
      </div>

      <div className="flex-1 min-h-0 border rounded-lg bg-white shadow-sm overflow-auto">
        <Table wrapperClassName="overflow-visible">
          <TableHeader className="sticky top-0 z-10 bg-muted">
            <TableRow>
              <TableHead className="w-[50px]">
                <Checkbox
                  checked={
                    draftInvoices.length > 0 &&
                    selectedIds.length === draftInvoices.length
                  }
                  onCheckedChange={handleSelectAll}
                  disabled={draftInvoices.length === 0}
                />
              </TableHead>
              <TableHead>원생명</TableHead>
              <TableHead>청구 연월</TableHead>
              <TableHead className="text-right">청구금액</TableHead>
              <TableHead className="text-right">납부금액</TableHead>
              <TableHead className="text-right">잔여금액</TableHead>
              <TableHead>납부기한</TableHead>
              <TableHead>상태</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              <TableRow>
                <TableCell colSpan={8} className="text-center py-8 text-muted-foreground">
                  로딩 중...
                </TableCell>
              </TableRow>
            ) : filteredInvoices.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} className="h-[300px]">
                  <div className="flex flex-col items-center justify-center h-full text-muted-foreground">
                    <FileText className="h-12 w-12 mb-4 opacity-30" />
                    <p className="text-base font-medium mb-1">
                      {periodLabel} 청구서가 없습니다
                    </p>
                    <p className="text-sm mb-4">청구서를 생성해주세요</p>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setShowCreateSheet(true)}
                    >
                      <Plus className="mr-2 h-4 w-4" />
                      청구서 생성
                    </Button>
                  </div>
                </TableCell>
              </TableRow>
            ) : (
              filteredInvoices.map((invoice) => {
                const isDraft = invoice.status === 'DRAFT';
                const isSelected = selectedIds.includes(invoice.id);
                return (
                  <TableRow
                    key={invoice.id}
                    className="cursor-pointer hover:bg-muted/50"
                    onClick={() => handleRowClick(invoice)}
                    data-state={isSelected && 'selected'}
                    style={{ borderLeft: `3px solid ${getStatusBorderColor(invoice.status)}` }}
                  >
                    <TableCell
                      className={`bg-muted/50 ${isDraft ? 'cursor-pointer hover:bg-muted' : ''}`}
                      onClick={(e) => {
                        e.stopPropagation();
                        if (isDraft) handleSelectOne(invoice.id, !isSelected);
                      }}
                    >
                      {isDraft && (
                        <Checkbox
                          checked={isSelected}
                          onCheckedChange={(checked) =>
                            handleSelectOne(invoice.id, checked as boolean)
                          }
                        />
                      )}
                    </TableCell>
                    <TableCell
                      className={`font-medium bg-muted/50 ${isDraft ? 'cursor-pointer hover:bg-muted' : ''}`}
                      onClick={(e) => {
                        if (isDraft) {
                          e.stopPropagation();
                          handleSelectOne(invoice.id, !isSelected);
                        }
                      }}
                    >
                      {invoice.studentName}
                    </TableCell>
                    <TableCell>
                      {formatBillingYearMonth(invoice.billingYearMonth)}
                    </TableCell>
                    <TableCell className="text-right">₩{formatAmount(invoice.amount)}</TableCell>
                    <TableCell className="text-right text-green-600">
                      ₩{formatAmount(invoice.paidAmount)}
                    </TableCell>
                    <TableCell className="text-right text-red-600">
                      ₩{formatAmount(invoice.remainingAmount)}
                    </TableCell>
                    <TableCell>{invoice.dueDate}</TableCell>
                    <TableCell>
                      <InvoiceStatusBadge status={invoice.status} />
                    </TableCell>
                  </TableRow>
                );
              })
            )}
          </TableBody>
        </Table>
      </div>

      <InvoiceDetailSheet
        invoiceId={selectedInvoiceId}
        isOpen={!!selectedInvoiceId}
        onClose={() => setSelectedInvoiceId(null)}
      />

      <InvoiceCreateSheet
        isOpen={showCreateSheet}
        onClose={() => setShowCreateSheet(false)}
      />

      <InvoiceBulkUpdateSheet
        isOpen={showBulkUpdateSheet}
        onClose={() => setShowBulkUpdateSheet(false)}
        selectedIds={selectedIds}
        dojangId={dojangId}
        onSuccess={() => setSelectedIds([])}
      />
    </div>
  );
}
