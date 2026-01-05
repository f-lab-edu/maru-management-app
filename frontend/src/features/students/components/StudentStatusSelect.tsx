import { useState } from 'react';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
} from '@/shared/components/ui/select';
import { StatusBadge } from './StatusBadge';
import { STUDENT_STATUS_LABEL, type StudentStatus } from '@/types/student';

interface StudentStatusSelectProps {
  status: StudentStatus;
  onStatusChange: (newStatus: StudentStatus) => void;
  disabled?: boolean;
}

const SELECTABLE_STATUSES: Exclude<StudentStatus, 'WITHDRAWN'>[] = ['ACTIVE', 'PAUSED'];

export function StudentStatusSelect({ status, onStatusChange, disabled }: StudentStatusSelectProps) {
  const [open, setOpen] = useState(false);

  if (status === 'WITHDRAWN') {
    return <StatusBadge status={status} />;
  }

  const handleValueChange = (value: string) => {
    const newStatus = value as StudentStatus;
    if (newStatus !== status) {
      onStatusChange(newStatus);
    }
    setOpen(false);
  };

  return (
    <Select value={status} onValueChange={handleValueChange} open={open} onOpenChange={setOpen}>
      <SelectTrigger
        className="w-auto h-auto p-0 border-0 bg-transparent hover:bg-transparent focus:ring-0 focus:ring-offset-0"
        disabled={disabled}
      >
        <StatusBadge status={status} />
      </SelectTrigger>
      <SelectContent>
        {SELECTABLE_STATUSES.map((statusOption) => (
          <SelectItem key={statusOption} value={statusOption}>
            {STUDENT_STATUS_LABEL[statusOption]}
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  );
}
