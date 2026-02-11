import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useAlert, useConfirm } from '@/hooks';
import {
  useIssueInvoice,
  useVoidInvoice,
  useRestoreInvoice,
  useDeleteInvoice,
  useUpdateInvoice,
  useCancelPayment,
  useSendPaymentLink,
} from './index';
import type { InvoiceDetailRes } from '../types';

const updateSchema = z.object({
  amount: z.number().positive('금액은 0보다 커야 합니다'),
  dueDate: z.string().min(1, '납부 기한을 선택해주세요'),
  note: z.string().max(500, '비고는 500자 이내여야 합니다').optional(),
});

export type UpdateData = z.infer<typeof updateSchema>;

interface UseInvoiceActionsProps {
  dojangId: string | null;
  invoiceId: string | null;
  invoice: InvoiceDetailRes | undefined;
  onClose: () => void;
}

export function useInvoiceActions({
  dojangId,
  invoiceId,
  invoice,
  onClose,
}: UseInvoiceActionsProps) {
  const { showSuccess, showError } = useAlert();
  const { confirm } = useConfirm();

  const { mutateAsync: issueInvoice, isPending: isIssuing } = useIssueInvoice(dojangId);
  const { mutateAsync: voidInvoice, isPending: isVoiding } = useVoidInvoice(dojangId);
  const { mutateAsync: restoreInvoice, isPending: isRestoring } = useRestoreInvoice(dojangId);
  const { mutateAsync: deleteInvoice, isPending: isDeleting } = useDeleteInvoice(dojangId);
  const { mutateAsync: updateInvoice, isPending: isUpdating } = useUpdateInvoice(dojangId);
  const { mutateAsync: cancelPayment } = useCancelPayment(dojangId);
  const { mutateAsync: sendPaymentLink, isPending: isSendingLink } = useSendPaymentLink(dojangId);

  const editForm = useForm<UpdateData>({
    resolver: zodResolver(updateSchema),
    defaultValues: {
      amount: invoice?.amount ?? 0,
      dueDate: invoice?.dueDate ?? '',
      note: invoice?.note ?? '',
    },
  });

  const handleUpdate = async (data: UpdateData, setIsEditing: (v: boolean) => void) => {
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

  const handleSendPaymentLink = async () => {
    if (!invoiceId) return;

    const { isConfirmed } = await confirm({
      title: '결제 문자 발송',
      text: '보호자에게 결제 링크를 SMS로 발송하시겠습니까?',
      confirmText: '발송',
      type: 'info',
    });

    if (!isConfirmed) return;

    try {
      await sendPaymentLink(invoiceId);
      showSuccess('결제 문자가 발송되었습니다');
    } catch (error) {
      const axiosError = error as { response?: { data?: { code?: string } } };
      const errorCode = axiosError.response?.data?.code;
      if (errorCode === 'SUB_MERCHANT_001') {
        showError('서브몰이 등록되지 않았습니다');
      } else if (errorCode === 'SUB_MERCHANT_303') {
        showError('서브몰이 활성화되지 않았습니다');
      } else {
        showError('결제 문자 발송에 실패했습니다');
      }
    }
  };

  const startEditing = (setIsEditing: (v: boolean) => void) => {
    if (invoice) {
      editForm.reset({
        amount: invoice.amount,
        dueDate: invoice.dueDate,
        note: invoice.note ?? '',
      });
      setIsEditing(true);
    }
  };

  const cancelEditing = (setIsEditing: (v: boolean) => void) => {
    setIsEditing(false);
    editForm.reset();
  };

  const canEdit = invoice && invoice.payments.length === 0 && invoice.status !== 'VOID';

  return {
    editForm,
    canEdit,
    isIssuing,
    isVoiding,
    isRestoring,
    isDeleting,
    isUpdating,
    isSendingLink,
    handleUpdate,
    handleIssue,
    handleVoid,
    handleRestore,
    handleDelete,
    handleCancelPayment,
    handleSendPaymentLink,
    startEditing,
    cancelEditing,
  };
}
