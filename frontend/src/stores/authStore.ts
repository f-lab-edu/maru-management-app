import { create } from 'zustand';
import { DojangSummary } from '../types/auth';

const STORAGE_KEY_PREFIX = 'maru:selected-dojang:';

interface AuthState {
  selectedDojang: DojangSummary | null;
  setSelectedDojang: (dojang: DojangSummary, userId: number) => void;
  clearSelectedDojang: (userId: number | null) => void;
  getLastSelectedDojangId: (userId: number | null) => number | null;
}

export const useAuthStore = create<AuthState>((set) => ({
  selectedDojang: null,

  setSelectedDojang: (dojang, userId) => {
    localStorage.setItem(`${STORAGE_KEY_PREFIX}${userId}`, JSON.stringify(dojang));
    set({ selectedDojang: dojang });
  },

  clearSelectedDojang: (userId) => {
    if (userId) {
      localStorage.removeItem(`${STORAGE_KEY_PREFIX}${userId}`);
    }
    set({ selectedDojang: null });
  },

  getLastSelectedDojangId: (userId) => {
    if (!userId) return null;
    const key = `${STORAGE_KEY_PREFIX}${userId}`;
    try {
      const stored = localStorage.getItem(key);
      if (stored) {
        const parsed = JSON.parse(stored);
        return parsed.dojangId ?? null;
      }
    } catch {
      localStorage.removeItem(key);
    }
    return null;
  },
}));
