// TODO: 테스트용. 프로덕션에서는 반드시 삭제할 것.

import { useState } from 'react';
import { useKonamiCode } from './useKonamiCode';
import apiClient from '../../services/api';

type UserRole = 'OWNER' | 'INSTRUCTOR';
type OnboardingStep = 'PROFILE_INPUT' | 'ROLE_SELECT' | 'DOJANG_INFO' | 'APPROVAL_WAIT' | 'COMPLETED';

interface FormData {
  name: string;
  phone: string;
  role: UserRole | '';
  onboardingStep: OnboardingStep;
}

const generateRandomPhone = () => `010${Math.floor(10000000 + Math.random() * 90000000)}`;

const getOnboardingPath = (step: OnboardingStep): string => {
  switch (step) {
    case 'PROFILE_INPUT': return '/onboarding/user-info';
    case 'ROLE_SELECT': return '/onboarding/role';
    case 'DOJANG_INFO': return '/onboarding/create-dojang';
    case 'APPROVAL_WAIT': return '/onboarding/search-dojang';
    case 'COMPLETED': return '/dashboard';
    default: return '/onboarding/user-info';
  }
};

export function DevLoginModal() {
  const [isOpen, setIsOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isSeedingDojangs, setIsSeedingDojangs] = useState(false);
  const [formData, setFormData] = useState<FormData>({
    name: '테스트유저',
    phone: generateRandomPhone(),
    role: '',
    onboardingStep: 'ROLE_SELECT',
  });

  useKonamiCode(() => setIsOpen(true));

  const handleSubmit = async () => {
    if (!formData.name || !formData.phone) {
      alert('이름과 휴대폰번호를 입력해주세요.');
      return;
    }

    setIsLoading(true);
    try {
      await apiClient.post('/dev/create-test-user', {
        name: formData.name,
        phone: formData.phone,
        role: formData.role || null,
        onboardingStep: formData.onboardingStep,
      });

      const targetPath = getOnboardingPath(formData.onboardingStep);
      window.location.href = targetPath;
    } catch (error) {
      console.error('테스트 유저 생성 실패:', error);
      alert('테스트 유저 생성 실패. 콘솔을 확인해주세요.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSeedDojangs = async () => {
    if (!confirm('9,371개 도장 데이터를 생성합니다. 계속할까요?')) return;

    setIsSeedingDojangs(true);
    try {
      const response = await apiClient.post<{ count: number }>('/dev/seed-dojangs');
      alert(`도장 ${response.data.count}개 생성 완료!`);
    } catch (error) {
      console.error('도장 시드 실패:', error);
      alert('도장 시드 실패. 콘솔을 확인해주세요.');
    } finally {
      setIsSeedingDojangs(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-[9999] flex items-center justify-center bg-black/50">
      <div className="bg-white rounded-lg p-6 w-[400px] shadow-xl">
        <h2 className="text-xl font-bold mb-4 text-red-600">🎮 DEV: 테스트 유저 생성</h2>

        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium mb-1">이름</label>
            <input
              type="text"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="w-full border rounded px-3 py-2"
            />
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">휴대폰번호</label>
            <input
              type="text"
              value={formData.phone}
              onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
              className="w-full border rounded px-3 py-2"
            />
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">역할</label>
            <select
              value={formData.role}
              onChange={(e) => setFormData({ ...formData, role: e.target.value as UserRole | '' })}
              className="w-full border rounded px-3 py-2"
            >
              <option value="">미선택 (PENDING)</option>
              <option value="OWNER">관장 (OWNER)</option>
              <option value="INSTRUCTOR">사범 (INSTRUCTOR)</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">온보딩 단계</label>
            <select
              value={formData.onboardingStep}
              onChange={(e) => setFormData({ ...formData, onboardingStep: e.target.value as OnboardingStep })}
              className="w-full border rounded px-3 py-2"
            >
              <option value="PROFILE_INPUT">프로필 입력</option>
              <option value="ROLE_SELECT">역할 선택</option>
              <option value="DOJANG_INFO">도장 정보 (관장)</option>
              <option value="APPROVAL_WAIT">승인 대기 (사범)</option>
              <option value="COMPLETED">완료</option>
            </select>
          </div>
        </div>

        <div className="flex gap-2 mt-6">
          <button
            onClick={() => setIsOpen(false)}
            className="flex-1 px-4 py-2 border rounded hover:bg-gray-100"
            disabled={isLoading}
          >
            취소
          </button>
          <button
            onClick={handleSubmit}
            className="flex-1 px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700"
            disabled={isLoading}
          >
            {isLoading ? '생성 중...' : '유저 생성'}
          </button>
        </div>

        <div className="border-t mt-6 pt-4">
          <p className="text-sm font-medium mb-2 text-gray-600">🗃️ 테스트 데이터</p>
          <button
            onClick={handleSeedDojangs}
            className="w-full px-4 py-2 bg-purple-600 text-white rounded hover:bg-purple-700 disabled:opacity-50"
            disabled={isSeedingDojangs}
          >
            {isSeedingDojangs ? '생성 중...' : '도장 9,371개 시드'}
          </button>
        </div>

        <p className="text-xs text-gray-400 mt-4 text-center">
          ↑↑↓↓←→←→BA로 다시 열 수 있습니다
        </p>
      </div>
    </div>
  );
}
