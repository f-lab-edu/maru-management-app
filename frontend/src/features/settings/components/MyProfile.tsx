import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { userService, UserProfileUpdateReq } from '@/services/userService';
import { useUser, userKeys, useAlert } from '@/hooks';
import { getApiErrorMessage } from '@/constants/errorMessages';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import { Loader2 } from 'lucide-react';

const profileSchema = z.object({
  name: z.string().min(1, '이름을 입력해주세요').max(50, '이름은 50자 이하여야 합니다'),
  phone: z.string().optional().nullable(),
});

type ProfileFormData = z.infer<typeof profileSchema>;

export const MyProfile = () => {
  const { showSuccess, showError } = useAlert();
  const queryClient = useQueryClient();
  const { data: user, isLoading } = useUser();

  const { mutateAsync: updateProfile, isPending } = useMutation({
    mutationFn: (data: UserProfileUpdateReq) => userService.updateMyProfile(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: userKeys.me() });
    },
  });

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isDirty },
  } = useForm<ProfileFormData>({
    resolver: zodResolver(profileSchema),
    defaultValues: {
      name: '',
      phone: '',
    },
  });

  useEffect(() => {
    if (user) {
      reset({
        name: user.name,
        phone: user.phone ?? '',
      });
    }
  }, [user, reset]);

  const onSubmit = async (data: ProfileFormData) => {
    try {
      await updateProfile({
        name: data.name,
        phone: data.phone || null,
      });
      showSuccess('프로필이 저장되었습니다');
    } catch (error) {
      showError(getApiErrorMessage(error, '프로필 저장에 실패했습니다'));
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
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
      <div className="grid gap-4 sm:grid-cols-2">
        <div className="space-y-2">
          <Label htmlFor="profile-name">이름</Label>
          <Input id="profile-name" {...register('name')} />
          {errors.name && <p className="text-sm text-red-500">{errors.name.message}</p>}
        </div>
        <div className="space-y-2">
          <Label htmlFor="profile-phone">전화번호</Label>
          <Input id="profile-phone" {...register('phone')} placeholder="010-0000-0000" />
        </div>
      </div>

      <div className="space-y-2">
        <Label htmlFor="profile-email">이메일</Label>
        <Input id="profile-email" value={user?.email ?? ''} disabled className="bg-slate-50" />
        <p className="text-xs text-slate-400">이메일은 소셜 로그인 계정에서 가져오며 변경할 수 없습니다</p>
      </div>

      <div className="flex justify-end pt-4">
        <Button type="submit" disabled={!isDirty || isPending}>
          {isPending ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : null}
          저장
        </Button>
      </div>
    </form>
  );
};
