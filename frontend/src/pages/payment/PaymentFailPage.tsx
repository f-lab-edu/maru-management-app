import { useSearchParams } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';

const PaymentFailPage = () => {
  const [searchParams] = useSearchParams();
  const code = searchParams.get('code');
  const message = searchParams.get('message');

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <div className="mx-auto mb-2 flex size-12 items-center justify-center rounded-full bg-destructive/10">
            <span className="text-2xl">&#10007;</span>
          </div>
          <CardTitle className="text-xl text-destructive">결제에 실패했습니다</CardTitle>
        </CardHeader>
        <CardContent className="text-center space-y-2">
          <p className="text-muted-foreground">
            {message ?? '결제 처리 중 오류가 발생했습니다'}
          </p>
          {code && (
            <p className="text-xs text-muted-foreground">
              오류 코드: {code}
            </p>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default PaymentFailPage;
