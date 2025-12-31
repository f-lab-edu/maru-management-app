import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from '@/shared/components/ui/sheet';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Textarea } from '@/shared/components/ui/textarea';
import { Label } from '@/shared/components/ui/label';
import { Checkbox } from '@/shared/components/ui/checkbox';
import { useBulkUpdateInvoices } from '../hooks';
import { useAlert } from '@/hooks';

const bulkUpdateSchema = z.object({
  amount: z.coerce.number().positive('금액은 0보다 커야 합니다').optional(),
  dueDate: z.string().optional(),
  note: z.string().max(500, '비고는 500자 이내여야 합니다').optional(),
});

type BulkUpdateFormData = z.infer<typeof bulkUpdateSchema>;

interface InvoiceBulkUpdateSheetProps {
  isOpen: boolean;
  onClose: () => void;
  selectedIds: string[];
  dojangId: string | null;
  onSuccess: () => void;
}

export function InvoiceBulkUpdateSheet({
  isOpen,
  onClose,
  selectedIds,
  dojangId,
  onSuccess,
}: InvoiceBulkUpdateSheetProps) {
  const { showSuccess, showError } = useAlert();
  const [enabledFields, setEnabledFields] = useState({
    amount: false,
    dueDate: false,
    note: false,
  });

  const { mutateAsync: bulkUpdate, isPending } = useBulkUpdateInvoices(dojangId);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<BulkUpdateFormData>({
    resolver: zodResolver(bulkUpdateSchema),
  });

  const handleClose = () => {
    reset();
    setEnabledFields({ amount: false, dueDate: false, note: false });
    onClose();
  };

  const onSubmit = async (data: BulkUpdateFormData) => {
    const hasEnabledField = Object.values(enabledFields).some(Boolean);
    if (!hasEnabledField) {
      showError('수정할 항목을 선택해주세요');
      return;
    }

    try {
      const request = {
        invoiceIds: selectedIds,
        ...(enabledFields.amount && data.amount ? { amount: data.amount } : {}),
        ...(enabledFields.dueDate && data.dueDate ? { dueDate: data.dueDate } : {}),
        ...(enabledFields.note ? { note: data.note || '' } : {}),
      };

      const result = await bulkUpdate(request);
      showSuccess(`${result.updatedCount}건 수정 완료 (${result.skippedCount}건 건너뜀)`);
      handleClose();
      onSuccess();
    } catch {
      showError('일괄 수정에 실패했습니다');
    }
  };

  const toggleField = (field: keyof typeof enabledFields) => {
    setEnabledFields((prev) => ({ ...prev, [field]: !prev[field] }));
  };

  return (
    <Sheet open={isOpen} onOpenChange={handleClose}>
      <SheetContent className="w-[400px] sm:w-[450px]">
        <SheetHeader>
          <SheetTitle>청구서 일괄 수정</SheetTitle>
          <SheetDescription>
            선택한 {selectedIds.length}건의 청구서를 수정합니다.
            수정할 항목을 체크하고 값을 입력하세요.
          </SheetDescription>
        </SheetHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-6 mt-6">
          <div className="space-y-4">
            <div className="flex items-start gap-3">
              <Checkbox
                id="enable-amount"
                checked={enabledFields.amount}
                onCheckedChange={() => toggleField('amount')}
                className="mt-1"
              />
              <div className="flex-1 space-y-2">
                <Label
                  htmlFor="enable-amount"
                  className={!enabledFields.amount ? 'text-muted-foreground' : ''}
                >
                  청구금액
                </Label>
                <Input
                  type="number"
                  placeholder="금액 입력"
                  disabled={!enabledFields.amount}
                  {...register('amount')}
                />
                {errors.amount && (
                  <p className="text-sm text-destructive">{errors.amount.message}</p>
                )}
              </div>
            </div>

            <div className="flex items-start gap-3">
              <Checkbox
                id="enable-dueDate"
                checked={enabledFields.dueDate}
                onCheckedChange={() => toggleField('dueDate')}
                className="mt-1"
              />
              <div className="flex-1 space-y-2">
                <Label
                  htmlFor="enable-dueDate"
                  className={!enabledFields.dueDate ? 'text-muted-foreground' : ''}
                >
                  납부기한
                </Label>
                <Input
                  type="date"
                  disabled={!enabledFields.dueDate}
                  {...register('dueDate')}
                />
                {errors.dueDate && (
                  <p className="text-sm text-destructive">{errors.dueDate.message}</p>
                )}
              </div>
            </div>

            <div className="flex items-start gap-3">
              <Checkbox
                id="enable-note"
                checked={enabledFields.note}
                onCheckedChange={() => toggleField('note')}
                className="mt-1"
              />
              <div className="flex-1 space-y-2">
                <Label
                  htmlFor="enable-note"
                  className={!enabledFields.note ? 'text-muted-foreground' : ''}
                >
                  비고
                </Label>
                <Textarea
                  placeholder="비고 입력 (500자 이내)"
                  disabled={!enabledFields.note}
                  rows={3}
                  {...register('note')}
                />
                {errors.note && (
                  <p className="text-sm text-destructive">{errors.note.message}</p>
                )}
              </div>
            </div>
          </div>

          <div className="flex justify-end gap-2 pt-4 border-t">
            <Button type="button" variant="outline" onClick={handleClose}>
              취소
            </Button>
            <Button type="submit" disabled={isPending}>
              {isPending ? '수정 중...' : '일괄 수정'}
            </Button>
          </div>
        </form>
      </SheetContent>
    </Sheet>
  );
}
