import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useAuthStore } from '@/stores/authStore';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';
import { X } from 'lucide-react';
import { useRecordPayment } from '../hooks';
import type { PaymentMethod } from '../types';
import { useAlert } from '@/hooks';

const PAYMENT_METHODS: { value: PaymentMethod; label: string }[] = [
  { value: 'CASH', label: '현금' },
  { value: 'CARD', label: '카드' },
  { value: 'TRANSFER', label: '계좌이체' },
  { value: 'PG', label: 'PG' },
  { value: 'OTHER', label: '기타' },
];

const paymentSchema = z.object({
  amount: z.number().positive('금액은 0보다 커야 합니다'),
  method: z.enum(['CASH', 'CARD', 'TRANSFER', 'PG', 'OTHER']),
});

type PaymentFormData = z.infer<typeof paymentSchema>;

interface PaymentRecordFormProps {
  invoiceId: string;
  remainingAmount: number;
  onClose: () => void;
  onSuccess: () => void;
}

export function PaymentRecordForm({
  invoiceId,
  remainingAmount,
  onClose,
  onSuccess,
}: PaymentRecordFormProps) {
  const { selectedDojang } = useAuthStore();
  const dojangId = selectedDojang?.dojangId ?? null;

  const { showSuccess, showError } = useAlert();
  const { mutateAsync: recordPayment, isPending } = useRecordPayment(dojangId);

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = useForm<PaymentFormData>({
    resolver: zodResolver(paymentSchema),
    defaultValues: {
      amount: remainingAmount,
      method: 'CASH',
    },
  });

  const currentAmount = watch('amount');

  const handleFullPayment = () => {
    setValue('amount', remainingAmount);
  };

  const onSubmit = async (data: PaymentFormData) => {
    if (data.amount > remainingAmount) {
      showError('수납 금액이 잔여 금액을 초과할 수 없습니다');
      return;
    }

    try {
      await recordPayment({
        invoiceId,
        request: data,
      });
      showSuccess('수납이 기록되었습니다');
      onSuccess();
    } catch {
      showError('수납 기록에 실패했습니다');
    }
  };

  const formatAmount = (amount: number) => {
    return new Intl.NumberFormat('ko-KR').format(amount);
  };

  return (
    <Card className="mt-4">
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between">
          <CardTitle className="text-base">수납 기록</CardTitle>
          <Button variant="ghost" size="icon" onClick={onClose}>
            <X className="h-4 w-4" />
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <Label htmlFor="amount">수납 금액</Label>
              <span className="text-xs text-muted-foreground">
                잔여: ₩{formatAmount(remainingAmount)}
              </span>
            </div>
            <div className="flex gap-2">
              <Input
                id="amount"
                type="number"
                {...register('amount', { valueAsNumber: true })}
                className="flex-1"
              />
              <Button type="button" variant="outline" onClick={handleFullPayment}>
                전액 납부
              </Button>
            </div>
            {errors.amount && (
              <p className="text-sm text-destructive">{errors.amount.message}</p>
            )}
            {currentAmount > remainingAmount && (
              <p className="text-sm text-destructive">잔여 금액을 초과할 수 없습니다</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="method">결제 방법</Label>
            <Select
              defaultValue="CASH"
              onValueChange={(value) => setValue('method', value as PaymentMethod)}
            >
              <SelectTrigger>
                <SelectValue placeholder="결제 방법 선택" />
              </SelectTrigger>
              <SelectContent>
                {PAYMENT_METHODS.map((method) => (
                  <SelectItem key={method.value} value={method.value}>
                    {method.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="flex gap-2 pt-2">
            <Button type="button" variant="outline" onClick={onClose} className="flex-1">
              취소
            </Button>
            <Button
              type="submit"
              disabled={isPending || currentAmount > remainingAmount}
              className="flex-1"
            >
              {isPending ? '처리 중...' : '수납 기록'}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
}
