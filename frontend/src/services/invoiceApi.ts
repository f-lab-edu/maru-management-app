import apiClient from './api';
import type {
  InvoiceStatus,
  InvoiceCreateReq,
  InvoiceBulkCreateReq,
  InvoiceUpdateReq,
  InvoiceBulkUpdateReq,
  BulkIssueReq,
  PaymentRecordReq,
  InvoiceDetailRes,
  InvoiceListRes,
  BulkCreateRes,
  BulkIssueRes,
  BulkUpdateRes,
  UnpaidListRes,
  PaymentStatisticsRes,
  StudentPaymentHistoryRes,
  PrepaidPaymentReq,
  PrepaidPaymentRes,
  YearlyStatistics,
  PaymentLinkRes,
  BatchPaymentLinkReq,
  RefundReq,
  RefundRes,
} from '@/features/payments/types';

const INVOICE_BASE = '/invoices';
const PAYMENT_BASE = '/payments';

export interface InvoiceFilterParams {
  status?: InvoiceStatus;
  sectionId?: string;
  divisionId?: string;
}

export const invoiceApi = {
  getInvoices: async (
    dojangId: string,
    filters?: InvoiceFilterParams
  ): Promise<InvoiceListRes[]> => {
    const response = await apiClient.get<InvoiceListRes[]>(INVOICE_BASE, {
      params: { dojangId, ...filters },
    });
    return response.data;
  },

  getInvoice: async (dojangId: string, id: string): Promise<InvoiceDetailRes> => {
    const response = await apiClient.get<InvoiceDetailRes>(`${INVOICE_BASE}/${id}`, {
      params: { dojangId },
    });
    return response.data;
  },

  createInvoice: async (dojangId: string, request: InvoiceCreateReq): Promise<InvoiceDetailRes> => {
    const response = await apiClient.post<InvoiceDetailRes>(INVOICE_BASE, request, {
      params: { dojangId },
    });
    return response.data;
  },

  createBulkInvoices: async (
    dojangId: string,
    request: InvoiceBulkCreateReq
  ): Promise<BulkCreateRes> => {
    const response = await apiClient.post<BulkCreateRes>(`${INVOICE_BASE}/bulk`, request, {
      params: { dojangId },
    });
    return response.data;
  },

  updateInvoice: async (
    dojangId: string,
    id: string,
    request: InvoiceUpdateReq
  ): Promise<InvoiceDetailRes> => {
    const response = await apiClient.patch<InvoiceDetailRes>(`${INVOICE_BASE}/${id}`, request, {
      params: { dojangId },
    });
    return response.data;
  },

  issueInvoice: async (dojangId: string, id: string): Promise<InvoiceDetailRes> => {
    const response = await apiClient.patch<InvoiceDetailRes>(
      `${INVOICE_BASE}/${id}/issue`,
      null,
      { params: { dojangId } }
    );
    return response.data;
  },

  voidInvoice: async (dojangId: string, id: string): Promise<InvoiceDetailRes> => {
    const response = await apiClient.patch<InvoiceDetailRes>(
      `${INVOICE_BASE}/${id}/void`,
      null,
      { params: { dojangId } }
    );
    return response.data;
  },

  restoreInvoice: async (dojangId: string, id: string): Promise<InvoiceDetailRes> => {
    const response = await apiClient.patch<InvoiceDetailRes>(
      `${INVOICE_BASE}/${id}/restore`,
      null,
      { params: { dojangId } }
    );
    return response.data;
  },

  deleteInvoice: async (dojangId: string, id: string): Promise<void> => {
    await apiClient.delete(`${INVOICE_BASE}/${id}`, {
      params: { dojangId },
    });
  },

  recordPayment: async (
    dojangId: string,
    invoiceId: string,
    request: PaymentRecordReq
  ): Promise<InvoiceDetailRes> => {
    const response = await apiClient.post<InvoiceDetailRes>(
      `${INVOICE_BASE}/${invoiceId}/payments`,
      request,
      { params: { dojangId } }
    );
    return response.data;
  },

  cancelPayment: async (
    dojangId: string,
    invoiceId: string,
    paymentId: string
  ): Promise<InvoiceDetailRes> => {
    const response = await apiClient.delete<InvoiceDetailRes>(
      `${INVOICE_BASE}/${invoiceId}/payments/${paymentId}`,
      { params: { dojangId } }
    );
    return response.data;
  },

  bulkIssueInvoices: async (dojangId: string, request: BulkIssueReq): Promise<BulkIssueRes> => {
    const response = await apiClient.post<BulkIssueRes>(
      `${INVOICE_BASE}/bulk-issue`,
      request,
      { params: { dojangId } }
    );
    return response.data;
  },

  bulkUpdateInvoices: async (
    dojangId: string,
    request: InvoiceBulkUpdateReq
  ): Promise<BulkUpdateRes> => {
    const response = await apiClient.patch<BulkUpdateRes>(
      `${INVOICE_BASE}/bulk-update`,
      request,
      { params: { dojangId } }
    );
    return response.data;
  },

  getUnpaidList: async (
    dojangId: string,
    sectionId?: string,
    divisionId?: string
  ): Promise<UnpaidListRes[]> => {
    const response = await apiClient.get<UnpaidListRes[]>(`${PAYMENT_BASE}/unpaid`, {
      params: { dojangId, sectionId, divisionId },
    });
    return response.data;
  },

  getPaymentStatistics: async (
    dojangId: string,
    year: number,
    month: number
  ): Promise<PaymentStatisticsRes> => {
    const response = await apiClient.get<PaymentStatisticsRes>(`${PAYMENT_BASE}/statistics`, {
      params: { dojangId, year, month },
    });
    return response.data;
  },

  getYearStatistics: async (
    dojangId: string,
    year: number
  ): Promise<YearlyStatistics> => {
    const response = await apiClient.get<YearlyStatistics>(`${PAYMENT_BASE}/statistics/yearly`, {
      params: { dojangId, year },
    });
    return response.data;
  },

  getStudentPaymentHistory: async (
    dojangId: string,
    studentId: string
  ): Promise<StudentPaymentHistoryRes> => {
    const response = await apiClient.get<StudentPaymentHistoryRes>(
      `${PAYMENT_BASE}/students/${studentId}/history`,
      { params: { dojangId } }
    );
    return response.data;
  },

  processPrepaidPayment: async (
    dojangId: string,
    request: PrepaidPaymentReq
  ): Promise<PrepaidPaymentRes> => {
    const response = await apiClient.post<PrepaidPaymentRes>(
      `${INVOICE_BASE}/prepaid-payment`,
      request,
      { params: { dojangId } }
    );
    return response.data;
  },

  sendPaymentLink: async (dojangId: string, invoiceId: string): Promise<PaymentLinkRes> => {
    const response = await apiClient.post<PaymentLinkRes>(
      `${INVOICE_BASE}/${invoiceId}/payment-links`,
      null,
      { params: { dojangId } }
    );
    return response.data;
  },

  sendBatchPaymentLinks: async (
    dojangId: string,
    request: BatchPaymentLinkReq
  ): Promise<PaymentLinkRes[]> => {
    const response = await apiClient.post<PaymentLinkRes[]>(
      `${INVOICE_BASE}/payment-links/batch`,
      request,
      { params: { dojangId } }
    );
    return response.data;
  },

  refundPayment: async (
    dojangId: string,
    paymentId: string,
    request: RefundReq
  ): Promise<RefundRes> => {
    const response = await apiClient.post<RefundRes>(
      `${PAYMENT_BASE}/${paymentId}/refunds`,
      request,
      { params: { dojangId } }
    );
    return response.data;
  },
};
