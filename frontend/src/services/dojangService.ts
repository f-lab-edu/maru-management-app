import apiClient from './api';

export interface DojangMe {
  id: string;
  name: string;
  phone: string | null;
  address: string | null;
  defaultTuition: number | null;
  autoInvoiceEnabled: boolean;
  autoInvoiceDay: number | null;
  autoInvoiceHour: number;
  autoAbsenceHour: number;
}

export interface DojangUpdateReq {
  name?: string;
  phone?: string | null;
  address?: string | null;
  defaultTuition?: number | null;
  autoInvoiceEnabled?: boolean;
  autoInvoiceDay?: number | null;
  autoInvoiceHour?: number;
  autoAbsenceHour?: number;
}

export const dojangService = {
  getMyDojang: async (): Promise<DojangMe> => {
    const response = await apiClient.get<DojangMe>('/dojangs/me');
    return response.data;
  },

  updateMyDojang: async (data: DojangUpdateReq): Promise<DojangMe> => {
    const response = await apiClient.patch<DojangMe>('/dojangs/me', data);
    return response.data;
  },
};
