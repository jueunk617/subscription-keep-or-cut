import { ChevronLeft, ChevronRight, Calendar } from 'lucide-react';

interface MonthSelectorProps {
  year: number;
  month: number;
  onYearChange: (year: number) => void;
  onMonthChange: (month: number) => void;
}

export default function MonthSelector({
  year,
  month,
  onYearChange,
  onMonthChange,
}: MonthSelectorProps) {
  const handlePrevMonth = () => {
    if (month === 1) {
      onYearChange(year - 1);
      onMonthChange(12);
    } else {
      onMonthChange(month - 1);
    }
  };

  const handleNextMonth = () => {
    if (month === 12) {
      onYearChange(year + 1);
      onMonthChange(1);
    } else {
      onMonthChange(month + 1);
    }
  };

  return (
    <div className="flex items-center gap-2">
      <button
        onClick={handlePrevMonth}
        className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
        aria-label="이전 달"
      >
        <ChevronLeft className="w-5 h-5" />
      </button>

      <div className="flex items-center gap-2 bg-white px-4 py-2 rounded-lg border border-gray-200 shadow-sm min-w-[160px] justify-center">
        <Calendar className="w-4 h-4 text-gray-500" />
        <span className="font-semibold text-gray-900">
          {year}년 {month}월
        </span>
      </div>

      <button
        onClick={handleNextMonth}
        className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
        aria-label="다음 달"
      >
        <ChevronRight className="w-5 h-5" />
      </button>
    </div>
  );
}