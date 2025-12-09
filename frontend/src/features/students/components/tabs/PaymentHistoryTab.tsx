import { CreditCard, AlertCircle, CheckCircle2 } from 'lucide-react';
import { Badge } from '@/shared/components/ui/badge';

interface PaymentHistoryTabProps {
  studentId: number;
}

interface PaymentItem {
  id: number;
  month: string;
  amount: number;
  status: 'PAID' | 'UNPAID' | 'PARTIAL';
  paidAt?: string;
}

// TODO: API 연동 시 실제 데이터로 교체
const MOCK_PAYMENTS: PaymentItem[] = [
  { id: 1, month: '2024년 12월', amount: 150000, status: 'UNPAID' },
  { id: 2, month: '2024년 11월', amount: 150000, status: 'PAID', paidAt: '2024-11-05' },
  { id: 3, month: '2024년 10월', amount: 150000, status: 'PAID', paidAt: '2024-10-03' },
];

const STATUS_CONFIG = {
  PAID: {
    label: '납부완료',
    className: 'bg-green-100 text-green-800',
    icon: CheckCircle2,
  },
  UNPAID: {
    label: '미납',
    className: 'bg-red-100 text-red-800',
    icon: AlertCircle,
  },
  PARTIAL: {
    label: '부분납부',
    className: 'bg-yellow-100 text-yellow-800',
    icon: AlertCircle,
  },
} as const;

export function PaymentHistoryTab({ studentId }: PaymentHistoryTabProps) {
  // TODO: useQuery로 실제 API 호출
  const payments = MOCK_PAYMENTS;
  console.log('수납내역 조회 - studentId:', studentId);

  const formatAmount = (amount: number) => {
    return new Intl.NumberFormat('ko-KR').format(amount) + '원';
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-2 text-sm text-muted-foreground">
        <CreditCard className="h-4 w-4" />
        <span>최근 3개월 수납 내역</span>
      </div>

      <div className="space-y-2">
        {payments.map((payment) => {
          const config = STATUS_CONFIG[payment.status];
          const Icon = config.icon;

          return (
            <div
              key={payment.id}
              className="flex items-center justify-between rounded-lg border p-3"
            >
              <div className="flex items-center gap-3">
                <Icon
                  className={`h-5 w-5 ${
                    payment.status === 'PAID' ? 'text-green-600' : 'text-red-600'
                  }`}
                />
                <div>
                  <p className="font-medium">{payment.month}</p>
                  <p className="text-sm text-muted-foreground">
                    {formatAmount(payment.amount)}
                  </p>
                </div>
              </div>
              <Badge variant="outline" className={`border-0 ${config.className}`}>
                {config.label}
              </Badge>
            </div>
          );
        })}
      </div>

      <p className="text-center text-xs text-muted-foreground">
        전체 수납 내역은 수납 관리 메뉴에서 확인하세요.
      </p>
    </div>
  );
}
