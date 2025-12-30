import { useState } from 'react';
import { Calendar, Phone, Plus, User, Users } from 'lucide-react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useAlert } from '@/hooks';
import { Avatar, AvatarFallback, AvatarImage } from '@/shared/components/ui/avatar';
import { Badge } from '@/shared/components/ui/badge';
import { Button } from '@/shared/components/ui/button';
import { Separator } from '@/shared/components/ui/separator';
import { calculateAge, formatDate } from '@/shared/utils/date';
import { GUARDIAN_RELATION_LABEL, type Student, type Guardian, type GuardianRelation } from '@/types/student';
import { studentService } from '@/services/studentService';
import { useAuthStore } from '@/stores/authStore';
import { GuardianEditModal } from '../GuardianEditModal';
import { GuardianDeleteDialog } from '../GuardianDeleteDialog';

interface StudentInfoTabProps {
  student: Student;
}

interface InfoRowProps {
  icon: React.ReactNode;
  label: string;
  value: string | React.ReactNode;
}

function InfoRow({ icon, label, value }: InfoRowProps) {
  return (
    <div className="flex items-start gap-3 py-2">
      <div className="mt-0.5 text-muted-foreground">{icon}</div>
      <div className="flex-1">
        <p className="text-sm text-muted-foreground">{label}</p>
        <p className="font-medium">{value}</p>
      </div>
    </div>
  );
}

