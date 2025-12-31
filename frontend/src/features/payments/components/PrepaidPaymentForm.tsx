import { useMemo } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import { Textarea } from '@/shared/components/ui/textarea';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { Calculator } from 'lucide-react';
import { useAuthStore } from '@/stores/authStore';
import { usePrepaidPayment } from '../hooks';
import { useAlert } from '@/hooks';
import { getApiErrorMessage } from '@/constants/errorMessages';
import type { PaymentMethod } from '../types';
import { PAYMENT_METHOD_LABEL } from '../types';

const currentDate = new Date();
const currentYear = currentDate.getFullYear();
const currentMonth = currentDate.getMonth() + 1;

const prepaidSchema = z.object({
  startYear: z.number().min(currentYear - 1).max(currentYear + 5),
  startMonth: z.number().min(1).max(12),
  endYear: z.number().min(currentYear - 1).max(currentYear + 5),
  endMonth: z.number().min(1).max(12),
  monthlyAmount: z.number().positive('월 수강료를 입력해주세요'),
  totalAmount: z.number().positive('총 결제 금액을 입력해주세요'),
  paymentMethod: z.enum(['CASH', 'CARD', 'TRANSFER', 'PG', 'OTHER'] as const),
  dueDate: z.string().optional(),
  note: z.string().max(500, '비고는 500자 이내여야 합니다').optional(),
}).refine((data) => {
  const start = data.startYear * 12 + data.startMonth;
  const end = data.endYear * 12 + data.endMonth;
  return end >= start;
}, {
  message: '종료 월이 시작 월보다 이전일 수 없습니다',
  path: ['endMonth'],
});

type PrepaidFormData = z.infer<typeof prepaidSchema>;

interface PrepaidPaymentFormProps {
  studentId: string;
  studentName: string;
  onSuccess: () => void;
}

const YEARS = Array.from({ length: 7 }, (_, i) => currentYear - 1 + i);
const MONTHS = Array.from({ length: 12 }, (_, i) => i + 1);

