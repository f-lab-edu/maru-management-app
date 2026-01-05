import { calculateAge, formatDate } from '@/shared/utils/date';

interface AgeCellProps {
  birth: string;
}

export function AgeCell({ birth }: AgeCellProps) {
  const age = calculateAge(birth);
  const formattedDate = formatDate(birth);

  return (
    <span className="text-muted-foreground">
      <span className="font-medium text-foreground">{age}세</span>
      <span className="ml-1 text-sm">({formattedDate})</span>
    </span>
  );
}
