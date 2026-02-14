import { useState } from 'react';
import { Bell, CheckCircle, XCircle, ChevronDown, ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';
import {
  Table,
  TableHeader,
  TableBody,
  TableRow,
  TableHead,
  TableCell,
} from '@/shared/components/ui/table';
import { Button } from '@/shared/components/ui/button';
import { Spinner } from '@/shared/components/ui/spinner';
import { cn } from '@/shared/utils';
import { useAuthStore } from '@/stores/authStore';
import { useNotificationSummary, useNotificationDetails } from '../hooks/useNotifications';
import { MessageStatusBadge } from './MessageStatusBadge';
import type { NotificationDailySummary, MessageStatus } from '@/types/message';

const getTodayString = () => new Date().toISOString().split('T')[0];

const formatDateLabel = (dateStr: string) => {
  const today = getTodayString();
  if (dateStr === today) return '오늘';
  return new Date(dateStr + 'T00:00:00').toLocaleDateString('ko-KR', {
    month: 'long',
    day: 'numeric',
  });
};

export const NotificationSummaryList = () => {
  const [page, setPage] = useState(0);
  const [expandedKey, setExpandedKey] = useState<string | null>(null);
  const { selectedDojang } = useAuthStore();
  const dojangId = selectedDojang?.dojangId ?? '';
  const { data, isLoading } = useNotificationSummary(dojangId, page);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <Spinner className="h-6 w-6" />
      </div>
    );
  }

  if (!data || data.content.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-12 text-muted-foreground">
        <Bell className="h-12 w-12 mb-4 opacity-50" />
        <p className="font-medium">자동 알림 내역이 없습니다</p>
        <p className="text-sm mt-1">출결 및 결제 알림이 자동으로 기록됩니다</p>
      </div>
    );
  }

  const today = getTodayString();
  const todaySummaries = data.content.filter((s) => s.sendDate === today);
  const stats = {
    total: todaySummaries.reduce((sum, s) => sum + s.totalCount, 0),
    accepted: todaySummaries.reduce((sum, s) => sum + s.acceptedCount, 0),
    failed: todaySummaries.reduce((sum, s) => sum + s.failedCount, 0),
  };

  const toggleExpand = (key: string) => {
    setExpandedKey((prev) => (prev === key ? null : key));
  };

  return (
    <div className="space-y-4">
      {/* 통계 카드 */}
      <div className="grid grid-cols-3 gap-3">
        <Card className="border shadow-sm">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-1 px-4 pt-3">
            <CardTitle className="text-xs font-medium text-muted-foreground">오늘 발송</CardTitle>
            <Bell className="h-3.5 w-3.5 text-muted-foreground" />
          </CardHeader>
          <CardContent className="px-4 pb-3 pt-0">
            <div className="text-2xl font-bold">{stats.total}</div>
            <p className="text-[10px] text-muted-foreground mt-0.5">건</p>
          </CardContent>
        </Card>
        <Card className="border shadow-sm">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-1 px-4 pt-3">
            <CardTitle className="text-xs font-medium text-muted-foreground">성공</CardTitle>
            <CheckCircle className="h-3.5 w-3.5 text-muted-foreground" />
          </CardHeader>
          <CardContent className="px-4 pb-3 pt-0">
            <div className="text-2xl font-bold text-green-600">{stats.accepted}</div>
            <p className="text-[10px] text-muted-foreground mt-0.5">건</p>
          </CardContent>
        </Card>
        <Card className="border shadow-sm">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-1 px-4 pt-3">
            <CardTitle className="text-xs font-medium text-muted-foreground">실패</CardTitle>
            <XCircle className="h-3.5 w-3.5 text-muted-foreground" />
          </CardHeader>
          <CardContent className="px-4 pb-3 pt-0">
            <div className="text-2xl font-bold text-red-600">{stats.failed}</div>
            <p className="text-[10px] text-muted-foreground mt-0.5">건</p>
          </CardContent>
        </Card>
      </div>

      {/* Summary Table */}
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead className="w-8" />
            <TableHead>날짜</TableHead>
            <TableHead>유형</TableHead>
            <TableHead className="text-right">전체</TableHead>
            <TableHead className="text-right">성공</TableHead>
            <TableHead className="text-right">실패</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {data.content.map((summary) => {
            const key = `${summary.sendDate}-${summary.messageType}`;
            const isExpanded = expandedKey === key;
            return (
              <SummaryRowGroup
                key={key}
                summary={summary}
                isExpanded={isExpanded}
                onToggle={() => toggleExpand(key)}
                dojangId={dojangId}
              />
            );
          })}
        </TableBody>
      </Table>

      {/* 페이징 */}
      {data.totalPages > 1 && (
        <div className="flex items-center justify-center gap-2 pt-4">
          <Button
            variant="outline"
            size="icon"
            className="h-8 w-8"
            onClick={() => setPage(0)}
            disabled={page === 0}
          >
            <ChevronsLeft className="h-4 w-4" />
          </Button>
          <Button
            variant="outline"
            size="icon"
            className="h-8 w-8"
            onClick={() => setPage((p) => p - 1)}
            disabled={page === 0}
          >
            <ChevronLeft className="h-4 w-4" />
          </Button>
          <span className="text-sm text-muted-foreground px-2">
            {page + 1} / {data.totalPages}
          </span>
          <Button
            variant="outline"
            size="icon"
            className="h-8 w-8"
            onClick={() => setPage((p) => p + 1)}
            disabled={page >= data.totalPages - 1}
          >
            <ChevronRight className="h-4 w-4" />
          </Button>
          <Button
            variant="outline"
            size="icon"
            className="h-8 w-8"
            onClick={() => setPage(data.totalPages - 1)}
            disabled={page >= data.totalPages - 1}
          >
            <ChevronsRight className="h-4 w-4" />
          </Button>
        </div>
      )}
    </div>
  );
};

