import { useQuery, useQueries, useMutation, useQueryClient } from '@tanstack/react-query';
import { invoiceApi } from '@/services/invoiceApi';
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
} from '../types';

export const invoiceKeys = {
  all: ['invoices'] as const,
  lists: () => [...invoiceKeys.all, 'list'] as const,
  list: (dojangId: string, status?: InvoiceStatus) =>
    [...invoiceKeys.lists(), { dojangId, status }] as const,
  details: () => [...invoiceKeys.all, 'detail'] as const,
  detail: (dojangId: string, id: string) => [...invoiceKeys.details(), dojangId, id] as const,
};

export const paymentKeys = {
  all: ['payments'] as const,
  unpaid: (dojangId: string) => [...paymentKeys.all, 'unpaid', dojangId] as const,
  statistics: (dojangId: string, year: number, month: number) =>
    [...paymentKeys.all, 'statistics', dojangId, year, month] as const,
  studentHistory: (dojangId: string, studentId: string) =>
    [...paymentKeys.all, 'history', dojangId, studentId] as const,
};

export function useInvoices(dojangId: string | null, status?: InvoiceStatus) {
  return useQuery<InvoiceListRes[], Error>({
    queryKey: invoiceKeys.list(dojangId!, status),
    queryFn: () => invoiceApi.getInvoices(dojangId!, status),
    enabled: !!dojangId,
  });
}

export function useInvoice(dojangId: string | null, id: string | null) {
  return useQuery<InvoiceDetailRes, Error>({
    queryKey: invoiceKeys.detail(dojangId!, id!),
    queryFn: () => invoiceApi.getInvoice(dojangId!, id!),
    enabled: !!dojangId && !!id,
  });
}

export function useCreateInvoice(dojangId: string | null) {
  const queryClient = useQueryClient();

  return useMutation<InvoiceDetailRes, Error, InvoiceCreateReq>({
    mutationFn: (request) => {
      if (!dojangId) throw new Error('도장 ID가 필요합니다');
      return invoiceApi.createInvoice(dojangId, request);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: invoiceKeys.all });
      queryClient.invalidateQueries({ queryKey: paymentKeys.all });
    },
  });
}

export function useCreateBulkInvoices(dojangId: string | null) {
  const queryClient = useQueryClient();

  return useMutation<BulkCreateRes, Error, InvoiceBulkCreateReq>({
    mutationFn: (request) => {
      if (!dojangId) throw new Error('도장 ID가 필요합니다');
      return invoiceApi.createBulkInvoices(dojangId, request);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: invoiceKeys.all });
      queryClient.invalidateQueries({ queryKey: paymentKeys.all });
    },
  });
}

export function useUpdateInvoice(dojangId: string | null) {
  const queryClient = useQueryClient();

  return useMutation<InvoiceDetailRes, Error, { id: string; request: InvoiceUpdateReq }>({
    mutationFn: ({ id, request }) => {
      if (!dojangId) throw new Error('도장 ID가 필요합니다');
      return invoiceApi.updateInvoice(dojangId, id, request);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: invoiceKeys.all });
      queryClient.invalidateQueries({ queryKey: paymentKeys.all });
    },
  });
}

export function useIssueInvoice(dojangId: string | null) {
  const queryClient = useQueryClient();

  return useMutation<InvoiceDetailRes, Error, string>({
    mutationFn: (id) => {
      if (!dojangId) throw new Error('도장 ID가 필요합니다');
      return invoiceApi.issueInvoice(dojangId, id);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: invoiceKeys.all });
      queryClient.invalidateQueries({ queryKey: paymentKeys.all });
    },
  });
}

export function useVoidInvoice(dojangId: string | null) {
  const queryClient = useQueryClient();

  return useMutation<InvoiceDetailRes, Error, string>({
    mutationFn: (id) => {
      if (!dojangId) throw new Error('도장 ID가 필요합니다');
      return invoiceApi.voidInvoice(dojangId, id);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: invoiceKeys.all });
      queryClient.invalidateQueries({ queryKey: paymentKeys.all });
    },
  });
}

export function useRestoreInvoice(dojangId: string | null) {
  const queryClient = useQueryClient();

  return useMutation<InvoiceDetailRes, Error, string>({
    mutationFn: (id) => {
      if (!dojangId) throw new Error('도장 ID가 필요합니다');
      return invoiceApi.restoreInvoice(dojangId, id);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: invoiceKeys.all });
      queryClient.invalidateQueries({ queryKey: paymentKeys.all });
    },
  });
}

export function useDeleteInvoice(dojangId: string | null) {
  const queryClient = useQueryClient();

  return useMutation<void, Error, string>({
    mutationFn: (id) => {
      if (!dojangId) throw new Error('도장 ID가 필요합니다');
      return invoiceApi.deleteInvoice(dojangId, id);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: invoiceKeys.all });
      queryClient.invalidateQueries({ queryKey: paymentKeys.all });
    },
  });
}

export function useRecordPayment(dojangId: string | null) {
  const queryClient = useQueryClient();

  return useMutation<
    InvoiceDetailRes,
    Error,
    { invoiceId: string; request: PaymentRecordReq }
  >({
    mutationFn: ({ invoiceId, request }) => {
      if (!dojangId) throw new Error('도장 ID가 필요합니다');
      return invoiceApi.recordPayment(dojangId, invoiceId, request);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: invoiceKeys.all });
      queryClient.invalidateQueries({ queryKey: paymentKeys.all });
    },
  });
}

