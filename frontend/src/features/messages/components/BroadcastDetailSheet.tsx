import { useState } from 'react';
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from '@/shared/components/ui/sheet';
import {
  Table,
  TableHeader,
  TableBody,
  TableRow,
  TableHead,
  TableCell,
} from '@/shared/components/ui/table';
import { ToggleGroup, ToggleGroupItem } from '@/shared/components/ui/toggle-group';
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationPrevious,
  PaginationNext,
} from '@/shared/components/ui/pagination';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/shared/components/ui/alert-dialog';
import { Button } from '@/shared/components/ui/button';
import { useAuthStore } from '@/stores/authStore';
import { useBroadcastDetail, useBroadcastRecipients, useResendFailed } from '../hooks/useBroadcasts';
import { BroadcastStatusBadge } from './BroadcastStatusBadge';
import { MessageStatusBadge } from './MessageStatusBadge';
import { CHANNEL_LABEL, RECIPIENT_TYPE_LABEL } from '@/types/message';
import type { BroadcastDetail, RecipientStatus } from '@/types/message';

const STATUS_FILTERS = [
  { value: '', label: '전체' },
  { value: 'ACCEPTED', label: '성공' },
  { value: 'DEAD', label: '실패' },
  { value: 'PENDING', label: '대기' },
] as const;

interface BroadcastDetailSheetProps {
  broadcastId: string | null;
  isOpen: boolean;
  onClose: () => void;
}

