import apiClient from './api';
import { SmsSendRequest, SmsSendResponse, SmsVerifyRequest, SmsVerifyResponse } from '../types/sms';

export const smsService = {
  sendVerificationCode: async (phone: string): Promise<SmsSendResponse> => {
    const request: SmsSendRequest = { phone };
    const response = await apiClient.post<SmsSendResponse>('/sms/send', request);
    return response.data;
  },

  verifyCode: async (phone: string, code: string): Promise<SmsVerifyResponse> => {
    const request: SmsVerifyRequest = { phone, code };
    const response = await apiClient.post<SmsVerifyResponse>('/sms/verify', request);
    return response.data;
  },
};
