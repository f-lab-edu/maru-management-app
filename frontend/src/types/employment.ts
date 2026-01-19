export type EmploymentStatus = 'PENDING' | 'ACTIVE' | 'SUSPENDED' | 'REJECTED' | 'LEFT';

export interface DojangSearchResult {
  id: string;
  name: string;
  address: string;
  ownerName: string;
}

export interface Employment {
  id: string;
  userId: string;
  dojangId: string;
  dojangName: string;
  status: EmploymentStatus;
  requestedAt: string;
  approvedAt?: string;
  rejectedAt?: string;
}

export interface PendingApprovalRequest {
  id: string;
  userId: string;
  userName: string;
  userEmail: string;
  userPhone?: string;
  requestedAt: string;
  status: EmploymentStatus;
}

export interface Instructor {
  id: string;
  userId: string;
  name: string;
  email: string;
  phone?: string;
  status: EmploymentStatus;
  joinedAt: string;
  suspendedAt?: string;
}

export interface InstructorDetail extends Instructor {
  permissions: string[];
}
