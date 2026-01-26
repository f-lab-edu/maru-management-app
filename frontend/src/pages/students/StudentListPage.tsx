import { useState, useMemo } from 'react';
import { RowSelectionState } from '@tanstack/react-table';
import { Plus } from 'lucide-react';
import { AxiosError } from 'axios';
import { Button } from '@/shared/components/ui/button';
import { useAuthStore } from '@/stores/authStore';
import { useAlert, useConfirm, usePermissions } from '@/hooks';
import { ErrorResponse } from '@/services/api';
import { StudentDataTable } from '@/features/students/components/StudentDataTable';
import { StudentTableToolbar } from '@/features/students/components/StudentTableToolbar';
import { createStudentColumns } from '@/features/students/components/studentColumns';
import { StudentDetailDrawer } from '@/features/students/components/StudentDetailDrawer';
import { StudentFormModal } from '@/features/students/components/StudentFormModal';
import { StatusChangeDialog } from '@/features/students/components/StatusChangeDialog';
import {
  useStudents,
  useStudent,
  useCreateStudent,
  useUpdateStudent,
  useDeleteStudent,
  useBulkDeleteStudents,
  useRestoreStudent,
} from '@/features/students/hooks/useStudents';
import { studentService } from '@/services/studentService';
import type { Student, StudentSummary, StudentStatus } from '@/types/student';

type StatusFilter = 'ALL' | 'ACTIVE' | 'PAUSED';
type ActiveTab = 'active' | 'withdrawn';

type ModalState =
  | { type: 'closed' }
  | { type: 'create' }
  | { type: 'edit'; student: Student }
  | { type: 'statusChange'; student: StudentSummary; newStatus: StudentStatus };

