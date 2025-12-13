import { Avatar, AvatarFallback, AvatarImage } from '@/shared/components/ui/avatar';

interface AttendanceStudentCellProps {
  id: string;
  name: string;
  photoUrl: string | null;
  className: string;
  onClick?: () => void;
}

export function AttendanceStudentCell({ name, photoUrl, className, onClick }: AttendanceStudentCellProps) {
  return (
    <div
      className={`flex items-center gap-3 ${onClick ? 'cursor-pointer hover:bg-gray-50' : ''}`}
      onClick={onClick}
    >
      <Avatar className="h-9 w-9">
        <AvatarImage src={photoUrl ?? undefined} alt={name} />
        <AvatarFallback className="bg-primary/10 text-primary text-sm font-medium">
          {name.slice(0, 2)}
        </AvatarFallback>
      </Avatar>
      <div className="flex flex-col">
        <span className="font-medium">{name}</span>
        <span className="text-xs text-gray-500">{className}</span>
      </div>
    </div>
  );
}
