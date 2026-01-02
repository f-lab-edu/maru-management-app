import { useState } from 'react';
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
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/shared/components/ui/tabs';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { useStudents } from '@/features/students/hooks/useStudents';
import { useCreateInvoice, useCreateBulkInvoices } from '../hooks';
import { useAlert } from '@/hooks';

const singleCreateSchema = z.object({
  studentId: z.string().min(1, '원생을 선택해주세요'),
  amount: z.number().positive('금액은 0보다 커야 합니다'),
  dueDate: z.string().min(1, '납부 기한을 선택해주세요'),
  note: z.string().max(500, '비고는 500자 이내여야 합니다').optional(),
  billingYearMonth: z.string().optional(),
});

const bulkCreateSchema = z.object({
  defaultAmount: z.number().positive('금액은 0보다 커야 합니다'),
  dueDate: z.string().min(1, '납부 기한을 선택해주세요'),
  note: z.string().max(500, '비고는 500자 이내여야 합니다').optional(),
  billingYearMonth: z.string().optional(),
});

type SingleCreateData = z.infer<typeof singleCreateSchema>;
type BulkCreateData = z.infer<typeof bulkCreateSchema>;

interface InvoiceCreateSheetProps {
  isOpen: boolean;
  onClose: () => void;
  preSelectedStudentId?: string;
}

