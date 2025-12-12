import { DayContentProps } from 'react-day-picker';
import { isSameDay } from 'date-fns';
import { Calendar } from '../../../shared/components/ui/calendar';
import { Card } from '../../../shared/components/ui/card';
import { cn } from '../../../shared/utils';
import { CalendarEvent } from '../types';

interface DashboardCalendarProps {
  date: Date | undefined;
  onSelect: (date: Date | undefined) => void;
  events: CalendarEvent[];
}

const EVENT_STYLES = {
  payment: "bg-blue-500",
  alert: "bg-red-500",
  info: "bg-green-500"
} as const;

export function DashboardCalendar({ date, onSelect, events }: DashboardCalendarProps) {

  function CustomDayContent(props: DayContentProps) {
    const { date: dayDate } = props;
    const dayEvents = events.filter(e => isSameDay(e.date, dayDate));

    return (
      <div className="relative flex flex-col items-center justify-center h-full w-full">
        <span>{dayDate.getDate()}</span>
        {dayEvents.length > 0 && (
          <div className="absolute bottom-1 flex gap-0.5">
            {dayEvents.map((e, i) => (
              <div key={i} className={cn("w-1.5 h-1.5 rounded-full", EVENT_STYLES[e.type])} />
            ))}
          </div>
        )}
      </div>
    );
  }

  return (
    <Card className="border-none shadow-sm bg-white overflow-hidden shrink-0">
      <div className="p-4">
        <Calendar
          mode="single"
          selected={date}
          onSelect={onSelect}
          className="rounded-md border-none w-full"
          classNames={{
            months: "flex w-full flex-col sm:flex-row space-y-4 sm:space-x-4 sm:space-y-0",
            month: "space-y-4 w-full",
            caption: "flex justify-center pt-1 relative items-center mb-4",
            caption_label: "text-lg font-bold",
            nav: "space-x-1 flex items-center",
            table: "w-full border-collapse space-y-1",
            head_row: "flex w-full justify-between",
            head_cell: "text-muted-foreground rounded-md w-full font-normal text-sm",
            row: "flex w-full mt-2 justify-between",
            cell: "h-9 w-full text-center text-sm p-0 relative [&:has([aria-selected].day-range-end)]:rounded-r-md [&:has([aria-selected].day-outside)]:bg-accent/50 [&:has([aria-selected])]:bg-accent first:[&:has([aria-selected])]:rounded-l-md last:[&:has([aria-selected])]:rounded-r-md focus-within:relative focus-within:z-20 lg:h-8 xl:h-10 2xl:h-12 min-[1700px]:h-14",
            day: cn(
              "h-9 w-9 p-0 font-normal aria-selected:opacity-100 hover:bg-slate-100 rounded-full transition-colors mx-auto text-sm lg:h-7 lg:w-7 lg:text-xs xl:h-10 xl:w-10 xl:text-sm 2xl:h-12 2xl:w-12 2xl:text-base min-[1700px]:h-14 min-[1700px]:w-14 min-[1700px]:text-lg"
            ),
            day_selected:
              "bg-primary text-primary-foreground hover:bg-primary hover:text-primary-foreground focus:bg-primary focus:text-primary-foreground",
            day_today: "bg-accent text-accent-foreground",
          }}
          components={{
            DayContent: CustomDayContent
          }}
        />
      </div>
    </Card>
  );
}