export function StudentInfoTab({ student }: StudentInfoTabProps) {
  const queryClient = useQueryClient();
  const { selectedDojang } = useAuthStore();
  const dojangId = selectedDojang?.dojangId ?? null;
  const { showSuccess, showError } = useAlert();

  const [editingGuardian, setEditingGuardian] = useState<Guardian | null>(null);
  const [deletingGuardian, setDeletingGuardian] = useState<Guardian | null>(null);
  const [isAddingGuardian, setIsAddingGuardian] = useState(false);

  const age = calculateAge(student.birth);
  const primaryGuardians = student.guardians.filter((g) => g.isPrimary);
  const otherGuardians = student.guardians.filter((g) => !g.isPrimary);

  const setPrimaryMutation = useMutation({
    mutationFn: (guardianId: string) =>
      studentService.setPrimaryGuardian(dojangId!, student.id, guardianId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['student', dojangId, student.id] });
      showSuccess('주 보호자 설정이 변경되었습니다');
    },
    onError: (error) => {
      console.error('주 보호자 설정 실패:', error);
      showError('주 보호자 설정에 실패했습니다');
    },
  });

  const updateMutation = useMutation({
    mutationFn: (data: { name: string; phone: string; relation: GuardianRelation }) =>
      studentService.updateGuardian(dojangId!, student.id, editingGuardian!.id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['student', dojangId, student.id] });
      setEditingGuardian(null);
      showSuccess('보호자 정보가 수정되었습니다');
    },
    onError: (error) => {
      console.error('보호자 정보 수정 실패:', error);
      showError('보호자 정보 수정에 실패했습니다');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (guardianId: string) =>
      studentService.deleteGuardian(dojangId!, student.id, guardianId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['student', dojangId, student.id] });
      setDeletingGuardian(null);
      showSuccess('보호자가 삭제되었습니다');
    },
    onError: (error) => {
      console.error('보호자 삭제 실패:', error);
      showError('보호자 삭제에 실패했습니다');
    },
  });

  const addMutation = useMutation({
    mutationFn: (data: { name: string; phone: string; relation: GuardianRelation }) =>
      studentService.addGuardian(dojangId!, student.id, { ...data, isPrimary: false }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['student', dojangId, student.id] });
      setIsAddingGuardian(false);
      showSuccess('보호자가 추가되었습니다');
    },
    onError: (error) => {
      console.error('보호자 추가 실패:', error);
      showError('보호자 추가에 실패했습니다');
    },
  });

  return (
    <div className="space-y-6">
      {/* 프로필 섹션 */}
      <div className="flex items-center gap-4">
        <Avatar className="h-20 w-20">
          <AvatarImage src={student.photoUrl} alt={student.name} />
          <AvatarFallback className="bg-primary/10 text-primary text-2xl font-medium">
            {student.name.slice(0, 2)}
          </AvatarFallback>
        </Avatar>
        <div>
          <h3 className="text-lg font-semibold">{student.name}</h3>
          <p className="text-muted-foreground">
            {age}세 ({formatDate(student.birth)})
          </p>
        </div>
      </div>

      <Separator />

      {/* 기본 정보 */}
      <div className="space-y-1">
        <h4 className="text-sm font-medium text-muted-foreground">기본 정보</h4>
        <div className="rounded-lg bg-muted/50 p-3">
          <InfoRow
            icon={<Calendar className="h-4 w-4" />}
            label="등록일"
            value={formatDate(student.enrolledAt)}
          />
          {student.phone && (
            <InfoRow
              icon={<Phone className="h-4 w-4" />}
              label="연락처"
              value={student.phone}
            />
          )}
        </div>
      </div>

      {/* 보호자 정보 */}
      <div className="space-y-1">
        <div className="flex items-center justify-between">
          <h4 className="text-sm font-medium text-muted-foreground">보호자 정보</h4>
          <Button
            variant="outline"
            size="sm"
            onClick={() => setIsAddingGuardian(true)}
          >
            <Plus className="mr-1 h-4 w-4" />
            추가
          </Button>
        </div>
        {student.guardians.length > 0 ? (
          <div className="space-y-2">
            {/* 주 보호자 */}
            {primaryGuardians.map((guardian) => (
              <div key={guardian.id} className="rounded-lg border-2 border-primary/20 bg-primary/5 p-3">
                <div className="mb-2 flex items-center gap-2">
                  <Badge variant="default" className="text-xs">
                    주 보호자
                  </Badge>
                  <span className="text-xs text-muted-foreground">
                    {GUARDIAN_RELATION_LABEL[guardian.relation]}
                  </span>
                </div>
                <div className="space-y-2">
                  <div className="flex items-start gap-3">
                    <User className="mt-0.5 h-4 w-4 text-muted-foreground" />
                    <div className="flex-1">
                      <p className="text-sm text-muted-foreground">이름 / 연락처</p>
                      <div className="flex items-center gap-2">
                        <span className="font-medium">{guardian.name}</span>
                        <span className="text-muted-foreground">{guardian.phone}</span>
                        {guardian.isVerified && (
                          <Badge variant="outline" className="text-xs text-green-600">
                            인증됨
                          </Badge>
                        )}
                      </div>
                    </div>
                  </div>
                  <div className="flex justify-end gap-2">
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => setPrimaryMutation.mutate(guardian.id)}
                      disabled={setPrimaryMutation.isPending}
                    >
                      주 보호자 해제
                    </Button>
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => setEditingGuardian(guardian)}
                    >
                      수정
                    </Button>
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => setDeletingGuardian(guardian)}
                    >
                      삭제
                    </Button>
                  </div>
                </div>
              </div>
            ))}

            {/* 기타 보호자 */}
            {otherGuardians.map((guardian) => (
              <div key={guardian.id} className="rounded-lg bg-muted/50 p-3">
                <div className="mb-2 flex items-center gap-2 text-sm text-muted-foreground">
                  <Users className="h-4 w-4" />
                  <span>{GUARDIAN_RELATION_LABEL[guardian.relation]}</span>
                </div>
                <div className="space-y-2">
                  <div className="flex items-center gap-2">
                    <span className="font-medium">{guardian.name}</span>
                    <span className="text-muted-foreground">{guardian.phone}</span>
                  </div>
                  <div className="flex justify-end gap-2">
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => setPrimaryMutation.mutate(guardian.id)}
                      disabled={setPrimaryMutation.isPending}
                    >
                      주 보호자로 설정
                    </Button>
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => setEditingGuardian(guardian)}
                    >
                      수정
                    </Button>
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => setDeletingGuardian(guardian)}
                    >
                      삭제
                    </Button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="rounded-lg border border-dashed p-6 text-center">
            <p className="text-sm text-muted-foreground">
              등록된 보호자가 없습니다.
            </p>
          </div>
        )}
      </div>

      <GuardianEditModal
        guardian={null}
        isOpen={isAddingGuardian}
        onClose={() => setIsAddingGuardian(false)}
        onSubmit={(data) => addMutation.mutate(data)}
        isLoading={addMutation.isPending}
        mode="add"
      />

      <GuardianEditModal
        guardian={editingGuardian}
        isOpen={!!editingGuardian}
        onClose={() => setEditingGuardian(null)}
        onSubmit={(data) => updateMutation.mutate(data)}
        isLoading={updateMutation.isPending}
      />

      <GuardianDeleteDialog
        guardian={deletingGuardian}
        isOpen={!!deletingGuardian}
        onClose={() => setDeletingGuardian(null)}
        onConfirm={() => deletingGuardian && deleteMutation.mutate(deletingGuardian.id)}
        isLoading={deleteMutation.isPending}
      />
    </div>
  );
}
