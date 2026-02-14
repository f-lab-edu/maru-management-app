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
import MessagePage from '../pages/messages/MessagePage';
import StudentListPage from '../pages/students/StudentListPage';
import AttendanceListPage from '../pages/attendance/AttendanceListPage';
import BillingPage from '../pages/billing/BillingPage';
import PaymentPage from '../pages/payment/PaymentPage';
import PaymentSuccessPage from '../pages/payment/PaymentSuccessPage';
import PaymentFailPage from '../pages/payment/PaymentFailPage';
import { OnboardingRoute, CompletedOnboardingRoute } from './AuthGuard';
import AuthLayout from '../layouts/AuthLayout';
import { DojangGuard } from '../components/auth/DojangGuard';

export const router = createBrowserRouter([
  {
    element: <App />,
    children: [
      { path: '/', element: <LandingPage /> },
      { path: '/oauth/callback/:provider', element: <OAuthCallbackPage /> },
      { path: '/pay/:token', element: <PaymentPage /> },
      { path: '/pay/:token/success', element: <PaymentSuccessPage /> },
      { path: '/pay/:token/fail', element: <PaymentFailPage /> },
      {
        element: <AuthLayout />,
        children: [
          { path: '/login', element: <LoginPage /> },
          {
            path: '/onboarding',
            element: (
              <OnboardingRoute>
                <Outlet />
              </OnboardingRoute>
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
          <CompletedOnboardingRoute>
            <DojangGuard>
              <DashboardLayout />
            </DojangGuard>
          </CompletedOnboardingRoute>
        ),
        children: [
          { path: 'dashboard', element: <DashboardPage /> },
          { path: 'students', element: <StudentListPage /> },
          { path: 'attendance', element: <AttendanceListPage /> },
          { path: 'billing', element: <BillingPage /> },
          { path: 'messages', element: <MessagePage /> },
          { path: 'settings', element: <SettingsPage /> },
        ],
      },
    ],
  },
]);
