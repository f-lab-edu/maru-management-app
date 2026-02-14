import { useState } from 'react';
import { MessageSquare, Send, CheckCircle, XCircle, Clock, ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Skeleton } from '@/shared/components/ui/skeleton';
import {
  Table,
  TableHeader,
  TableBody,
  TableRow,
  TableHead,
  TableCell,
} from '@/shared/components/ui/table';
import { useAuthStore } from '@/stores/authStore';
import { useBroadcasts } from '../hooks/useBroadcasts';
import { BroadcastStatusBadge } from './BroadcastStatusBadge';
import { CHANNEL_LABEL, RECIPIENT_TYPE_LABEL } from '@/types/message';

interface BroadcastListProps {
  onRowClick: (broadcastId: string) => void;
}

export const BroadcastList = ({ onRowClick }: BroadcastListProps) => {
  const [page, setPage] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');
  const { selectedDojang } = useAuthStore();
  const dojangId = selectedDojang?.dojangId ?? '';
  const { data, isLoading } = useBroadcasts(dojangId, page);

  const filtered = data?.content.filter(
    (b) => b.title.toLowerCase().includes(searchQuery.toLowerCase())
  ) ?? [];

  const stats = data ? {
    totalBroadcasts: data.content.length,
    accepted: data.content.reduce((sum, b) => sum + b.acceptedCount, 0),
    failed: data.content.reduce((sum, b) => sum + b.failedCount, 0),
    pending: data.content.reduce((sum, b) => sum + b.pendingCount, 0),
  } : null;

  const formatDate = (dateStr: string) =>
    new Date(dateStr).toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' });

  return (
    <div className="space-y-4">
      {/* 통계 카드 */}
      {stats && (
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          <Card className="border shadow-sm">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-1 px-4 pt-3">
              <CardTitle className="text-xs font-medium text-muted-foreground">전체 발송</CardTitle>
              <Send className="h-3.5 w-3.5 text-muted-foreground" />
            </CardHeader>
            <CardContent className="px-4 pb-3 pt-0">
              <div className="text-2xl font-bold">{stats.totalBroadcasts}</div>
              <p className="text-[10px] text-muted-foreground mt-0.5">이번 페이지 기준</p>
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
          <Card className="border shadow-sm">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-1 px-4 pt-3">
              <CardTitle className="text-xs font-medium text-muted-foreground">대기</CardTitle>
              <Clock className="h-3.5 w-3.5 text-muted-foreground" />
            </CardHeader>
            <CardContent className="px-4 pb-3 pt-0">
              <div className="text-2xl font-bold">{stats.pending}</div>
              <p className="text-[10px] text-muted-foreground mt-0.5">건</p>
            </CardContent>
          </Card>
        </div>
      )}

      {/* 검색 */}
      <Input
        placeholder="제목으로 검색..."
        value={searchQuery}
        onChange={(e) => setSearchQuery(e.target.value)}
        className="max-w-sm"
      />

      {/* 테이블 */}
      <div className="rounded-xl border bg-white shadow-sm">
        <Table>
          <TableHeader className="bg-muted">
            <TableRow>
              <TableHead>제목</TableHead>
              <TableHead className="w-[80px]">채널</TableHead>
              <TableHead className="w-[80px]">대상</TableHead>
              <TableHead className="w-[80px]">상태</TableHead>
              <TableHead className="w-[60px] text-right">성공</TableHead>
              <TableHead className="w-[60px] text-right">실패</TableHead>
              <TableHead className="w-[100px]">발송일</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              <TableSkeleton />
            ) : filtered.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} className="h-32 text-center">
                  <div className="flex flex-col items-center text-muted-foreground">
                    <MessageSquare className="h-8 w-8 mb-2 opacity-50" />
                    <p className="text-sm">
                      {searchQuery ? '검색 결과가 없습니다' : '발송한 단체 문자가 없습니다'}
                    </p>
                  </div>
                </TableCell>
              </TableRow>
            ) : (
              filtered.map((broadcast) => (
                <TableRow
                  key={broadcast.id}
                  className="cursor-pointer hover:bg-muted/50"
                  onClick={() => onRowClick(broadcast.id)}
                >
                  <TableCell className="font-medium">{broadcast.title}</TableCell>
                  <TableCell className="text-muted-foreground">
                    {CHANNEL_LABEL[broadcast.channel]}
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {RECIPIENT_TYPE_LABEL[broadcast.recipientType]}
                  </TableCell>
                  <TableCell>
                    <BroadcastStatusBadge status={broadcast.status} />
                  </TableCell>
                  <TableCell className="text-right text-green-600">
                    {broadcast.acceptedCount}
                  </TableCell>
                  <TableCell className="text-right text-red-600">
                    {broadcast.failedCount}
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {formatDate(broadcast.createdAt)}
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>

        {/* 페이징 */}
        {data && data.totalPages > 1 && (
          <div className="flex items-center justify-center gap-2 border-t px-4 py-3">
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
    </div>
  );
};

const TableSkeleton = () => (
  <>
    {Array.from({ length: 5 }).map((_, i) => (
      <TableRow key={i}>
        {Array.from({ length: 7 }).map((_, j) => (
          <TableCell key={j}>
            <Skeleton className="h-4 w-full" />
          </TableCell>
        ))}
      </TableRow>
    ))}
  </>
);
