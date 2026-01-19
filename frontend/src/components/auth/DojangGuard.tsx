import { useEffect, useRef, type ReactNode } from 'react';
import { Loader2 } from 'lucide-react';
import { useUser, useMyDojangs, useSelectDojang } from '../../hooks';
import { useAuthStore } from '../../stores/authStore';
import { useUIStore } from '../../stores/uiStore';

interface DojangGuardProps {
  children: ReactNode;
}

export function DojangGuard({ children }: DojangGuardProps) {
  const { data: user } = useUser();
  const { data: dojangs, isLoading: isLoadingDojangs } = useMyDojangs();
  const { mutate: selectDojang, isPending: isSelectingDojang } = useSelectDojang();
  const selectedDojang = useAuthStore((state) => state.selectedDojang);
  const getLastSelectedDojangId = useAuthStore((state) => state.getLastSelectedDojangId);
  const showLoading = useUIStore((state) => state.showLoading);
  const hideLoading = useUIStore((state) => state.hideLoading);

  const hasAutoSelected = useRef(false);

  const onboardingCompleted = user?.onboardingStep === 'COMPLETED';
  const availableDojangs = dojangs?.dojangs ?? [];

  useEffect(() => {
    if (
      !onboardingCompleted ||
      availableDojangs.length === 0 ||
      selectedDojang ||
      isSelectingDojang ||
      hasAutoSelected.current
    ) {
      return;
    }

    hasAutoSelected.current = true;

    const lastSelectedId = getLastSelectedDojangId(user?.id ?? null);
    const lastSelectedDojang = lastSelectedId
      ? availableDojangs.find((d) => d.dojangId === lastSelectedId)
      : null;

    const targetDojang = lastSelectedDojang ?? availableDojangs[0];

    showLoading(`${targetDojang.dojangName} 도장으로 접속 중입니다.`);
    selectDojang(targetDojang, {
      onSettled: () => hideLoading(),
    });
  }, [
    onboardingCompleted,
    availableDojangs,
    selectedDojang,
    isSelectingDojang,
    selectDojang,
    getLastSelectedDojangId,
    user?.id,
    showLoading,
    hideLoading,
  ]);

  // 도장 목록 로딩 중
  if (isLoadingDojangs) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50">
        <div className="flex flex-col items-center gap-3 text-slate-600">
          <Loader2 className="w-6 h-6 animate-spin text-primary" />
          <p className="text-sm font-medium">도장 정보를 불러오는 중입니다...</p>
        </div>
      </div>
    );
  }

  // 도장 자동 선택 진행 중 - GlobalLoadingOverlay가 App.tsx에서 표시됨
  if (onboardingCompleted && availableDojangs.length > 0 && !selectedDojang) {
    return null;
  }

  return <>{children}</>;
}
