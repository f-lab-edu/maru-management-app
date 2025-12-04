import { Check, X, Loader2 } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../../../shared/components/ui/card';
import { Button } from '../../../shared/components/ui/button';
import { Avatar, AvatarFallback } from '../../../shared/components/ui/avatar';
import { Badge } from '../../../shared/components/ui/badge';
import { usePendingRequests, useApproveRequest, useRejectRequest } from '../../employment';

export function InstructorApproval() {
  const { data: requests, isLoading } = usePendingRequests();
  const approveRequest = useApproveRequest();
  const rejectRequest = useRejectRequest();

  const handleApprove = async (employmentId: number) => {
    try {
      await approveRequest.mutateAsync(employmentId);
    } catch (error) {
      console.error('승인 처리 실패:', error);
    }
  };

  const handleReject = async (employmentId: number) => {
    if (!confirm('정말 거절하시겠습니까?')) return;
    try {
      await rejectRequest.mutateAsync(employmentId);
    } catch (error) {
      console.error('거절 처리 실패:', error);
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    );
  }

  const pendingRequests = requests?.filter(r => r.status === 'PENDING') ?? [];
  const processedRequests = requests?.filter(r => r.status !== 'PENDING') ?? [];

  return (
    <div className="space-y-6">
      <Card className="border-none shadow-sm">
        <CardHeader>
          <CardTitle className="text-lg flex items-center gap-2">
            승인 대기 목록
          </CardTitle>
          <CardDescription>
            도장 가입을 요청한 사범님들의 목록입니다. 승인 시 사범 권한이 부여됩니다.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {pendingRequests.map((request) => (
              <div key={request.id} className="flex items-center justify-between p-4 rounded-lg border border-slate-100 bg-white hover:border-primary/20 transition-colors">
                <div className="flex items-center gap-4">
                  <Avatar className="h-10 w-10 bg-slate-100">
                    <AvatarFallback className="text-slate-600">{request.userName[0]}</AvatarFallback>
                  </Avatar>
                  <div>
                    <div className="flex items-center gap-2">
                      <p className="font-medium text-slate-900">{request.userName}</p>
                      <Badge variant="secondary" className="text-xs font-normal">사범 신청</Badge>
                    </div>
                    <p className="text-sm text-slate-500 mt-0.5">{request.userEmail}{request.userPhone && ` • ${request.userPhone}`}</p>
                    <p className="text-xs text-slate-400 mt-1">요청일: {new Date(request.requestedAt).toLocaleDateString('ko-KR')}</p>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => handleReject(request.id)}
                    disabled={rejectRequest.isPending}
                    className="text-red-600 hover:text-red-700 hover:bg-red-50 border-red-100"
                  >
                    <X className="h-4 w-4 mr-1" />
                    거절
                  </Button>
                  <Button
                    size="sm"
                    onClick={() => handleApprove(request.id)}
                    disabled={approveRequest.isPending}
                    className="bg-primary hover:bg-primary/90"
                  >
                    <Check className="h-4 w-4 mr-1" />
                    승인
                  </Button>
                </div>
              </div>
            ))}
            {pendingRequests.length === 0 && (
              <div className="text-center py-8 text-slate-500">
                대기 중인 승인 요청이 없습니다.
              </div>
            )}
          </div>
        </CardContent>
      </Card>

      {processedRequests.length > 0 && (
        <Card className="border-none shadow-sm opacity-60">
          <CardHeader>
            <CardTitle className="text-lg text-slate-600">최근 처리 내역</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-2">
              {processedRequests.map((request) => (
                <div key={request.id} className="flex items-center justify-between p-3 rounded-lg bg-slate-50">
                  <div className="flex items-center gap-3">
                    <Avatar className="h-8 w-8 bg-slate-200">
                      <AvatarFallback className="text-xs text-slate-500">{request.userName[0]}</AvatarFallback>
                    </Avatar>
                    <div>
                      <p className="text-sm font-medium text-slate-700">{request.userName}</p>
                      <p className="text-xs text-slate-500">{new Date(request.requestedAt).toLocaleDateString('ko-KR')}</p>
                    </div>
                  </div>
                  <Badge variant="outline" className="bg-white text-slate-500">
                    {request.status === 'REJECTED' ? '거절됨' : '승인됨'}
                  </Badge>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
