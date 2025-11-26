import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { useAuth } from '../../contexts/AuthContext';
import { userService } from '../../services/userService';
import { Button } from '../../shared/components/ui/button';
import { Input } from '../../shared/components/ui/input';
import { Label } from '../../shared/components/ui/label';
import { CardHeader, CardTitle, CardDescription, CardContent } from '../../shared/components/ui/card';
import { OnboardingBackButton } from './components/OnboardingBackButton';
import { User, Mail, Phone, Loader2 } from 'lucide-react';

const userInfoSchema = z.object({
  name: z.string().min(2, { message: '이름은 2글자 이상이어야 합니다.' }),
  email: z.string().email({ message: '올바른 이메일 형식이 아닙니다.' }),
  phone: z.string().regex(/^01[0-9]-\d{3,4}-\d{4}$/, { message: '010-0000-0000 형식으로 입력해주세요.' }),
});

type UserInfoFormValues = z.infer<typeof userInfoSchema>;

export default function UserInfoPage() {
  const navigate = useNavigate();
  const { user, logout, refreshUser } = useAuth();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const form = useForm<UserInfoFormValues>({
    resolver: zodResolver(userInfoSchema),
    defaultValues: {
      name: user?.name || '',
      email: user?.email || '',
      phone: user?.phone || '',
    },
  });

  const onSubmit = async (data: UserInfoFormValues) => {
    setIsSubmitting(true);
    try {
      await userService.updateProfile(data);
      await refreshUser();
      navigate('/onboarding/role');
    } catch (error) {
      console.error('프로필 업데이트 실패:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handlePrevious = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <div className="animate-in fade-in slide-in-from-right-8 duration-500 relative">
      <OnboardingBackButton onClick={handlePrevious} />
      <CardHeader className="pb-8 pt-2">
        <CardTitle className="text-3xl font-bold text-slate-900">기본 정보 입력</CardTitle>
        <CardDescription className="text-lg mt-2">
          서비스 이용을 위해 필요한 정보를 입력해주세요.
        </CardDescription>
      </CardHeader>

      <CardContent className="px-8 pb-10 mx-auto">
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
          <div className="space-y-2">
            <Label htmlFor="name">이름</Label>
            <div className="relative">
              <User className="absolute left-3 top-3 h-5 w-5 text-slate-400" />
              <Input
                id="name"
                placeholder="홍길동"
                {...form.register('name')}
                className={`pl-10 h-12 text-lg ${form.formState.errors.name ? 'border-red-500' : ''}`}
              />
            </div>
            {form.formState.errors.name && (
              <p className="text-sm text-red-500">{form.formState.errors.name.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="email">이메일</Label>
            <div className="relative">
              <Mail className="absolute left-3 top-3 h-5 w-5 text-slate-400" />
              <Input
                id="email"
                type="email"
                placeholder="example@email.com"
                {...form.register('email')}
                className={`pl-10 h-12 text-lg ${form.formState.errors.email ? 'border-red-500' : ''}`}
              />
            </div>
            {form.formState.errors.email && (
              <p className="text-sm text-red-500">{form.formState.errors.email.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="phone">전화번호</Label>
            <div className="relative">
              <Phone className="absolute left-3 top-3 h-5 w-5 text-slate-400" />
              <Input
                id="phone"
                placeholder="010-1234-5678"
                {...form.register('phone')}
                className={`pl-10 h-12 text-lg ${form.formState.errors.phone ? 'border-red-500' : ''}`}
              />
            </div>
            {form.formState.errors.phone && (
              <p className="text-sm text-red-500">{form.formState.errors.phone.message}</p>
            )}
          </div>

          <div className="pt-4">
            <Button
              type="submit"
              className="w-full h-12 text-base"
              disabled={isSubmitting}
            >
              {isSubmitting ? (
                <>
                  <Loader2 className="mr-2 h-5 w-5 animate-spin" />
                  저장 중...
                </>
              ) : (
                '다음'
              )}
            </Button>
          </div>
        </form>
      </CardContent>
    </div>
  );
}
