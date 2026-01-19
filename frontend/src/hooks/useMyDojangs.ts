import { useQuery } from '@tanstack/react-query';
import { userService } from '../services/userService';
import { userKeys, useUser } from './useUser';

export function useMyDojangs() {
  const { data: user } = useUser();
  const isEnabled = Boolean(user?.id && user?.onboardingStep === 'COMPLETED');

  return useQuery({
    queryKey: userKeys.dojangs(),
    queryFn: userService.getMyDojangs,
    enabled: isEnabled,
    staleTime: 1000 * 60 * 5,
    retry: false,
  });
}