export function useCancelPayment(dojangId: string | null) {
  const queryClient = useQueryClient();

  return useMutation<InvoiceDetailRes, Error, { invoiceId: string; paymentId: string }>({
    mutationFn: ({ invoiceId, paymentId }) => {
      if (!dojangId) throw new Error('도장 ID가 필요합니다');
      return invoiceApi.cancelPayment(dojangId, invoiceId, paymentId);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: invoiceKeys.all });
      queryClient.invalidateQueries({ queryKey: paymentKeys.all });
    },
  });
}

export function useBulkIssueInvoices(dojangId: string | null) {
  const queryClient = useQueryClient();

  return useMutation<BulkIssueRes, Error, BulkIssueReq>({
    mutationFn: (request) => {
      if (!dojangId) throw new Error('도장 ID가 필요합니다');
      return invoiceApi.bulkIssueInvoices(dojangId, request);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: invoiceKeys.all });
      queryClient.invalidateQueries({ queryKey: paymentKeys.all });
    },
  });
}

export function useBulkUpdateInvoices(dojangId: string | null) {
  const queryClient = useQueryClient();

  return useMutation<BulkUpdateRes, Error, InvoiceBulkUpdateReq>({
    mutationFn: (request) => {
      if (!dojangId) throw new Error('도장 ID가 필요합니다');
      return invoiceApi.bulkUpdateInvoices(dojangId, request);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: invoiceKeys.all });
      queryClient.invalidateQueries({ queryKey: paymentKeys.all });
    },
  });
}

export function useUnpaidList(dojangId: string | null) {
  return useQuery<UnpaidListRes[], Error>({
    queryKey: paymentKeys.unpaid(dojangId!),
    queryFn: () => invoiceApi.getUnpaidList(dojangId!),
    enabled: !!dojangId,
  });
}

export function usePaymentStatistics(dojangId: string | null, year: number, month: number) {
  return useQuery<PaymentStatisticsRes, Error>({
    queryKey: paymentKeys.statistics(dojangId!, year, month),
    queryFn: () => invoiceApi.getPaymentStatistics(dojangId!, year, month),
    enabled: !!dojangId && year > 0 && month > 0,
  });
}

export function useStudentPaymentHistory(dojangId: string | null, studentId: string | null) {
  return useQuery<StudentPaymentHistoryRes, Error>({
    queryKey: paymentKeys.studentHistory(dojangId!, studentId!),
    queryFn: () => invoiceApi.getStudentPaymentHistory(dojangId!, studentId!),
    enabled: !!dojangId && !!studentId,
  });
}

export function useYearlyStatistics(dojangId: string | null, year: number) {
  const months = Array.from({ length: 12 }, (_, i) => i + 1);

  const queries = useQueries({
    queries: months.map((month) => ({
      queryKey: paymentKeys.statistics(dojangId!, year, month),
      queryFn: () => invoiceApi.getPaymentStatistics(dojangId!, year, month),
      enabled: !!dojangId && year > 0,
    })),
  });

  const isLoading = queries.some((q) => q.isLoading);
  const isError = queries.some((q) => q.isError);

  const data: YearlyStatistics | undefined = !isLoading && !isError
    ? {
        year,
        totalPaidAmount: queries.reduce((sum, q) => sum + (q.data?.totalPaidAmount ?? 0), 0),
        totalUnpaidAmount: queries.reduce((sum, q) => sum + (q.data?.totalUnpaidAmount ?? 0), 0),
        paidInvoiceCount: queries.reduce((sum, q) => sum + (q.data?.paidInvoiceCount ?? 0), 0),
        unpaidInvoiceCount: queries.reduce((sum, q) => sum + (q.data?.unpaidInvoiceCount ?? 0), 0),
        partialInvoiceCount: queries.reduce((sum, q) => sum + (q.data?.partialInvoiceCount ?? 0), 0),
        monthlyData: months.map((month, i) => ({
          month,
          totalAmount: (queries[i].data?.totalPaidAmount ?? 0) + (queries[i].data?.totalUnpaidAmount ?? 0),
          paidAmount: queries[i].data?.totalPaidAmount ?? 0,
          unpaidAmount: queries[i].data?.totalUnpaidAmount ?? 0,
          paidCount: queries[i].data?.paidInvoiceCount ?? 0,
          unpaidCount: queries[i].data?.unpaidInvoiceCount ?? 0,
          partialCount: queries[i].data?.partialInvoiceCount ?? 0,
        })),
      }
    : undefined;

  return { data, isLoading, isError };
}

export { useInvoiceActions } from './useInvoiceActions';
export type { UpdateData } from './useInvoiceActions';

export function usePrepaidPayment(dojangId: string | null) {
  const queryClient = useQueryClient();

  return useMutation<PrepaidPaymentRes, Error, PrepaidPaymentReq>({
    mutationFn: (request) => {
      if (!dojangId) throw new Error('도장 ID가 필요합니다');
      return invoiceApi.processPrepaidPayment(dojangId, request);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: invoiceKeys.all });
      queryClient.invalidateQueries({ queryKey: paymentKeys.all });
    },
  });
}
