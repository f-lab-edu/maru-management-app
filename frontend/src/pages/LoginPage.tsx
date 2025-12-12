import { useState } from 'react';
import { authService, OAuthProvider } from '../services/authService';
import { Button } from '../shared/components/ui/button';
import { CardHeader, CardTitle, CardDescription, CardContent } from '../shared/components/ui/card';
import { Loader2 } from 'lucide-react';

export default function LoginPage() {
  const [isLoading, setIsLoading] = useState<OAuthProvider | null>(null);

  const handleLogin = async (provider: OAuthProvider) => {
    setIsLoading(provider);
    try {
      const authUrl = await authService.getOAuthUrl(provider);
      window.location.href = authUrl;
    } catch (error) {
      console.error('OAuth URL 조회 실패:', error);
      setIsLoading(null);
    }
  };

  return (
    <div className="animate-in fade-in zoom-in duration-300">
      <CardHeader className="text-center space-y-4 pb-8 pt-12">
        <div className="mx-auto w-16 h-16 rounded-2xl bg-primary flex items-center justify-center mb-4 shadow-lg">
          <span className="text-3xl font-bold text-white">M</span>
        </div>
        <CardTitle className="text-3xl font-bold tracking-tight text-slate-900">MARU 로그인</CardTitle>
        <CardDescription className="text-slate-500 text-lg">
          스마트한 도장 관리의 시작
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4 px-8 pb-12">
        <Button
          className="w-full h-14 bg-white text-slate-700 border border-slate-200 hover:bg-slate-50 font-medium text-base relative shadow-sm transition-all hover:shadow-md"
          onClick={() => handleLogin('google')}
          disabled={isLoading !== null}
        >
          {isLoading === 'google' ? (
            <Loader2 className="w-6 h-6 animate-spin" />
          ) : (
            <>
              <svg className="w-6 h-6 absolute left-4" viewBox="0 0 24 24">
                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
                <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" />
                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" />
              </svg>
              Google 계정으로 계속하기
            </>
          )}
        </Button>

        <Button
          className="w-full h-14 bg-[#FEE500] text-[#191919] hover:bg-[#FEE500]/90 font-medium text-base relative shadow-sm transition-all hover:shadow-md border border-[#FEE500]"
          onClick={() => handleLogin('kakao')}
          disabled={isLoading !== null}
        >
          {isLoading === 'kakao' ? (
            <Loader2 className="w-6 h-6 animate-spin" />
          ) : (
            <>
              <svg className="w-6 h-6 absolute left-4" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 3C5.925 3 1 6.925 1 11.775c0 2.9 1.75 5.5 4.625 7.125-.2.75-.725 2.725-.825 3.125-.125.45.175.45.375.3 2.575-1.75 3.6-2.45 4.325-2.95.5.075 1.025.125 1.525.125 6.075 0 11-3.925 11-8.775S17.075 3 12 3z"/>
              </svg>
              카카오 계정으로 계속하기
            </>
          )}
        </Button>
      </CardContent>
    </div>
  );
}
