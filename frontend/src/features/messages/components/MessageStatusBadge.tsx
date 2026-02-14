import { Badge } from '@/shared/components/ui/badge';
import type { MessageStatus } from '@/types/message';

const STATUS_CONFIG: Record<
  MessageStatus,
  { label: string; variant: 'default' | 'secondary' | 'destructive' | 'outline' }
> = {
  PENDING: { label: '대기', variant: 'secondary' },
  PROCESSING: { label: '처리중', variant: 'outline' },
  ACCEPTED: { label: '성공', variant: 'default' },
  DEAD: { label: '실패', variant: 'destructive' },
};

export const MessageStatusBadge = ({ status }: { status: MessageStatus }) => {
  const config = STATUS_CONFIG[status];
  return (
    <Badge variant={config.variant} className="text-xs">
      {config.label}
    </Badge>
  );
};
