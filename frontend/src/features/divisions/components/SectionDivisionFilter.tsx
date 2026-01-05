import { Loader2 } from 'lucide-react';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '../../../shared/components/ui/select';
import { Label } from '../../../shared/components/ui/label';
import { useSections, useDivisions } from '../hooks';

interface SectionDivisionFilterProps {
  dojangId: string;
  selectedSectionId: string | null;
  selectedDivisionId: string | null;
  onSectionChange: (sectionId: string | null) => void;
  onDivisionChange: (divisionId: string | null) => void;
  compact?: boolean;
}

const ALL_VALUE = '__ALL__';

export function SectionDivisionFilter({
  dojangId,
  selectedSectionId,
  selectedDivisionId,
  onSectionChange,
  onDivisionChange,
  compact = false,
}: SectionDivisionFilterProps) {
  const { data: sectionsData, isLoading: sectionsLoading } = useSections(dojangId);
  const { data: divisionsData, isLoading: divisionsLoading } = useDivisions(
    dojangId,
    selectedSectionId
  );

  const sections = sectionsData?.sections ?? [];
  const divisions = divisionsData?.divisions ?? [];

  const handleSectionChange = (value: string) => {
    const newValue = value === ALL_VALUE ? null : value;
    onSectionChange(newValue);
    onDivisionChange(null);
  };

  const handleDivisionChange = (value: string) => {
    const newValue = value === ALL_VALUE ? null : value;
    onDivisionChange(newValue);
  };

  if (sectionsLoading) {
    return (
      <div className="flex items-center gap-2 text-sm text-slate-500">
        <Loader2 className="h-4 w-4 animate-spin" />
        <span>수련부 로딩 중...</span>
      </div>
    );
  }

  if (sections.length === 0) {
    return null;
  }

  if (compact) {
    return (
      <div className="flex items-center gap-2">
        <Select
          value={selectedSectionId ?? ALL_VALUE}
          onValueChange={handleSectionChange}
        >
          <SelectTrigger className="w-[140px]">
            <SelectValue placeholder="수련부 선택" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value={ALL_VALUE}>전체 수련부</SelectItem>
            {sections.map((section) => (
              <SelectItem key={section.id} value={section.id}>
                {section.name}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

        <Select
          value={selectedDivisionId ?? ALL_VALUE}
          onValueChange={handleDivisionChange}
          disabled={!selectedSectionId || divisionsLoading}
        >
          <SelectTrigger className="w-[140px]">
            <SelectValue placeholder="수련반 선택" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value={ALL_VALUE}>전체 수련반</SelectItem>
            {divisions.map((division) => (
              <SelectItem key={division.id} value={division.id}>
                {division.name}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>
    );
  }

  return (
    <div className="flex flex-col sm:flex-row gap-4">
      <div className="space-y-1.5">
        <Label className="text-sm text-slate-500">수련부</Label>
        <Select
          value={selectedSectionId ?? ALL_VALUE}
          onValueChange={handleSectionChange}
        >
          <SelectTrigger className="w-full sm:w-[180px]">
            <SelectValue placeholder="수련부 선택" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value={ALL_VALUE}>전체 수련부</SelectItem>
            {sections.map((section) => (
              <SelectItem key={section.id} value={section.id}>
                {section.name}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <div className="space-y-1.5">
        <Label className="text-sm text-slate-500">수련반</Label>
        <Select
          value={selectedDivisionId ?? ALL_VALUE}
          onValueChange={handleDivisionChange}
          disabled={!selectedSectionId || divisionsLoading}
        >
          <SelectTrigger className="w-full sm:w-[180px]">
            {divisionsLoading ? (
              <span className="flex items-center gap-2">
                <Loader2 className="h-3 w-3 animate-spin" />
                로딩 중...
              </span>
            ) : (
              <SelectValue placeholder="수련반 선택" />
            )}
          </SelectTrigger>
          <SelectContent>
            <SelectItem value={ALL_VALUE}>전체 수련반</SelectItem>
            {divisions.map((division) => (
              <SelectItem key={division.id} value={division.id}>
                {division.name}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>
    </div>
  );
}
