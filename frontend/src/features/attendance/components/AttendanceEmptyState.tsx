import { CalendarX2 } from 'lucide-react';

interface AttendanceEmptyStateProps {
  message?: string;
  description?: string;
}

export const AttendanceEmptyState = ({
  message = '출석 데이터가 없습니다',
  description = '선택한 기간에 출석 기록이 없습니다.',
}: AttendanceEmptyStateProps) => {
  return (
    <div className="flex flex-col items-center justify-center py-12 px-4">
      <div className="mb-4 rounded-full bg-gray-100 p-6">
        <CalendarX2 className="h-12 w-12 text-gray-400" />
      </div>
      <h3 className="mb-2 text-lg font-semibold text-gray-900">{message}</h3>
      <p className="text-center text-sm text-gray-500 max-w-md">{description}</p>
    </div>
  );
};