export function InvoiceCreateSheet({
  isOpen,
  onClose,
  preSelectedStudentId,
}: InvoiceCreateSheetProps) {
  const { selectedDojang } = useAuthStore();
  const dojangId = selectedDojang?.dojangId ?? null;

  const [activeTab, setActiveTab] = useState<'single' | 'bulk'>(
    preSelectedStudentId ? 'single' : 'single'
  );

  const { showSuccess, showError } = useAlert();

  const { data: studentsData } = useStudents(dojangId);
  const students = studentsData?.students?.filter((s) => s.status === 'ACTIVE') ?? [];

  const { mutateAsync: createInvoice, isPending: isCreating } = useCreateInvoice(dojangId);
  const { mutateAsync: createBulkInvoices, isPending: isBulkCreating } =
    useCreateBulkInvoices(dojangId);

  const now = new Date();
  const currentYear = now.getFullYear();
  const currentMonth = now.getMonth() + 1;
  const defaultBillingYearMonth = `${currentYear}-${String(currentMonth).padStart(2, '0')}`;
  const defaultDueDate = new Date(now.getFullYear(), now.getMonth() + 1, 10)
    .toISOString()
    .split('T')[0];

  const singleForm = useForm<SingleCreateData>({
    resolver: zodResolver(singleCreateSchema),
    defaultValues: {
      studentId: preSelectedStudentId ?? '',
      amount: 100000,
      dueDate: defaultDueDate,
      note: '',
      billingYearMonth: defaultBillingYearMonth,
    },
  });

  const bulkForm = useForm<BulkCreateData>({
    resolver: zodResolver(bulkCreateSchema),
    defaultValues: {
      defaultAmount: 100000,
      dueDate: defaultDueDate,
      note: '',
      billingYearMonth: defaultBillingYearMonth,
    },
  });

  const handleOpenChange = (open: boolean) => {
    if (!open) {
      const swalContainer = document.querySelector('.swal2-container');
      if (swalContainer) return;
      onClose();
    }
  };

  const handleSingleSubmit = async (data: SingleCreateData) => {
    try {
      await createInvoice(data);
      showSuccess('청구서가 생성되었습니다');
      singleForm.reset();
      onClose();
    } catch {
      showError('청구서 생성에 실패했습니다');
    }
  };

  const handleBulkSubmit = async (data: BulkCreateData) => {
    try {
      const result = await createBulkInvoices({
        ...data,
        studentIds: undefined,
      });
      showSuccess(
        `${result.createdCount}건 청구서가 생성되었습니다 (${result.skippedCount}건 건너뜀)`
      );
      bulkForm.reset();
      onClose();
    } catch {
      showError('청구서 생성에 실패했습니다');
    }
  };

  return (
    <Sheet open={isOpen} onOpenChange={handleOpenChange} modal={false}>
      <SheetContent side="right" className="w-[400px] overflow-y-auto sm:w-[540px]">
        <SheetHeader>
          <SheetTitle>청구서 생성</SheetTitle>
        </SheetHeader>

        <div className="mt-6">
          {preSelectedStudentId ? (
            <form
              onSubmit={singleForm.handleSubmit(handleSingleSubmit)}
              className="space-y-4"
            >
              <div className="space-y-2">
                <Label htmlFor="billingYearMonth">청구 연월</Label>
                <Input
                  type="month"
                  {...singleForm.register('billingYearMonth')}
                  placeholder="YYYY-MM"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="amount">금액 *</Label>
                <Input
                  type="number"
                  {...singleForm.register('amount', { valueAsNumber: true })}
                />
                {singleForm.formState.errors.amount && (
                  <p className="text-sm text-destructive">
                    {singleForm.formState.errors.amount.message}
                  </p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="dueDate">납부 기한 *</Label>
                <Input type="date" {...singleForm.register('dueDate')} />
                {singleForm.formState.errors.dueDate && (
                  <p className="text-sm text-destructive">
                    {singleForm.formState.errors.dueDate.message}
                  </p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="note">비고</Label>
                <Textarea
                  {...singleForm.register('note')}
                  placeholder="청구서에 대한 메모를 입력하세요"
                />
              </div>

              <div className="flex gap-2 pt-4">
                <Button
                  type="button"
                  variant="outline"
                  onClick={onClose}
                  className="flex-1"
                >
                  취소
                </Button>
                <Button type="submit" disabled={isCreating} className="flex-1">
                  {isCreating ? '생성 중...' : '청구서 생성'}
                </Button>
              </div>
            </form>
          ) : (
            <Tabs
              value={activeTab}
              onValueChange={(v) => setActiveTab(v as 'single' | 'bulk')}
            >
              <TabsList className="grid w-full grid-cols-2">
                <TabsTrigger value="single">단일 생성</TabsTrigger>
                <TabsTrigger value="bulk">일괄 생성</TabsTrigger>
              </TabsList>

              <TabsContent value="single" className="mt-4">
                <form
                  onSubmit={singleForm.handleSubmit(handleSingleSubmit)}
                  className="space-y-4"
                >
                  <div className="space-y-2">
                    <Label htmlFor="studentId">원생 선택 *</Label>
                    <Select
                      value={singleForm.watch('studentId')}
                      onValueChange={(v) => singleForm.setValue('studentId', v)}
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="원생을 선택해주세요" />
                      </SelectTrigger>
                      <SelectContent>
                        {students.map((student) => (
                          <SelectItem key={student.id} value={student.id}>
                            {student.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    {singleForm.formState.errors.studentId && (
                      <p className="text-sm text-destructive">
                        {singleForm.formState.errors.studentId.message}
                      </p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="billingYearMonth">청구 연월</Label>
                    <Input
                      type="month"
                      {...singleForm.register('billingYearMonth')}
                      placeholder="YYYY-MM"
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="amount">금액 *</Label>
                    <Input
                      type="number"
                      {...singleForm.register('amount', { valueAsNumber: true })}
                    />
                    {singleForm.formState.errors.amount && (
                      <p className="text-sm text-destructive">
                        {singleForm.formState.errors.amount.message}
                      </p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="dueDate">납부 기한 *</Label>
                    <Input type="date" {...singleForm.register('dueDate')} />
                    {singleForm.formState.errors.dueDate && (
                      <p className="text-sm text-destructive">
                        {singleForm.formState.errors.dueDate.message}
                      </p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="note">비고</Label>
                    <Textarea
                      {...singleForm.register('note')}
                      placeholder="청구서에 대한 메모를 입력하세요"
                    />
                  </div>

                  <div className="flex gap-2 pt-4">
                    <Button
                      type="button"
                      variant="outline"
                      onClick={onClose}
                      className="flex-1"
                    >
                      취소
                    </Button>
                    <Button type="submit" disabled={isCreating} className="flex-1">
                      {isCreating ? '생성 중...' : '청구서 생성'}
                    </Button>
                  </div>
                </form>
              </TabsContent>

              <TabsContent value="bulk" className="mt-4">
                <form
                  onSubmit={bulkForm.handleSubmit(handleBulkSubmit)}
                  className="space-y-4"
                >
                  <div className="space-y-2">
                    <Label htmlFor="billingYearMonth">청구 연월</Label>
                    <Input
                      type="month"
                      {...bulkForm.register('billingYearMonth')}
                      placeholder="YYYY-MM"
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="defaultAmount">기본 금액 *</Label>
                    <Input
                      type="number"
                      {...bulkForm.register('defaultAmount', { valueAsNumber: true })}
                    />
                    <p className="text-xs text-muted-foreground">
                      모든 청구서에 동일 금액이 적용됩니다
                    </p>
                    {bulkForm.formState.errors.defaultAmount && (
                      <p className="text-sm text-destructive">
                        {bulkForm.formState.errors.defaultAmount.message}
                      </p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="dueDate">납부 기한 *</Label>
                    <Input type="date" {...bulkForm.register('dueDate')} />
                    {bulkForm.formState.errors.dueDate && (
                      <p className="text-sm text-destructive">
                        {bulkForm.formState.errors.dueDate.message}
                      </p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="note">비고</Label>
                    <Textarea
                      {...bulkForm.register('note')}
                      placeholder="청구서에 대한 메모를 입력하세요"
                    />
                  </div>

                  <div className="bg-muted p-3 rounded-lg">
                    <p className="text-sm">
                      <strong>대상 원생:</strong> {students.length}명
                    </p>
                    <p className="text-xs text-muted-foreground mt-1">
                      이미 해당 연월에 청구서가 있는 원생은 건너뜁니다
                    </p>
                  </div>

                  <div className="flex gap-2 pt-4">
                    <Button
                      type="button"
                      variant="outline"
                      onClick={onClose}
                      className="flex-1"
                    >
                      취소
                    </Button>
                    <Button type="submit" disabled={isBulkCreating} className="flex-1">
                      {isBulkCreating ? '생성 중...' : `${students.length}명 일괄 생성`}
                    </Button>
                  </div>
                </form>
              </TabsContent>
            </Tabs>
          )}
        </div>
      </SheetContent>
    </Sheet>
  );
}
