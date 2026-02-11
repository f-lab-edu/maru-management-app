import { useEffect, useState } from 'react';
import { useParams, useSearchParams } from 'react-router-dom';
import { paymentPageApi, type PgPaymentConfirmRes } from '@/services/paymentPageApi';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';
import { Spinner } from '@/shared/components/ui/spinner';

const PaymentSuccessPage = () => {
  const { token } = useParams<{ token: string }>();
  const [searchParams] = useSearchParams();
  const [loading, setLoading] = useState(true);
  const [result, setResult] = useState<PgPaymentConfirmRes | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const confirmPayment = async () => {
      if (!token) return;

      const paymentKey = searchParams.get('paymentKey');
      const orderId = searchParams.get('orderId');
      const amount = searchParams.get('amount');

      if (!paymentKey || !orderId || !amount) {
        setError('결제 정보가 올바르지 않습니다');
        setLoading(false);
        return;
      }

      try {
        const confirmResult = await paymentPageApi.confirmPayment(token, {
          paymentKey,
          orderId,
          amount: Number(amount),
        });
        setResult(confirmResult);
      } catch (err: unknown) {
        const axiosError = err as { response?: { data?: { message?: string } } };
        setError(axiosError.response?.data?.message ?? '결제 승인에 실패했습니다');
      } finally {
        setLoading(false);
      }
    };

    confirmPayment();
  }, [token, searchParams]);

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-50">
        <div className="text-center space-y-4">
          <Spinner className="mx-auto size-8" />
          <p className="text-muted-foreground">결제를 처리하고 있습니다...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-50 p-4">
        <Card className="w-full max-w-md">
          <CardHeader className="text-center">
            <div className="mx-auto mb-2 flex size-12 items-center justify-center rounded-full bg-destructive/10">
              <span className="text-2xl">!</span>
            </div>
            <CardTitle className="text-xl text-destructive">결제 승인 실패</CardTitle>
          </CardHeader>
          <CardContent className="text-center">
            <p className="text-muted-foreground">{error}</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (!result) return null;

  const formatAmount = (amount: number) =>
    new Intl.NumberFormat('ko-KR').format(amount);

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <div className="mx-auto mb-2 flex size-12 items-center justify-center rounded-full bg-green-100">
            <span className="text-2xl text-green-600">&#10003;</span>
          </div>
          <CardTitle className="text-xl">결제가 완료되었습니다</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2 rounded-lg bg-muted/50 p-4">
            <div className="flex justify-between">
              <span className="text-muted-foreground">결제 금액</span>
              <span className="font-bold text-primary">{formatAmount(result.amount)}원</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">결제 수단</span>
              <span className="font-medium">{result.pgMethod}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">주문번호</span>
              <span className="text-sm font-medium">{result.orderId}</span>
            </div>
          </div>
          {result.receiptUrl && (
            <a
              href={result.receiptUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="block text-center text-sm text-primary underline"
            >
              영수증 보기
            </a>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default PaymentSuccessPage;
