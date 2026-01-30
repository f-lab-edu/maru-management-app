import { useEffect } from 'react';
import { useForm, useFieldArray } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Plus, Trash2, User } from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/shared/components/ui/dialog';
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
import { Separator } from '@/shared/components/ui/separator';
import { Badge } from '@/shared/components/ui/badge';
import type { Student, GuardianRelation } from '@/types/student';
import { GUARDIAN_RELATION_LABEL } from '@/types/student';

const guardianSchema = z.object({
  name: z.string().min(1, '이름을 입력해주세요'),
  phone: z.string().min(1, '연락처를 입력해주세요'),
  relation: z.enum(['FATHER', 'MOTHER', 'GRANDPARENT', 'OTHER'] as const),
  isPrimary: z.boolean(),
});

const studentFormSchema = z.object({
  name: z.string().min(1, '이름을 입력해주세요'),
  birth: z.string().min(1, '생년월일을 입력해주세요'),
  phone: z.string().optional(),
  guardians: z.array(guardianSchema),
});

type StudentFormData = z.infer<typeof studentFormSchema>;

interface StudentFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: StudentFormData) => void;
  student?: Student | null;
  isLoading?: boolean;
}

const RELATION_OPTIONS: { value: GuardianRelation; label: string }[] = [
  { value: 'FATHER', label: GUARDIAN_RELATION_LABEL.FATHER },
  { value: 'MOTHER', label: GUARDIAN_RELATION_LABEL.MOTHER },
  { value: 'GRANDPARENT', label: GUARDIAN_RELATION_LABEL.GRANDPARENT },
  { value: 'OTHER', label: GUARDIAN_RELATION_LABEL.OTHER },
];

