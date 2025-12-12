import { ArrowLeft } from 'lucide-react';
import { Button } from '../../../shared/components/ui/button';

interface OnboardingBackButtonProps {
  onClick: () => void;
}

export function OnboardingBackButton({ onClick }: OnboardingBackButtonProps) {
  return (
    <Button
      variant="ghost"
      className="w-fit pt-10 pb-6 hover:bg-transparent text-slate-500"
      onClick={onClick}
    >
      <ArrowLeft className="w-4 h-4 mr-2" />
      이전으로
    </Button>
  );
}
