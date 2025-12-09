export type UserRole = 'OWNER' | 'INSTRUCTOR';

export type OnboardingStep =
  | 'PROFILE_INPUT'
  | 'ROLE_SELECT'
  | 'DOJANG_INFO'
  | 'APPROVAL_WAIT'
  | 'COMPLETED';

export type OAuthProvider = 'GOOGLE' | 'KAKAO';

export interface DojangSummary {
  dojangId: number;
  dojangName: string;
  tenantId: number;
  role: UserRole;
}

export interface MyDojangsRes {
  dojangs: DojangSummary[];
  count: number;
}

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

