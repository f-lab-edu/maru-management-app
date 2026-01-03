import * as React from 'react';
import { CalendarIcon } from 'lucide-react';
import { cn } from '@/shared/utils';
import { Button } from './button';
import { Popover, PopoverContent, PopoverTrigger } from './popover';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from './select';

export interface MonthPickerProps {
  value?: string;
  onChange?: (value: string) => void;
  placeholder?: string;
  disabled?: boolean;
  className?: string;
  yearRange?: number;
}

export const MonthPicker = React.forwardRef<HTMLButtonElement, MonthPickerProps>(
  (
    {
      value,
      onChange,
      placeholder = '연월을 선택하세요',
      disabled = false,
      className,
      yearRange = 2,
    },
    ref
  ) => {
  const [isOpen, setIsOpen] = React.useState(false);

  const now = new Date();
  const currentYear = now.getFullYear();
  const currentMonth = now.getMonth() + 1;

  const parseValue = (val?: string) => {
    if (!val) return { year: currentYear, month: currentMonth };
    const [y, m] = val.split('-').map(Number);
    return { year: y || currentYear, month: m || currentMonth };
  };

  const { year: selectedYear, month: selectedMonth } = parseValue(value);

  const handleYearChange = (newYear: string) => {
    const yearNum = parseInt(newYear, 10);
    const newValue = `${yearNum}-${String(selectedMonth).padStart(2, '0')}`;
    onChange?.(newValue);
  };

  const handleMonthChange = (newMonth: string) => {
    const monthNum = parseInt(newMonth, 10);
    const newValue = `${selectedYear}-${String(monthNum).padStart(2, '0')}`;
    onChange?.(newValue);
  };

  const formatDisplayValue = () => {
    if (!value) return placeholder;
    return `${selectedYear}년 ${selectedMonth}월`;
  };

  const yearOptions = React.useMemo(() => {
    const years: number[] = [];
    for (let i = currentYear - yearRange; i <= currentYear + yearRange; i++) {
      years.push(i);
    }
    return years;
  }, [currentYear, yearRange]);

  const monthOptions = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12];

    return (
      <Popover open={isOpen} onOpenChange={setIsOpen}>
        <PopoverTrigger asChild>
          <Button
            ref={ref}
            variant="outline"
            className={cn(
              'w-full justify-start text-left font-normal',
              !value && 'text-muted-foreground',
              className
            )}
            disabled={disabled}
          >
            <CalendarIcon className="mr-2 h-4 w-4" />
            {formatDisplayValue()}
          </Button>
        </PopoverTrigger>
        <PopoverContent className="w-auto p-4" align="start">
          <div className="space-y-3">
            <div className="space-y-2">
              <label className="text-sm font-medium">년도</label>
              <Select value={selectedYear.toString()} onValueChange={handleYearChange}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {yearOptions.map((year) => (
                    <SelectItem key={year} value={year.toString()}>
                      {year}년
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium">월</label>
              <Select value={selectedMonth.toString()} onValueChange={handleMonthChange}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {monthOptions.map((month) => (
                    <SelectItem key={month} value={month.toString()}>
                      {month}월
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <Button
              className="w-full"
              size="sm"
              onClick={() => setIsOpen(false)}
            >
              확인
            </Button>
          </div>
        </PopoverContent>
      </Popover>
    );
  }
);

MonthPicker.displayName = 'MonthPicker';