export const BroadcastDetailSheet = ({ broadcastId, isOpen, onClose }: BroadcastDetailSheetProps) => {
  const { selectedDojang } = useAuthStore();
  const dojangId = selectedDojang?.dojangId ?? '';
  const [statusFilter, setStatusFilter] = useState('');
  const [recipientsPage, setRecipientsPage] = useState(0);

  const [resendConfirmOpen, setResendConfirmOpen] = useState(false);

  const { data: detail, isLoading: detailLoading } = useBroadcastDetail(dojangId, broadcastId ?? '');
  const { data: recipientsData, isLoading: recipientsLoading } = useBroadcastRecipients(
    dojangId,
    broadcastId ?? '',
    recipientsPage,
    statusFilter,
  );
  const resendMutation = useResendFailed(dojangId);

  const handleResend = () => {
    if (!broadcastId) return;
    resendMutation.mutate(broadcastId, {
      onSuccess: (data) => {
        setResendConfirmOpen(false);
        alert(`${data.resendCount}건의 메시지를 재발송합니다.`);
      },
      onError: () => {
        setResendConfirmOpen(false);
      },
    });
  };

  const handleOpenChange = (open: boolean) => {
    if (!open) {
      const swalContainer = document.querySelector('.swal2-container');
      if (swalContainer) return;
      setStatusFilter('');
      setRecipientsPage(0);
      onClose();
    }
  };

  const handleInteractOutside = (e: Event) => {
    const target = e.target as HTMLElement;
    if (
      target.closest('tr') ||
      target.closest('button') ||
      target.closest('[role="dialog"]') ||
      target.closest('[data-radix-select-content]') ||
      target.closest('[data-radix-popper-content-wrapper]')
    ) {
      e.preventDefault();
    }
  };

  return (
    <>
    <Sheet open={isOpen} onOpenChange={handleOpenChange} modal={false}>
      <SheetContent
        side="right"
        className="w-[400px] overflow-y-auto sm:w-[580px] sm:max-w-[580px] z-50"
        hideOverlay
        onInteractOutside={handleInteractOutside}
        onPointerDownOutside={handleInteractOutside}
        aria-describedby={undefined}
      >
        <SheetHeader>
          <SheetTitle>발송 상세</SheetTitle>
        </SheetHeader>

        {detailLoading ? (
          <div className="flex items-center justify-center py-8">
            <span className="text-muted-foreground">로딩 중...</span>
          </div>
        ) : detail ? (
          <div className="mt-6 space-y-6">
            <BroadcastInfoSection detail={detail} />

            {detail.failedCount > 0 && (
              <Button
                variant="outline"
                size="sm"
                className="w-full text-red-600 border-red-200 hover:bg-red-50"
                onClick={() => setResendConfirmOpen(true)}
                disabled={resendMutation.isPending}
              >
                {resendMutation.isPending ? '재발송 중...' : `실패 메시지 재발송 (${detail.failedCount}건)`}
              </Button>
            )}

            {/* 본문 */}
            <div className="space-y-2">
              <h3 className="text-sm font-medium text-muted-foreground">본문</h3>
              <div className="p-4 border rounded-lg bg-muted/30 whitespace-pre-wrap text-sm">
                {detail.body}
              </div>
            </div>

            {/* 수신자 목록 */}
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <h3 className="text-sm font-medium text-muted-foreground">수신자 목록</h3>
                <ToggleGroup
                type="single"
                value={statusFilter}
                onValueChange={(value) => {
                  setStatusFilter(value);
                  setRecipientsPage(0);
                }}
              >
                {STATUS_FILTERS.map((filter) => (
                  <ToggleGroupItem key={filter.value} value={filter.value} className="text-xs">
                    {filter.label}
                  </ToggleGroupItem>
                ))}
              </ToggleGroup>
              </div>

              {recipientsLoading ? (
                <div className="flex items-center justify-center py-8">
                  <span className="text-muted-foreground text-sm">로딩 중...</span>
                </div>
              ) : !recipientsData || recipientsData.content.length === 0 ? (
                <div className="py-8 text-center text-muted-foreground text-sm">수신자가 없습니다</div>
              ) : (
                <>
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>보호자명</TableHead>
                        <TableHead>원생</TableHead>
                        <TableHead>연락처</TableHead>
                        <TableHead>상태</TableHead>
                        <TableHead>발송 시각</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {recipientsData.content.map((recipient, idx) => (
                        <RecipientRow key={`${recipient.guardianId}-${idx}`} recipient={recipient} />
                      ))}
                    </TableBody>
                  </Table>

                  {recipientsData.totalPages > 1 && (
                    <Pagination className="pt-2">
                      <PaginationContent>
                        <PaginationItem>
                          <PaginationPrevious
                            onClick={(e) => { e.preventDefault(); setRecipientsPage((p) => Math.max(0, p - 1)); }}
                            aria-disabled={recipientsPage === 0}
                            className={recipientsPage === 0 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
                          />
                        </PaginationItem>
                        <PaginationItem>
                          <span className="text-sm text-muted-foreground px-2">
                            {recipientsPage + 1} / {recipientsData.totalPages}
                          </span>
                        </PaginationItem>
                        <PaginationItem>
                          <PaginationNext
                            onClick={(e) => { e.preventDefault(); setRecipientsPage((p) => Math.min(recipientsData.totalPages - 1, p + 1)); }}
                            aria-disabled={recipientsPage >= recipientsData.totalPages - 1}
                            className={recipientsPage >= recipientsData.totalPages - 1 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
                          />
                        </PaginationItem>
                      </PaginationContent>
                    </Pagination>
                  )}
                </>
              )}
            </div>
          </div>
        ) : null}
      </SheetContent>
    </Sheet>

    <AlertDialog open={resendConfirmOpen} onOpenChange={setResendConfirmOpen}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>실패 메시지 재발송</AlertDialogTitle>
          <AlertDialogDescription>
            {detail?.failedCount ?? 0}건의 실패 메시지를 재발송합니다. 계속하시겠습니까?
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>취소</AlertDialogCancel>
          <AlertDialogAction onClick={handleResend} disabled={resendMutation.isPending}>
            재발송
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
    </>
  );
};

const BroadcastInfoSection = ({ detail }: { detail: BroadcastDetail }) => {
  const createdDate = new Date(detail.createdAt).toLocaleString('ko-KR', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });

  return (
    <div className="grid grid-cols-2 gap-3">
      <div className="space-y-1">
        <p className="text-xs text-muted-foreground">상태</p>
        <BroadcastStatusBadge status={detail.status} />
      </div>
      <div className="space-y-1">
        <p className="text-xs text-muted-foreground">채널</p>
        <p className="text-sm font-medium">{CHANNEL_LABEL[detail.channel]}</p>
      </div>
      <div className="space-y-1">
        <p className="text-xs text-muted-foreground">수신 대상</p>
        <p className="text-sm font-medium">{RECIPIENT_TYPE_LABEL[detail.recipientType]}</p>
      </div>
      <div className="space-y-1">
        <p className="text-xs text-muted-foreground">발송자</p>
        <p className="text-sm font-medium">{detail.sentByUserName ?? '-'}</p>
      </div>
      <div className="space-y-1">
        <p className="text-xs text-muted-foreground">발송일</p>
        <p className="text-sm font-medium">{createdDate}</p>
      </div>
      <div className="space-y-1">
        <p className="text-xs text-muted-foreground">전체</p>
        <p className="text-sm font-medium">{detail.totalCount}건</p>
      </div>
      <div className="space-y-1">
        <p className="text-xs text-muted-foreground">성공</p>
        <p className="text-sm font-medium text-green-600">{detail.acceptedCount}건</p>
      </div>
      <div className="space-y-1">
        <p className="text-xs text-muted-foreground">실패</p>
        <p className="text-sm font-medium text-red-600">{detail.failedCount}건</p>
      </div>
    </div>
  );
};

const RecipientRow = ({ recipient }: { recipient: RecipientStatus }) => {
  const sentTime = recipient.sentAt
    ? new Date(recipient.sentAt).toLocaleTimeString('ko-KR', {
        hour: '2-digit',
        minute: '2-digit',
      })
    : '-';

  return (
    <TableRow>
      <TableCell>{recipient.guardianName ?? '-'}</TableCell>
      <TableCell className="text-muted-foreground">{recipient.studentName ?? '-'}</TableCell>
      <TableCell className="text-muted-foreground">{recipient.phone ?? '-'}</TableCell>
      <TableCell>
        <MessageStatusBadge status={recipient.messageStatus} />
      </TableCell>
      <TableCell className="text-muted-foreground">{sentTime}</TableCell>
    </TableRow>
  );
};
