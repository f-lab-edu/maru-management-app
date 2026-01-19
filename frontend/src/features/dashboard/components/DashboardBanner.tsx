import { useUser } from '../../../hooks';
import { useAuthStore } from '../../../stores/authStore';

interface DashboardBannerProps {
  activeStudents: number;
}

export function DashboardBanner({ activeStudents }: DashboardBannerProps) {
  const { data: user } = useUser();
  const selectedDojang = useAuthStore((state) => state.selectedDojang);

  const roleLabel = selectedDojang?.role === 'OWNER' ? '관장' : '사범';

  return (
    <div className="bg-slate-900 rounded-xl p-6 text-white relative overflow-hidden shrink-0">
      <div className="relative z-10">
        <h2 className="text-2xl font-bold mb-2">
          환영합니다, {user?.name || ''} <span className="px-2 py-0.5 rounded bg-amber-500/20 text-amber-400 font-semibold">{roleLabel}</span> 님!
        </h2>
        <p className="text-slate-300">
          오늘도 활기찬 도장 운영을 응원합니다.
          현재 <span className="text-secondary font-bold">{activeStudents}명</span>의 원생이 수련 중입니다.
        </p>
      </div>
      <div className="absolute right-0 top-0 h-full w-1/3 bg-gradient-to-l from-primary/20 to-transparent" />
    </div>
  );
}
