import { useState } from 'react';
import { useAuthStore } from '@/stores/authStore';
import { usePermissions } from '@/hooks';
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from '@/shared/components/ui/sheet';
import { cn } from '@/shared/utils';
import { useInvoice, useStudentPaymentHistory } from '../hooks';
import { useInvoiceActions } from '../hooks/useInvoiceActions';
import { InvoiceStatusBadge } from './InvoiceStatusBadge';
import { InvoiceCurrentTab } from './InvoiceCurrentTab';
import { InvoiceHistoryTab } from './InvoiceHistoryTab';
import { PrepaidPaymentForm } from './PrepaidPaymentForm';

type TabValue = 'current' | 'history';

interface InvoiceDetailSheetProps {
  invoiceId: string | null;
  isOpen: boolean;
  onClose: () => void;
}

export function InvoiceDetailSheet({ invoiceId, isOpen, onClose }: InvoiceDetailSheetProps) {
  const { selectedDojang } = useAuthStore();
  const dojangId = selectedDojang?.dojangId ?? null;
  const { hasPermission } = usePermissions();
  const canUpdate = hasPermission('PAYMENT_UPDATE');

  const [showPaymentForm, setShowPaymentForm] = useState(false);
  const [showPrepaidSheet, setShowPrepaidSheet] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [activeTab, setActiveTab] = useState<TabValue>('current');

  const { data: invoice, isLoading } = useInvoice(dojangId, invoiceId);
  const { data: paymentHistory, isLoading: isHistoryLoading } = useStudentPaymentHistory(
    dojangId,
    invoice?.studentId ?? null
  );

  const {
    editForm,
    canEdit,
    isIssuing,
    isVoiding,
    isRestoring,
    isDeleting,
    isUpdating,
    handleUpdate,
    handleIssue,
    handleVoid,
    handleRestore,
    handleDelete,
    handleCancelPayment,
    startEditing,
    cancelEditing,
  } = useInvoiceActions({
    dojangId,
    invoiceId,
    invoice,
    onClose,
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

  if (!invoice && !isLoading) {
    return null;
  }

  return (
    <>
      <Sheet open={isOpen} onOpenChange={handleOpenChange} modal={false}>
        <SheetContent
          side="right"
          className="w-[400px] overflow-y-auto sm:w-[540px] sm:max-w-[540px] z-50"
          hideOverlay
          onInteractOutside={handleInteractOutside}
          onPointerDownOutside={handleInteractOutside}
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
                <InvoiceCurrentTab
                  invoice={invoice}
                  invoiceId={invoiceId!}
                  isEditing={isEditing}
                  showPaymentForm={showPaymentForm}
                  setShowPaymentForm={setShowPaymentForm}
                  editForm={editForm}
                  canEdit={!!canEdit}
                  canUpdate={canUpdate}
                  isIssuing={isIssuing}
                  isVoiding={isVoiding}
                  isRestoring={isRestoring}
                  isDeleting={isDeleting}
                  isUpdating={isUpdating}
                  onUpdate={(data) => handleUpdate(data, setIsEditing)}
                  onIssue={handleIssue}
                  onVoid={handleVoid}
                  onRestore={handleRestore}
                  onDelete={handleDelete}
                  onCancelPayment={handleCancelPayment}
                  onStartEditing={() => startEditing(setIsEditing)}
                  onCancelEditing={() => cancelEditing(setIsEditing)}
                  formatAmount={formatAmount}
                />
              ) : (
                <InvoiceHistoryTab
                  paymentHistory={paymentHistory}
                  isLoading={isHistoryLoading}
                  showPrepaidSheet={showPrepaidSheet}
                  setShowPrepaidSheet={setShowPrepaidSheet}
                  formatAmount={formatAmount}
                />
              )}
            </div>
          ) : null}
        </SheetContent>
      </Sheet>

      {invoice && (
        <Sheet
          open={showPrepaidSheet}
          onOpenChange={setShowPrepaidSheet}
          modal={false}
        >
          <SheetContent
            side="right"
            className="w-[400px] overflow-y-auto sm:w-[420px] sm:max-w-[420px] z-40"
            style={{ right: 540 }}
            hideOverlay
            hideCloseButton
            onInteractOutside={(e) => e.preventDefault()}
            onPointerDownOutside={(e) => e.preventDefault()}
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