export function StudentFormModal({
  isOpen,
  onClose,
  onSubmit,
  student,
  isLoading,
}: StudentFormModalProps) {
  const isEditMode = !!student;

  const {
    register,
    control,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors },
  } = useForm<StudentFormData>({
    resolver: zodResolver(studentFormSchema),
    defaultValues: {
      name: '',
      birth: '',
      phone: '',
      guardians: [],
    },
  });

  const { fields, append, remove } = useFieldArray({
    control,
    name: 'guardians',
  });

  const watchedGuardians = watch('guardians');

  useEffect(() => {
    if (!isOpen) return;

    if (student) {
      reset({
        name: student.name,
        birth: student.birth,
        phone: student.phone || '',
        guardians: student.guardians.map((g) => ({
          name: g.name,
          phone: g.phone,
          relation: g.relation,
          isPrimary: g.isPrimary,
        })),
      });
    } else {
      reset({
        name: '',
        birth: '',
        phone: '',
        guardians: [],
      });
    }
  }, [isOpen, student, reset]);

  const handleAddGuardian = () => {
    const hasPrimary = watchedGuardians?.some((g) => g.isPrimary);
    append({
      name: '',
      phone: '',
      relation: 'MOTHER',
      isPrimary: !hasPrimary,
    });
  };

  const handleSetPrimary = (index: number) => {
    watchedGuardians?.forEach((_, i) => {
      setValue(`guardians.${i}.isPrimary`, i === index);
    });
  };

  const handleFormSubmit = (data: StudentFormData) => {
    onSubmit(data);
  };

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <User className="h-5 w-5" />
            {isEditMode ? '수련생 정보 수정' : '수련생 등록'}
          </DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-6">
          {/* 기본 정보 섹션 */}
          <div className="space-y-4">
            <h3 className="text-sm font-medium text-muted-foreground">기본 정보</h3>
            <div className="grid gap-4">
              <div className="grid gap-2">
                <Label htmlFor="name">이름 *</Label>
                <Input
                  id="name"
                  placeholder="홍길동"
                  {...register('name')}
                  className={errors.name ? 'border-destructive' : ''}
                />
                {errors.name && (
                  <p className="text-xs text-destructive">{errors.name.message}</p>
                )}
              </div>

              <div className="grid gap-2">
                <Label htmlFor="birth">생년월일 *</Label>
                <Input
                  id="birth"
                  type="date"
                  {...register('birth')}
                  className={errors.birth ? 'border-destructive' : ''}
                />
                {errors.birth && (
                  <p className="text-xs text-destructive">{errors.birth.message}</p>
                )}
              </div>

              <div className="grid gap-2">
                <Label htmlFor="phone">연락처 (선택)</Label>
                <Input
                  id="phone"
                  placeholder="010-0000-0000"
                  {...register('phone')}
                />
                <p className="text-xs text-muted-foreground">
                  성인 수련생의 경우 본인 연락처를 입력해주세요
                </p>
              </div>
            </div>
          </div>

          <Separator />

          {/* 보호자 정보 섹션 */}
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-medium text-muted-foreground">보호자 정보</h3>
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={handleAddGuardian}
              >
                <Plus className="mr-1 h-4 w-4" />
                보호자 추가
              </Button>
            </div>

            {fields.length === 0 ? (
              <div className="rounded-lg border border-dashed p-6 text-center">
                <p className="text-sm text-muted-foreground">
                  등록된 보호자가 없습니다.
                  <br />
                  성인 수련생이 아니라면 보호자를 추가해주세요.
                </p>
              </div>
            ) : (
              <div className="space-y-4">
                {fields.map((field, index) => (
                  <div
                    key={field.id}
                    className="relative rounded-lg border bg-muted/30 p-4"
                  >
                    {/* 주 보호자 표시 */}
                    {watchedGuardians?.[index]?.isPrimary && (
                      <Badge className="absolute -top-2 left-3 text-xs">
                        주 보호자
                      </Badge>
                    )}

                    <div className="grid gap-3">
                      <div className="flex items-center justify-between">
                        <Label className="text-sm">보호자 {index + 1}</Label>
                        <div className="flex gap-2">
                          {!watchedGuardians?.[index]?.isPrimary && (
                            <Button
                              type="button"
                              variant="ghost"
                              size="sm"
                              onClick={() => handleSetPrimary(index)}
                              className="h-7 text-xs"
                            >
                              주 보호자로 설정
                            </Button>
                          )}
                          <Button
                            type="button"
                            variant="ghost"
                            size="icon"
                            onClick={() => remove(index)}
                            className="h-7 w-7 text-destructive hover:text-destructive"
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </div>
                      </div>

                      <div className="grid grid-cols-2 gap-3">
                        <div className="grid gap-1.5">
                          <Label htmlFor={`guardian-name-${index}`} className="text-xs">
                            이름
                          </Label>
                          <Input
                            id={`guardian-name-${index}`}
                            placeholder="이름"
                            {...register(`guardians.${index}.name`)}
                            className="h-9"
                          />
                        </div>
                        <div className="grid gap-1.5">
                          <Label htmlFor={`guardian-relation-${index}`} className="text-xs">
                            관계
                          </Label>
                          <Select
                            value={watchedGuardians?.[index]?.relation}
                            onValueChange={(value) =>
                              setValue(`guardians.${index}.relation`, value as GuardianRelation)
                            }
                          >
                            <SelectTrigger className="h-9">
                              <SelectValue />
                            </SelectTrigger>
                            <SelectContent>
                              {RELATION_OPTIONS.map((opt) => (
                                <SelectItem key={opt.value} value={opt.value}>
                                  {opt.label}
                                </SelectItem>
                              ))}
                            </SelectContent>
                          </Select>
                        </div>
                      </div>

                      <div className="grid gap-1.5">
                        <Label htmlFor={`guardian-phone-${index}`} className="text-xs">
                          연락처
                        </Label>
                        <Input
                          id={`guardian-phone-${index}`}
                          placeholder="010-0000-0000"
                          {...register(`guardians.${index}.phone`)}
                          className="h-9"
                        />
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose}>
              취소
            </Button>
            <Button type="submit" disabled={isLoading}>
              {isLoading ? '저장 중...' : isEditMode ? '수정' : '등록'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
