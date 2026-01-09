import { UseFormReturn } from 'react-hook-form';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import { Textarea } from '@/shared/components/ui/textarea';
import { Separator } from '@/shared/components/ui/separator';
import { DatePicker } from '@/shared/components/ui/date-picker';
import { Send, XCircle, RotateCcw, Trash2, CreditCard, Pencil, X, AlertTriangle } from 'lucide-react';
import { PaymentRecordForm } from './PaymentRecordForm';
import { PaymentList } from './PaymentList';
import type { InvoiceDetailRes, InvoiceStatus } from '../types';
import type { UpdateData } from '../hooks/useInvoiceActions';
import { formatBillingYearMonth } from '../utils';

interface InvoiceCurrentTabProps {
  invoice: InvoiceDetailRes;
  invoiceId: string;
  isEditing: boolean;
  showPaymentForm: boolean;
  setShowPaymentForm: (v: boolean) => void;
  editForm: UseFormReturn<UpdateData>;
  canEdit: boolean;
  isIssuing: boolean;
  isVoiding: boolean;
  isRestoring: boolean;
  isDeleting: boolean;
  isUpdating: boolean;
  onUpdate: (data: UpdateData) => void;
  onIssue: () => void;
  onVoid: () => void;
  onRestore: () => void;
  onDelete: () => void;
  onCancelPayment: (paymentId: string) => void;
  onStartEditing: () => void;
  onCancelEditing: () => void;
  formatAmount: (amount: number) => string;
}

export function InvoiceCurrentTab({
  invoice,
  invoiceId,
  isEditing,
  showPaymentForm,
  setShowPaymentForm,
  editForm,
  canEdit,
  isIssuing,
  isVoiding,
  isRestoring,
  isDeleting,
  isUpdating,
  onUpdate,
  onIssue,
  onVoid,
  onRestore,
  onDelete,
  onCancelPayment,
  onStartEditing,
  onCancelEditing,
  formatAmount,
}: InvoiceCurrentTabProps) {
  const isStudentDeleted = invoice.studentDeleted ?? false;

  const renderActionButtons = (status: InvoiceStatus) => {
    if (isEditing) return null;

    const editButton = canEdit && (
      <Button variant="outline" onClick={onStartEditing} disabled={isStudentDeleted} className="flex-1">
        <Pencil className="mr-2 h-4 w-4" />
        수정
      </Button>
    );

    switch (status) {
      case 'DRAFT':
        return (
          <>
            {editButton}
            <Button onClick={onIssue} disabled={isStudentDeleted || isIssuing} className="flex-1">
              <Send className="mr-2 h-4 w-4" />
              {isIssuing ? '처리 중...' : '발행'}
            </Button>
            <Button variant="destructive" onClick={onDelete} disabled={isDeleting}>
              <Trash2 className="mr-2 h-4 w-4" />
              삭제
            </Button>
          </>
        );
      case 'OPEN':
        return (
          <>
            {editButton}
            <Button onClick={() => setShowPaymentForm(true)} disabled={isStudentDeleted} className="flex-1">
              <CreditCard className="mr-2 h-4 w-4" />
              수납 기록
            </Button>
            <Button variant="outline" onClick={onVoid} disabled={isStudentDeleted || isVoiding}>
              <XCircle className="mr-2 h-4 w-4" />
              무효화
            </Button>
          </>
        );
      case 'PARTIAL':
        return (
          <Button onClick={() => setShowPaymentForm(true)} disabled={isStudentDeleted} className="flex-1">
            <CreditCard className="mr-2 h-4 w-4" />
            수납 기록
          </Button>
        );
      case 'VOID':
        return (
          <Button onClick={onRestore} disabled={isRestoring} className="flex-1">
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

  return (
    <div className="space-y-6">
      {isStudentDeleted && (
        <div className="flex items-center gap-2 rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700">
          <AlertTriangle className="h-4 w-4 flex-shrink-0" />
          <span>퇴원한 원생의 청구서입니다. 수정 및 납부가 불가능합니다.</span>
        </div>
      )}

      {isEditing ? (
        <form onSubmit={editForm.handleSubmit(onUpdate)} className="space-y-4">
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
            <DatePicker
              value={editForm.watch('dueDate')}
              onChange={(value) => editForm.setValue('dueDate', value)}
              placeholder="납부 기한을 선택해주세요"
            />
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
              onClick={onCancelEditing}
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
                {formatBillingYearMonth(invoice.billingYearMonth)}
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
              onCancelPayment={onCancelPayment}
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
  );
}
