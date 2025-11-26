import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { OnboardingStep } from '../types/auth';

const getOnboardingPath = (step: OnboardingStep): string => {
  switch (step) {
    case 'PROFILE_INPUT':
      return '/onboarding/user-info';
    case 'ROLE_SELECT':
      return '/onboarding/role';
    case 'DOJANG_INFO':
      return '/onboarding/create-dojang';
    case 'APPROVAL_WAIT':
      return '/onboarding/search-dojang';
    case 'COMPLETED':
      return '/dashboard';
    default:
      return '/onboarding/user-info';
  }
};

export function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { user, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return <>{children}</>;
}

export function PublicRoute({ children }: { children: React.ReactNode }) {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      </div>
    );
  }

  if (user) {
    const redirectPath = getOnboardingPath(user.onboardingStep);
    return <Navigate to={redirectPath} replace />;
  }

  return <>{children}</>;
}

export function OnboardingRoute({ children }: { children: React.ReactNode }) {
  const { user, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (user.onboardingStep === 'COMPLETED') {
    return <Navigate to="/dashboard" replace />;
  }

  return <>{children}</>;
}

export function CompletedOnboardingRoute({ children }: { children: React.ReactNode }) {
  const { user, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (user.onboardingStep !== 'COMPLETED') {
    const redirectPath = getOnboardingPath(user.onboardingStep);
    return <Navigate to={redirectPath} replace />;
  }

  return <>{children}</>;
}
