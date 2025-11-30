import { useAuth } from '../../contexts/AuthContext';
import { userService } from '../../services/userService';
import { CardHeader, CardTitle, CardDescription, CardContent } from '../../shared/components/ui/card';
import { Clock, ArrowLeft } from 'lucide-react';
import { OnboardingBackButton } from './components/OnboardingBackButton';
import { Button } from '../../shared/components/ui/button';

export default function SearchDojangPage() {
  const { refreshUser } = useAuth();

  const handlePrevious = async () => {
    try {
      await userService.rollbackOnboardingStep();
      await refreshUser();
    } catch (error) {
      console.error('이전 단계 이동 실패:', error);
    }
  };

  return (
    <div className="animate-in fade-in slide-in-from-right-8 duration-500">
      <OnboardingBackButton onClick={handlePrevious} />
      <CardHeader className="pb-8 pt-2">
        <CardTitle className="text-3xl font-bold text-slate-900">도장 찾기</CardTitle>
        <CardDescription className="text-lg mt-2">
          소속된 도장을 검색하여 가입을 요청하세요.
        </CardDescription>
      </CardHeader>

      <CardContent className="px-8 pb-10 space-y-6">
        <div className="text-center py-16 bg-slate-50 rounded-xl border border-dashed border-slate-200">
          <Clock className="w-16 h-16 mx-auto mb-4 text-slate-300" />
          <h3 className="text-xl font-semibold text-slate-700 mb-2">준비 중입니다</h3>
          <p className="text-slate-500 mb-6">
            사범님 가입 기능은 현재 개발 중입니다.<br />
            빠른 시일 내에 서비스를 제공할 예정입니다.
          </p>
          <Button
            variant="outline"
            onClick={handlePrevious}
            className="gap-2"
          >
            <ArrowLeft className="w-4 h-4" />
            역할 선택으로 돌아가기
          </Button>
        </div>
      </CardContent>
    </div>
  );
}
