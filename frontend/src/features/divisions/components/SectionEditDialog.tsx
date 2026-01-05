import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Loader2 } from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '../../../shared/components/ui/dialog';
import { Button } from '../../../shared/components/ui/button';
import { Input } from '../../../shared/components/ui/input';
import { Label } from '../../../shared/components/ui/label';
import { useUpdateSection } from '../hooks';
import type { SectionRes } from '../types';

const formSchema = z.object({
  name: z.string().min(1, '수련부 이름을 입력해주세요.').max(50, '50자 이하로 입력해주세요.'),
});

type FormData = z.infer<typeof formSchema>;

interface SectionEditDialogProps {
  dojangId: string;
  section: SectionRes | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function SectionEditDialog({
  dojangId,
  section,
  open,
  onOpenChange,
}: SectionEditDialogProps) {
  const updateSection = useUpdateSection(dojangId);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(formSchema),
    defaultValues: { name: '' },
  });

  useEffect(() => {
    if (section) {
      reset({ name: section.name });
    }
  }, [section, reset]);

  const onSubmit = async (data: FormData) => {
    if (!section) return;

    try {
      await updateSection.mutateAsync({
        sectionId: section.id,
        data,
      });
      onOpenChange(false);
    } catch (error) {
      console.error('수련부 수정 실패:', error);
    }
  };

  const handleClose = (open: boolean) => {
    if (!open) {
      reset();
    }
    onOpenChange(open);
  };

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-[400px]">
        <DialogHeader>
          <DialogTitle>수련부 수정</DialogTitle>
          <DialogDescription>
            수련부 이름을 변경합니다.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)}>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="name">수련부 이름</Label>
              <Input
                id="name"
                placeholder="예: 초등부, 유아부"
                {...register('name')}
                className={errors.name ? 'border-red-500' : ''}
              />
              {errors.name && (
                <p className="text-sm text-red-500">{errors.name.message}</p>
              )}
            </div>
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => handleClose(false)}
            >
              취소
            </Button>
            <Button type="submit" disabled={updateSection.isPending}>
              {updateSection.isPending && (
                <Loader2 className="h-4 w-4 mr-2 animate-spin" />
              )}
              저장
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
