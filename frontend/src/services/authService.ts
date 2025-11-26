import apiClient from './api';

export type OAuthProvider = 'google' | 'kakao';

interface OAuthUrlRes {
  authorizationUrl: string;
}

export const authService = {
  getOAuthUrl: async (provider: OAuthProvider): Promise<string> => {
    const response = await apiClient.get<OAuthUrlRes>(`/auth/oauth/${provider}`);
    return response.data.authorizationUrl;
  },

  handleOAuthCallback: async (provider: OAuthProvider, code: string): Promise<void> => {
    await apiClient.post(`/auth/oauth/callback/${provider}`, { code });
  },

  refreshToken: async (): Promise<void> => {
    await apiClient.post('/auth/refresh');
  },

  logout: async (): Promise<void> => {
    await apiClient.post('/auth/logout');
  },
};
