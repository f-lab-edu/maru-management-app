import { Users, Shield, Bell, User, GraduationCap } from 'lucide-react';
import { SettingsLayout } from '../features/settings/components/SettingsLayout';
import { InstructorApproval } from '../features/settings/components/InstructorApproval';
import { DivisionSettings } from '../features/divisions';
import { SettingsTab } from '../features/settings/types';

export default function SettingsPage() {
  const tabs: SettingsTab[] = [
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
      component: <div className="p-4 text-slate-500">준비 중인 기능입니다.</div>
    },
    {
      id: 'notifications',
      label: '알림 설정',
      icon: Bell,
      description: '푸시 알림 및 이메일 수신 설정을 관리합니다.',
      component: <div className="p-4 text-slate-500">준비 중인 기능입니다.</div>
    },
    {
      id: 'profile',
      label: '내 정보',
      icon: User,
      description: '계정 정보 및 비밀번호를 변경합니다.',
      component: <div className="p-4 text-slate-500">준비 중인 기능입니다.</div>
    },
  ];

  return <SettingsLayout tabs={tabs} />;
}
