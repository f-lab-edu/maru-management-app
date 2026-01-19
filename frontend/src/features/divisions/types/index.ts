// Section (수련부)
export interface SectionRes {
  id: string;
  name: string;
  displayOrder: number;
  divisionCount: number;
}

export interface SectionListRes {
  sections: SectionRes[];
}

export interface SectionCreateReq {
  name: string;
}

export interface SectionUpdateReq {
  name: string;
}

export interface SectionReorderReq {
  sectionIds: string[];
}

// Division (수련반)
export type DayOfWeek = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';

export interface DivisionRes {
  id: string;
  sectionId: string;
  sectionName: string;
  name: string;
  displayOrder: number;
  scheduleDays: DayOfWeek[];
  startTime: string | null;
  endTime: string | null;
  studentCount: number;
}

export interface DivisionListRes {
  divisions: DivisionRes[];
}

export interface DivisionDetailRes {
  id: string;
  sectionId: string;
  sectionName: string;
  name: string;
  displayOrder: number;
  scheduleDays: DayOfWeek[];
  startTime: string | null;
  endTime: string | null;
  studentCount: number;
}

export interface DivisionCreateReq {
  sectionId: string;
  name: string;
  scheduleDays?: DayOfWeek[];
  startTime?: string;
  endTime?: string;
}

export interface DivisionUpdateReq {
  name?: string;
  scheduleDays?: DayOfWeek[];
  startTime?: string;
  endTime?: string;
}

export interface DivisionReorderReq {
  sectionId: string;
  divisionIds: string[];
}

// Enrollment (원생 등록)
export interface EnrolledStudentRes {
  studentId: string;
  studentName: string;
  enrolledAt: string;
}

export interface EnrolledStudentListRes {
  students: EnrolledStudentRes[];
}

export interface BulkEnrollmentReq {
  studentIds: string[];
}

export interface BulkEnrollmentRes {
  enrolledCount: number;
  skippedCount: number;
  skippedStudentIds: string[];
}
