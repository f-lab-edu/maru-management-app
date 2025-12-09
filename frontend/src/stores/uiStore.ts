import { create } from 'zustand';

interface GlobalLoading {
  isLoading: boolean;
  message: string;
}

interface UIState {
  globalLoading: GlobalLoading;
  showLoading: (message?: string) => void;
  hideLoading: () => void;
}

export const useUIStore = create<UIState>((set) => ({
  globalLoading: {
    isLoading: false,
    message: '',
  },

  showLoading: (message = '잠시만 기다려주세요...') => {
    set({ globalLoading: { isLoading: true, message } });
  },

  hideLoading: () => {
    set({ globalLoading: { isLoading: false, message: '' } });
  },
}));
