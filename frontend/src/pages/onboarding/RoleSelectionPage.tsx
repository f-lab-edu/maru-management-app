import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { userService } from '../../services/userService';
import { UserRole } from '../../types/auth';
import { CardHeader, CardTitle, CardDescription, CardContent } from '../../shared/components/ui/card';
import { Button } from '../../shared/components/ui/button';
import { UserCog, GraduationCap, Check, Loader2 } from 'lucide-react';
import { cn } from '../../shared/utils';
import { OnboardingBackButton } from './components/OnboardingBackButton';

export default function RoleSelectionPage() {
  const navigate = useNavigate();
  const { refreshUser } = useAuth();
  const [selectedRole, setSelectedRole] = useState<UserRole | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleContinue = async () => {
    if (!selectedRole) return;

    setIsSubmitting(true);
    try {
      await userService.selectRole(selectedRole);
      await refreshUser();

      if (selectedRole === 'OWNER') {
        navigate('/onboarding/create-dojang');
      } else {
        navigate('/onboarding/search-dojang');
      }
    } catch (error) {
      console.error('역할 선택 실패:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handlePrevious = () => {
    navigate('/onboarding/user-info');
  };

  return (
    <div className="animate-in fade-in slide-in-from-right-8 duration-500 relative">
      <OnboardingBackButton onClick={handlePrevious} />
      <CardHeader className="text-center pb-8">
        <CardTitle className="text-3xl font-bold text-slate-900">환영합니다!</CardTitle>
        <CardDescription className="text-lg mt-2">
          MARU를 어떤 용도로 사용하시나요?
        </CardDescription>
      </CardHeader>

      <CardContent className="px-8 pb-10">
        <div className="grid md:grid-cols-2 gap-6 mb-8 max-w-3xl mx-auto">
          <div
            className={cn(
              "relative cursor-pointer rounded-xl border-2 p-6 transition-all duration-200 hover:border-primary/50 hover:bg-slate-50 flex flex-col items-center justify-center",
              selectedRole === 'OWNER' ? "border-primary bg-primary/5 ring-2 ring-primary/20" : "border-slate-100"
            )}
            onClick={() => setSelectedRole('OWNER')}
          >
            {selectedRole === 'OWNER' && (
              <div className="absolute top-4 right-4 text-primary">
                <Check className="w-6 h-6" />
              </div>
            )}
            <div className={cn(
              "w-20 h-20 rounded-full flex items-center justify-center mb-4 transition-colors",
              selectedRole === 'OWNER' ? "bg-primary text-white" : "bg-slate-100 text-slate-500"
            )}>
              <UserCog className="w-12 h-12" />
            </div>
            <h3 className="text-xl font-bold text-slate-900 mb-2">관장님</h3>
            <p className="text-sm text-slate-500 leading-relaxed text-center">
              도장을 개설하고 원생/수납/출결 등<br/>
              전반적인 운영 관리를 합니다.
            </p>
          </div>

          <div
            className={cn(
              "relative cursor-pointer rounded-xl border-2 p-6 transition-all duration-200 hover:border-primary/50 hover:bg-slate-50 flex flex-col items-center justify-center",
              selectedRole === 'INSTRUCTOR' ? "border-primary bg-primary/5 ring-2 ring-primary/20" : "border-slate-100"
            )}
            onClick={() => setSelectedRole('INSTRUCTOR')}
          >
            {selectedRole === 'INSTRUCTOR' && (
              <div className="absolute top-4 right-4 text-primary">
                <Check className="w-6 h-6" />
              </div>
            )}
            <div className={cn(
              "w-20 h-20 rounded-full flex items-center justify-center mb-4 transition-colors",
              selectedRole === 'INSTRUCTOR' ? "bg-primary text-white" : "bg-slate-100 text-slate-500"
            )}>
              <GraduationCap className="w-12 h-12" />
            </div>
            <h3 className="text-xl font-bold text-slate-900 mb-2">사범님</h3>
            <p className="text-sm text-slate-500 leading-relaxed text-center">
              소속 도장에 가입하여 원생 지도 및<br/>
              담당 수련반을 관리합니다.
            </p>
          </div>
        </div>

        <Button
          className="w-full h-12 text-lg"
          size="lg"
          disabled={!selectedRole || isSubmitting}
          onClick={handleContinue}
        >
          {isSubmitting ? (
            <>
              <Loader2 className="mr-2 h-5 w-5 animate-spin" />
              처리 중...
            </>
          ) : (
            '다음으로 계속하기'
          )}
        </Button>
      </CardContent>
    </div>
  );
}
