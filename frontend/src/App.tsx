import { Outlet } from 'react-router-dom';
import { TooltipProvider } from './shared/components/ui/tooltip';
import { DevLoginModal } from './features/dev';
import { GlobalLoadingOverlay } from './components/GlobalLoadingOverlay';

// TODO: 테스트용. 프로덕션에서는 반드시 삭제할 것.
const isDev = import.meta.env.DEV;

function App() {
  return (
    <TooltipProvider delayDuration={200}>
      <div className="min-h-screen bg-background">
        <Outlet />
        {isDev && <DevLoginModal />}
        <GlobalLoadingOverlay />
      </div>
    </TooltipProvider>
  );
}

export default App;
