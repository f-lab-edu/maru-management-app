import type { DayOfWeek } from '../types';

const DAY_LABELS: Record<DayOfWeek, string> = {
  MONDAY: '월',
  TUESDAY: '화',
  WEDNESDAY: '수',
  THURSDAY: '목',
  FRIDAY: '금',
  SATURDAY: '토',
  SUNDAY: '일',
};

const DAY_ORDER: DayOfWeek[] = [
  'MONDAY',
  'TUESDAY',
  'WEDNESDAY',
  'THURSDAY',
  'FRIDAY',
  'SATURDAY',
  'SUNDAY',
];

export const formatScheduleDays = (days: DayOfWeek[]): string => {
  if (days.length === 0) return '';

  const sortedDays = [...days].sort(
    (a, b) => DAY_ORDER.indexOf(a) - DAY_ORDER.indexOf(b)
  );

  return sortedDays.map((day) => DAY_LABELS[day]).join('');
};

export const formatTimeRange = (startTime: string | null, endTime: string | null): string => {
  if (!startTime && !endTime) return '';
  if (startTime && endTime) return `${startTime}~${endTime}`;
  if (startTime) return `${startTime}~`;
  return `~${endTime}`;
};

export const getDayLabel = (day: DayOfWeek): string => DAY_LABELS[day];

export const getAllDays = (): DayOfWeek[] => DAY_ORDER;
