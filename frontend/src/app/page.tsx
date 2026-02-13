'use client';

import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { dashboardApi } from '@/src/lib/api';
import { getCurrentYearMonth } from '@/src/lib/utils';
import DashboardHeader from '@/components/dashboard/DashboardHeader';
import DashboardStats from '@/components/dashboard/DashboardStats';
import SubscriptionList from '@/components/dashboard/SubscriptionList';
import MonthSelector from '@/components/dashboard/MonthSelector';
import AddSubscriptionButton from '@/components/subscription/AddSubscriptionButton';
import EmptyState from '@/components/common/EmptyState';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import ErrorMessage from '@/components/common/ErrorMessage';

export default function HomePage() {
  const { year: currentYear, month: currentMonth } = getCurrentYearMonth();
  const [selectedYear, setSelectedYear] = useState(currentYear);
  const [selectedMonth, setSelectedMonth] = useState(currentMonth);

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['dashboard', selectedYear, selectedMonth],
    queryFn: async () => {
      const response = await dashboardApi.getMonthly(selectedYear, selectedMonth);
      return response.data.data;
    },
  });

  if (isLoading) {
    return <LoadingSpinner />;
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <ErrorMessage message="대시보드를 불러오는 중 오류가 발생했습니다." />
      </div>
    );
  }

  const hasSubscriptions = data && data.subscriptions.length > 0;

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <DashboardHeader />
        
        <div className="mt-8 space-y-6">
          {/* 월 선택 및 구독 추가 버튼 */}
          <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
            <MonthSelector
              year={selectedYear}
              month={selectedMonth}
              onYearChange={setSelectedYear}
              onMonthChange={setSelectedMonth}
            />
            <AddSubscriptionButton onSuccess={refetch} />
          </div>

          {hasSubscriptions ? (
            <>
              {/* 통계 카드 */}
              <DashboardStats
                totalMonthlyCost={data.totalMonthlyCost}
                totalAnnualWaste={data.totalAnnualWasteEstimate}
                subscriptionCount={data.subscriptions.length}
              />

              {/* 구독 목록 */}
              <SubscriptionList
                subscriptions={data.subscriptions}
                year={selectedYear}
                month={selectedMonth}
                onRefresh={refetch}
              />
            </>
          ) : (
            <EmptyState
              title="등록된 구독이 없습니다"
              description="구독을 추가하고 효율성을 분석해보세요"
              actionLabel="구독 추가하기"
              onAction={() => {
                const addButton = document.querySelector('[data-add-subscription]') as HTMLButtonElement;
                addButton?.click();
              }}
            />
          )}
        </div>
      </div>
    </div>
  );
}
