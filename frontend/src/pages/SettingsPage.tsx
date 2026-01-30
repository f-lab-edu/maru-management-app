import { Users, Shield, User, GraduationCap, Building2 } from 'lucide-react';
import { SettingsLayout } from '../features/settings/components/SettingsLayout';
import { InstructorApproval } from '../features/settings/components/InstructorApproval';
import { InstructorPermissions } from '../features/settings/components/InstructorPermissions';
import { DojangSettings } from '../features/settings/components/DojangSettings';
import { MyProfile } from '../features/settings/components/MyProfile';
import { DivisionSettings } from '../features/divisions';
import { SettingsTab } from '../features/settings/types';
import { usePermissions } from '@/hooks';

export default function SettingsPage() {
  const { isOwner } = usePermissions();

  const allTabs: SettingsTab[] = [
    {
      id: 'approval',
      label: '사범 승인 관리',
      icon: Users,
      description: '도장 가입을 요청한 사범님들을 승인하거나 관리합니다.',
      component: <InstructorApproval />
    },
    {
      id: 'divisions',
      label: '수련반 관리',
      icon: GraduationCap,
      description: '수련부와 수련반을 생성하고 관리합니다.',
      component: <DivisionSettings />
    },
    {
      id: 'permissions',
      label: '권한 설정',
      icon: Shield,
      description: '사범님들의 권한을 세부적으로 설정합니다.',
      component: <InstructorPermissions />
    },
    {
      id: 'dojang-settings',
      label: '도장 설정',
      icon: Building2,
      description: '도장 정보와 수납 설정을 관리합니다.',
      component: <DojangSettings />
    },
    {
      id: 'profile',
      label: '내 정보',
      icon: User,
      description: '이름과 전화번호를 수정합니다.',
      component: <MyProfile />
    },
  ];

  // OWNER가 아니면 사범승인관리, 권한설정, 도장설정 탭 제외
  const tabs = allTabs.filter((tab) => {
    if (!isOwner && (tab.id === 'approval' || tab.id === 'permissions' || tab.id === 'dojang-settings')) {
      return false;
    }
    return true;
  });

  return <SettingsLayout tabs={tabs} />;
}
