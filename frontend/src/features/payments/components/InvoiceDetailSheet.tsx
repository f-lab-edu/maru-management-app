import { useState, useMemo } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useAuthStore } from '@/stores/authStore';
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from '@/shared/components/ui/sheet';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import { Textarea } from '@/shared/components/ui/textarea';
import { Separator } from '@/shared/components/ui/separator';
import { Card, CardContent } from '@/shared/components/ui/card';
import { Badge } from '@/shared/components/ui/badge';
import { cn } from '@/shared/utils';
import { Send, XCircle, RotateCcw, Trash2, CreditCard, Pencil, X, Calendar, Wallet, CalendarPlus } from 'lucide-react';
import {
  useInvoice,
  useIssueInvoice,
  useVoidInvoice,
  useRestoreInvoice,
  useDeleteInvoice,
  useUpdateInvoice,
  useCancelPayment,
  useStudentPaymentHistory,
} from '../hooks';
import { InvoiceStatusBadge } from './InvoiceStatusBadge';
import { PaymentRecordForm } from './PaymentRecordForm';
import { PaymentList } from './PaymentList';
import { PrepaidPaymentForm } from './PrepaidPaymentForm';
import { useAlert, useConfirm } from '@/hooks';
import type { InvoiceStatus, PaymentHistoryItem } from '../types';
import { PAYMENT_METHOD_LABEL } from '../types';

const updateSchema = z.object({
  amount: z.number().positive('금액은 0보다 커야 합니다'),
  dueDate: z.string().min(1, '납부 기한을 선택해주세요'),
  note: z.string().max(500, '비고는 500자 이내여야 합니다').optional(),
});

type UpdateData = z.infer<typeof updateSchema>;
type TabValue = 'current' | 'history';

interface InvoiceDetailSheetProps {
  invoiceId: string | null;
  isOpen: boolean;
  onClose: () => void;
}

