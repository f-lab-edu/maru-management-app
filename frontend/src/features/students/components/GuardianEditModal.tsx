import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
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
import { GUARDIAN_RELATION_LABEL, type Guardian, type GuardianRelation } from '@/types/student';

interface GuardianEditModalProps {
  guardian: Guardian | null;
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: { name: string; phone: string; relation: GuardianRelation }) => void;
  isLoading: boolean;
  mode?: 'edit' | 'add';
}

interface GuardianFormData {
  name: string;
  phone: string;
  relation: GuardianRelation;
}

export function GuardianEditModal({
  guardian,
  isOpen,
  onClose,
  onSubmit,
  isLoading,
  mode = 'edit',
}: GuardianEditModalProps) {
  const isAddMode = mode === 'add';
  const {
    register,
    handleSubmit,
    setValue,
    watch,
    reset,
    formState: { errors },
  } = useForm<GuardianFormData>();

  const relationValue = watch('relation');

  useEffect(() => {
    if (isAddMode) {
      reset({
        name: '',
        phone: '',
        relation: 'MOTHER',
      });
    } else if (guardian) {
      reset({
        name: guardian.name,
        phone: guardian.phone,
        relation: guardian.relation,
      });
    }
  }, [guardian, reset, isAddMode]);

  const handleFormSubmit = (data: GuardianFormData) => {
    onSubmit(data);
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>{isAddMode ? '보호자 추가' : '보호자 정보 수정'}</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="name">이름 *</Label>
            <Input
              id="name"
              {...register('name', { required: '이름을 입력해주세요' })}
              placeholder="홍길동"
            />
            {errors.name && <p className="text-sm text-red-500">{errors.name.message}</p>}
          </div>

          <div className="space-y-2">
            <Label htmlFor="phone">연락처 *</Label>
            <Input
              id="phone"
              {...register('phone', {
                required: '연락처를 입력해주세요',
                pattern: {
                  value: /^01[0-9]-?\d{3,4}-?\d{4}$/,
                  message: '올바른 연락처 형식이 아닙니다',
                },
              })}
              placeholder="010-1234-5678"
            />
            {errors.phone && <p className="text-sm text-red-500">{errors.phone.message}</p>}
          </div>

          <div className="space-y-2">
            <Label>관계 *</Label>
            <Select value={relationValue} onValueChange={(value) => setValue('relation', value as GuardianRelation)}>
              <SelectTrigger>
                <SelectValue placeholder="관계 선택" />
              </SelectTrigger>
              <SelectContent>
                {Object.entries(GUARDIAN_RELATION_LABEL).map(([value, label]) => (
                  <SelectItem key={value} value={value}>
                    {label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {errors.relation && (
              <p className="text-sm text-red-500">{errors.relation.message}</p>
            )}
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose} disabled={isLoading}>
              취소
            </Button>
            <Button type="submit" disabled={isLoading}>
              {isLoading ? '저장 중...' : '저장'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
