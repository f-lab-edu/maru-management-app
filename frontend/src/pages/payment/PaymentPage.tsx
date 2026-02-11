import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { loadTossPayments, ANONYMOUS } from '@tosspayments/tosspayments-sdk';
import { paymentPageApi, type PaymentPageRes } from '@/services/paymentPageApi';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { Spinner } from '@/shared/components/ui/spinner';

type PaymentMethod = 'CARD' | 'TRANSFER';

const PaymentPage = () => {
  const { token } = useParams<{ token: string }>();
  const [paymentInfo, setPaymentInfo] = useState<PaymentPageRes | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedMethod, setSelectedMethod] = useState<PaymentMethod>('CARD');
  const [paying, setPaying] = useState(false);

  useEffect(() => {
    const fetchPaymentInfo = async () => {
      if (!token) return;
      try {
        const info = await paymentPageApi.getPaymentInfo(token);
        setPaymentInfo(info);
      } catch (err: unknown) {
        const axiosError = err as { response?: { data?: { message?: string } } };
        setError(axiosError.response?.data?.message ?? '결제 정보를 불러올 수 없습니다');
      } finally {
        setLoading(false);
      }
    };

    fetchPaymentInfo();
  }, [token]);

  const handlePayment = async () => {
    if (!paymentInfo || !token || paying) return;

    setPaying(true);
    try {
      const tossPayments = await loadTossPayments(paymentInfo.clientKey);
      const payment = tossPayments.payment({ customerKey: ANONYMOUS });

      const baseUrl = window.location.origin;
      const successUrl = `${baseUrl}/pay/${token}/success`;
      const failUrl = `${baseUrl}/pay/${token}/fail`;

      const commonParams = {
        amount: { currency: 'KRW' as const, value: paymentInfo.remainingAmount },
        orderId: paymentInfo.orderId,
        orderName: `${paymentInfo.dojangName} ${paymentInfo.billingYearMonth} 수업료`,
        successUrl,
        failUrl,
      };

      if (selectedMethod === 'TRANSFER') {
        await payment.requestPayment({ method: 'TRANSFER', ...commonParams });
      } else {
        await payment.requestPayment({ method: 'CARD', ...commonParams });
      }
    } catch (err: unknown) {
      const tossError = err as { code?: string; message?: string };
      if (tossError.code === 'USER_CANCEL') {
        setPaying(false);
        return;
      }
      setError(tossError.message ?? '결제 요청 중 오류가 발생했습니다');
      setPaying(false);
    }
  };

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-50">
        <Spinner className="size-8" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-50 p-4">
        <Card className="w-full max-w-md">
          <CardContent className="pt-6 text-center">
            <p className="text-lg font-medium text-destructive">{error}</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (!paymentInfo) return null;

  const formatAmount = (amount: number) =>
    new Intl.NumberFormat('ko-KR').format(amount);

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <CardTitle className="text-xl">{paymentInfo.dojangName}</CardTitle>
          <p className="text-sm text-muted-foreground">수업료 결제</p>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="space-y-3 rounded-lg bg-muted/50 p-4">
            <div className="flex justify-between">
              <span className="text-muted-foreground">원생</span>
              <span className="font-medium">{paymentInfo.studentName}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">청구 월</span>
              <span className="font-medium">{paymentInfo.billingYearMonth}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">총 청구 금액</span>
              <span className="font-medium">{formatAmount(paymentInfo.totalAmount)}원</span>
            </div>
            {paymentInfo.totalAmount !== paymentInfo.remainingAmount && (
              <div className="flex justify-between">
                <span className="text-muted-foreground">기 납부 금액</span>
                <span className="font-medium">
                  {formatAmount(paymentInfo.totalAmount - paymentInfo.remainingAmount)}원
                </span>
              </div>
            )}
            <div className="border-t pt-3">
              <div className="flex justify-between">
                <span className="font-semibold">결제 금액</span>
                <span className="text-lg font-bold text-primary">
                  {formatAmount(paymentInfo.remainingAmount)}원
                </span>
              </div>
            </div>
          </div>

          <div className="space-y-2">
            <p className="text-sm font-medium">결제 수단</p>
            <div className="grid grid-cols-2 gap-2">
              <Button
                variant={selectedMethod === 'CARD' ? 'default' : 'outline'}
                onClick={() => setSelectedMethod('CARD')}
              >
                카드 결제
              </Button>
              <Button
                variant={selectedMethod === 'TRANSFER' ? 'default' : 'outline'}
                onClick={() => setSelectedMethod('TRANSFER')}
              >
                계좌이체
              </Button>
            </div>
          </div>

          <Button
            className="w-full"
            size="lg"
            onClick={handlePayment}
            disabled={paying}
          >
            {paying ? (
              <>
                <Spinner className="size-4" />
                결제 진행 중...
              </>
            ) : (
              `${formatAmount(paymentInfo.remainingAmount)}원 결제하기`
            )}
          </Button>
        </CardContent>
      </Card>
    </div>
  );
};

export default PaymentPage;
