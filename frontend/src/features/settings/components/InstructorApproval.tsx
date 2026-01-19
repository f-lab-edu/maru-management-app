import { Check, X, Loader2 } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../../../shared/components/ui/card';
import { Button } from '../../../shared/components/ui/button';
import { Avatar, AvatarFallback } from '../../../shared/components/ui/avatar';
import { Badge } from '../../../shared/components/ui/badge';
import { usePendingRequests, useApproveRequest, useRejectRequest } from '../../employment';
import { useConfirm } from '../../../hooks';

export function InstructorApproval() {
  const { data: requests, isLoading } = usePendingRequests();
  const approveRequest = useApproveRequest();
  const rejectRequest = useRejectRequest();
  const { confirmDelete } = useConfirm();

  const handleApprove = async (employmentId: string) => {
    try {
      await approveRequest.mutateAsync(employmentId);
    } catch (error) {
      console.error('승인 처리 실패:', error);
    }
  };

  const handleReject = async (employmentId: string) => {
    const { isConfirmed } = await confirmDelete({
      title: '가입 요청 거절',
      text: '이 사범의 가입 요청을 거절하시겠습니까?',
      confirmText: '거절',
      cancelText: '취소',
    });
    if (!isConfirmed) return;
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

  const pendingRequests = requests ?? [];

  return (
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
  );
}
