import { Clock, CheckCircle2, XCircle, Trash2, Building2, Loader2 } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../../shared/components/ui/card';
import { Button } from '../../shared/components/ui/button';
import { Badge } from '../../shared/components/ui/badge';
import { useMyRequests, useCancelRequest } from '../../features/employment';
import { EmploymentStatus } from '../../types/employment';

const statusConfig: Record<EmploymentStatus, { label: string; variant: 'default' | 'secondary' | 'destructive' | 'outline'; icon: typeof Clock }> = {
  PENDING: { label: '승인 대기', variant: 'secondary', icon: Clock },
  ACTIVE: { label: '승인됨', variant: 'default', icon: CheckCircle2 },
  REJECTED: { label: '거절됨', variant: 'destructive', icon: XCircle },
  SUSPENDED: { label: '정지됨', variant: 'outline', icon: XCircle },
  LEFT: { label: '퇴사', variant: 'outline', icon: XCircle },
};

export default function ApprovalStatusPage() {
  const { data: requests, isLoading } = useMyRequests();
  const cancelRequest = useCancelRequest();

  const handleCancel = async (employmentId: number) => {
    if (!confirm('승인 요청을 취소하시겠습니까?')) return;
    try {
      await cancelRequest.mutateAsync(employmentId);
    } catch (error) {
      console.error('요청 취소 실패:', error);
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
    <div className="max-w-2xl mx-auto p-6 space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">승인 요청 현황</h1>
        <p className="text-slate-500 mt-1">도장 가입 요청 상태를 확인하세요</p>
      </div>

      <Card className="border-none shadow-sm">
        <CardHeader>
          <CardTitle className="text-lg flex items-center gap-2">
            <Clock className="w-5 h-5 text-amber-500" />
            대기 중인 요청
          </CardTitle>
          <CardDescription>
            관장님의 승인을 기다리고 있는 요청입니다
          </CardDescription>
        </CardHeader>
        <CardContent>
          {pendingRequests.length === 0 ? (
            <div className="text-center py-8 text-slate-500">
              대기 중인 승인 요청이 없습니다
            </div>
          ) : (
            <div className="space-y-3">
              {pendingRequests.map((request) => {
                const config = statusConfig[request.status];
                const StatusIcon = config.icon;
                return (
                  <div
                    key={request.id}
                    className="flex items-center justify-between p-4 rounded-lg border border-slate-200 bg-white"
                  >
                    <div className="flex items-center gap-4">
                      <div className="w-10 h-10 rounded-full bg-slate-100 flex items-center justify-center">
                        <Building2 className="w-5 h-5 text-slate-600" />
                      </div>
                      <div>
                        <p className="font-medium text-slate-900">{request.dojangName}</p>
                        <p className="text-sm text-slate-500">
                          요청일: {new Date(request.requestedAt).toLocaleDateString('ko-KR')}
                        </p>
                      </div>
                    </div>
                    <div className="flex items-center gap-3">
                      <Badge variant={config.variant} className="gap-1">
                        <StatusIcon className="w-3 h-3" />
                        {config.label}
                      </Badge>
                      <Button
                        size="sm"
                        variant="ghost"
                        onClick={() => handleCancel(request.id)}
                        disabled={cancelRequest.isPending}
                        className="text-red-600 hover:text-red-700 hover:bg-red-50"
                      >
                        <Trash2 className="w-4 h-4" />
                      </Button>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </CardContent>
      </Card>

      {processedRequests.length > 0 && (
        <Card className="border-none shadow-sm">
          <CardHeader>
            <CardTitle className="text-lg text-slate-600">처리된 요청</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-2">
              {processedRequests.map((request) => {
                const config = statusConfig[request.status];
                const StatusIcon = config.icon;
                return (
                  <div
                    key={request.id}
                    className="flex items-center justify-between p-3 rounded-lg bg-slate-50"
                  >
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded-full bg-slate-200 flex items-center justify-center">
                        <Building2 className="w-4 h-4 text-slate-500" />
                      </div>
                      <div>
                        <p className="text-sm font-medium text-slate-700">{request.dojangName}</p>
                        <p className="text-xs text-slate-500">
                          {request.approvedAt
                            ? `승인일: ${new Date(request.approvedAt).toLocaleDateString('ko-KR')}`
                            : request.rejectedAt
                              ? `거절일: ${new Date(request.rejectedAt).toLocaleDateString('ko-KR')}`
                              : `요청일: ${new Date(request.requestedAt).toLocaleDateString('ko-KR')}`
                          }
                        </p>
                      </div>
                    </div>
                    <Badge variant={config.variant} className="gap-1">
                      <StatusIcon className="w-3 h-3" />
                      {config.label}
                    </Badge>
                  </div>
                );
              })}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
