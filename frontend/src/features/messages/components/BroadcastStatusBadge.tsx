import { Badge } from '@/shared/components/ui/badge';
import type { BroadcastStatus } from '@/types/message';

const STATUS_CONFIG: Record<
  BroadcastStatus,
  { label: string; variant: 'default' | 'secondary' | 'destructive' | 'outline' }
> = {
  CREATED: { label: '생성', variant: 'secondary' },
  DISPATCHING: { label: '발송중', variant: 'outline' },
  COMPLETED: { label: '완료', variant: 'default' },
  PARTIAL_FAILED: { label: '일부실패', variant: 'destructive' },
};

export const BroadcastStatusBadge = ({ status }: { status: BroadcastStatus }) => {
  const config = STATUS_CONFIG[status];
  return (
    <Badge variant={config.variant} className="text-xs">
      {config.label}
    </Badge>
  );
};
