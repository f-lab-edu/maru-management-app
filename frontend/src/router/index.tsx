import { createBrowserRouter } from 'react-router-dom';
import App from '../App';
import HomePage from '../pages/HomePage';
// import { ProtectedRoute } from '../components/auth/ProtectedRoute';

export const router = createBrowserRouter([
  {
    element: <App />,
    children: [
      { path: '/', element: <HomePage /> },
      // 로그인 페이지 예시 (추후 구현)
      // { path: '/login', element: <LoginPage /> },

      // ProtectedRoute 사용 예시:
      // 인증이 필요한 페이지는 ProtectedRoute로 감싸기
      // {
      //   path: '/dashboard',
      //   element: (
      //     <ProtectedRoute>
      //       <DashboardPage />
      //     </ProtectedRoute>
      //   ),
      // },
      // {
      //   path: '/students',
      //   element: (
      //     <ProtectedRoute>
      //       <StudentsPage />
      //     </ProtectedRoute>
      //   ),
      // },
    ],
  },
]);
