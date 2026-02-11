import axios from 'axios';

const baseURL = import.meta.env.VITE_API_BASE_URL;

const paymentClient = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
});

export interface PaymentPageRes {
  invoiceId: string;
  studentName: string;
  billingYearMonth: string;
  totalAmount: number;
  remainingAmount: number;
  dojangName: string;
  clientKey: string;
  orderId: string;
}

export interface PgPaymentConfirmReq {
  paymentKey: string;
  orderId: string;
  amount: number;
}

export interface PgPaymentConfirmRes {
  paymentId: string;
  paymentKey: string;
  orderId: string;
  amount: number;
  pgMethod: string;
  receiptUrl: string;
}

export const paymentPageApi = {
  getPaymentInfo: async (token: string): Promise<PaymentPageRes> => {
    const response = await paymentClient.get<PaymentPageRes>(`/pay/${token}`);
    return response.data;
  },

  confirmPayment: async (token: string, request: PgPaymentConfirmReq): Promise<PgPaymentConfirmRes> => {
    const response = await paymentClient.post<PgPaymentConfirmRes>(`/pay/${token}/confirm`, request);
    return response.data;
  },
};
