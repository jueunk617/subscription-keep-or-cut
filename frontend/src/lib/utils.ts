import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';
import type { EvaluationStatus, BillingCycle, CategoryType, UsageUnit } from '@/src/types/api';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

// 숫자를 천단위 콤마로 포맷팅
export function formatNumber(num: number): string {
  return new Intl.NumberFormat('ko-KR').format(num);
}

// 금액 포맷팅
export function formatCurrency(amount: number): string {
  return `${formatNumber(amount)}원`;
}

// 평가 상태별 색상 반환
export function getStatusColor(status: EvaluationStatus): string {
  const colors = {
    EFFICIENT: 'text-success-600 bg-success-50 border-success-200',
    KEEP: 'text-blue-600 bg-blue-50 border-blue-200',
    REVIEW: 'text-warning-600 bg-warning-50 border-warning-200',
    INEFFICIENT: 'text-orange-600 bg-orange-50 border-orange-200',
    GHOST: 'text-danger-600 bg-danger-50 border-danger-200',
  };
  return colors[status];
}

// 평가 상태별 한글명
export function getStatusLabel(status: EvaluationStatus): string {
  const labels = {
    EFFICIENT: '효율적',
    KEEP: '유지 가능',
    REVIEW: '재검토 필요',
    INEFFICIENT: '비효율',
    GHOST: '유령 구독',
  };
  return labels[status];
}

// 평가 상태별 설명
export function getStatusDescription(status: EvaluationStatus): string {
  const descriptions = {
    EFFICIENT: '기준 이상 활용 중 - 유지 권장',
    KEEP: '준수한 활용도 - 유지 가능',
    REVIEW: '저활용 - 이용 패턴 재검토 필요',
    INEFFICIENT: '심각한 저활용 - 해지 고려',
    GHOST: '미사용 - 즉시 해지 권장',
  };
  return descriptions[status];
}

// 결제 주기 한글명
export function getBillingCycleLabel(cycle: BillingCycle): string {
  const labels = {
    MONTHLY: '월간',
    QUARTERLY: '분기',
    ANNUAL: '연간',
  };
  return labels[cycle];
}

// 카테고리 한글명
export function getCategoryLabel(categoryName: string): string {
  const labels: Record<string, string> = {
    OTT: 'OTT',
    MUSIC: '음악 스트리밍',
    EBOOK: '전자책',
    AI_TOOL: 'AI 도구',
    WORK_TOOL: '업무 도구',
    CLOUD: '클라우드 스토리지',
  };
  return labels[categoryName] || categoryName;
}

// 카테고리 타입별 단위 표시
export function getUnitLabel(unit: UsageUnit): string {
  return unit === 'MINUTES' ? '분' : '일';
}

// 효율 점수에 따른 그래프 색상
export function getEfficiencyColor(rate: number): string {
  if (rate >= 100) return '#22c55e'; // success-500
  if (rate >= 70) return '#3b82f6'; // blue-500
  if (rate >= 40) return '#eab308'; // warning-500
  return '#ef4444'; // danger-500
}

// 월 선택 옵션 생성
export function getMonthOptions(): Array<{ value: number; label: string }> {
  return Array.from({ length: 12 }, (_, i) => ({
    value: i + 1,
    label: `${i + 1}월`,
  }));
}

// 연도 선택 옵션 생성
export function getYearOptions(): Array<{ value: number; label: string }> {
  const currentYear = new Date().getFullYear();
  return Array.from({ length: 5 }, (_, i) => ({
    value: currentYear - i,
    label: `${currentYear - i}년`,
  }));
}

// 현재 연월 반환
export function getCurrentYearMonth(): { year: number; month: number } {
  const now = new Date();
  return {
    year: now.getFullYear(),
    month: now.getMonth() + 1,
  };
}

// YYYY-MM 형식으로 변환
export function formatYearMonth(year: number, month: number): string {
  return `${year}-${String(month).padStart(2, '0')}`;
}