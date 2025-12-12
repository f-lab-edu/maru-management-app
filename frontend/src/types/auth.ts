export type UserRole = 'OWNER' | 'INSTRUCTOR';

export type OnboardingStep =
  | 'PROFILE_INPUT'
  | 'ROLE_SELECT'
  | 'DOJANG_INFO'
  | 'APPROVAL_WAIT'
  | 'COMPLETED';

export type OAuthProvider = 'GOOGLE' | 'KAKAO';

export interface User {
  id: number;
  name: string;
  email: string;
  phone?: string;
  profileImageUrl?: string;
  role?: UserRole;
  onboardingStep: OnboardingStep;
  oauthProvider: OAuthProvider;
  createdAt: string;
  lastLoginAt?: string;
}

export interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  logout: () => Promise<void>;
  refreshUser: () => Promise<void>;
}
