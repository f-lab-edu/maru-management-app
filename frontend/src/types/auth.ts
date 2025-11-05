export interface User {
  id: string;
  email: string;
  name: string;
  phone?: string;
  role: 'OWNER' | 'INSTRUCTOR';
  onboardingStep?: 'ROLE_SELECT' | 'DOJO_INFO' | 'APPROVAL_WAIT' | 'COMPLETED';
}

export interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  refreshUser: () => Promise<void>;
}
