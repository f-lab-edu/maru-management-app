import { useState } from 'react';

export function useDashboardCalendar() {
  const [date, setDate] = useState<Date | undefined>(new Date());
  const [selectedDateDetails, setSelectedDateDetails] = useState<boolean>(false);

  const handleDateSelect = (newDate: Date | undefined) => {
    if (newDate?.getTime() === date?.getTime()) {
      setSelectedDateDetails(false);
      setDate(undefined);
    } else {
      setDate(newDate);
      setSelectedDateDetails(!!newDate);
    }
  };

  const resetSelection = () => {
    setDate(undefined);
    setSelectedDateDetails(false);
  };

  return {
    date,
    selectedDateDetails,
    handleDateSelect,
    resetSelection
  };
}