export function InvoiceDetailSheet({ invoiceId, isOpen, onClose }: InvoiceDetailSheetProps) {
  const { selectedDojang } = useAuthStore();
  const dojangId = selectedDojang?.dojangId ?? null;

  const [showPaymentForm, setShowPaymentForm] = useState(false);
  const [showPrepaidSheet, setShowPrepaidSheet] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [activeTab, setActiveTab] = useState<TabValue>('current');

  const { data: invoice, isLoading } = useInvoice(dojangId, invoiceId);
  const { data: paymentHistory, isLoading: isHistoryLoading } = useStudentPaymentHistory(
    dojangId,
    invoice?.studentId ?? null
  );

  const { showSuccess, showError } = useAlert();
  const { confirm } = useConfirm();

  const { mutateAsync: issueInvoice, isPending: isIssuing } = useIssueInvoice(dojangId);
  const { mutateAsync: voidInvoice, isPending: isVoiding } = useVoidInvoice(dojangId);
  const { mutateAsync: restoreInvoice, isPending: isRestoring } = useRestoreInvoice(dojangId);
  const { mutateAsync: deleteInvoice, isPending: isDeleting } = useDeleteInvoice(dojangId);
  const { mutateAsync: updateInvoice, isPending: isUpdating } = useUpdateInvoice(dojangId);
  const { mutateAsync: cancelPayment } = useCancelPayment(dojangId);

  const editForm = useForm<UpdateData>({
    resolver: zodResolver(updateSchema),
    defaultValues: {
      amount: invoice?.amount ?? 0,
      dueDate: invoice?.dueDate ?? '',
      note: invoice?.note ?? '',
    },
  });

  const handleOpenChange = (open: boolean) => {
    if (!open) {
      const swalContainer = document.querySelector('.swal2-container');
      if (swalContainer) return;
      setIsEditing(false);
      setShowPaymentForm(false);
      setShowPrepaidSheet(false);
      setActiveTab('current');
      onClose();
    }
  };

  const formatAmount = (amount: number) => {
    return new Intl.NumberFormat('ko-KR').format(amount);
  };

  const startEditing = () => {
    if (invoice) {
      editForm.reset({
        amount: invoice.amount,
        dueDate: invoice.dueDate,
        note: invoice.note ?? '',
      });
      setIsEditing(true);
    }
  };

  const cancelEditing = () => {
    setIsEditing(false);
    editForm.reset();
  };

  const handleUpdate = async (data: UpdateData) => {
    if (!invoiceId) return;

    try {
      await updateInvoice({ id: invoiceId, request: data });
      showSuccess('청구서가 수정되었습니다');
      setIsEditing(false);
    } catch {
      showError('수정에 실패했습니다');
    }
  };

  const handleIssue = async () => {
    if (!invoiceId) return;

    const { isConfirmed } = await confirm({
      title: '청구서 발행',
      text: '청구서를 발행하시겠습니까? 발행 후에는 금액을 수정할 수 없습니다.',
      confirmText: '발행',
      type: 'info',
    });

    if (!isConfirmed) return;

    try {
      await issueInvoice(invoiceId);
      showSuccess('청구서가 발행되었습니다');
    } catch {
      showError('발행 처리에 실패했습니다');
    }
  };

  const handleVoid = async () => {
    if (!invoiceId) return;

    const { isConfirmed } = await confirm({
      title: '청구서 무효화',
      text: '청구서를 무효화하시겠습니까?',
      confirmText: '무효화',
      type: 'warning',
    });

    if (!isConfirmed) return;

    try {
      await voidInvoice(invoiceId);
      showSuccess('청구서가 무효화되었습니다');
    } catch {
      showError('무효화 처리에 실패했습니다');
    }
  };

  const handleRestore = async () => {
    if (!invoiceId) return;

    const { isConfirmed } = await confirm({
      title: '청구서 복구',
      text: '청구서를 임시저장 상태로 복구하시겠습니까?',
      confirmText: '복구',
      type: 'info',
    });

    if (!isConfirmed) return;

    try {
      await restoreInvoice(invoiceId);
      showSuccess('청구서가 복구되었습니다');
    } catch {
      showError('복구 처리에 실패했습니다');
    }
  };

  const handleDelete = async () => {
    if (!invoiceId) return;

    const { isConfirmed } = await confirm({
      title: '청구서 삭제',
      text: '청구서를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.',
      confirmText: '삭제',
      type: 'danger',
    });

    if (!isConfirmed) return;

    try {
      await deleteInvoice(invoiceId);
      showSuccess('청구서가 삭제되었습니다');
      onClose();
    } catch {
      showError('삭제 처리에 실패했습니다');
    }
  };

  const handleCancelPayment = async (paymentId: string) => {
    if (!invoiceId) return;

    const { isConfirmed } = await confirm({
      title: '수납 취소',
      text: '수납을 취소(환불)하시겠습니까?',
      confirmText: '확인',
      type: 'warning',
    });

    if (!isConfirmed) return;

    try {
      await cancelPayment({ invoiceId, paymentId });
      showSuccess('수납이 취소되었습니다');
    } catch {
      showError('수납 취소에 실패했습니다');
    }
  };

  const canEdit = invoice && invoice.payments.length === 0 && invoice.status !== 'VOID';

  const yearlyStats = useMemo(() => {
    const payments = paymentHistory?.payments ?? [];
    const grouped: Record<number, { total: number; count: number }> = {};

    payments.forEach((p: PaymentHistoryItem) => {
      if (p.status === 'REFUNDED') return;
      if (!grouped[p.billingYear]) {
        grouped[p.billingYear] = { total: 0, count: 0 };
      }
      grouped[p.billingYear].total += p.amount;
      grouped[p.billingYear].count += 1;
    });

    return Object.entries(grouped)
      .map(([year, data]) => ({ year: Number(year), ...data }))
      .sort((a, b) => b.year - a.year);
  }, [paymentHistory?.payments]);

  const paymentsByMonth = useMemo(() => {
    const payments = paymentHistory?.payments ?? [];
    const grouped: Record<string, PaymentHistoryItem[]> = {};

    payments.forEach((p: PaymentHistoryItem) => {
      const key = `${p.billingYear}-${String(p.billingMonth).padStart(2, '0')}`;
      if (!grouped[key]) {
        grouped[key] = [];
      }
      grouped[key].push(p);
    });

    return Object.entries(grouped)
      .sort(([a], [b]) => b.localeCompare(a))
      .map(([key, items]) => {
        const [year, month] = key.split('-').map(Number);
        const total = items
          .filter((p) => p.status !== 'REFUNDED')
          .reduce((sum, p) => sum + p.amount, 0);
        const refundTotal = items
          .filter((p) => p.status === 'REFUNDED')
          .reduce((sum, p) => sum + p.amount, 0);
        return {
          year,
          month,
          payments: items.sort((a, b) => {
            const dateA = a.refundedAt ? new Date(a.refundedAt) : new Date(a.paidAt);
            const dateB = b.refundedAt ? new Date(b.refundedAt) : new Date(b.paidAt);
            return dateB.getTime() - dateA.getTime();
          }),
          total,
          refundTotal,
        };
      });
  }, [paymentHistory?.payments]);

  const totalPaidAmount = paymentHistory?.totalPaidAmount ?? 0;

  const renderActionButtons = (status: InvoiceStatus) => {
    if (isEditing) return null;

    const editButton = canEdit && (
      <Button variant="outline" onClick={startEditing} className="flex-1">
        <Pencil className="mr-2 h-4 w-4" />
        수정
      </Button>
    );

    switch (status) {
      case 'DRAFT':
        return (
          <>
            {editButton}
            <Button onClick={handleIssue} disabled={isIssuing} className="flex-1">
              <Send className="mr-2 h-4 w-4" />
              {isIssuing ? '처리 중...' : '발행'}
            </Button>
            <Button variant="destructive" onClick={handleDelete} disabled={isDeleting}>
              <Trash2 className="mr-2 h-4 w-4" />
              삭제
            </Button>
          </>
        );
      case 'OPEN':
        return (
          <>
            {editButton}
            <Button onClick={() => setShowPaymentForm(true)} className="flex-1">
              <CreditCard className="mr-2 h-4 w-4" />
              수납 기록
            </Button>
            <Button variant="outline" onClick={handleVoid} disabled={isVoiding}>
              <XCircle className="mr-2 h-4 w-4" />
              무효화
            </Button>
          </>
        );
      case 'PARTIAL':
        return (
          <Button onClick={() => setShowPaymentForm(true)} className="flex-1">
            <CreditCard className="mr-2 h-4 w-4" />
            수납 기록
          </Button>
        );
      case 'VOID':
        return (
          <Button onClick={handleRestore} disabled={isRestoring} className="flex-1">
            <RotateCcw className="mr-2 h-4 w-4" />
            {isRestoring ? '처리 중...' : '복구'}
          </Button>
        );
      case 'PAID':
        return null;
      default:
        return null;
    }
  };

  if (!invoice && !isLoading) {
    return null;
  }

  return (
    <>
      {/* 메인 청구서 상세 Sheet */}
      <Sheet open={isOpen} onOpenChange={handleOpenChange} modal={false}>
        <SheetContent
          side="right"
          className="w-[400px] overflow-y-auto sm:w-[540px] sm:max-w-[540px] z-50"
          hideOverlay
          onInteractOutside={(e) => {
            const target = e.target as HTMLElement;
            // 테이블, 버튼, 다른 Sheet 등은 닫지 않음
            if (target.closest('tr') || target.closest('button') || target.closest('[role="dialog"]')) {
              e.preventDefault();
            }
          }}
          onPointerDownOutside={(e) => {
            const target = e.target as HTMLElement;
            if (target.closest('tr') || target.closest('button') || target.closest('[role="dialog"]')) {
              e.preventDefault();
            }
          }}
        >
          <SheetHeader>
            <SheetTitle>청구서 상세</SheetTitle>
          </SheetHeader>

          {isLoading ? (
            <div className="flex items-center justify-center py-8">
              <span className="text-muted-foreground">로딩 중...</span>
            </div>
          ) : invoice ? (
            <div className="mt-6 space-y-4">
              <div className="flex items-center justify-between">
                <span className="text-lg font-semibold">{invoice.studentName}</span>
                <InvoiceStatusBadge status={invoice.status} />
              </div>

              <div className="inline-flex rounded-lg bg-muted p-1 w-full">
                <button
                  onClick={() => {
                    setActiveTab('current');
                    setShowPrepaidSheet(false);
                  }}
                  className={cn(
                    'flex-1 rounded-md px-4 py-2 text-sm font-medium transition-all',
                    activeTab === 'current'
                      ? 'bg-white text-foreground shadow-sm'
                      : 'text-muted-foreground hover:text-foreground'
                  )}
                >
                  이번달
                </button>
                <button
                  onClick={() => {
                    setActiveTab('history');
                    setShowPrepaidSheet(false);
                  }}
                  className={cn(
                    'flex-1 rounded-md px-4 py-2 text-sm font-medium transition-all',
                    activeTab === 'history'
                      ? 'bg-white text-foreground shadow-sm'
                      : 'text-muted-foreground hover:text-foreground'
                  )}
                >
                  전체
                </button>
              </div>

              {activeTab === 'current' ? (
                <div className="space-y-6">
                  {isEditing ? (
                    <form onSubmit={editForm.handleSubmit(handleUpdate)} className="space-y-4">
                      <div className="space-y-2">
                        <Label htmlFor="amount">청구 금액 *</Label>
                        <Input
                          type="number"
                          {...editForm.register('amount', { valueAsNumber: true })}
                        />
                        {editForm.formState.errors.amount && (
                          <p className="text-sm text-destructive">
                            {editForm.formState.errors.amount.message}
                          </p>
                        )}
                      </div>

                      <div className="space-y-2">
                        <Label htmlFor="dueDate">납부 기한 *</Label>
                        <Input type="date" {...editForm.register('dueDate')} />
                        {editForm.formState.errors.dueDate && (
                          <p className="text-sm text-destructive">
                            {editForm.formState.errors.dueDate.message}
                          </p>
                        )}
                      </div>

                      <div className="space-y-2">
                        <Label htmlFor="note">비고</Label>
                        <Textarea
                          {...editForm.register('note')}
                          placeholder="청구서에 대한 메모를 입력하세요"
                        />
                      </div>

                      <div className="flex gap-2 pt-2">
                        <Button
                          type="button"
                          variant="outline"
                          onClick={cancelEditing}
                          className="flex-1"
                        >
                          <X className="mr-2 h-4 w-4" />
                          취소
                        </Button>
                        <Button type="submit" disabled={isUpdating} className="flex-1">
                          {isUpdating ? '저장 중...' : '저장'}
                        </Button>
                      </div>
                    </form>
                  ) : (
                    <>
                      <div className="grid grid-cols-2 gap-3 text-sm">
                        <div>
                          <span className="text-muted-foreground">청구 금액</span>
                          <p className="font-medium">₩{formatAmount(invoice.amount)}</p>
                        </div>
                        <div>
                          <span className="text-muted-foreground">납부 금액</span>
                          <p className="font-medium text-green-600">
                            ₩{formatAmount(invoice.paidAmount)}
                          </p>
                        </div>
                        <div>
                          <span className="text-muted-foreground">잔여 금액</span>
                          <p className="font-medium text-red-600">
                            ₩{formatAmount(invoice.remainingAmount)}
                          </p>
                        </div>
                        <div>
                          <span className="text-muted-foreground">납부 기한</span>
                          <p className="font-medium">{invoice.dueDate}</p>
                        </div>
                        <div>
                          <span className="text-muted-foreground">청구 연월</span>
                          <p className="font-medium">
                            {invoice.billingYear}년 {invoice.billingMonth}월
                          </p>
                        </div>
                        {invoice.issueDate && (
                          <div>
                            <span className="text-muted-foreground">발행일</span>
                            <p className="font-medium">{invoice.issueDate}</p>
                          </div>
                        )}
                      </div>

                      {invoice.note && (
                        <div>
                          <span className="text-sm text-muted-foreground">비고</span>
                          <p className="text-sm mt-1">{invoice.note}</p>
                        </div>
                      )}

                      <Separator />

                      <div>
                        <h4 className="font-medium mb-3">수납 내역</h4>
                        <PaymentList
                          payments={invoice.payments}
                          onCancelPayment={handleCancelPayment}
                          canCancel={invoice.status !== 'VOID'}
                        />
                      </div>

                      <Separator />

                      <div className="flex gap-2">{renderActionButtons(invoice.status)}</div>

                      {showPaymentForm && invoiceId && (
                        <PaymentRecordForm
                          invoiceId={invoiceId}
                          remainingAmount={invoice.remainingAmount}
                          onClose={() => setShowPaymentForm(false)}
                          onSuccess={() => setShowPaymentForm(false)}
                        />
                      )}
                    </>
                  )}
                </div>
              ) : (
                <div className="space-y-4">
                  {isHistoryLoading ? (
                    <div className="flex items-center justify-center py-8">
                      <span className="text-muted-foreground">로딩 중...</span>
                    </div>
                  ) : (
                    <>
                      <div className="flex gap-3">
                        <Card className="flex-1 border-0 bg-gradient-to-r from-emerald-50 to-teal-50">
                          <CardContent className="p-4">
                            <div className="flex items-center gap-3">
                              <div className="p-2 rounded-full bg-emerald-100">
                                <Wallet className="h-5 w-5 text-emerald-600" />
                              </div>
                              <div>
                                <p className="text-sm text-muted-foreground">총 납부 금액</p>
                                <p className="text-xl font-bold text-emerald-700">
                                  ₩{formatAmount(totalPaidAmount)}
                                </p>
                              </div>
                            </div>
                          </CardContent>
                        </Card>
                        <Button
                          variant="outline"
                          className={cn(
                            "h-auto flex-col gap-1 px-4 py-3 border-primary/30 hover:bg-primary/5 transition-colors",
                            showPrepaidSheet && "bg-primary/10 border-primary"
                          )}
                          onClick={(e) => {
                            e.stopPropagation();
                            setShowPrepaidSheet(!showPrepaidSheet);
                          }}
                        >
                          <CalendarPlus className={cn(
                            "h-5 w-5 text-primary",
                            showPrepaidSheet && "text-primary"
                          )} />
                          <span className="text-xs font-medium">선납 등록</span>
                        </Button>
                      </div>

                      {yearlyStats.length > 0 && (
                        <div className="space-y-2">
                          <h4 className="font-medium text-sm text-muted-foreground">연도별 납부</h4>
                          <div className="grid gap-2">
                            {yearlyStats.map((stat) => (
                              <div
                                key={stat.year}
                                className="flex items-center justify-between rounded-lg border p-3"
                              >
                                <div className="flex items-center gap-2">
                                  <Calendar className="h-4 w-4 text-muted-foreground" />
                                  <span className="font-medium">{stat.year}년</span>
                                  <Badge variant="secondary" className="text-xs">
                                    {stat.count}건
                                  </Badge>
                                </div>
                                <span className="font-semibold text-emerald-600">
                                  ₩{formatAmount(stat.total)}
                                </span>
                              </div>
                            ))}
                          </div>
                        </div>
                      )}

                      <Separator />

                      <div className="space-y-4">
                        <h4 className="font-medium text-sm text-muted-foreground">
                          월별 납부 내역
                        </h4>
                        {paymentsByMonth.length === 0 ? (
                          <p className="text-sm text-muted-foreground py-4 text-center">
                            납부 내역이 없습니다
                          </p>
                        ) : (
                          <div className="space-y-4">
                            {paymentsByMonth.map((monthData) => (
                              <div key={`${monthData.year}-${monthData.month}`} className="space-y-2">
                                <div className="flex items-center justify-between">
                                  <span className="font-semibold text-sm">
                                    {monthData.year}년 {monthData.month}월
                                  </span>
                                  <span className="text-emerald-600 font-medium text-sm">
                                    ₩{formatAmount(monthData.total)}
                                  </span>
                                </div>
                                <div className="space-y-1 pl-2 border-l-2 border-muted">
                                  {monthData.payments.map((payment: PaymentHistoryItem) => {
                                    const isRefunded = payment.status === 'REFUNDED';
                                    return (
                                      <div
                                        key={payment.paymentId}
                                        className={cn(
                                          'flex items-center justify-between rounded-md p-2 text-sm',
                                          isRefunded ? 'bg-red-50' : 'bg-muted/30'
                                        )}
                                      >
                                        <div className="flex items-center gap-2">
                                          {isRefunded ? (
                                            <Badge variant="destructive" className="text-xs">
                                              환불
                                            </Badge>
                                          ) : (
                                            <Badge variant="outline" className="text-xs">
                                              {PAYMENT_METHOD_LABEL[payment.method as keyof typeof PAYMENT_METHOD_LABEL] ?? payment.method}
                                            </Badge>
                                          )}
                                          <span className="text-xs text-muted-foreground">
                                            {isRefunded ? payment.refundedAt : payment.paidAt}
                                          </span>
                                        </div>
                                        <span className={cn(
                                          'font-medium',
                                          isRefunded ? 'text-red-600' : 'text-emerald-600'
                                        )}>
                                          ₩{formatAmount(payment.amount)}
                                        </span>
                                      </div>
                                    );
                                  })}
                                </div>
                              </div>
                            ))}
                          </div>
                        )}
                      </div>
                    </>
                  )}
                </div>
              )}
            </div>
          ) : null}
        </SheetContent>
      </Sheet>

      {/* 선납 수납 Sheet - 메인 Sheet 뒤에 붙음 */}
      {invoice && (
        <Sheet open={showPrepaidSheet} onOpenChange={setShowPrepaidSheet} modal={false}>
          <SheetContent
            side="right"
            className="w-[400px] overflow-y-auto sm:w-[420px] sm:max-w-[420px] z-40"
            style={{ right: 540 }}
            hideOverlay
            hideCloseButton
            onInteractOutside={(e) => e.preventDefault()}
          >
            <SheetHeader>
              <SheetTitle>선납 수납 등록</SheetTitle>
            </SheetHeader>
            <div className="mt-6">
              <PrepaidPaymentForm
                studentId={invoice.studentId}
                studentName={invoice.studentName}
                onSuccess={() => setShowPrepaidSheet(false)}
              />
            </div>
          </SheetContent>
        </Sheet>
      )}
    </>
  );
}
