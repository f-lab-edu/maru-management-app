import { Building2, ChevronDown, Check } from 'lucide-react';
import { useMyDojangs, useSelectDojang } from '../../hooks';
import { useAuthStore } from '../../stores/authStore';
import { useUIStore } from '../../stores/uiStore';
import { Button } from '../../shared/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '../../shared/components/ui/dropdown-menu';

export function DojangSwitcher() {
  const { data: dojangsData } = useMyDojangs();
  const selectDojangMutation = useSelectDojang();
  const selectedDojang = useAuthStore((state) => state.selectedDojang);
  const showLoading = useUIStore((state) => state.showLoading);
  const hideLoading = useUIStore((state) => state.hideLoading);

  const dojangs = dojangsData?.dojangs ?? [];
  const hasMultipleDojangs = dojangs.length > 1;

  if (!selectedDojang) {
    return null;
  }

  // 도장 1개: 이름만 표시
  if (!hasMultipleDojangs) {
    return (
      <div className="flex items-center gap-2 px-3 py-1.5 text-sm font-medium text-slate-700">
        <Building2 className="h-4 w-4 text-slate-500" />
        <span>{selectedDojang.dojangName}</span>
      </div>
    );
  }

  // 도장 2개 이상: 드롭다운
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button
          variant="ghost"
          className="flex items-center gap-2 px-3 h-9 text-sm font-medium"
        >
          <Building2 className="h-4 w-4 text-slate-500" />
          <span>{selectedDojang.dojangName}</span>
          <ChevronDown className="h-4 w-4 text-slate-400" />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="start" className="w-56">
        {dojangs.map((dojang) => {
          const isSelected = dojang.dojangId === selectedDojang.dojangId;
          const roleLabel = dojang.role === 'OWNER' ? '관장' : '사범';

          return (
            <DropdownMenuItem
              key={dojang.dojangId}
              onClick={async () => {
                if (!isSelected) {
                  showLoading(`${dojang.dojangName} 도장으로 안전하게 정보를 변경하고 있습니다.`);
                  // TODO: E2E 테스트용 딜레이 - 테스트 후 제거
                  await new Promise((r) => setTimeout(r, 3000));
                  selectDojangMutation.mutate(dojang, {
                    onSettled: () => hideLoading(),
                  });
                }
              }}
              className="flex items-center justify-between cursor-pointer"
            >
              <div className="flex flex-col">
                <span className="font-medium">{dojang.dojangName}</span>
                <span className="text-xs text-slate-500">{roleLabel}</span>
              </div>
              {isSelected && <Check className="h-4 w-4 text-primary" />}
            </DropdownMenuItem>
          );
        })}
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
