import React, { createContext, useContext, useState, useEffect } from 'react';
import { User, AuthContextType } from '../types/auth';
import apiClient from '../services/api';

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const fetchUserInfo = async (): Promise<void> => {
    try {
      const response = await apiClient.get<User>('/auth/me');
      setUser(response.data);
      setIsAuthenticated(true);
    } catch (error) {
      console.error('사용자 정보 조회 실패:', error);
      setUser(null);
      setIsAuthenticated(false);
    }
  };

  const login = async (email: string, password: string): Promise<void> => {
    try {
      await apiClient.post('/auth/login', { email, password });
      await fetchUserInfo();
    } catch (error) {
      console.error('로그인 실패:', error);
      setUser(null);
      setIsAuthenticated(false);
      throw error;
    }
  };

  const logout = async (): Promise<void> => {
    try {
      await apiClient.post('/auth/logout');
    } catch (error) {
      console.error('로그아웃 API 호출 실패:', error);
    } finally {
      setUser(null);
      setIsAuthenticated(false);
    }
  };

  const refreshUser = async (): Promise<void> => {
    await fetchUserInfo();
  };

  useEffect(() => {
    const initializeAuth = async () => {
      setIsLoading(true);
      await fetchUserInfo();
      setIsLoading(false);
    };

    initializeAuth();
  }, [fetchUserInfo]);

  const value: AuthContextType = {
    user,
    isAuthenticated,
    isLoading,
    login,
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