export function PrepaidPaymentForm({
  studentId,
  studentName,
  onSuccess,
}: PrepaidPaymentFormProps) {
  const { selectedDojang } = useAuthStore();
  const dojangId = selectedDojang?.dojangId ?? null;
  const { showSuccess, showError } = useAlert();
  const { mutateAsync: processPrepaidPayment, isPending } = usePrepaidPayment(dojangId);

  const form = useForm<PrepaidFormData>({
    resolver: zodResolver(prepaidSchema),
    defaultValues: {
      startYear: currentYear,
      startMonth: currentMonth,
      endYear: currentYear,
      endMonth: Math.min(currentMonth + 2, 12),
      monthlyAmount: 100000,
      totalAmount: 0,
      paymentMethod: 'CARD',
      dueDate: new Date().toISOString().split('T')[0],
      note: '',
    },
  });

  const watchedValues = form.watch();

  const calculatedInfo = useMemo(() => {
    const start = watchedValues.startYear * 12 + watchedValues.startMonth;
    const end = watchedValues.endYear * 12 + watchedValues.endMonth;
    const monthCount = Math.max(1, end - start + 1);
    const originalTotal = monthCount * watchedValues.monthlyAmount;
    const discountAmount = originalTotal - (watchedValues.totalAmount || 0);
    const discountPercent = originalTotal > 0
      ? ((discountAmount / originalTotal) * 100).toFixed(1)
      : '0';

    return {
      monthCount,
      originalTotal,
      discountAmount,
      discountPercent,
    };
  }, [
    watchedValues.startYear,
    watchedValues.startMonth,
    watchedValues.endYear,
    watchedValues.endMonth,
    watchedValues.monthlyAmount,
    watchedValues.totalAmount,
  ]);

  const applyFullAmount = () => {
    form.setValue('totalAmount', calculatedInfo.originalTotal);
  };

  const handleSubmit = async (data: PrepaidFormData) => {
    try {
      const result = await processPrepaidPayment({
        studentId,
        startYear: data.startYear,
        startMonth: data.startMonth,
        endYear: data.endYear,
        endMonth: data.endMonth,
        monthlyAmount: data.monthlyAmount,
        totalAmount: data.totalAmount,
        paymentMethod: data.paymentMethod as PaymentMethod,
        dueDate: data.dueDate || undefined,
        note: data.note || undefined,
      });

      showSuccess(`${result.invoiceCount}개월 선납 처리 완료! (₩${result.totalAmount.toLocaleString()})`);
      onSuccess();
    } catch (error) {
      showError(getApiErrorMessage(error, '선납 처리에 실패했습니다'));
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <p className="text-sm text-muted-foreground">
          <span className="font-semibold text-foreground">{studentName}</span> 원생의 여러 달 수강료를 한 번에 수납합니다
        </p>
      </div>

      <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-5">
        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label className="text-xs text-muted-foreground">시작</Label>
            <div className="flex gap-2">
              <Select
                value={String(watchedValues.startYear)}
                onValueChange={(v) => form.setValue('startYear', Number(v))}
              >
                <SelectTrigger className="flex-1">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {YEARS.map((y) => (
                    <SelectItem key={y} value={String(y)}>
                      {y}년
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <Select
                value={String(watchedValues.startMonth)}
                onValueChange={(v) => form.setValue('startMonth', Number(v))}
              >
                <SelectTrigger className="w-20">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {MONTHS.map((m) => (
                    <SelectItem key={m} value={String(m)}>
                      {m}월
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>

          <div className="space-y-2">
            <Label className="text-xs text-muted-foreground">종료</Label>
            <div className="flex gap-2">
              <Select
                value={String(watchedValues.endYear)}
                onValueChange={(v) => form.setValue('endYear', Number(v))}
              >
                <SelectTrigger className="flex-1">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {YEARS.map((y) => (
                    <SelectItem key={y} value={String(y)}>
                      {y}년
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <Select
                value={String(watchedValues.endMonth)}
                onValueChange={(v) => form.setValue('endMonth', Number(v))}
              >
                <SelectTrigger className="w-20">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {MONTHS.map((m) => (
                    <SelectItem key={m} value={String(m)}>
                      {m}월
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            {form.formState.errors.endMonth && (
              <p className="text-xs text-destructive">
                {form.formState.errors.endMonth.message}
              </p>
            )}
          </div>
        </div>

        <div className="rounded-lg bg-primary/5 border border-primary/20 p-4 text-center">
          <span className="text-3xl font-bold text-primary">
            {calculatedInfo.monthCount}개월
          </span>
          <p className="text-sm text-muted-foreground mt-1">
            {watchedValues.startYear}년 {watchedValues.startMonth}월 ~{' '}
            {watchedValues.endYear}년 {watchedValues.endMonth}월
          </p>
        </div>

        <div className="space-y-2">
          <Label htmlFor="monthlyAmount">월 수강료</Label>
          <Input
            type="number"
            {...form.register('monthlyAmount', { valueAsNumber: true })}
            placeholder="100000"
          />
          {form.formState.errors.monthlyAmount && (
            <p className="text-xs text-destructive">
              {form.formState.errors.monthlyAmount.message}
            </p>
          )}
        </div>

        <div className="space-y-2">
          <div className="flex items-center justify-between">
            <Label htmlFor="totalAmount">실 결제 금액</Label>
            <Button
              type="button"
              variant="ghost"
              size="sm"
              onClick={applyFullAmount}
              className="h-7 text-xs"
            >
              <Calculator className="mr-1 h-3 w-3" />
              정가 적용 (₩{calculatedInfo.originalTotal.toLocaleString()})
            </Button>
          </div>
          <Input
            type="number"
            {...form.register('totalAmount', { valueAsNumber: true })}
            placeholder={String(calculatedInfo.originalTotal)}
          />
          {form.formState.errors.totalAmount && (
            <p className="text-xs text-destructive">
              {form.formState.errors.totalAmount.message}
            </p>
          )}
        </div>

        {calculatedInfo.discountAmount > 0 && (
          <div className="rounded-lg bg-emerald-50 border border-emerald-200 p-3">
            <div className="flex justify-between text-sm">
              <span className="text-emerald-700">할인 금액</span>
              <span className="font-semibold text-emerald-700">
                -₩{calculatedInfo.discountAmount.toLocaleString()} ({calculatedInfo.discountPercent}%)
              </span>
            </div>
          </div>
        )}

        <div className="space-y-2">
          <Label>결제 수단</Label>
          <Select
            value={watchedValues.paymentMethod}
            onValueChange={(v) => form.setValue('paymentMethod', v as PaymentMethod)}
          >
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {Object.entries(PAYMENT_METHOD_LABEL).map(([value, label]) => (
                <SelectItem key={value} value={value}>
                  {label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-2">
          <Label htmlFor="note">비고 (선택)</Label>
          <Textarea
            {...form.register('note')}
            placeholder="예: 3개월 선납 10% 할인"
            rows={2}
          />
        </div>

        <Button type="submit" disabled={isPending} className="w-full" size="lg">
          {isPending ? '처리 중...' : `${calculatedInfo.monthCount}개월 선납 등록`}
        </Button>
      </form>
    </div>
  );
}
