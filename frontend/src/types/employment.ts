export type EmploymentStatus = 'PENDING' | 'ACTIVE' | 'SUSPENDED' | 'REJECTED' | 'LEFT';

export interface DojangSearchResult {
  id: number;
  name: string;
  address: string;
  ownerName: string;
  phone?: string;
}

export interface Employment {
  id: number;
  userId: number;
  dojangId: number;
  dojangName: string;
  status: EmploymentStatus;
  requestedAt: string;
  approvedAt?: string;
  rejectedAt?: string;
}

export interface PendingApprovalRequest {
  id: number;
  userId: number;
  userName: string;
  userEmail: string;
  userPhone?: string;
  requestedAt: string;
  status: EmploymentStatus;
}
