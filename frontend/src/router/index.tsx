import { createBrowserRouter, Outlet } from 'react-router-dom';
import App from '../App';
import LandingPage from '../pages/LandingPage';
import DashboardLayout from '../layouts/DashboardLayout';
import DashboardPage from '../pages/DashboardPage';
import LoginPage from '../pages/LoginPage';
import OAuthCallbackPage from '../pages/OAuthCallbackPage';
import RoleSelectionPage from '../pages/onboarding/RoleSelectionPage';
import CreateDojangPage from '../pages/onboarding/CreateDojangPage';
import SearchDojangPage from '../pages/onboarding/SearchDojangPage';
import UserInfoPage from '../pages/onboarding/UserInfoPage';
import SettingsPage from '../pages/SettingsPage';
import { OnboardingRoute, CompletedOnboardingRoute } from './AuthGuard';
import AuthLayout from '../layouts/AuthLayout';
import { AuthProvider } from '../contexts/AuthContext';

const AuthenticatedLayout = ({ children }: { children: React.ReactNode }) => (
  <AuthProvider>{children}</AuthProvider>
);

export const router = createBrowserRouter([
  {
    element: <App />,
    children: [
      { path: '/', element: <LandingPage /> },
      { path: '/oauth/callback/:provider', element: <OAuthCallbackPage /> },
      {
        element: <AuthLayout />,
        children: [
          { path: '/login', element: <LoginPage /> },
          {
            path: '/onboarding',
            element: (
              <AuthenticatedLayout>
                <OnboardingRoute>
                  <Outlet />
                </OnboardingRoute>
              </AuthenticatedLayout>
            ),
            children: [
              { path: 'user-info', element: <UserInfoPage /> },
              { path: 'role', element: <RoleSelectionPage /> },
              { path: 'create-dojang', element: <CreateDojangPage /> },
              { path: 'search-dojang', element: <SearchDojangPage /> },
            ]
          }
        ]
      },
      {
        path: '/',
        element: (
          <AuthenticatedLayout>
            <CompletedOnboardingRoute>
              <DashboardLayout />
            </CompletedOnboardingRoute>
          </AuthenticatedLayout>
        ),
        children: [
          { path: 'dashboard', element: <DashboardPage /> },
          { path: 'settings', element: <SettingsPage /> },
        ],
      },
    ],
  },
]);
