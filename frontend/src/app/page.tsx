'use client';

import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { dashboardApi, subscriptionApi } from '@/src/lib/api';
import { getCurrentYearMonth, formatCurrency } from '@/src/lib/utils';
import DashboardHeader from '@/src/components/dashboard/DashboardHeader';
import DashboardStats from '@/src/components/dashboard/DashboardStats';
import SubscriptionList from '@/src/components/dashboard/SubscriptionList';
import MonthSelector from '@/src/components/dashboard/MonthSelector';
import AddSubscriptionButton from '@/src/components/subscription/AddSubscriptionButton';
import EmptyState from '@/src/components/common/EmptyState';
import UsageInputModal from '@/src/components/usage/UsageInputModal';
import LoadingSpinner from '@/src/components/common/LoadingSpinner';
import ErrorMessage from '@/src/components/common/ErrorMessage';

export default function HomePage() {
  const { year: currentYear, month: currentMonth } = getCurrentYearMonth();
  const [selectedYear, setSelectedYear] = useState(currentYear);
  const [selectedMonth, setSelectedMonth] = useState(currentMonth);
  const [usageTarget, setUsageTarget] = useState<{
    id: number;
    name: string;
    categoryName: string;
  } | null>(null);

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['dashboard', selectedYear, selectedMonth],
    queryFn: async () => {
      const response = await dashboardApi.getMonthly(selectedYear, selectedMonth);
      return response.data.data;
    },
  });

  const {
    data: allSubscriptions,
    refetch: refetchAllSubscriptions,
  } = useQuery({
    queryKey: ['subscriptions'],
    queryFn: async () => {
      const response = await subscriptionApi.getAll();
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

  const hasDashboardSubscriptions = data && data.subscriptions.length > 0;
  const hasAnySubscriptions = allSubscriptions && allSubscriptions.length > 0;

  const handleCloseUsageModal = () => setUsageTarget(null);

  const handleUsageSuccess = () => {
    handleCloseUsageModal();
    refetch();
    refetchAllSubscriptions();
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <header className="flex justify-center">
          <DashboardHeader />
        </header>

        <div className="mt-8 space-y-6">
          {/* 분석 대상 / 구독 추가 섹션 */}
          <section className="flex flex-col items-center gap-3">
            <div className="flex items-center justify-center gap-12 text-sm font-medium text-gray-500">
              <span>분석 대상</span>
              <span>구독 추가</span>
            </div>
            <div className="flex flex-col items-center gap-3 sm:flex-row sm:gap-6">
              <MonthSelector
                year={selectedYear}
                month={selectedMonth}
                onYearChange={setSelectedYear}
                onMonthChange={setSelectedMonth}
              />
              <AddSubscriptionButton
                onSuccess={() => {
                  refetch();
                  refetchAllSubscriptions();
                }}
              />
            </div>
          </section>

          {hasDashboardSubscriptions ? (
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
          ) : hasAnySubscriptions ? (
            <section className="space-y-4 animate-slide-in">
              <div className="card">
                <div className="card-body space-y-2">
                  <h2 className="text-lg font-semibold text-gray-900">등록된 구독</h2>
                  <p className="text-sm text-gray-600">
                    아직 이번 달 사용량이 없어 분석 카드는 보이지 않지만,
                    등록된 구독 목록은 아래에서 확인할 수 있어요.
                  </p>
                </div>
              </div>

              <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
                {allSubscriptions!.map((subscription) => (
                  <div key={subscription.id} className="card">
                    <div className="card-body flex items-center justify-between gap-4">
                      <div className="flex-1">
                        <p className="text-sm font-medium text-gray-900">
                          {subscription.name}
                        </p>
                        <p className="text-xs text-gray-500">
                          {subscription.categoryName}
                        </p>
                      </div>
                      <div className="text-right space-y-2">
                        <div>
                          <p className="text-xs text-gray-500 mb-1">월 부담 금액</p>
                          <p className="text-sm font-semibold text-gray-900">
                            {formatCurrency(subscription.monthlyShareCost)}
                          </p>
                        </div>
                        <button
                          type="button"
                          className="btn-primary text-xs px-3 py-1.5"
                          onClick={() =>
                            setUsageTarget({
                              id: subscription.id,
                              name: subscription.name,
                              categoryName: subscription.categoryName,
                            })
                          }
                        >
                          사용량 입력
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </section>
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
          {usageTarget && (
            <UsageInputModal
              subscription={usageTarget}
              year={selectedYear}
              month={selectedMonth}
              onClose={handleCloseUsageModal}
              onSuccess={handleUsageSuccess}
            />
          )}
        </div>
      </div>
    </div>
  );
}
