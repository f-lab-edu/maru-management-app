import { Outlet } from 'react-router-dom';
import { DevLoginModal } from './features/dev';
import { GlobalLoadingOverlay } from './components/GlobalLoadingOverlay';

// TODO: 테스트용. 프로덕션에서는 반드시 삭제할 것.
const isDev = import.meta.env.DEV;

function App() {
  return (
    <div className="min-h-screen bg-background">
      <Outlet />
      {isDev && <DevLoginModal />}
      <GlobalLoadingOverlay />
    </div>
  );
}

export default App;
