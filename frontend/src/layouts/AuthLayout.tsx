import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useRef, useState, useEffect } from 'react';
import { Card, CardContent } from '../shared/components/ui/card';
import { Button } from '../shared/components/ui/button';
import { ArrowLeft } from 'lucide-react';
import { cn } from '../shared/utils';

const HERO_IMAGE = 'https://images.unsplash.com/photo-1518611012118-696072aa579a?q=80&w=2000&auto=format&fit=crop';

export default function AuthLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const contentRef = useRef<HTMLDivElement>(null);
  const [height, setHeight] = useState<number | undefined>(undefined);

  const isOnboarding = location.pathname.includes('/onboarding');

  useEffect(() => {
    const el = contentRef.current;
    if (!el) return;

    const observer = new ResizeObserver((entries) => {
      const entry = entries[0];
      if (entry) {
        setHeight(entry.contentRect.height);
      }
    });

    observer.observe(el);
    return () => observer.disconnect();
  }, []);

  const handleBackToMain = () => {
    navigate('/');
  };

  return (
    <div className="min-h-screen flex items-center justify-center relative overflow-hidden bg-slate-900">
      <div
        className="absolute inset-0 z-0 bg-cover bg-center bg-no-repeat transition-opacity duration-1000"
        style={{
          backgroundImage: `url(${HERO_IMAGE})`,
          opacity: 0.6
        }}
      >
        <div className="absolute inset-0 bg-black/60 backdrop-blur-[2px]" />
      </div>

      <div className="absolute top-8 left-8 z-20">
        <Button
          variant="ghost"
          className="text-white hover:text-white hover:bg-white/20 gap-2 pl-2"
          onClick={handleBackToMain}
        >
          <ArrowLeft className="w-5 h-5" />
          메인으로 돌아가기
        </Button>
      </div>

      <div className={cn(
        "relative z-10 w-full px-4",
        isOnboarding ? "max-w-4xl" : "max-w-md"
      )}>
        <Card className="w-full bg-white/95 backdrop-blur-sm border-none shadow-2xl overflow-hidden">
          <CardContent
            className="p-0 transition-[height] duration-300 ease-out"
            style={{ height: height ? `${height}px` : 'auto' }}
          >
            <div ref={contentRef}>
              <Outlet />
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
