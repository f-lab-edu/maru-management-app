import { Badge } from '@/shared/components/ui/badge';
import type { InvoiceStatus } from '../types';

const STATUS_CONFIG: Record<
  InvoiceStatus,
  { label: string; variant: 'default' | 'secondary' | 'destructive' | 'outline' }
> = {
  DRAFT: { label: '임시저장', variant: 'secondary' },
  OPEN: { label: '미납', variant: 'destructive' },
  PARTIAL: { label: '부분납부', variant: 'outline' },
  PAID: { label: '완납', variant: 'default' },
  VOID: { label: '무효', variant: 'secondary' },
};

export function InvoiceStatusBadge({ status }: { status: InvoiceStatus }) {
  const config = STATUS_CONFIG[status];

  return (
    <Badge variant={config.variant} className="text-xs">
      {config.label}
    </Badge>
  );
}
