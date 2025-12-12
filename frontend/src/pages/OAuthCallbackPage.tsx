import { useEffect, useRef } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { authService, OAuthProvider } from '../services/authService';
import { Loader2 } from 'lucide-react';

export default function OAuthCallbackPage() {
  const navigate = useNavigate();
  const { provider } = useParams<{ provider: string }>();
  const [searchParams] = useSearchParams();
  const hasExecuted = useRef(false);

  const mutation = useMutation({
    mutationFn: ({ provider, code }: { provider: OAuthProvider; code: string }) =>
      authService.handleOAuthCallback(provider, code),
    onSuccess: () => {
      navigate('/onboarding/user-info', { replace: true });
    },
    onError: (error) => {
      console.error('OAuth 콜백 처리 실패:', error);
    },
  });

  useEffect(() => {
    if (hasExecuted.current) return;
    hasExecuted.current = true;

    const code = searchParams.get('code');

    if (!code || !provider || !['google', 'kakao'].includes(provider)) {
      return;
    }

    mutation.mutate({ provider: provider as OAuthProvider, code });
  }, []);

  const getErrorMessage = () => {
    const code = searchParams.get('code');
    if (!code) return '인가 코드가 없습니다.';
    if (!provider || !['google', 'kakao'].includes(provider)) {
      return '유효하지 않은 OAuth 제공자입니다.';
    }
    return '로그인 처리 중 오류가 발생했습니다.';
  };

  const showError = mutation.isError || !searchParams.get('code') ||
    !provider || !['google', 'kakao'].includes(provider);

  if (showError) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50">
        <div className="text-center">
          <p className="text-red-500 mb-4">{getErrorMessage()}</p>
          <button
            onClick={() => navigate('/login')}
            className="text-primary hover:underline"
          >
            로그인 페이지로 돌아가기
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50">
      <div className="text-center">
        <Loader2 className="w-8 h-8 animate-spin mx-auto mb-4 text-primary" />
        <p className="text-slate-600">로그인 처리 중...</p>
      </div>
    </div>
  );
}
