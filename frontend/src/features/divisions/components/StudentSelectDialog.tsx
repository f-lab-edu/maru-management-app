import { useState, useMemo } from 'react';
import { Loader2, Search, UserPlus } from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '../../../shared/components/ui/dialog';
import { Button } from '../../../shared/components/ui/button';
import { Input } from '../../../shared/components/ui/input';
import { Checkbox } from '../../../shared/components/ui/checkbox';
import { ScrollArea } from '../../../shared/components/ui/scroll-area';
import { Badge } from '../../../shared/components/ui/badge';
import { useStudents } from '../../students/hooks/useStudents';
import { useBulkEnrollStudents } from '../hooks';
import { STUDENT_STATUS_LABEL } from '../../../types/student';
import type { StudentStatus } from '../../../types/student';

interface StudentSelectDialogProps {
  dojangId: string;
  sectionId: string;
  divisionId: string;
  divisionName: string;
  enrolledStudentIds: string[];
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function StudentSelectDialog({
  dojangId,
  sectionId,
  divisionId,
  divisionName,
  enrolledStudentIds,
  open,
  onOpenChange,
}: StudentSelectDialogProps) {
  const [selectedIds, setSelectedIds] = useState<string[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [hideEnrolledElsewhere, setHideEnrolledElsewhere] = useState(false);

  const { data: studentsData, isLoading } = useStudents(dojangId);
  const bulkEnrollMutation = useBulkEnrollStudents(dojangId, divisionId, sectionId);

  const availableStudents = useMemo(() => {
    const students = studentsData?.students ?? [];
    return students.filter((student) => {
      // 현재 수련반에 이미 등록된 원생 제외
      if (enrolledStudentIds.includes(student.id)) return false;
      // ACTIVE 상태만 표시
      if (student.status !== 'ACTIVE') return false;
      // 다른 반 소속 원생 숨기기 옵션
      if (hideEnrolledElsewhere && student.hasEnrollment) return false;
      // 검색어 필터
      if (searchQuery) {
        const query = searchQuery.toLowerCase();
        return student.name.toLowerCase().includes(query);
      }
      return true;
    });
  }, [studentsData, enrolledStudentIds, searchQuery, hideEnrolledElsewhere]);

  const handleToggle = (studentId: string) => {
    setSelectedIds((prev) =>
      prev.includes(studentId)
        ? prev.filter((id) => id !== studentId)
        : [...prev, studentId]
    );
  };

  const handleSelectAll = () => {
    if (selectedIds.length === availableStudents.length) {
      setSelectedIds([]);
    } else {
      setSelectedIds(availableStudents.map((s) => s.id));
    }
  };

  const handleSubmit = async () => {
    if (selectedIds.length === 0) return;

    try {
      await bulkEnrollMutation.mutateAsync({ studentIds: selectedIds });
      setSelectedIds([]);
      setSearchQuery('');
      onOpenChange(false);
    } catch (error) {
      console.error('원생 등록 실패:', error);
    }
  };

  const handleClose = (open: boolean) => {
    if (!open) {
      setSelectedIds([]);
      setSearchQuery('');
    }
    onOpenChange(open);
  };

  const getStatusLabel = (status: StudentStatus) => {
    if (status === 'WITHDRAWN') return '탈퇴';
    return STUDENT_STATUS_LABEL[status];
  };

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>원생 추가</DialogTitle>
          <DialogDescription>
            {divisionName}에 등록할 원생을 선택하세요.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4 py-2">
          <div className="space-y-3">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
              <Input
                placeholder="원생 이름 검색..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-9"
              />
            </div>
            <label className="flex items-center gap-2 text-sm text-slate-600 cursor-pointer px-1">
              <Checkbox
                checked={hideEnrolledElsewhere}
                onCheckedChange={(checked) => setHideEnrolledElsewhere(checked === true)}
              />
              다른 반 소속 원생 숨기기
            </label>
          </div>

          {isLoading ? (
            <div className="flex items-center justify-center h-48">
              <Loader2 className="w-6 h-6 animate-spin text-primary" />
            </div>
          ) : availableStudents.length > 0 ? (
            <>
              <div className="flex items-center justify-between px-1">
                <label className="flex items-center gap-2 text-sm text-slate-600 cursor-pointer">
                  <Checkbox
                    checked={selectedIds.length === availableStudents.length && availableStudents.length > 0}
                    onCheckedChange={handleSelectAll}
                  />
                  전체 선택 ({selectedIds.length}/{availableStudents.length})
                </label>
              </div>

              <ScrollArea className="h-[calc(100vh-400px)] min-h-[200px] border rounded-lg">
                <div className="p-2 space-y-1 pr-4">
                  {availableStudents.map((student) => (
                    <label
                      key={student.id}
                      className="flex items-center gap-3 p-3 rounded-lg hover:bg-slate-50 cursor-pointer"
                    >
                      <Checkbox
                        checked={selectedIds.includes(student.id)}
                        onCheckedChange={() => handleToggle(student.id)}
                      />
                      <div className="flex-1 min-w-0">
                        <p className="font-medium text-slate-900">{student.name}</p>
                        <p className="text-sm text-slate-500">{student.birth}</p>
                      </div>
                      <Badge variant="secondary" className="text-xs">
                        {getStatusLabel(student.status)}
                      </Badge>
                    </label>
                  ))}
                </div>
              </ScrollArea>
            </>
          ) : (
            <div className="text-center py-12 text-slate-500">
              {searchQuery
                ? '검색 결과가 없습니다.'
                : '추가할 수 있는 원생이 없습니다.'}
            </div>
          )}
        </div>

        <DialogFooter>
          <Button
            type="button"
            variant="outline"
            onClick={() => handleClose(false)}
          >
            취소
          </Button>
          <Button
            onClick={handleSubmit}
            disabled={selectedIds.length === 0 || bulkEnrollMutation.isPending}
          >
            {bulkEnrollMutation.isPending ? (
              <Loader2 className="h-4 w-4 mr-2 animate-spin" />
            ) : (
              <UserPlus className="h-4 w-4 mr-2" />
            )}
            {selectedIds.length}명 추가
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
