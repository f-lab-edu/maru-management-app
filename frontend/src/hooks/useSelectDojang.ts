import { useMutation, useQueryClient } from '@tanstack/react-query';
import { authService } from '../services/authService';
import { useAuthStore } from '../stores/authStore';
import { DojangSummary } from '../types/auth';
import { useUser } from './useUser';

export function useSelectDojang() {
  const queryClient = useQueryClient();
  const { data: user } = useUser();
  const setSelectedDojang = useAuthStore((state) => state.setSelectedDojang);
  const clearSelectedDojang = useAuthStore((state) => state.clearSelectedDojang);

  return useMutation({
    mutationFn: (dojang: DojangSummary) => authService.selectDojang(dojang.dojangId),
    onSuccess: (_, dojang) => {
      if (user?.id) {
        setSelectedDojang(dojang, user.id);
      }
      queryClient.invalidateQueries();
    },
    onError: () => {
      clearSelectedDojang(user?.id ?? null);
    },
  });
}
