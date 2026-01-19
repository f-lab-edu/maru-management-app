import { Bell } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '../../../shared/components/ui/card';
import { Button } from '../../../shared/components/ui/button';
import { cn } from '../../../shared/utils';
import { NotificationItem } from '../types';

interface NotificationListProps {
  notifications: NotificationItem[];
  hasMore?: boolean;
  onLoadMore?: () => void;
  isLoading?: boolean;
}

export function NotificationList({
  notifications,
  hasMore = false,
  onLoadMore,
  isLoading = false
}: NotificationListProps) {
  return (
    <Card className="border-none shadow-sm bg-white flex-1 flex flex-col min-h-0">
      <CardHeader className="shrink-0">
        <CardTitle className="flex items-center gap-2">
          <Bell className="h-5 w-5 text-primary" />
          주요 알림
        </CardTitle>
      </CardHeader>
      <CardContent className="flex-1 overflow-y-auto">
        <div className="space-y-4">
          {notifications.map((item, i) => (
            <div key={i} className="flex items-start gap-4 p-3 rounded-lg hover:bg-slate-50 transition-colors cursor-pointer group">
              <div className={cn(
                "h-2 w-2 mt-2 rounded-full",
                item.type === 'payment' ? "bg-emerald-500" :
                item.type === 'alert' ? "bg-red-500" : "bg-blue-500"
              )} />
              <div className="flex-1">
                <p className="text-sm font-medium text-slate-900 group-hover:text-primary transition-colors">{item.title}</p>
                <p className="text-xs text-slate-500 mt-0.5">{item.desc}</p>
              </div>
              <span className="text-xs text-slate-400 whitespace-nowrap">{item.time}</span>
            </div>
          ))}
          {hasMore && onLoadMore && (
            <Button
              variant="ghost"
              className="w-full text-xs text-slate-500 hover:text-primary"
              onClick={onLoadMore}
              disabled={isLoading}
            >
              {isLoading ? '불러오는 중...' : '더보기'}
            </Button>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
