import { Link, useLocation } from 'react-router-dom';
import { LayoutDashboard, Users, CalendarCheck, CreditCard, Settings, MessageSquare } from 'lucide-react';
import { cn } from '../shared/utils';
import { Button } from '../shared/components/ui/button';
import { usePermissions } from '../hooks/usePermissions';
import { getMenuRequiredPermissions } from '../constants/menuPermissions';

interface SidebarProps {
  isOpen: boolean;
  onClose: () => void;
}

export function Sidebar({ isOpen, onClose }: SidebarProps) {
  const location = useLocation();
  const { hasAnyPermission } = usePermissions();

  const menuItems = [
    { icon: LayoutDashboard, label: '대시보드', path: '/dashboard' },
    { icon: Users, label: '원생 관리', path: '/students' },
    { icon: CalendarCheck, label: '출석 관리', path: '/attendance' },
    { icon: CreditCard, label: '수납 관리', path: '/billing' },
    { icon: Settings, label: '설정', path: '/settings' },
  ];

  const visibleMenuItems = menuItems.filter((item) => {
    const requiredPermissions = getMenuRequiredPermissions(item.path);
    return hasAnyPermission(requiredPermissions);
  });

  return (
    <aside
      className={cn(
        "fixed inset-y-0 left-0 z-50 w-64 bg-slate-900 text-white transition-transform duration-300 ease-in-out lg:translate-x-0 lg:static lg:inset-0 flex flex-col",
        !isOpen && "-translate-x-full"
      )}
    >
      <div className="h-16 flex items-center px-6 border-b border-slate-800">
        <span className="text-xl font-bold">MARU</span>
      </div>

      <div className="flex-1 p-4 space-y-2 overflow-y-auto">
        {visibleMenuItems.map((item) => (
          <Link
            key={item.path}
            to={item.path}
            className={cn(
              "flex items-center gap-3 px-4 py-3 rounded-lg transition-colors",
              location.pathname === item.path
                ? "bg-primary text-primary-foreground"
                : "text-slate-400 hover:text-white hover:bg-slate-800"
            )}
            onClick={onClose}
          >
            <item.icon className="h-5 w-5" />
            <span>{item.label}</span>
          </Link>
        ))}
      </div>

      <div className="p-4 border-t border-slate-800">
        <div className="bg-slate-800 rounded-xl p-4 cursor-pointer hover:bg-slate-700 transition-colors group">
          <div className="flex items-center gap-3 mb-2">
            <div className="h-8 w-8 rounded-full bg-primary/20 flex items-center justify-center text-primary">
              <MessageSquare className="h-4 w-4" />
            </div>
            <div>
              <p className="text-sm font-medium text-white">Support 24/7</p>
              <p className="text-xs text-slate-400">Contact us anytime</p>
            </div>
          </div>
          <Button className="w-full text-xs h-8 bg-primary hover:bg-primary/90" size="sm">
            Chat Now
          </Button>
        </div>
      </div>
    </aside>
  );
}
