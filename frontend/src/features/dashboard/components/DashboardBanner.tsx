import { useAuth } from '../../../contexts/AuthContext';

interface DashboardBannerProps {
  activeStudents: number;
}

export function DashboardBanner({ activeStudents }: DashboardBannerProps) {
  const { user } = useAuth();

  return (
    <div className="bg-slate-900 rounded-xl p-6 text-white relative overflow-hidden shrink-0">
      <div className="relative z-10">
        <h2 className="text-2xl font-bold mb-2">환영합니다, {user?.name || '관장'}님!</h2>
        <p className="text-slate-300">
          오늘도 활기찬 도장 운영을 응원합니다.
          현재 <span className="text-secondary font-bold">{activeStudents}명</span>의 원생이 수련 중입니다.
        </p>
      </div>
      <div className="absolute right-0 top-0 h-full w-1/3 bg-gradient-to-l from-primary/20 to-transparent" />
    </div>
  );
}
