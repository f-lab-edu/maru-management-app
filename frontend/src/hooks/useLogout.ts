import { useMutation, useQueryClient } from '@tanstack/react-query';
import { authService } from '../services/authService';
import { useAuthStore } from '../stores/authStore';
import { useUser } from './useUser';
import { userKeys } from './useUser';

export function useLogout() {
  const queryClient = useQueryClient();
  const { data: user } = useUser();
  const clearSelectedDojang = useAuthStore((state) => state.clearSelectedDojang);

  return useMutation({
    mutationFn: authService.logout,
    onSettled: () => {
      clearSelectedDojang(user?.id ?? null);
      queryClient.setQueryData(userKeys.me(), null);
      queryClient.removeQueries({ queryKey: userKeys.dojangs() });
    },
  });
}
