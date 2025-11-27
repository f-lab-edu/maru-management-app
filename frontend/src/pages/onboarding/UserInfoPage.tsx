import { useState, useEffect, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { useAuth } from '../../contexts/AuthContext';
import { userService } from '../../services/userService';
import { smsService } from '../../services/smsService';
import { Button } from '../../shared/components/ui/button';
import { Input } from '../../shared/components/ui/input';
import { Label } from '../../shared/components/ui/label';
import { CardHeader, CardTitle, CardDescription, CardContent } from '../../shared/components/ui/card';
import { OnboardingBackButton } from './components/OnboardingBackButton';
import { User, Mail, Phone, Loader2, CheckCircle2, KeyRound } from 'lucide-react';
import { AxiosError } from 'axios';
import { ErrorResponse } from '../../services/api';

const userInfoSchema = z.object({
  name: z.string().min(2, { message: '이름은 2글자 이상이어야 합니다.' }),
  email: z.string().email({ message: '올바른 이메일 형식이 아닙니다.' }),
  phone: z.string().regex(/^010-\d{4}-\d{4}$/, { message: '010-0000-0000 형식으로 입력해주세요.' }),
});

type UserInfoFormValues = z.infer<typeof userInfoSchema>;

const VERIFICATION_TTL = 180;
const RESEND_COOLDOWN = 60;

export default function UserInfoPage() {
  const navigate = useNavigate();
  const { user, logout, refreshUser } = useAuth();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [verificationCode, setVerificationCode] = useState('');
  const [isPhoneVerified, setIsPhoneVerified] = useState(false);
  const [isSendingCode, setIsSendingCode] = useState(false);
  const [isVerifying, setIsVerifying] = useState(false);
  const [codeSent, setCodeSent] = useState(false);
  const [timer, setTimer] = useState(0);
  const [resendTimer, setResendTimer] = useState(0);
  const [verificationError, setVerificationError] = useState('');
  const [verifiedPhone, setVerifiedPhone] = useState('');
  const otpRef = useRef<HTMLInputElement>(null);

  const form = useForm<UserInfoFormValues>({
    resolver: zodResolver(userInfoSchema),
    values: {
      name: user?.name || '',
      email: user?.email || '',
      phone: user?.phone || '',
    },
  });

  const phoneValue = form.watch('phone');

  useEffect(() => {
    if (timer > 0) {
      const interval = setInterval(() => setTimer((t) => t - 1), 1000);
      return () => clearInterval(interval);
    }
  }, [timer]);

  useEffect(() => {
    if (resendTimer > 0) {
      const interval = setInterval(() => setResendTimer((t) => t - 1), 1000);
      return () => clearInterval(interval);
    }
  }, [resendTimer]);

  useEffect(() => {
    if (isPhoneVerified && phoneValue !== verifiedPhone) {
      setIsPhoneVerified(false);
      setVerifiedPhone('');
      setCodeSent(false);
      setVerificationCode('');
      setTimer(0);
    }
  }, [phoneValue, isPhoneVerified, verifiedPhone]);

  const formatTime = useCallback((seconds: number) => {
    const min = Math.floor(seconds / 60);
    const sec = seconds % 60;
    return `${min}:${sec.toString().padStart(2, '0')}`;
  }, []);

  const handleSendCode = async () => {
    const phoneValid = await form.trigger('phone');
    if (!phoneValid) return;

    setIsSendingCode(true);
    setVerificationError('');
    setVerificationCode('');

    try {
      const response = await smsService.sendVerificationCode(phoneValue);
      setCodeSent(true);
      setTimer(response.expiresIn || VERIFICATION_TTL);
      setResendTimer(RESEND_COOLDOWN);
      setTimeout(() => otpRef.current?.focus(), 100);
    } catch (error) {
      const axiosError = error as AxiosError<ErrorResponse>;
      const message = axiosError.response?.data?.message || '인증번호 발송에 실패했습니다.';
      setVerificationError(message);
    } finally {
      setIsSendingCode(false);
    }
  };

  const handleVerifyCode = useCallback(async (code: string) => {
    if (code.length !== 6 || isVerifying || timer === 0) return;

    setIsVerifying(true);
    setVerificationError('');

    try {
      const response = await smsService.verifyCode(phoneValue, code);
      if (response.verified) {
        setIsPhoneVerified(true);
        setVerifiedPhone(phoneValue);
        setCodeSent(false);
        setTimer(0);
        otpRef.current?.blur();

        if (response.isExistingUser) {
          alert('기존 계정과 연결되었습니다. 다시 로그인해주세요.');
          await logout();
          navigate('/login');
          return;
        }
      }
    } catch (error) {
      const axiosError = error as AxiosError<ErrorResponse>;
      const message = axiosError.response?.data?.message || '인증에 실패했습니다.';
      setVerificationError(message);
      setVerificationCode('');
    } finally {
      setIsVerifying(false);
    }
  }, [phoneValue, isVerifying, timer, logout, navigate]);

  const onSubmit = async (data: UserInfoFormValues) => {
    if (!isPhoneVerified) {
      setVerificationError('전화번호 인증이 필요합니다.');
      return;
    }

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

  const isPhoneValid = /^010-\d{4}-\d{4}$/.test(phoneValue);

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
            <div className="flex gap-2">
              <div className="relative flex-1">
                <Phone className="absolute left-3 top-3 h-5 w-5 text-slate-400" />
                <Input
                  id="phone"
                  placeholder="010-1234-5678"
                  {...form.register('phone')}
                  disabled={isPhoneVerified}
                  className={`pl-10 h-12 text-lg ${form.formState.errors.phone ? 'border-red-500' : ''} ${isPhoneVerified ? 'bg-green-50 border-green-500' : ''}`}
                />
                {isPhoneVerified && (
                  <CheckCircle2 className="absolute right-3 top-3 h-5 w-5 text-green-500" />
                )}
              </div>
              {!isPhoneVerified && (
                <Button
                  type="button"
                  variant={codeSent ? 'outline' : 'default'}
                  className="h-12 px-4 whitespace-nowrap min-w-[100px]"
                  onClick={handleSendCode}
                  disabled={!isPhoneValid || isSendingCode || (codeSent && resendTimer > 0)}
                >
                  {isSendingCode ? (
                    <Loader2 className="h-4 w-4 animate-spin" />
                  ) : codeSent ? (
                    '재발송'
                  ) : (
                    '인증 요청'
                  )}
                </Button>
              )}
            </div>
            {form.formState.errors.phone && (
              <p className="text-sm text-red-500">{form.formState.errors.phone.message}</p>
            )}
            {isPhoneVerified && (
              <p className="text-sm text-green-600">전화번호가 인증되었습니다.</p>
            )}
            
          </div>

          {codeSent && !isPhoneVerified && (
            <div className="space-y-2 animate-in fade-in slide-in-from-top-4 duration-300">
              <Label htmlFor="verificationCode">인증번호</Label>
              <div className="relative">
                <KeyRound className="absolute left-3 top-3 h-5 w-5 text-slate-400" />
                <Input
                  ref={otpRef}
                  id="verificationCode"
                  placeholder="6자리 인증번호"
                  value={verificationCode}
                  onChange={(e) => {
                    const value = e.target.value.replace(/\D/g, '').slice(0, 6);
                    setVerificationCode(value);
                    setVerificationError('');
                    if (value.length === 6) {
                      handleVerifyCode(value);
                    }
                  }}
                  disabled={isVerifying || timer === 0}
                  className="pl-10 h-12 text-lg"
                  maxLength={6}
                />
                {timer > 0 && (
                  <span className="absolute right-3 top-3 text-sm text-orange-500 font-medium">
                    {formatTime(timer)}
                  </span>
                )}
              </div>
              {isVerifying && (
                <p className="text-sm text-slate-500">인증 확인 중...</p>
              )}
              {timer === 0 && (
                <p className="text-sm text-red-500">인증번호가 만료되었습니다. 다시 요청해주세요.</p>
              )}
            </div>
          )}

          {verificationError && (
            <p className="text-sm text-red-500">{verificationError}</p>
          )}

          <div className="pt-4">
            <Button
              type="submit"
              className="w-full h-12 text-base"
              disabled={isSubmitting || !isPhoneVerified}
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
