import { useQuery } from '@tanstack/react-query';
import { userService } from '../services/userService';
import { User } from '../types/auth';

export const userKeys = {
  all: ['user'] as const,
  me: () => [...userKeys.all, 'me'] as const,
  dojangs: () => [...userKeys.all, 'dojangs'] as const,
};

export function useUser() {
  return useQuery<User>({
    queryKey: userKeys.me(),
    queryFn: userService.getMe,
    retry: false,
    staleTime: 1000 * 60 * 5,
    refetchInterval: (query) =>
      query.state.data?.onboardingStep === 'APPROVAL_WAIT' ? 5000 : false,
  });
}
