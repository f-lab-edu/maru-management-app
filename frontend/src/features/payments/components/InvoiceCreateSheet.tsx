import { useState, useMemo } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Check, ChevronsUpDown, X } from 'lucide-react';
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
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from '@/shared/components/ui/command';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/shared/components/ui/popover';
import { Badge } from '@/shared/components/ui/badge';
import { MonthPicker } from '@/shared/components/ui/month-picker';
import { DatePicker } from '@/shared/components/ui/date-picker';
import { useStudents } from '@/features/students/hooks/useStudents';
import { useSections, useDivisions } from '@/features/divisions';
import { useCreateInvoice, useCreateBulkInvoices } from '../hooks';
import { useAlert } from '@/hooks';
import { cn } from '@/shared/utils';

const singleCreateSchema = z.object({
  studentId: z.string().min(1, '원생을 선택해주세요'),
  amount: z.number().positive('금액은 0보다 커야 합니다'),
  dueDate: z.string().min(1, '납부 기한을 선택해주세요'),
  note: z.string().max(500, '비고는 500자 이내여야 합니다').optional(),
  billingYearMonth: z.string().optional(),
});

const bulkCreateSchema = z.object({
  studentIds: z.array(z.string()).min(1, '최소 1명 이상의 원생을 선택해주세요'),
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

  const [sectionId, setSectionId] = useState<string | null>(null);
  const [divisionId, setDivisionId] = useState<string | null>(null);
  const [bulkComboboxOpen, setBulkComboboxOpen] = useState(false);
  const [bulkSearchQuery, setBulkSearchQuery] = useState('');
  const [singleComboboxOpen, setSingleComboboxOpen] = useState(false);
  const [singleSearchQuery, setSingleSearchQuery] = useState('');

  const { data: allStudentsData } = useStudents(dojangId);
  const { data: filteredStudentsData } = useStudents(dojangId, {
    sectionId: sectionId ?? undefined,
    divisionId: divisionId ?? undefined,
  });

  const { data: sectionsData } = useSections(dojangId ?? '');
  const { data: divisionsData } = useDivisions(dojangId ?? '', sectionId);

  const sections = sectionsData?.sections ?? [];
  const divisions = divisionsData?.divisions ?? [];
  const allStudents = allStudentsData?.students?.filter((s) => s.status === 'ACTIVE') ?? [];
  const students = filteredStudentsData?.students?.filter((s) => s.status === 'ACTIVE') ?? [];

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
      studentIds: [],
      defaultAmount: 100000,
      dueDate: defaultDueDate,
      note: '',
      billingYearMonth: defaultBillingYearMonth,
    },
  });

  const selectedStudentIds = bulkForm.watch('studentIds') ?? [];

  const bulkFilteredStudents = useMemo(() => {
    if (!bulkSearchQuery) return students;
    const lowerQuery = bulkSearchQuery.toLowerCase();
    return students.filter(
      (student) =>
        student.name.toLowerCase().includes(lowerQuery)
    );
  }, [students, bulkSearchQuery]);

  const singleFilteredStudents = useMemo(() => {
    if (!singleSearchQuery) return students;
    const lowerQuery = singleSearchQuery.toLowerCase();
    return students.filter(
      (student) =>
        student.name.toLowerCase().includes(lowerQuery)
    );
  }, [students, singleSearchQuery]);

  const handleOpenChange = (open: boolean) => {
    if (!open) {
      const swalContainer = document.querySelector('.swal2-container');
      if (swalContainer) return;

      setSectionId(null);
      setDivisionId(null);
      setBulkSearchQuery('');
      setSingleSearchQuery('');
      singleForm.reset();
      bulkForm.reset({
        studentIds: [],
        defaultAmount: 100000,
        dueDate: defaultDueDate,
        note: '',
        billingYearMonth: defaultBillingYearMonth,
      });
      onClose();
    }
  };

  const handleSingleSubmit = async (data: SingleCreateData) => {
    try {
      await createInvoice(data);
      showSuccess('청구서가 생성되었습니다');
      singleForm.reset();
      onClose();
    } catch (error) {
      const axiosError = error as { response?: { data?: { code?: string } } };
      const errorCode = axiosError.response?.data?.code;
      if (errorCode === 'INVOICE_301') {
        showError('해당 월에 이미 청구서가 존재합니다');
      } else {
        showError('청구서 생성에 실패했습니다');
      }
    }
  };

  const handleBulkSubmit = async (data: BulkCreateData) => {
    try {
      const result = await createBulkInvoices(data);
      showSuccess(
        `${result.createdCount}건 청구서가 생성되었습니다 (${result.skippedCount}건 건너뜀)`
      );
      bulkForm.reset();
      onClose();
    } catch {
      showError('청구서 생성에 실패했습니다');
    }
  };

  const handleSectionChange = (value: string) => {
    const newValue = value === '__ALL__' ? null : value;
    setSectionId(newValue);
    setDivisionId(null);
  };

  const handleDivisionChange = (value: string) => {
    const newValue = value === '__ALL__' ? null : value;
    setDivisionId(newValue);
  };

  const toggleStudent = (studentId: string) => {
    const current = selectedStudentIds;
    const updated = current.includes(studentId)
      ? current.filter((id) => id !== studentId)
      : [...current, studentId];
    bulkForm.setValue('studentIds', updated);
  };

  const selectAll = () => {
    bulkForm.setValue(
      'studentIds',
      bulkFilteredStudents.map((s) => s.id)
    );
  };

  const deselectAll = () => {
    bulkForm.setValue('studentIds', []);
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
                <MonthPicker
                  value={singleForm.watch('billingYearMonth')}
                  onChange={(value) => singleForm.setValue('billingYearMonth', value)}
                  placeholder="청구 연월을 선택하세요"
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
                <DatePicker
                  value={singleForm.watch('dueDate')}
                  onChange={(value) => singleForm.setValue('dueDate', value)}
                  placeholder="납부 기한을 선택해주세요"
                />
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
                    <Popover open={singleComboboxOpen} onOpenChange={setSingleComboboxOpen}>
                      <PopoverTrigger asChild>
                        <Button
                          variant="outline"
                          role="combobox"
                          aria-expanded={singleComboboxOpen}
                          className="w-full justify-between font-normal"
                        >
                          <span className="truncate">
                            {singleForm.watch('studentId')
                              ? students.find((s) => s.id === singleForm.watch('studentId'))?.name
                              : '원생을 선택해주세요'}
                          </span>
                          <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                        </Button>
                      </PopoverTrigger>
                      <PopoverContent className="w-[--radix-popover-trigger-width] p-0" align="start">
                        <Command>
                          <CommandInput
                            placeholder="이름, 연락처로 검색..."
                            value={singleSearchQuery}
                            onValueChange={setSingleSearchQuery}
                          />
                          <CommandList>
                            <CommandEmpty>원생을 찾을 수 없습니다.</CommandEmpty>
                            <CommandGroup>
                              {singleFilteredStudents.map((student) => (
                                <CommandItem
                                  key={student.id}
                                  value={student.id}
                                  onSelect={() => {
                                    singleForm.setValue('studentId', student.id);
                                    setSingleComboboxOpen(false);
                                    setSingleSearchQuery('');
                                  }}
                                >
                                  <Check
                                    className={cn(
                                      'mr-2 h-4 w-4',
                                      singleForm.watch('studentId') === student.id
                                        ? 'opacity-100'
                                        : 'opacity-0'
                                    )}
                                  />
                                  <div className="flex flex-col">
                                    <span>{student.name}</span>
                                  </div>
                                </CommandItem>
                              ))}
                            </CommandGroup>
                          </CommandList>
                        </Command>
                      </PopoverContent>
                    </Popover>
                    {singleForm.formState.errors.studentId && (
                      <p className="text-sm text-destructive">
                        {singleForm.formState.errors.studentId.message}
                      </p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="billingYearMonth">청구 연월</Label>
                    <MonthPicker
                      value={singleForm.watch('billingYearMonth')}
                      onChange={(value) => singleForm.setValue('billingYearMonth', value)}
                      placeholder="청구 연월을 선택하세요"
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
                    <DatePicker
                      value={singleForm.watch('dueDate')}
                      onChange={(value) => singleForm.setValue('dueDate', value)}
                      placeholder="납부 기한을 선택해주세요"
                    />
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
                  {sections.length > 0 && (
                    <div className="space-y-3 rounded-lg border p-3">
                      <Label className="text-sm font-medium">필터</Label>
                      <div className="flex gap-2">
                        <Select
                          value={sectionId ?? '__ALL__'}
                          onValueChange={handleSectionChange}
                        >
                          <SelectTrigger className="flex-1">
                            <SelectValue placeholder="수련부 선택" />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="__ALL__">전체 수련부</SelectItem>
                            {sections.map((section) => (
                              <SelectItem key={section.id} value={section.id}>
                                {section.name}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>

                        <Select
                          value={divisionId ?? '__ALL__'}
                          onValueChange={handleDivisionChange}
                          disabled={!sectionId}
                        >
                          <SelectTrigger className="flex-1">
                            <SelectValue placeholder="수련반 선택" />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="__ALL__">전체 수련반</SelectItem>
                            {divisions.map((division) => (
                              <SelectItem key={division.id} value={division.id}>
                                {division.name}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </div>
                    </div>
                  )}

                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <Label>원생 선택 *</Label>
                      <div className="flex gap-2">
                        <Button
                          type="button"
                          variant="ghost"
                          size="sm"
                          onClick={selectAll}
                          disabled={bulkFilteredStudents.length === 0}
                        >
                          전체 선택
                        </Button>
                        <Button
                          type="button"
                          variant="ghost"
                          size="sm"
                          onClick={deselectAll}
                          disabled={selectedStudentIds.length === 0}
                        >
                          선택 해제
                        </Button>
                      </div>
                    </div>

                    <Popover open={bulkComboboxOpen} onOpenChange={setBulkComboboxOpen}>
                      <PopoverTrigger asChild>
                        <Button
                          variant="outline"
                          role="combobox"
                          aria-expanded={bulkComboboxOpen}
                          className="w-full justify-between font-normal"
                        >
                          <span className="truncate">
                            {selectedStudentIds.length > 0
                              ? `${selectedStudentIds.length}명 선택됨`
                              : '원생을 선택해주세요'}
                          </span>
                          <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                        </Button>
                      </PopoverTrigger>
                      <PopoverContent className="w-[--radix-popover-trigger-width] p-0" align="start">
                        <Command>
                          <CommandInput
                            placeholder="이름, 연락처로 검색..."
                            value={bulkSearchQuery}
                            onValueChange={setBulkSearchQuery}
                          />
                          <CommandList>
                            <CommandEmpty>원생을 찾을 수 없습니다.</CommandEmpty>
                            <CommandGroup>
                              {bulkFilteredStudents.map((student) => {
                                const isSelected = selectedStudentIds.includes(student.id);
                                return (
                                  <CommandItem
                                    key={student.id}
                                    value={student.id}
                                    onSelect={() => toggleStudent(student.id)}
                                  >
                                    <Check
                                      className={cn(
                                        'mr-2 h-4 w-4',
                                        isSelected ? 'opacity-100' : 'opacity-0'
                                      )}
                                    />
                                    <div className="flex flex-col">
                                      <span>{student.name}</span>
                                    </div>
                                  </CommandItem>
                                );
                              })}
                            </CommandGroup>
                          </CommandList>
                        </Command>
                      </PopoverContent>
                    </Popover>

                    {selectedStudentIds.length > 0 && (
                      <div className="flex flex-wrap items-center gap-1.5 rounded-md border p-2">
                        {selectedStudentIds.slice(0, 5).map((studentId) => {
                          const student = allStudents.find((s) => s.id === studentId);
                          if (!student) return null;
                          return (
                            <Badge key={studentId} variant="secondary" className="gap-1">
                              {student.name}
                              <button
                                type="button"
                                onClick={() => toggleStudent(studentId)}
                                className="ml-1 rounded-full hover:bg-muted"
                              >
                                <X className="h-3 w-3" />
                              </button>
                            </Badge>
                          );
                        })}
                        {selectedStudentIds.length > 5 && (
                          <span className="text-sm text-muted-foreground">
                            외 {selectedStudentIds.length - 5}명
                          </span>
                        )}
                      </div>
                    )}

                    {bulkForm.formState.errors.studentIds && (
                      <p className="text-sm text-destructive">
                        {bulkForm.formState.errors.studentIds.message}
                      </p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="billingYearMonth">청구 연월</Label>
                    <MonthPicker
                      value={bulkForm.watch('billingYearMonth')}
                      onChange={(value) => bulkForm.setValue('billingYearMonth', value)}
                      placeholder="청구 연월을 선택하세요"
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
                    <DatePicker
                      value={bulkForm.watch('dueDate')}
                      onChange={(value) => bulkForm.setValue('dueDate', value)}
                      placeholder="납부 기한을 선택해주세요"
                    />
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
                      <strong>선택된 원생:</strong> {selectedStudentIds.length}명
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
                    <Button
                      type="submit"
                      disabled={isBulkCreating || selectedStudentIds.length === 0}
                      className="flex-1"
                    >
                      {isBulkCreating
                        ? '생성 중...'
                        : `${selectedStudentIds.length}명 일괄 생성`}
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
