import { useState, useEffect } from 'react';
import { useMutation } from '@tanstack/react-query';
import { X, Clock, Calendar as CalendarIcon } from 'lucide-react';
import { usageApi } from '@/src/lib/api';
import type { SubscriptionSummary } from '@/src/types/api';
import { getCategoryLabel, formatYearMonth } from '@/src/lib/utils';

interface UsageInputModalProps {
  subscription: SubscriptionSummary;
  year: number;
  month: number;
  onClose: () => void;
  onSuccess: () => void;
}

export default function UsageInputModal({
  subscription,
  year,
  month,
  onClose,
  onSuccess,
}: UsageInputModalProps) {
  const [usageValue, setUsageValue] = useState('');
  const [error, setError] = useState('');

  const isContentType = ['OTT', 'MUSIC', 'EBOOK'].includes(subscription.categoryName);
  const unit = isContentType ? '분' : '일';
  const placeholder = isContentType ? '이용 시간 (분)' : '사용 일수';

  const mutation = useMutation({
    mutationFn: () => usageApi.record({
      subscriptionId: subscription.id,
      date: formatYearMonth(year, month),
      usageValue: Number(usageValue),
    }),
    onSuccess: () => {
      onSuccess();
    },
    onError: (error: any) => {
      const errorData = error.response?.data;
      if (errorData?.data?.[0]?.message) {
        setError(errorData.data[0].message);
      } else {
        setError('사용량 기록에 실패했습니다.');
      }
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    const value = Number(usageValue);

    // 클라이언트 검증
    if (isNaN(value) || value < 0) {
      setError('0 이상의 숫자를 입력해주세요');
      return;
    }

    // 일수 검증
    if (!isContentType) {
      const daysInMonth = new Date(year, month, 0).getDate();
      if (value > daysInMonth) {
        setError(`${month}월은 ${daysInMonth}일까지 있습니다`);
        return;
      }
    }

    // 분 검증
    if (isContentType) {
      const daysInMonth = new Date(year, month, 0).getDate();
      const maxMinutes = daysInMonth * 24 * 60;
      if (value > maxMinutes) {
        setError(`${month}월의 최대 이용 가능 시간은 ${maxMinutes}분입니다`);
        return;
      }
    }

    mutation.mutate();
  };

  const getGuideText = () => {
    if (isContentType) {
      return '월 총 이용 시간을 분 단위로 입력하세요. (예: 1시간 30분 = 90분)';
    }
    return '이번 달 실제로 사용한 일수를 입력하세요. (0~31일)';
  };

  const getExampleText = () => {
    switch (subscription.categoryName) {
      case 'OTT':
        return '예시: 영화 2편(4시간) + 드라마 10화(5시간) = 540분';
      case 'MUSIC':
        return '예시: 하루 평균 1시간씩 20일 = 1,200분';
      case 'EBOOK':
        return '예시: 책 1권 완독(5시간) = 300분';
      case 'AI_TOOL':
        return '예시: 주 3회 사용 × 4주 = 12일';
      case 'WORK_TOOL':
        return '예시: 평일 매일 사용 = 약 20일';
      case 'CLOUD':
        return '예시: 거의 매일 동기화 = 약 25일';
      default:
        return '';
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-xl max-w-md w-full animate-slide-in">
        {/* 헤더 */}
        <div className="border-b border-gray-200 px-6 py-4 flex items-center justify-between">
          <div>
            <h2 className="text-xl font-semibold text-gray-900">사용량 입력</h2>
            <p className="text-sm text-gray-500 mt-1">
              {subscription.name} · {getCategoryLabel(subscription.categoryName)}
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* 폼 */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {/* 대상 월 표시 */}
          <div className="flex items-center gap-2 p-3 bg-blue-50 border border-blue-200 rounded-lg">
            <CalendarIcon className="w-4 h-4 text-blue-600" />
            <span className="text-sm font-medium text-blue-900">
              {year}년 {month}월
            </span>
          </div>

          {/* 사용량 입력 */}
          <div>
            <label className="label flex items-center gap-2">
              {isContentType ? <Clock className="w-4 h-4" /> : <CalendarIcon className="w-4 h-4" />}
              {placeholder}
            </label>
            <div className="relative">
              <input
                type="number"
                value={usageValue}
                onChange={(e) => setUsageValue(e.target.value)}
                placeholder="0"
                min="0"
                step={isContentType ? '1' : '1'}
                className="input pr-12"
                autoFocus
              />
              <span className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 text-sm">
                {unit}
              </span>
            </div>
            {error && (
              <p className="mt-1 text-xs text-danger-600">{error}</p>
            )}
          </div>

          {/* 안내 문구 */}
          <div className="space-y-2 text-sm">
            <p className="text-gray-600">
              {getGuideText()}
            </p>
            <p className="text-gray-500 text-xs">
              {getExampleText()}
            </p>
          </div>

          {/* 버튼 */}
          <div className="flex gap-3 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="btn-secondary flex-1"
              disabled={mutation.isPending}
            >
              취소
            </button>
            <button
              type="submit"
              className="btn-primary flex-1"
              disabled={mutation.isPending}
            >
              {mutation.isPending ? '기록 중...' : '기록하기'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
