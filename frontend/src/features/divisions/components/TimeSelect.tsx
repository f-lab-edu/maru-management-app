import { useState, useEffect } from 'react';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '../../../shared/components/ui/select';

interface TimeSelectProps {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  disabled?: boolean;
}

const HOURS_12 = Array.from({ length: 12 }, (_, i) => (i + 1).toString());
const MINUTES = ['00', '10', '20', '30', '40', '50'];

type Period = 'AM' | 'PM';

function parse24HourTime(time: string): { period: Period; hour: string; minute: string } | null {
  if (!time || !time.includes(':')) return null;

  const [hourStr, minuteStr] = time.split(':');
  const hour24 = parseInt(hourStr, 10);

  if (isNaN(hour24) || hour24 < 0 || hour24 > 23) return null;

  const period: Period = hour24 < 12 ? 'AM' : 'PM';
  const hour12 = hour24 === 0 ? 12 : hour24 > 12 ? hour24 - 12 : hour24;

  return {
    period,
    hour: hour12.toString(),
    minute: minuteStr,
  };
}

function format24HourTime(period: Period, hour: string, minute: string): string {
  const hour12 = parseInt(hour, 10);
  let hour24: number;

  if (period === 'AM') {
    hour24 = hour12 === 12 ? 0 : hour12;
  } else {
    hour24 = hour12 === 12 ? 12 : hour12 + 12;
  }

  return `${hour24.toString().padStart(2, '0')}:${minute}`;
}

export function TimeSelect({
  value,
  onChange,
  disabled = false,
}: TimeSelectProps) {
  const parsed = parse24HourTime(value);

  const [period, setPeriod] = useState<Period>(parsed?.period ?? 'AM');
  const [hour, setHour] = useState(parsed?.hour ?? '');
  const [minute, setMinute] = useState(parsed?.minute ?? '');

  useEffect(() => {
    const parsed = parse24HourTime(value);
    if (parsed) {
      setPeriod(parsed.period);
      setHour(parsed.hour);
      setMinute(parsed.minute);
    }
  }, [value]);

  useEffect(() => {
    if (hour && minute) {
      const newValue = format24HourTime(period, hour, minute);
      if (newValue !== value) {
        onChange(newValue);
      }
    }
  }, [period, hour, minute]);

  return (
    <div className="flex gap-2">
      <Select
        value={period}
        onValueChange={(v) => setPeriod(v as Period)}
        disabled={disabled}
      >
        <SelectTrigger className="flex-1">
          <SelectValue />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="AM">오전</SelectItem>
          <SelectItem value="PM">오후</SelectItem>
        </SelectContent>
      </Select>

      <Select
        value={hour || undefined}
        onValueChange={setHour}
        disabled={disabled}
      >
        <SelectTrigger className="flex-1">
          <SelectValue placeholder="시" />
        </SelectTrigger>
        <SelectContent className="max-h-[240px]">
          {HOURS_12.map((h) => (
            <SelectItem key={h} value={h}>
              {h}시
            </SelectItem>
          ))}
        </SelectContent>
      </Select>

      <Select
        value={minute || undefined}
        onValueChange={setMinute}
        disabled={disabled}
      >
        <SelectTrigger className="flex-1">
          <SelectValue placeholder="분" />
        </SelectTrigger>
        <SelectContent>
          {MINUTES.map((m) => (
            <SelectItem key={m} value={m}>
              {m}분
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  );
}
