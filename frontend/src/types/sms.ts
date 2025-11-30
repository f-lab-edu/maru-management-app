export interface SmsSendRequest {
  phone: string;
}

export interface SmsSendResponse {
  phone: string;
  expiresIn: number;
  message: string;
}

export interface SmsVerifyRequest {
  phone: string;
  code: string;
}

export interface SmsVerifyResponse {
  verified: boolean;
  userId: number;
  isExistingUser: boolean;
}
