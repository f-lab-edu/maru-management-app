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
import { useCreateSection } from '../hooks';

const formSchema = z.object({
  name: z.string().min(1, '수련부 이름을 입력해주세요.').max(50, '50자 이하로 입력해주세요.'),
});

type FormData = z.infer<typeof formSchema>;

interface SectionCreateDialogProps {
  dojangId: string;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function SectionCreateDialog({
  dojangId,
  open,
  onOpenChange,
}: SectionCreateDialogProps) {
  const createSection = useCreateSection(dojangId);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(formSchema),
    defaultValues: { name: '' },
  });

  const onSubmit = async (data: FormData) => {
    try {
      await createSection.mutateAsync(data);
      reset();
      onOpenChange(false);
    } catch (error) {
      console.error('수련부 생성 실패:', error);
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
          <DialogTitle>수련부 추가</DialogTitle>
          <DialogDescription>
            새로운 수련부를 생성합니다. 수련부는 여러 수련반을 포함할 수 있습니다.
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
            <Button type="submit" disabled={createSection.isPending}>
              {createSection.isPending && (
                <Loader2 className="h-4 w-4 mr-2 animate-spin" />
              )}
              추가
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