interface SummaryRowGroupProps {
  summary: NotificationDailySummary;
  isExpanded: boolean;
  onToggle: () => void;
  dojangId: string;
}

const SummaryRowGroup = ({ summary, isExpanded, onToggle, dojangId }: SummaryRowGroupProps) => {
  return (
    <>
      <TableRow
        className="cursor-pointer hover:bg-muted/50"
        onClick={onToggle}
      >
        <TableCell className="w-8 px-2">
          <ChevronDown
            className={cn(
              'h-4 w-4 text-muted-foreground transition-transform',
              isExpanded && 'rotate-180',
            )}
          />
        </TableCell>
        <TableCell>{formatDateLabel(summary.sendDate)}</TableCell>
        <TableCell>{summary.messageTypeLabel}</TableCell>
        <TableCell className="text-right">{summary.totalCount}</TableCell>
        <TableCell className="text-right text-green-600">{summary.acceptedCount}</TableCell>
        <TableCell className="text-right text-red-600">
          {summary.failedCount > 0 ? summary.failedCount : '-'}
        </TableCell>
      </TableRow>
      {isExpanded && (
        <NotificationDetailRows
          dojangId={dojangId}
          sendDate={summary.sendDate}
          messageType={summary.messageType}
        />
      )}
    </>
  );
};

interface NotificationDetailRowsProps {
  dojangId: string;
  sendDate: string;
  messageType: string;
}

const NotificationDetailRows = ({ dojangId, sendDate, messageType }: NotificationDetailRowsProps) => {
  const [detailPage, setDetailPage] = useState(0);
  const { data, isLoading } = useNotificationDetails(dojangId, sendDate, messageType, detailPage);

  if (isLoading) {
    return (
      <TableRow>
        <TableCell colSpan={6}>
          <div className="flex items-center justify-center py-4">
            <Spinner className="h-4 w-4" />
          </div>
        </TableCell>
      </TableRow>
    );
  }

  if (!data || data.content.length === 0) {
    return (
      <TableRow>
        <TableCell colSpan={6}>
          <div className="text-center text-sm text-muted-foreground py-4">상세 내역이 없습니다</div>
        </TableCell>
      </TableRow>
    );
  }

  return (
    <>
      {/* 상세 헤더 */}
      <TableRow className="bg-muted/30">
        <TableCell />
        <TableCell className="text-xs font-medium text-muted-foreground">보호자명</TableCell>
        <TableCell className="text-xs font-medium text-muted-foreground">제목</TableCell>
        <TableCell className="text-xs font-medium text-muted-foreground text-right">상태</TableCell>
        <TableCell className="text-xs font-medium text-muted-foreground text-right" colSpan={2}>발송 시각</TableCell>
      </TableRow>
      {data.content.map((detail) => {
        const sentTime = detail.sentAt
          ? new Date(detail.sentAt).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })
          : '-';
        return (
          <TableRow key={detail.id} className="bg-muted/10">
            <TableCell />
            <TableCell className="text-sm">{detail.guardianName ?? '-'}</TableCell>
            <TableCell className="text-sm text-muted-foreground">{detail.title}</TableCell>
            <TableCell className="text-right">
              <MessageStatusBadge status={detail.status as MessageStatus} />
            </TableCell>
            <TableCell className="text-right text-sm text-muted-foreground" colSpan={2}>
              {sentTime}
            </TableCell>
          </TableRow>
        );
      })}
      {data.totalPages > 1 && (
        <TableRow className="bg-muted/10">
          <TableCell colSpan={6}>
            <div className="flex items-center justify-center gap-2 py-1">
              <Button
                variant="ghost"
                size="sm"
                className="h-7 text-xs"
                onClick={(e) => { e.stopPropagation(); setDetailPage((p) => Math.max(0, p - 1)); }}
                disabled={detailPage === 0}
              >
                이전
              </Button>
              <span className="text-xs text-muted-foreground">
                {detailPage + 1} / {data.totalPages}
              </span>
              <Button
                variant="ghost"
                size="sm"
                className="h-7 text-xs"
                onClick={(e) => { e.stopPropagation(); setDetailPage((p) => Math.min(data.totalPages - 1, p + 1)); }}
                disabled={detailPage >= data.totalPages - 1}
              >
                다음
              </Button>
            </div>
          </TableCell>
        </TableRow>
      )}
    </>
  );
};
