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
import { Loader2, MapPin, Phone, Store } from 'lucide-react';
import { OnboardingBackButton } from './components/OnboardingBackButton';

const createDojangSchema = z.object({
  name: z.string().min(2, '도장 이름은 2글자 이상이어야 합니다').max(100, '도장 이름은 100글자 이하여야 합니다'),
  address: z.string().min(1, '주소를 입력해주세요'),
  phone: z.string().min(1, '연락처를 입력해주세요'),
});

type CreateDojangForm = z.infer<typeof createDojangSchema>;

export default function CreateDojangPage() {
  const navigate = useNavigate();
  const { refreshUser } = useAuth();
  const [isLoading, setIsLoading] = useState(false);

  const { register, handleSubmit, formState: { errors } } = useForm<CreateDojangForm>({
    resolver: zodResolver(createDojangSchema),
  });

  const onSubmit = async (data: CreateDojangForm) => {
    setIsLoading(true);
    try {
      await userService.createDojang(data);
      await refreshUser();
      navigate('/dashboard');
    } catch (error) {
      console.error('도장 생성 실패:', error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="animate-in fade-in slide-in-from-right-8 duration-500 relative">
      <OnboardingBackButton onClick={() => navigate('/onboarding/role')} />
      <CardHeader className="pb-8 pt-10">
        <CardTitle className="text-3xl font-bold text-slate-900">도장 개설하기</CardTitle>
        <CardDescription className="text-lg mt-2">
          관장님의 도장 정보를 입력해주세요.
        </CardDescription>
      </CardHeader>

      <CardContent className="px-8 pb-10">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
          <div className="space-y-2">
            <Label htmlFor="name" className="text-slate-600">도장 이름</Label>
            <div className="relative">
              <Store className="absolute left-3 top-3 h-5 w-5 text-slate-400" />
              <Input
                id="name"
                placeholder="예: 청룡 태권도"
                className={`pl-10 h-12 text-lg ${errors.name ? 'border-red-500' : ''}`}
                {...register('name')}
              />
            </div>
            {errors.name && <p className="text-sm text-red-500">{errors.name.message}</p>}
          </div>

          <div className="space-y-2">
            <Label htmlFor="address" className="text-slate-600">주소</Label>
            <div className="relative">
              <MapPin className="absolute left-3 top-3 h-5 w-5 text-slate-400" />
              <Input
                id="address"
                placeholder="서울시 강남구..."
                className={`pl-10 h-12 text-lg ${errors.address ? 'border-red-500' : ''}`}
                {...register('address')}
              />
            </div>
            {errors.address && <p className="text-sm text-red-500">{errors.address.message}</p>}
          </div>

          <div className="space-y-2">
            <Label htmlFor="phone" className="text-slate-600">연락처</Label>
            <div className="relative">
              <Phone className="absolute left-3 top-3 h-5 w-5 text-slate-400" />
              <Input
                id="phone"
                placeholder="02-1234-5678"
                className={`pl-10 h-12 text-lg ${errors.phone ? 'border-red-500' : ''}`}
                {...register('phone')}
              />
            </div>
            {errors.phone && <p className="text-sm text-red-500">{errors.phone.message}</p>}
          </div>

          <Button type="submit" className="w-full h-12 text-lg mt-4" disabled={isLoading}>
            {isLoading ? (
              <>
                <Loader2 className="mr-2 h-5 w-5 animate-spin" />
                도장 개설 중...
              </>
            ) : (
              '도장 개설 완료'
            )}
          </Button>
        </form>
      </CardContent>
    </div>
  );
}
