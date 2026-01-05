import { Loader2 } from 'lucide-react';
import { useUIStore } from '../stores/uiStore';

export function GlobalLoadingOverlay() {
  const { isLoading, message } = useUIStore((state) => state.globalLoading);

  if (!isLoading) {
    return null;
  }

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center bg-white/80 backdrop-blur-sm">
      <div className="flex flex-col items-center gap-4">
        <Loader2 className="h-10 w-10 animate-spin text-primary" />
        <p className="text-sm font-medium text-slate-600">{message}</p>
      </div>
    </div>
  );
}
