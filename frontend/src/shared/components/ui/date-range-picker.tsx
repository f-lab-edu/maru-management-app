import { CalendarIcon } from "lucide-react"
import { format } from "date-fns"
import { ko } from "date-fns/locale"
import { DateRange } from "react-day-picker"

import { cn } from "@/shared/utils"
import { Button } from "./button"
import { Calendar } from "./calendar"
import { Popover, PopoverContent, PopoverTrigger } from "./popover"

interface DateRangePickerProps {
  dateRange: DateRange | undefined
  onDateRangeChange: (range: DateRange | undefined) => void
  className?: string
  placeholder?: string
}

export const DateRangePicker = ({
  dateRange,
  onDateRangeChange,
  className,
  placeholder = "날짜 범위를 선택하세요",
}: DateRangePickerProps) => {
  return (
    <div className={cn("grid gap-2", className)}>
      <Popover>
        <PopoverTrigger asChild>
          <Button
            id="date"
            variant="outline"
            className={cn(
              "w-[300px] justify-start text-left font-normal",
              !dateRange && "text-muted-foreground"
            )}
          >
            <CalendarIcon className="mr-2 h-4 w-4" />
            {dateRange?.from ? (
              dateRange.to ? (
                <>
                  {format(dateRange.from, "yyyy년 M월 d일", { locale: ko })} -{" "}
                  {format(dateRange.to, "yyyy년 M월 d일", { locale: ko })}
                </>
              ) : (
                format(dateRange.from, "yyyy년 M월 d일", { locale: ko })
              )
            ) : (
              <span>{placeholder}</span>
            )}
          </Button>
        </PopoverTrigger>
        <PopoverContent className="w-auto p-0" align="start">
          <Calendar
            initialFocus
            mode="range"
            defaultMonth={dateRange?.from}
            selected={dateRange}
            onSelect={onDateRangeChange}
            numberOfMonths={2}
            locale={ko}
          />
        </PopoverContent>
      </Popover>
    </div>
  )
}
