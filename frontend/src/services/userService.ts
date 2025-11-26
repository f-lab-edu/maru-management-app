import apiClient from './api';
import { User, UserRole, OnboardingStep } from '../types/auth';

export interface OnboardingProfileReq {
  name: string;
  email: string;
  phone: string;
}

export interface OnboardingProfileRes {
  userId: number;
  name: string;
  email: string;
  phone: string;
  onboardingStep: OnboardingStep;
}

export interface OnboardingRoleReq {
  role: UserRole;
}

export interface OnboardingRoleRes {
  userId: number;
  role: UserRole;
  onboardingStep: OnboardingStep;
}

export interface OnboardingDojangReq {
  name: string;
  address: string;
  phone: string;
}

export interface OnboardingDojangRes {
  userId: number;
  tenantId: number;
  dojang: {
    id: number;
    name: string;
    address: string;
    phone: string;
  };
  onboardingStep: OnboardingStep;
}

export const userService = {
  getMe: async (): Promise<User> => {
    const response = await apiClient.get<User>('/users/me');
    return response.data;
  },

  updateProfile: async (data: OnboardingProfileReq): Promise<OnboardingProfileRes> => {
    const response = await apiClient.post<OnboardingProfileRes>('/users/onboarding/profile', data);
    return response.data;
  },

  selectRole: async (role: UserRole): Promise<OnboardingRoleRes> => {
    const response = await apiClient.post<OnboardingRoleRes>('/users/onboarding/role', { role });
    return response.data;
  },

  createDojang: async (data: OnboardingDojangReq): Promise<OnboardingDojangRes> => {
    const response = await apiClient.post<OnboardingDojangRes>('/users/onboarding/dojang', data);
    return response.data;
  },
};
