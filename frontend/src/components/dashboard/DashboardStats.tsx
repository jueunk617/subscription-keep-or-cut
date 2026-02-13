import { TrendingDown, Wallet, Package } from 'lucide-react';
import { formatCurrency } from '@/src/lib/utils';

interface DashboardStatsProps {
  totalMonthlyCost: number;
  totalAnnualWaste: number;
  subscriptionCount: number;
}

export default function DashboardStats({
  totalMonthlyCost,
  totalAnnualWaste,
  subscriptionCount,
}: DashboardStatsProps) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-6 animate-slide-in">
      {/* 월 총 지출 */}
      <div className="card">
        <div className="card-body">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600 mb-1">월 총 지출</p>
              <p className="text-2xl font-bold text-gray-900">
                {formatCurrency(totalMonthlyCost)}
              </p>
            </div>
            <div className="bg-primary-50 p-3 rounded-lg">
              <Wallet className="w-6 h-6 text-primary-600" />
            </div>
          </div>
        </div>
      </div>

      {/* 연간 예상 낭비액 */}
      <div className="card">
        <div className="card-body">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600 mb-1">연간 예상 낭비액</p>
              <p className="text-2xl font-bold text-danger-600">
                {formatCurrency(totalAnnualWaste)}
              </p>
            </div>
            <div className="bg-danger-50 p-3 rounded-lg">
              <TrendingDown className="w-6 h-6 text-danger-600" />
            </div>
          </div>
          {totalAnnualWaste > 0 && (
            <p className="text-xs text-gray-500 mt-2">
              💡 지금 최적화하면 절감 가능한 금액입니다
            </p>
          )}
        </div>
      </div>

      {/* 구독 수 */}
      <div className="card">
        <div className="card-body">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600 mb-1">평가된 구독</p>
              <p className="text-2xl font-bold text-gray-900">
                {subscriptionCount}개
              </p>
            </div>
            <div className="bg-blue-50 p-3 rounded-lg">
              <Package className="w-6 h-6 text-blue-600" />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
