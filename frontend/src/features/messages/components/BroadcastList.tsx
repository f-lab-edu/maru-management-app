import { useState } from 'react';
import { MessageSquare, ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight } from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { Skeleton } from '@/shared/components/ui/skeleton';
import { ToggleGroup, ToggleGroupItem } from '@/shared/components/ui/toggle-group';
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

const CHANNEL_FILTER_OPTIONS: { value: string; label: string }[] = [
  { value: '', label: '전체' },
  { value: 'SMS', label: 'SMS' },
  { value: 'KAKAO', label: '카카오톡' },
  { value: 'EXPO_PUSH', label: '푸시 알림' },
];

interface BroadcastListProps {
  onRowClick: (broadcastId: string) => void;
}

export const BroadcastList = ({ onRowClick }: BroadcastListProps) => {
  const [page, setPage] = useState(0);
  const [channelFilter, setChannelFilter] = useState('');
  const { selectedDojang } = useAuthStore();
  const dojangId = selectedDojang?.dojangId ?? '';
  const { data, isLoading } = useBroadcasts(dojangId, page);

  const filtered = channelFilter
    ? data?.content.filter((b) => b.channel === channelFilter) ?? []
    : data?.content ?? [];

  const handleChannelChange = (value: string) => {
    setChannelFilter(value);
  };

  const formatDate = (dateStr: string) =>
    new Date(dateStr).toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' });

  return (
    <div className="space-y-4">
      {/* 채널 필터 */}
      <ToggleGroup
        type="single"
        value={channelFilter}
        onValueChange={handleChannelChange}
        className="justify-start"
      >
        {CHANNEL_FILTER_OPTIONS.map((opt) => (
          <ToggleGroupItem
            key={opt.value}
            value={opt.value}
            className="text-xs px-3 py-1 h-7 data-[state=on]:bg-foreground data-[state=on]:text-background data-[state=on]:font-semibold"
          >
            {opt.label}
          </ToggleGroupItem>
        ))}
      </ToggleGroup>

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
                    <p className="text-sm">발송한 단체 문자가 없습니다</p>
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
