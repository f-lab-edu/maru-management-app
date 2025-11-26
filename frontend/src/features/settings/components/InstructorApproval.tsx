import { Check, X } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../../../shared/components/ui/card';
import { Button } from '../../../shared/components/ui/button';
import { Avatar, AvatarFallback } from '../../../shared/components/ui/avatar';
import { Badge } from '../../../shared/components/ui/badge';

const MOCK_REQUESTS = [
  { id: 1, name: '김철수', email: 'kim@example.com', phone: '010-1234-5678', date: '2024-11-22', status: 'pending' },
  { id: 2, name: '이영희', email: 'lee@example.com', phone: '010-9876-5432', date: '2024-11-21', status: 'pending' },
  { id: 3, name: '박지성', email: 'park@example.com', phone: '010-5555-4444', date: '2024-11-20', status: 'rejected' },
];

export function InstructorApproval() {
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
            {MOCK_REQUESTS.filter(r => r.status === 'pending').map((request) => (
              <div key={request.id} className="flex items-center justify-between p-4 rounded-lg border border-slate-100 bg-white hover:border-primary/20 transition-colors">
                <div className="flex items-center gap-4">
                  <Avatar className="h-10 w-10 bg-slate-100">
                    <AvatarFallback className="text-slate-600">{request.name[0]}</AvatarFallback>
                  </Avatar>
                  <div>
                    <div className="flex items-center gap-2">
                      <p className="font-medium text-slate-900">{request.name}</p>
                      <Badge variant="secondary" className="text-xs font-normal">사범 신청</Badge>
                    </div>
                    <p className="text-sm text-slate-500 mt-0.5">{request.email} • {request.phone}</p>
                    <p className="text-xs text-slate-400 mt-1">요청일: {request.date}</p>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <Button size="sm" variant="outline" className="text-red-600 hover:text-red-700 hover:bg-red-50 border-red-100">
                    <X className="h-4 w-4 mr-1" />
                    거절
                  </Button>
                  <Button size="sm" className="bg-primary hover:bg-primary/90">
                    <Check className="h-4 w-4 mr-1" />
                    승인
                  </Button>
                </div>
              </div>
            ))}
            {MOCK_REQUESTS.filter(r => r.status === 'pending').length === 0 && (
              <div className="text-center py-8 text-slate-500">
                대기 중인 승인 요청이 없습니다.
              </div>
            )}
          </div>
        </CardContent>
      </Card>

      <Card className="border-none shadow-sm opacity-60">
        <CardHeader>
          <CardTitle className="text-lg text-slate-600">최근 처리 내역</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-2">
            {MOCK_REQUESTS.filter(r => r.status !== 'pending').map((request) => (
              <div key={request.id} className="flex items-center justify-between p-3 rounded-lg bg-slate-50">
                <div className="flex items-center gap-3">
                  <Avatar className="h-8 w-8 bg-slate-200">
                    <AvatarFallback className="text-xs text-slate-500">{request.name[0]}</AvatarFallback>
                  </Avatar>
                  <div>
                    <p className="text-sm font-medium text-slate-700">{request.name}</p>
                    <p className="text-xs text-slate-500">{request.date}</p>
                  </div>
                </div>
                <Badge variant="outline" className="bg-white text-slate-500">
                  {request.status === 'rejected' ? '거절됨' : '승인됨'}
                </Badge>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
