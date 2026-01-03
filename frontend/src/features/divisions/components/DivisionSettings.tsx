import { useState } from 'react';
import { Loader2 } from 'lucide-react';
import { useAuthStore } from '@/stores/authStore';
import { useSections } from '../hooks';
import { SectionList } from './SectionList';
import { DivisionList } from './DivisionList';

export function DivisionSettings() {
  const { selectedDojang } = useAuthStore();
  const dojangId = selectedDojang?.dojangId ?? null;
  const [selectedSectionId, setSelectedSectionId] = useState<string | null>(null);

  const { data: sectionsData, isLoading } = useSections(dojangId ?? '');

  if (!dojangId) {
    return (
      <div className="text-center py-8 text-slate-500">
        도장 정보를 불러올 수 없습니다.
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    );
  }

  const sections = sectionsData?.sections ?? [];

  return (
    <div className="flex flex-col lg:flex-row gap-6">
      {/* 수련부 목록 */}
      <div className="w-full lg:w-64 shrink-0">
        <SectionList
          dojangId={dojangId}
          sections={sections}
          selectedSectionId={selectedSectionId}
          onSelectSection={setSelectedSectionId}
        />
      </div>

      {/* 수련반 목록 */}
      <div className="flex-1 min-w-0">
        {selectedSectionId ? (
          <DivisionList
            dojangId={dojangId}
            sectionId={selectedSectionId}
            sectionName={sections.find(s => s.id === selectedSectionId)?.name ?? ''}
          />
        ) : (
          <div className="flex items-center justify-center h-64 bg-slate-50 rounded-lg border border-dashed border-slate-200">
            <p className="text-slate-500">수련부를 선택하면 수련반 목록이 표시됩니다.</p>
          </div>
        )}
      </div>
    </div>
  );
}
