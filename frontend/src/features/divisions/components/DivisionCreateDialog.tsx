import { useForm, Controller } from 'react-hook-form';
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
import { ToggleGroup, ToggleGroupItem } from '../../../shared/components/ui/toggle-group';
import { TimeSelect } from './TimeSelect';
import { useCreateDivision } from '../hooks';
import { getAllDays, getDayLabel } from '../utils';
import type { DayOfWeek } from '../types';

const formSchema = z.object({
  name: z.string().min(1, '수련반 이름을 입력해주세요.').max(50, '50자 이하로 입력해주세요.'),
  scheduleDays: z.array(z.string()).optional(),
  startTime: z.string().optional(),
  endTime: z.string().optional(),
});

type FormData = z.infer<typeof formSchema>;

interface DivisionCreateDialogProps {
  dojangId: string;
  sectionId: string;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function DivisionCreateDialog({
  dojangId,
  sectionId,
  open,
  onOpenChange,
}: DivisionCreateDialogProps) {
  const createDivision = useCreateDivision(dojangId);

  const {
    register,
    handleSubmit,
    reset,
    control,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      name: '',
      scheduleDays: [],
      startTime: '',
      endTime: '',
    },
  });

  const onSubmit = async (data: FormData) => {
    try {
      await createDivision.mutateAsync({
        sectionId,
        name: data.name,
        scheduleDays: data.scheduleDays as DayOfWeek[] | undefined,
        startTime: data.startTime || undefined,
        endTime: data.endTime || undefined,
      });
      reset();
      onOpenChange(false);
    } catch (error) {
      console.error('수련반 생성 실패:', error);
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
      <DialogContent className="sm:max-w-[620px]">
        <DialogHeader>
          <DialogTitle>수련반 추가</DialogTitle>
          <DialogDescription>
            새로운 수련반을 생성합니다. 수련 요일과 시간을 설정할 수 있습니다.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)}>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="name">수련반 이름</Label>
              <Input
                id="name"
                placeholder="예: A반, 1반"
                {...register('name')}
                className={errors.name ? 'border-red-500' : ''}
              />
              {errors.name && (
                <p className="text-sm text-red-500">{errors.name.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label>수련 요일</Label>
              <Controller
                name="scheduleDays"
                control={control}
                render={({ field }) => (
                  <ToggleGroup
                    type="multiple"
                    value={field.value}
                    onValueChange={field.onChange}
                    className="justify-start flex-wrap"
                  >
                    {getAllDays().map((day) => (
                      <ToggleGroupItem
                        key={day}
                        value={day}
                        className="px-3"
                      >
                        {getDayLabel(day)}
                      </ToggleGroupItem>
                    ))}
                  </ToggleGroup>
                )}
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>시작 시간</Label>
                <Controller
                  name="startTime"
                  control={control}
                  render={({ field }) => (
                    <TimeSelect
                      value={field.value ?? ''}
                      onChange={field.onChange}
                      placeholder="시작 시간"
                    />
                  )}
                />
              </div>
              <div className="space-y-2">
                <Label>종료 시간</Label>
                <Controller
                  name="endTime"
                  control={control}
                  render={({ field }) => (
                    <TimeSelect
                      value={field.value ?? ''}
                      onChange={field.onChange}
                      placeholder="종료 시간"
                    />
                  )}
                />
              </div>
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
            <Button type="submit" disabled={createDivision.isPending}>
              {createDivision.isPending && (
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
