import { Download } from 'lucide-react';
import { Button } from '@/shared/components/ui/button';

interface AttendanceHeaderProps {
  dojangName?: string;
  onDownload?: () => void;
}

export const AttendanceHeader = ({ dojangName, onDownload }: AttendanceHeaderProps) => {
  return (
    <header className="flex items-center justify-between">
      <div>
        <h1 className="text-2xl font-bold">출결 관리</h1>
        {dojangName && (
          <p className="text-sm text-muted-foreground">{dojangName}</p>
        )}
      </div>
      {onDownload && (
        <Button onClick={onDownload} variant="outline">
          <Download className="mr-2 h-4 w-4" />
          Excel 내보내기
        </Button>
      )}
    </header>
  );
};
