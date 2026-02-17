import { Link, useLocation } from 'react-router-dom';
import { LayoutDashboard, Users, CalendarCheck, CreditCard, MessageSquare, Settings } from 'lucide-react';
import { cn } from '../shared/utils';
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
    { icon: MessageSquare, label: '메시지', path: '/messages' },
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
              location.pathname === item.path || location.pathname.startsWith(item.path + '/')
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

    </aside>
  );
}