export default function StudentListPage() {
  const { selectedDojang } = useAuthStore();
  const dojangId = selectedDojang?.dojangId ?? null;
  const { showError, showSuccess } = useAlert();
  const { confirmDelete } = useConfirm();
  const { hasPermission } = usePermissions();

  const canCreate = hasPermission('STUDENT_CREATE');
  const canUpdate = hasPermission('STUDENT_UPDATE');
  const canDelete = hasPermission('STUDENT_DELETE');

  // 탭 필터 상태
  const [activeTab, setActiveTab] = useState<ActiveTab>('active');

  // 필터 상태
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL');
  const [searchQuery, setSearchQuery] = useState('');

  // 수련부/수련반 필터 상태
  const [sectionId, setSectionId] = useState<string | null>(null);
  const [divisionId, setDivisionId] = useState<string | null>(null);

  const { data: studentsData, isLoading } = useStudents(dojangId, {
    sectionId: sectionId ?? undefined,
    divisionId: divisionId ?? undefined,
    includeWithdrawn: true,
  });

  // 선택 상태
  const [rowSelection, setRowSelection] = useState<RowSelectionState>({});

  // Drawer 상태
  const [selectedStudentId, setSelectedStudentId] = useState<string | null>(null);
  const { data: selectedStudent, isLoading: isLoadingDetail } = useStudent(
    dojangId,
    selectedStudentId
  );

  // 모달 상태
  const [modalState, setModalState] = useState<ModalState>({ type: 'closed' });

  // Mutations
  const createMutation = useCreateStudent(dojangId ?? '');
  const updateMutation = useUpdateStudent(dojangId ?? '');
  const deleteMutation = useDeleteStudent(dojangId ?? '');
  const bulkDeleteMutation = useBulkDeleteStudents(dojangId ?? '');
  const restoreMutation = useRestoreStudent(dojangId ?? '');

  // 필터링된 데이터
  const filteredStudents = useMemo(() => {
    if (!studentsData?.students) return [];

    return studentsData.students.filter((student) => {
      if (activeTab === 'withdrawn') {
        return student.status === 'WITHDRAWN';
      } else {
        if (student.status === 'WITHDRAWN') {
          return false;
        }
        if (statusFilter !== 'ALL' && student.status !== statusFilter) {
          return false;
        }
        return true;
      }
    });
  }, [studentsData?.students, activeTab, statusFilter]);

  // 통계
  const stats = useMemo(() => {
    const students = studentsData?.students ?? [];
    return {
      total: students.filter((s) => s.status !== 'WITHDRAWN').length,
      activeCount: students.filter((s) => s.status === 'ACTIVE').length,
      pausedCount: students.filter((s) => s.status === 'PAUSED').length,
      withdrawnCount: students.filter((s) => s.status === 'WITHDRAWN').length,
    };
  }, [studentsData?.students]);

  // 선택된 수련생 수
  const selectedCount = Object.keys(rowSelection).length;

  // 선택된 수련생 ID 목록
  const selectedStudentIds = useMemo(() => {
    return Object.keys(rowSelection)
      .filter((key) => rowSelection[key])
      .map((index) => filteredStudents[Number(index)]?.id)
      .filter((id): id is string => id !== undefined);
  }, [rowSelection, filteredStudents]);

  // 컬럼 정의
  const columns = useMemo(
    () =>
      createStudentColumns({
        onEdit: handleOpenEditModal,
        onDelete: handleDeleteStudent,
        onStatusChange: handleOpenStatusChangeDialog,
        onRestore: handleRestoreStudent,
        isWithdrawnTab: activeTab === 'withdrawn',
        canUpdate,
        canDelete,
      }),
    [activeTab, canUpdate, canDelete]
  );

  // Drawer 핸들러
  function handleRowClick(student: StudentSummary) {
    if (student.status === 'WITHDRAWN') {
      return;
    }
    setSelectedStudentId(student.id);
  }

  function handleCloseDrawer() {
    setSelectedStudentId(null);
  }

  // 모달 핸들러
  function handleOpenCreateModal() {
    setModalState({ type: 'create' });
  }

  function handleOpenEditModal(student: StudentSummary) {
    if (selectedStudent && selectedStudent.id === student.id) {
      setModalState({ type: 'edit', student: selectedStudent });
    } else {
      setModalState({
        type: 'edit',
        student: {
          ...student,
          guardians: [],
        } as Student,
      });
    }
  }

  async function handleDeleteStudent(student: StudentSummary) {
    const { isConfirmed } = await confirmDelete({
      title: '수련생 퇴원',
      text: `${student.name} 수련생을 퇴원 처리하시겠습니까?`,
      confirmText: '퇴원',
      cancelText: '취소',
    });
    if (!isConfirmed) return;
    try {
      await deleteMutation.mutateAsync(student.id);
    } catch (error) {
      const axiosError = error as AxiosError<ErrorResponse>;
      const errorCode = axiosError.response?.data?.code || 'COMMON_003';
      showError(errorCode);
    }
  }

  async function handleBulkDeleteStudents() {
    if (selectedCount === 0) return;
    const { isConfirmed } = await confirmDelete({
      title: '수련생 일괄 퇴원',
      text: `선택한 ${selectedCount}명의 수련생을 퇴원 처리하시겠습니까?`,
      confirmText: `${selectedCount}명 퇴원`,
      cancelText: '취소',
    });
    if (!isConfirmed) return;
    try {
      await bulkDeleteMutation.mutateAsync(selectedStudentIds);
      setRowSelection({});
    } catch (error) {
      const axiosError = error as AxiosError<ErrorResponse>;
      const errorCode = axiosError.response?.data?.code || 'COMMON_003';
      showError(errorCode);
    }
  }

  async function handleRestoreStudent(student: StudentSummary) {
    const { isConfirmed } = await confirmDelete({
      title: '수련생 복구',
      text: `${student.name} 수련생을 복구하시겠습니까?`,
      confirmText: '복구',
      cancelText: '취소',
    });
    if (!isConfirmed) return;
    try {
      await restoreMutation.mutateAsync(student.id);
      showSuccess('수련생이 복구되었습니다.');
    } catch (error) {
      const axiosError = error as AxiosError<ErrorResponse>;
      const errorCode = axiosError.response?.data?.code || 'COMMON_003';
      showError(errorCode);
    }
  }

  function handleOpenStatusChangeDialog(student: StudentSummary, newStatus: StudentStatus) {
    setModalState({ type: 'statusChange', student, newStatus });
  }

  function handleCloseModal() {
    setModalState({ type: 'closed' });
  }

  // 폼 제출 핸들러
  async function handleFormSubmit(data: {
    name: string;
    birth: string;
    phone?: string;
    guardians: Array<{
      name: string;
      phone: string;
      relation: 'FATHER' | 'MOTHER' | 'GRANDPARENT' | 'OTHER';
      isPrimary: boolean;
    }>;
  }) {
    if (!dojangId) return;

    try {
      if (modalState.type === 'create') {
        const student = await createMutation.mutateAsync({
          name: data.name,
          birth: data.birth,
          phone: data.phone,
        });

        for (const guardian of data.guardians) {
          await studentService.addGuardian(dojangId, student.id, guardian);
        }
      } else if (modalState.type === 'edit') {
        await updateMutation.mutateAsync({
          studentId: modalState.student.id,
          data: {
            name: data.name,
            birth: data.birth,
            phone: data.phone,
          },
        });
      }
      handleCloseModal();
    } catch (error) {
      const axiosError = error as AxiosError<ErrorResponse>;
      const errorCode = axiosError.response?.data?.code || 'COMMON_003';
      showError(errorCode);
    }
  }

  // 상태 변경 핸들러
  async function handleConfirmStatusChange(reason?: string) {
    if (modalState.type === 'statusChange') {
      try {
        await updateMutation.mutateAsync({
          studentId: modalState.student.id,
          data: {
            name: modalState.student.name,
            birth: modalState.student.birth,
            status: modalState.newStatus,
            statusChangeReason: reason,
          },
        });
        handleCloseModal();
      } catch (error) {
        console.error('상태 변경 실패:', error);
      }
    }
  }

  const isFormLoading = createMutation.isPending || updateMutation.isPending;

  return (
    <div className="space-y-6 p-4 lg:p-8">
      {/* 헤더 */}
      <header className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">수련생 관리</h1>
          <p className="text-sm text-muted-foreground">
            {selectedDojang?.dojangName}의 수련생 목록
          </p>
        </div>
        <Button onClick={handleOpenCreateModal} disabled={!canCreate}>
          <Plus className="mr-2 h-4 w-4" />
          수련생 등록
        </Button>
      </header>

      {/* 툴바 (탭 필터 + 수련부/반 필터 + 검색 + 단체 액션) */}
      <StudentTableToolbar
        dojangId={dojangId}
        activeTab={activeTab}
        onActiveTabChange={setActiveTab}
        statusFilter={statusFilter}
        onStatusFilterChange={setStatusFilter}
        searchQuery={searchQuery}
        onSearchQueryChange={setSearchQuery}
        selectedCount={selectedCount}
        onBulkDelete={handleBulkDeleteStudents}
        stats={stats}
        sectionId={sectionId}
        divisionId={divisionId}
        onSectionChange={setSectionId}
        onDivisionChange={setDivisionId}
        canDelete={canDelete}
      />

      {/* 테이블 */}
      <StudentDataTable
        columns={columns}
        data={filteredStudents}
        isLoading={isLoading}
        onRowClick={handleRowClick}
        rowSelection={rowSelection}
        onRowSelectionChange={setRowSelection}
        globalFilter={searchQuery}
      />

      {/* 상세 Drawer */}
      <StudentDetailDrawer
        student={selectedStudent ?? null}
        isLoading={isLoadingDetail}
        isOpen={selectedStudentId !== null}
        onClose={handleCloseDrawer}
      />

      {/* 등록/수정 모달 */}
      <StudentFormModal
        isOpen={modalState.type === 'create' || modalState.type === 'edit'}
        onClose={handleCloseModal}
        onSubmit={handleFormSubmit}
        student={modalState.type === 'edit' ? modalState.student : null}
        isLoading={isFormLoading}
      />

      {/* 상태 변경 확인 다이얼로그 */}
      <StatusChangeDialog
        open={modalState.type === 'statusChange'}
        onOpenChange={(open) => {
          if (!open) handleCloseModal();
        }}
        studentName={modalState.type === 'statusChange' ? modalState.student.name : ''}
        currentStatus={modalState.type === 'statusChange' ? modalState.student.status : 'ACTIVE'}
        newStatus={modalState.type === 'statusChange' ? modalState.newStatus : 'ACTIVE'}
        onConfirm={handleConfirmStatusChange}
        isLoading={updateMutation.isPending}
      />
    </div>
  );
}
