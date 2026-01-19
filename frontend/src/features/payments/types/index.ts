export type InvoiceStatus = 'DRAFT' | 'OPEN' | 'PARTIAL' | 'PAID' | 'VOID';
export type PaymentMethod = 'CASH' | 'CARD' | 'TRANSFER' | 'PG' | 'OTHER';
export type PaymentStatus = 'COMPLETED' | 'REFUNDED';

export interface InvoiceListRes {
  id: string;
  studentName: string;
  amount: number;
  paidAmount: number;
  remainingAmount: number;
  status: InvoiceStatus;
  dueDate: string;
  issueDate: string | null;
  billingYearMonth: string;
  studentDeleted: boolean;
}

export interface PaymentRes {
  id: string;
  amount: number;
  method: PaymentMethod;
  status: PaymentStatus;
  paidAt: string;
  refundedAt: string | null;
}

export interface InvoiceDetailRes {
  id: string;
  studentId: string;
  studentName: string;
  amount: number;
  paidAmount: number;
  remainingAmount: number;
  status: InvoiceStatus;
  dueDate: string;
  issueDate: string | null;
  note: string | null;
  billingYearMonth: string;
  studentDeleted: boolean;
  payments: PaymentRes[];
}

export interface InvoiceCreateReq {
  studentId: string;
  amount: number;
  dueDate: string;
  note?: string;
  billingYearMonth?: string;
}

export interface InvoiceBulkCreateReq {
  studentIds?: string[];
  defaultAmount: number;
  dueDate: string;
  note?: string;
  billingYearMonth?: string;
}

export interface BulkCreateRes {
  createdCount: number;
  skippedCount: number;
}

export interface InvoiceUpdateReq {
  amount?: number;
  dueDate?: string;
  note?: string;
}

export interface InvoiceBulkUpdateReq {
  invoiceIds: string[];
  amount?: number;
  dueDate?: string;
  note?: string;
}

export interface BulkUpdateRes {
  totalCount: number;
  updatedCount: number;
  skippedCount: number;
}

export interface BulkIssueReq {
  invoiceIds: string[];
}

export interface BulkIssueRes {
  issuedCount: number;
  failedCount: number;
  failedInvoices: FailedInvoice[];
}

export interface FailedInvoice {
  invoiceId: string;
  reason: string;
}

export interface PaymentRecordReq {
  amount: number;
  method: PaymentMethod;
}

export interface GuardianInfo {
  name: string;
  phone: string;
}

export interface UnpaidListRes {
  invoiceId: string;
  studentName: string;
  guardians: GuardianInfo[];
  amount: number;
  paidAmount: number;
  remainingAmount: number;
  dueDate: string;
  overdueDays: number;
  billingYearMonth: string;
}

export interface PaymentStatisticsRes {
  totalPaidAmount: number;
  totalUnpaidAmount: number;
  paidInvoiceCount: number;
  unpaidInvoiceCount: number;
  partialInvoiceCount: number;
}

export interface StudentPaymentHistoryRes {
  studentId: string;
  studentName: string;
  totalPaidAmount: number;
  payments: PaymentHistoryItem[];
}

export interface PaymentHistoryItem {
  paymentId: string;
  invoiceId: string;
  billingYearMonth: string;
  amount: number;
  method: string;
  status: string;
  paidAt: string;
  refundedAt: string | null;
}

export const INVOICE_STATUS_LABEL: Record<InvoiceStatus, string> = {
  DRAFT: '임시저장',
  OPEN: '발행됨',
  PARTIAL: '부분납부',
  PAID: '완납',
  VOID: '무효',
};

export const INVOICE_STATUS_COLOR: Record<InvoiceStatus, string> = {
  DRAFT: 'bg-gray-100 text-gray-800',
  OPEN: 'bg-blue-100 text-blue-800',
  PARTIAL: 'bg-orange-100 text-orange-800',
  PAID: 'bg-green-100 text-green-800',
  VOID: 'bg-red-100 text-red-800',
};

export const PAYMENT_METHOD_LABEL: Record<PaymentMethod, string> = {
  CASH: '현금',
  CARD: '카드',
  TRANSFER: '계좌이체',
  PG: 'PG결제',
  OTHER: '기타',
};

export interface PrepaidPaymentReq {
  studentId: string;
  startYearMonth: string;
  endYearMonth: string;
  monthlyAmount: number;
  totalAmount: number;
  paymentMethod: PaymentMethod;
  dueDate?: string;
  note?: string;
}

export interface PrepaidPaymentRes {
  invoiceCount: number;
  totalAmount: number;
  discountAmount: number;
  invoiceIds: string[];
}

export interface MonthlyStatisticsData {
  month: number;
  totalAmount: number;
  paidAmount: number;
  unpaidAmount: number;
  paidCount: number;
  unpaidCount: number;
  partialCount: number;
}

export interface YearlyStatistics {
  year: number;
  totalPaidAmount: number;
  totalUnpaidAmount: number;
  paidInvoiceCount: number;
  unpaidInvoiceCount: number;
  partialInvoiceCount: number;
  monthlyData: MonthlyStatisticsData[];
}
