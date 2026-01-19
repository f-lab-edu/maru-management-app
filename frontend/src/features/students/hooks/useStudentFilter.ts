import { useState, useMemo } from 'react';
import type { StudentSummary, StudentStats } from '@/types/student';

export type StatusFilter = 'ALL' | 'ACTIVE' | 'PAUSED';

export function useStudentFilter(students: StudentSummary[] | undefined) {
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL');

  const filteredStudents = useMemo(() => {
    if (!students) return [];
    if (statusFilter === 'ALL') return students;
    return students.filter((s) => s.status === statusFilter);
  }, [students, statusFilter]);

  const stats = useMemo<StudentStats>(() => {
    if (!students) return { activeCount: 0, pausedCount: 0 };
    return {
      activeCount: students.filter((s) => s.status === 'ACTIVE').length,
      pausedCount: students.filter((s) => s.status === 'PAUSED').length,
    };
  }, [students]);

  return {
    statusFilter,
    setStatusFilter,
    filteredStudents,
    stats,
  };
}
