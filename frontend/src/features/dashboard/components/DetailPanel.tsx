import { ChevronRight } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '../../../shared/components/ui/card';
import { Button } from '../../../shared/components/ui/button';
import { Avatar, AvatarFallback } from '../../../shared/components/ui/avatar';
import { Applicant } from '../types';

interface DetailPanelProps {
  selectedDate: Date | undefined;
  isDetailsView: boolean;
  onBack: () => void;
  applicants: Applicant[];
}

export function DetailPanel({ selectedDate, isDetailsView, onBack, applicants }: DetailPanelProps) {
  return (
    <Card className="border-none shadow-sm bg-white flex-1 flex flex-col min-h-0">
      <CardHeader className="pb-2 shrink-0">
        <CardTitle className="text-base">
          {isDetailsView ? (
            <span className="flex items-center gap-2">
              {selectedDate?.toLocaleDateString('ko-KR', { month: 'long', day: 'numeric' })} 일정
            </span>
          ) : (
            '신규 입관 문의'
          )}
        </CardTitle>
      </CardHeader>
      <CardContent className="flex-1 overflow-y-auto">
        {isDetailsView ? (
          <div className="space-y-4 animate-in fade-in slide-in-from-bottom-2 duration-300">
            <div className="p-4 rounded-lg bg-blue-50 border border-blue-100">
              <p className="text-sm font-medium text-blue-900">유소년부 정기 승급심사</p>
              <p className="text-xs text-blue-600 mt-1">오후 2:00 - 4:00</p>
            </div>
            <div className="p-4 rounded-lg bg-slate-50 border border-slate-100">
              <p className="text-sm font-medium text-slate-900">학부모 상담 (이민준)</p>
              <p className="text-xs text-slate-500 mt-1">오후 5:30</p>
            </div>
            <Button
              variant="outline"
              className="w-full text-xs"
              onClick={onBack}
            >
              목록으로 돌아가기
            </Button>
          </div>
        ) : (
          <div className="space-y-4 animate-in fade-in slide-in-from-bottom-2 duration-300">
            {applicants.map((applicant, i) => (
              <div key={i} className="flex items-center justify-between p-3 rounded-lg border border-slate-100 hover:border-primary/20 hover:bg-slate-50 transition-all group">
                <div className="flex items-center gap-3">
                  <Avatar className="h-8 w-8 bg-slate-100">
                    <AvatarFallback className="text-xs text-slate-500">{applicant.name[0]}</AvatarFallback>
                  </Avatar>
                  <div>
                    <p className="text-sm font-medium text-slate-900">{applicant.name} <span className="text-xs text-slate-400 font-normal">({applicant.age})</span></p>
                    <p className="text-xs text-slate-500">{applicant.phone}</p>
                  </div>
                </div>
                <ChevronRight className="h-4 w-4 text-slate-300 group-hover:text-primary transition-colors" />
              </div>
            ))}
            <Button variant="ghost" className="w-full text-xs text-slate-500 hover:text-primary">
              전체 보기
            </Button>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
