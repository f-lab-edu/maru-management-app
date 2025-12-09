import { useState, useMemo } from 'react';
import { RowSelectionState } from '@tanstack/react-table';
import { Plus } from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { useAuthStore } from '@/stores/authStore';
import { StudentDataTable } from '@/features/students/components/StudentDataTable';
import { StudentTableToolbar } from '@/features/students/components/StudentTableToolbar';
import { createStudentColumns } from '@/features/students/components/studentColumns';
import { StudentDetailDrawer } from '@/features/students/components/StudentDetailDrawer';
import { StudentFormModal } from '@/features/students/components/StudentFormModal';
import { DeleteStudentDialog } from '@/features/students/components/DeleteStudentDialog';
import { BulkDeleteDialog } from '@/features/students/components/BulkDeleteDialog';
import { StatusChangeDialog } from '@/features/students/components/StatusChangeDialog';
import {
  useStudents,
  useStudent,
  useCreateStudent,
  useUpdateStudent,
  useDeleteStudent,
  useBulkDeleteStudents,
} from '@/features/students/hooks/useStudents';
import { studentService } from '@/services/studentService';
import type { Student, StudentSummary, StudentStatus } from '@/types/student';

type StatusFilter = 'ALL' | 'ACTIVE' | 'PAUSED';

type ModalState =
  | { type: 'closed' }
  | { type: 'create' }
  | { type: 'edit'; student: Student }
  | { type: 'delete'; student: StudentSummary }
  | { type: 'bulkDelete' }
  | { type: 'statusChange'; student: StudentSummary; newStatus: StudentStatus };

export default function StudentListPage() {
  const { selectedDojang } = useAuthStore();
  const dojangId = selectedDojang?.dojangId ?? null;

  const { data: studentsData, isLoading } = useStudents(dojangId);

  // 필터 상태
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL');
  const [searchQuery, setSearchQuery] = useState('');

  // 선택 상태
  const [rowSelection, setRowSelection] = useState<RowSelectionState>({});

  // Drawer 상태
  const [selectedStudentId, setSelectedStudentId] = useState<number | null>(null);
  const { data: selectedStudent, isLoading: isLoadingDetail } = useStudent(
    dojangId,
    selectedStudentId
  );

  // 모달 상태
  const [modalState, setModalState] = useState<ModalState>({ type: 'closed' });

  // Mutations
  const createMutation = useCreateStudent(dojangId ?? 0);
  const updateMutation = useUpdateStudent(dojangId ?? 0);
  const deleteMutation = useDeleteStudent(dojangId ?? 0);
  const bulkDeleteMutation = useBulkDeleteStudents(dojangId ?? 0);

  // 필터링된 데이터
  const filteredStudents = useMemo(() => {
    if (!studentsData?.students) return [];

    return studentsData.students.filter((student) => {
      if (statusFilter !== 'ALL' && student.status !== statusFilter) {
        return false;
      }
      return true;
    });
  }, [studentsData?.students, statusFilter]);

  // 통계
  const stats = useMemo(() => {
    const students = studentsData?.students ?? [];
    return {
      total: students.length,
      activeCount: students.filter((s) => s.status === 'ACTIVE').length,
      pausedCount: students.filter((s) => s.status === 'PAUSED').length,
    };
  }, [studentsData?.students]);

  // 선택된 수련생 수
  const selectedCount = Object.keys(rowSelection).length;

  // 선택된 수련생 ID 목록
  const selectedStudentIds = useMemo(() => {
    return Object.keys(rowSelection)
      .filter((key) => rowSelection[key])
      .map((index) => filteredStudents[Number(index)]?.id)
      .filter((id): id is number => id !== undefined);
  }, [rowSelection, filteredStudents]);

  // 컬럼 정의
  const columns = useMemo(
    () =>
      createStudentColumns({
        onEdit: handleOpenEditModal,
        onDelete: handleOpenDeleteDialog,
        onStatusChange: handleOpenStatusChangeDialog,
      }),
    []
  );

  // Drawer 핸들러
  function handleRowClick(student: StudentSummary) {
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

  function handleOpenDeleteDialog(student: StudentSummary) {
    setModalState({ type: 'delete', student });
  }

  function handleOpenBulkDeleteDialog() {
    if (selectedCount > 0) {
      setModalState({ type: 'bulkDelete' });
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
      console.error('수련생 저장 실패:', error);
    }
  }

  // 삭제 확인 핸들러
  async function handleConfirmDelete() {
    if (modalState.type === 'delete') {
      await deleteMutation.mutateAsync(modalState.student.id);
      handleCloseModal();
    }
  }

  // 단체 삭제 핸들러
  async function handleConfirmBulkDelete() {
    try {
      await bulkDeleteMutation.mutateAsync(selectedStudentIds);
      setRowSelection({});
      handleCloseModal();
    } catch (error) {
      console.error('단체 삭제 실패:', error);
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
            phone: modalState.student.phone,
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
  const isDeleteLoading = deleteMutation.isPending || bulkDeleteMutation.isPending;

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
        <Button onClick={handleOpenCreateModal}>
          <Plus className="mr-2 h-4 w-4" />
          수련생 등록
        </Button>
      </header>

      {/* 툴바 (탭 필터 + 검색 + 단체 액션) */}
      <StudentTableToolbar
        statusFilter={statusFilter}
        onStatusFilterChange={setStatusFilter}
        searchQuery={searchQuery}
        onSearchQueryChange={setSearchQuery}
        selectedCount={selectedCount}
        onBulkDelete={handleOpenBulkDeleteDialog}
        stats={stats}
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

      {/* 개별 삭제 확인 다이얼로그 */}
      <DeleteStudentDialog
        isOpen={modalState.type === 'delete'}
        onClose={handleCloseModal}
        onConfirm={handleConfirmDelete}
        student={modalState.type === 'delete' ? modalState.student : null}
        isLoading={isDeleteLoading}
      />

      {/* 단체 삭제 확인 다이얼로그 */}
      <BulkDeleteDialog
        isOpen={modalState.type === 'bulkDelete'}
        onClose={handleCloseModal}
        onConfirm={handleConfirmBulkDelete}
        count={selectedCount}
        isLoading={isDeleteLoading}
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
