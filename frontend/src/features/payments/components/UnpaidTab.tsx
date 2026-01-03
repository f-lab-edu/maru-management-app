import { useState, useMemo } from 'react';
import { useAuthStore } from '@/stores/authStore';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';
import { Input } from '@/shared/components/ui/input';
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
import { Badge } from '@/shared/components/ui/badge';
import { AlertTriangle, Phone, CheckCircle } from 'lucide-react';
import { useUnpaidList } from '../hooks';
import { InvoiceDetailSheet } from './InvoiceDetailSheet';
import { PAYMENT_COLORS } from '../constants/chartColors';
import type { UnpaidListRes } from '../types';
import { formatBillingYearMonth, extractYear, extractMonth } from '../utils';
import { SectionDivisionFilter } from '@/features/divisions';

const SEVERITY_CONFIG = {
  critical: { label: '심각', color: '#dc2626', bg: '#fee2e2', days: '7일+' },
  warning: { label: '경고', color: '#ea580c', bg: '#ffedd5', days: '3~6일' },
  mild: { label: '주의', color: '#ca8a04', bg: '#fef9c3', days: '1~2일' },
};

export function UnpaidTab() {
  const { selectedDojang } = useAuthStore();
  const dojangId = selectedDojang?.dojangId ?? null;

  const now = new Date();
  const [yearFilter, setYearFilter] = useState<number | 'ALL'>(now.getFullYear());
  const [monthFilter, setMonthFilter] = useState<number | 'ALL'>(now.getMonth() + 1);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedInvoiceId, setSelectedInvoiceId] = useState<string | null>(null);

  const [sectionId, setSectionId] = useState<string | null>(null);
  const [divisionId, setDivisionId] = useState<string | null>(null);

  const { data: unpaidList = [], isLoading } = useUnpaidList(dojangId, sectionId, divisionId);

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

  const filteredList = useMemo(() => {
    let result = unpaidList;

    if (yearFilter !== 'ALL') {
      result = result.filter((item) => extractYear(item.billingYearMonth) === yearFilter);
    }

    if (monthFilter !== 'ALL') {
      result = result.filter((item) => extractMonth(item.billingYearMonth) === monthFilter);
    }

    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      result = result.filter(
        (item) =>
          item.studentName.toLowerCase().includes(query) ||
          item.guardianName?.toLowerCase().includes(query)
      );
    }

    return result;
  }, [unpaidList, yearFilter, monthFilter, searchQuery]);

  const summary = useMemo(() => {
    const critical = filteredList.filter((item) => item.overdueDays >= 7).length;
    const warning = filteredList.filter(
      (item) => item.overdueDays >= 3 && item.overdueDays < 7
    ).length;
    const mild = filteredList.filter(
      (item) => item.overdueDays >= 1 && item.overdueDays < 3
    ).length;
    const totalAmount = filteredList.reduce((sum, item) => sum + item.remainingAmount, 0);
    const total = filteredList.length;
    return { critical, warning, mild, totalAmount, total };
  }, [filteredList]);

  const formatAmount = (amount: number) => {
    return new Intl.NumberFormat('ko-KR').format(amount);
  };

  const getRowBorderColor = (days: number) => {
    if (days >= 7) return SEVERITY_CONFIG.critical.color;
    if (days >= 3) return SEVERITY_CONFIG.warning.color;
    if (days >= 1) return SEVERITY_CONFIG.mild.color;
    return 'transparent';
  };

  const getOverdueBadgeStyle = (days: number) => {
    if (days >= 7) return { backgroundColor: SEVERITY_CONFIG.critical.bg, color: SEVERITY_CONFIG.critical.color };
    if (days >= 3) return { backgroundColor: SEVERITY_CONFIG.warning.bg, color: SEVERITY_CONFIG.warning.color };
    if (days >= 1) return { backgroundColor: SEVERITY_CONFIG.mild.bg, color: SEVERITY_CONFIG.mild.color };
    return { backgroundColor: '#f3f4f6', color: '#6b7280' };
  };

  const handleRowClick = (item: UnpaidListRes) => {
    if (selectedInvoiceId === item.invoiceId) {
      setSelectedInvoiceId(null);
    } else {
      setSelectedInvoiceId(item.invoiceId);
    }
  };

  if (unpaidList.length === 0 && !isLoading) {
    return (
      <div className="flex-1 flex flex-col items-center justify-center text-center">
        <div className="p-4 rounded-full bg-emerald-50 mb-4">
          <CheckCircle className="h-12 w-12 text-emerald-500" />
        </div>
        <h3 className="text-lg font-semibold mb-1">미납자가 없습니다</h3>
        <p className="text-muted-foreground">모든 청구서가 정상 납부되었습니다</p>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-4 flex-1 min-h-0">
      <div className="grid gap-4 grid-cols-4 shrink-0">
        <Card className="border shadow-sm hover:shadow-md transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-1 px-4 pt-3">
            <CardTitle className="text-xs font-medium text-muted-foreground">총 미납 금액</CardTitle>
            <div className="p-1.5 rounded-full" style={{ backgroundColor: PAYMENT_COLORS.unpaid.light }}>
              <AlertTriangle className="h-3.5 w-3.5" style={{ color: PAYMENT_COLORS.unpaid.main }} />
            </div>
          </CardHeader>
          <CardContent className="px-4 pb-3 pt-0">
            <div className="text-2xl font-bold" style={{ color: PAYMENT_COLORS.unpaid.dark }}>
              ₩{formatAmount(summary.totalAmount)}
            </div>
            <p className="text-[10px] text-muted-foreground mt-0.5">{summary.total}건</p>
          </CardContent>
        </Card>

        <Card className="border shadow-sm hover:shadow-md transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-1 px-4 pt-3">
            <CardTitle className="text-xs font-medium text-muted-foreground">심각</CardTitle>
            <div
              className="w-3 h-3 rounded-full"
              style={{ backgroundColor: SEVERITY_CONFIG.critical.color }}
            />
          </CardHeader>
          <CardContent className="px-4 pb-3 pt-0">
            <div className="text-2xl font-bold" style={{ color: SEVERITY_CONFIG.critical.color }}>
              {summary.critical}건
            </div>
            <p className="text-[10px] text-muted-foreground mt-0.5">7일 이상 연체</p>
          </CardContent>
        </Card>

        <Card className="border shadow-sm hover:shadow-md transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-1 px-4 pt-3">
            <CardTitle className="text-xs font-medium text-muted-foreground">경고</CardTitle>
            <div
              className="w-3 h-3 rounded-full"
              style={{ backgroundColor: SEVERITY_CONFIG.warning.color }}
            />
          </CardHeader>
          <CardContent className="px-4 pb-3 pt-0">
            <div className="text-2xl font-bold" style={{ color: SEVERITY_CONFIG.warning.color }}>
              {summary.warning}건
            </div>
            <p className="text-[10px] text-muted-foreground mt-0.5">3~6일 연체</p>
          </CardContent>
        </Card>

        <Card className="border shadow-sm hover:shadow-md transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-1 px-4 pt-3">
            <CardTitle className="text-xs font-medium text-muted-foreground">주의</CardTitle>
            <div
              className="w-3 h-3 rounded-full"
              style={{ backgroundColor: SEVERITY_CONFIG.mild.color }}
            />
          </CardHeader>
          <CardContent className="px-4 pb-3 pt-0">
            <div className="text-2xl font-bold" style={{ color: SEVERITY_CONFIG.mild.color }}>
              {summary.mild}건
            </div>
            <p className="text-[10px] text-muted-foreground mt-0.5">1~2일 연체</p>
          </CardContent>
        </Card>
      </div>

      <div className="flex items-center gap-3 shrink-0 flex-wrap">
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

        {dojangId && (
          <SectionDivisionFilter
            dojangId={dojangId}
            selectedSectionId={sectionId}
            selectedDivisionId={divisionId}
            onSectionChange={setSectionId}
            onDivisionChange={setDivisionId}
            compact
          />
        )}

        <Input
          placeholder="원생 또는 보호자 이름 검색..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="w-[250px]"
        />
      </div>

      <div className="flex-1 min-h-0 border rounded-lg bg-white shadow-sm overflow-auto">
        <Table wrapperClassName="overflow-visible">
          <TableHeader className="sticky top-0 bg-muted z-10">
            <TableRow>
              <TableHead className="font-semibold">원생명</TableHead>
              <TableHead>청구연월</TableHead>
              <TableHead>보호자</TableHead>
              <TableHead>연락처</TableHead>
              <TableHead className="text-right">미납금액</TableHead>
              <TableHead>납부기한</TableHead>
              <TableHead className="text-center">연체</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              <TableRow>
                <TableCell colSpan={7} className="text-center py-8 text-muted-foreground">
                  로딩 중...
                </TableCell>
              </TableRow>
            ) : filteredList.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} className="h-[200px]">
                  <div className="flex flex-col items-center justify-center h-full text-muted-foreground">
                    <AlertTriangle className="h-10 w-10 mb-3 opacity-30" />
                    <p className="text-sm">검색 결과가 없습니다</p>
                  </div>
                </TableCell>
              </TableRow>
            ) : (
              filteredList.map((item) => (
                <TableRow
                  key={item.invoiceId}
                  className="cursor-pointer hover:bg-muted/50"
                  onClick={() => handleRowClick(item)}
                  style={{ borderLeft: `3px solid ${getRowBorderColor(item.overdueDays)}` }}
                >
                  <TableCell className="font-medium">{item.studentName}</TableCell>
                  <TableCell className="text-muted-foreground">
                    {formatBillingYearMonth(item.billingYearMonth)}
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {item.guardianName || '-'}
                  </TableCell>
                  <TableCell>
                    {item.guardianPhone ? (
                      <div className="flex items-center gap-1 text-muted-foreground">
                        <Phone className="h-3 w-3" />
                        <span className="text-sm">{item.guardianPhone}</span>
                      </div>
                    ) : (
                      <span className="text-muted-foreground">-</span>
                    )}
                  </TableCell>
                  <TableCell className="text-right">
                    <span className="font-semibold" style={{ color: PAYMENT_COLORS.unpaid.dark }}>
                      ₩{formatAmount(item.remainingAmount)}
                    </span>
                  </TableCell>
                  <TableCell className="text-muted-foreground">{item.dueDate}</TableCell>
                  <TableCell className="text-center">
                    <Badge
                      variant="outline"
                      className="border-0 font-medium"
                      style={getOverdueBadgeStyle(item.overdueDays)}
                    >
                      {item.overdueDays}일
                    </Badge>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      <InvoiceDetailSheet
        invoiceId={selectedInvoiceId}
        isOpen={!!selectedInvoiceId}
        onClose={() => setSelectedInvoiceId(null)}
      />
    </div>
  );
}
