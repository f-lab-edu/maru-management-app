import type { RangeStudentRow, AttendanceStatus } from '../types';

interface AttendanceSummary {
  totalStudents: number;
  present: number;
  absent: number;
  sick: number;
  excused: number;
}

/**
 * 기간별 출석 데이터에서 통계 계산
 */
export function calculateAttendanceSummary(
  students: RangeStudentRow[],
  dates: string[]
): AttendanceSummary {
  const summary: AttendanceSummary = {
    totalStudents: students.length,
    present: 0,
    absent: 0,
    sick: 0,
    excused: 0,
  };

  // 모든 학생의 모든 날짜에 대한 출석 상태를 집계
  students.forEach((student) => {
    dates.forEach((date) => {
      const attendance = student.attendances[date];
      if (attendance) {
        switch (attendance.status) {
          case 'PRESENT':
            summary.present++;
            break;
          case 'ABSENT':
            summary.absent++;
            break;
          case 'SICK':
            summary.sick++;
            break;
          case 'EXCUSED':
            summary.excused++;
            break;
        }
      }
    });
  });

  return summary;
}

/**
 * 오늘 날짜 기준 통계 계산
 */
export function calculateTodaySummary(
  students: RangeStudentRow[],
  todayDate: string
): AttendanceSummary {
  return calculateAttendanceSummary(students, [todayDate]);
}

/**
 * 필터링된 학생 목록에서 특정 상태의 학생 수 계산
 */
export function countByStatus(
  students: RangeStudentRow[],
  date: string,
  status: AttendanceStatus
): number {
  return students.filter((student) => {
    const attendance = student.attendances[date];
    return attendance?.status === status;
  }).length;
}
