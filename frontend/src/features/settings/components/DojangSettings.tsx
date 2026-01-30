import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { dojangService, DojangUpdateReq } from '@/services/dojangService';
import { useAlert, userKeys } from '@/hooks';
import { getApiErrorMessage } from '@/constants/errorMessages';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import { Checkbox } from '@/shared/components/ui/checkbox';
import { Loader2 } from 'lucide-react';

const dojangSettingsSchema = z.object({
  name: z.string().min(2, '도장 이름은 2자 이상이어야 합니다').max(100, '도장 이름은 100자 이하여야 합니다'),
  phone: z.string().optional().nullable(),
  address: z.string().optional().nullable(),
  defaultTuition: z.number().int().min(0, '수련비는 0 이상이어야 합니다').optional().nullable(),
  autoInvoiceEnabled: z.boolean(),
  autoInvoiceDay: z.number().int().min(1, '1일 이상이어야 합니다').max(28, '28일 이하여야 합니다').optional().nullable(),
});

type DojangSettingsFormData = z.infer<typeof dojangSettingsSchema>;

const dojangKeys = {
  me: ['dojang', 'me'] as const,
};

export const DojangSettings = () => {
  const { showSuccess, showError } = useAlert();
  const queryClient = useQueryClient();

  const { data: dojang, isLoading } = useQuery({
    queryKey: dojangKeys.me,
    queryFn: dojangService.getMyDojang,
  });

  const { mutateAsync: updateDojang, isPending } = useMutation({
    mutationFn: (data: DojangUpdateReq) => dojangService.updateMyDojang(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: dojangKeys.me });
      queryClient.invalidateQueries({ queryKey: userKeys.dojangs() });
    },
  });

  const {
    register,
    handleSubmit,
    reset,
    watch,
    setValue,
    formState: { errors, isDirty },
  } = useForm<DojangSettingsFormData>({
    resolver: zodResolver(dojangSettingsSchema),
    defaultValues: {
      name: '',
      phone: '',
      address: '',
      defaultTuition: null,
      autoInvoiceEnabled: false,
      autoInvoiceDay: null,
    },
  });

  useEffect(() => {
    if (dojang) {
      reset({
        name: dojang.name,
        phone: dojang.phone ?? '',
        address: dojang.address ?? '',
        defaultTuition: dojang.defaultTuition,
        autoInvoiceEnabled: dojang.autoInvoiceEnabled,
        autoInvoiceDay: dojang.autoInvoiceDay,
      });
    }
  }, [dojang, reset]);

  const autoInvoiceEnabled = watch('autoInvoiceEnabled');

  const onSubmit = async (data: DojangSettingsFormData) => {
    try {
      await updateDojang({
        name: data.name,
        phone: data.phone || null,
        address: data.address || null,
        defaultTuition: data.defaultTuition,
        autoInvoiceEnabled: data.autoInvoiceEnabled,
        autoInvoiceDay: data.autoInvoiceEnabled ? data.autoInvoiceDay : null,
      });
      showSuccess('도장 설정이 저장되었습니다');
    } catch (error) {
      showError(getApiErrorMessage(error, '도장 설정 저장에 실패했습니다'));
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <Loader2 className="h-6 w-6 animate-spin text-slate-400" />
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-8">
      <section className="space-y-4">
        <h3 className="text-lg font-semibold text-slate-900">도장 정보</h3>
        <div className="grid gap-4 sm:grid-cols-2">
          <div className="space-y-2">
            <Label htmlFor="name">도장명</Label>
            <Input id="name" {...register('name')} />
            {errors.name && <p className="text-sm text-red-500">{errors.name.message}</p>}
          </div>
          <div className="space-y-2">
            <Label htmlFor="phone">전화번호</Label>
            <Input id="phone" {...register('phone')} placeholder="010-0000-0000" />
          </div>
        </div>
        <div className="space-y-2">
          <Label htmlFor="address">주소</Label>
          <Input id="address" {...register('address')} placeholder="도장 주소를 입력하세요" />
        </div>
      </section>

      <div className="border-t border-slate-200" />

      <section className="space-y-4">
        <h3 className="text-lg font-semibold text-slate-900">수납 설정</h3>
        <div className="grid gap-4 sm:grid-cols-2">
          <div className="space-y-2">
            <Label htmlFor="defaultTuition">기본 수련비 (원)</Label>
            <Input
              id="defaultTuition"
              type="number"
              {...register('defaultTuition', { valueAsNumber: true })}
              placeholder="0"
            />
            {errors.defaultTuition && (
              <p className="text-sm text-red-500">{errors.defaultTuition.message}</p>
            )}
          </div>
        </div>

        <div className="flex items-center gap-3">
          <Checkbox
            id="autoInvoiceEnabled"
            checked={autoInvoiceEnabled}
            onCheckedChange={(checked) => setValue('autoInvoiceEnabled', checked === true, { shouldDirty: true })}
          />
          <Label htmlFor="autoInvoiceEnabled" className="cursor-pointer">
            자동 청구서 발행
          </Label>
        </div>

        {autoInvoiceEnabled && (
          <div className="ml-7 space-y-2">
            <Label htmlFor="autoInvoiceDay">매월 발행일</Label>
            <div className="flex items-center gap-2">
              <Input
                id="autoInvoiceDay"
                type="number"
                className="w-24"
                {...register('autoInvoiceDay', { valueAsNumber: true })}
                min={1}
                max={28}
              />
              <span className="text-sm text-slate-500">일</span>
            </div>
            {errors.autoInvoiceDay && (
              <p className="text-sm text-red-500">{errors.autoInvoiceDay.message}</p>
            )}
          </div>
        )}
      </section>

      <div className="flex justify-end pt-4">
        <Button type="submit" disabled={!isDirty || isPending}>
          {isPending ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : null}
          저장
        </Button>
      </div>
    </form>
  );
};
