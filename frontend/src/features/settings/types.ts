import { LucideIcon } from 'lucide-react';
import { ReactNode } from 'react';

export interface SettingsTab {
  id: string;
  label: string;
  icon: LucideIcon;
  component: ReactNode;
  description?: string;
}
