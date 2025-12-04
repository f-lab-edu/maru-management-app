import { createContext, useContext } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { User, AuthContextType } from '../types/auth';
import { userService } from '../services/userService';
import { authService } from '../services/authService';

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const queryClient = useQueryClient();

  const { data: user, isLoading } = useQuery({
    queryKey: ['user', 'me'],
    queryFn: userService.getMe,
    retry: false,
    staleTime: 1000 * 60 * 5,
    refetchInterval: (query) =>
      query.state.data?.onboardingStep === 'APPROVAL_WAIT' ? 5000 : false,
  });

  const isAuthenticated = !!user;

  const logout = async (): Promise<void> => {
    try {
      await authService.logout();
    } catch (error) {
      console.error('로그아웃 실패:', error);
    } finally {
      queryClient.setQueryData(['user', 'me'], null);
    }
  };

  const refreshUser = async (): Promise<void> => {
    await queryClient.invalidateQueries({ queryKey: ['user', 'me'] });
  };

  const value: AuthContextType = {
    user: user ?? null,
    isAuthenticated,
    isLoading,
    logout,
    refreshUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth는 AuthProvider 내부에서만 사용할 수 있습니다');
  }
  return context;
};
