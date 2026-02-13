import { useState } from 'react';
import { Trash2, Edit3, AlertTriangle, Sparkles } from 'lucide-react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import type { SubscriptionSummary } from '@/src/types/api';
import { subscriptionApi } from '@/src/lib/api';
import {
  formatCurrency,
  getStatusColor,
  getStatusLabel,
  getStatusDescription,
  getCategoryLabel,
  getCategoryUnitLabel,
  getEfficiencyColor,
} from '@/src/lib/utils';

interface SubscriptionCardProps {
  subscription: SubscriptionSummary;
  onUsageInput: (subscription: SubscriptionSummary) => void;
  onDelete: () => void;
}

export default function SubscriptionCard({
  subscription,
  onUsageInput,
  onDelete,
}: SubscriptionCardProps) {
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const queryClient = useQueryClient();

  const deleteMutation = useMutation({
    mutationFn: () => subscriptionApi.delete(subscription.id),
    onSuccess: () => {
      onDelete();
      // 대시보드/전체 구독 목록 모두 갱신
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      queryClient.invalidateQueries({ queryKey: ['subscriptions'] });
    },
  });

  const handleDelete = () => {
    deleteMutation.mutate();
    setShowDeleteConfirm(false);
  };

  const statusColor = getStatusColor(subscription.status);
  const efficiencyColor = getEfficiencyColor(subscription.efficiencyRate);
  const unitLabel = getCategoryUnitLabel(subscription.categoryName);

  return (
    <div className="card hover:shadow-md transition-shadow">
      <div className="card-body space-y-4">
        {/* 헤더 */}
        <div className="flex items-start justify-between">
          <div className="flex-1">
            <div className="flex items-center gap-2 mb-1">
              <h3 className="font-semibold text-lg text-gray-900">
                {subscription.name}
              </h3>
              {subscription.trial && (
                <span className="badge bg-purple-50 text-purple-700 border-purple-200">
                  체험판
                </span>
              )}
            </div>
            <p className="text-sm text-gray-500">
              {getCategoryLabel(subscription.categoryName)}
            </p>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={() => onUsageInput(subscription)}
              className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
              title="사용량 입력"
            >
              <Edit3 className="w-4 h-4 text-gray-600" />
            </button>
            <button
              onClick={() => setShowDeleteConfirm(true)}
              className="p-2 hover:bg-danger-50 rounded-lg transition-colors"
              title="삭제"
            >
              <Trash2 className="w-4 h-4 text-danger-600" />
            </button>
          </div>
        </div>

        {/* 효율 점수 바 */}
        <div className="space-y-2">
          <div className="flex justify-between items-center text-sm">
            <span className="text-gray-600">효율 점수</span>
            <span className="font-semibold" style={{ color: efficiencyColor }}>
              {subscription.efficiencyRate.toFixed(1)}%
            </span>
          </div>
          <div className="status-bar">
            <div
              className="status-bar-fill"
              style={{
                width: `${Math.min(subscription.efficiencyRate, 100)}%`,
                backgroundColor: efficiencyColor,
              }}
            />
          </div>
        </div>

        {/* 상태 배지 */}
        <div className="flex items-start gap-2">
          <span className={`badge ${statusColor}`}>
            {getStatusLabel(subscription.status)}
          </span>
          <p className="text-xs text-gray-600 flex-1">
            {getStatusDescription(subscription.status)}
          </p>
        </div>

        {/* 통계 정보 */}
        <div className="grid grid-cols-2 gap-4 pt-4 border-t border-gray-100">
          <div>
            <p className="text-xs text-gray-500 mb-1">단위({unitLabel})당 비용</p>
            <p className="text-sm font-semibold text-gray-900">
              {formatCurrency(subscription.costPerUnit)}
            </p>
          </div>
          
          {subscription.trial ? (
            <div>
              <p className="text-xs text-gray-500 mb-1">잠재 연간 낭비</p>
              <p className="text-sm font-semibold text-orange-600">
                {formatCurrency(subscription.potentialAnnualWaste)}
              </p>
            </div>
          ) : (
            <div>
              <p className="text-xs text-gray-500 mb-1">연간 낭비액</p>
              <p className="text-sm font-semibold text-danger-600">
                {formatCurrency(subscription.annualWaste)}
              </p>
            </div>
          )}
        </div>

        {/* 경고 메시지 */}
        {subscription.status === 'GHOST' && (
          <div className="flex items-start gap-2 p-3 bg-danger-50 border border-danger-200 rounded-lg">
            <AlertTriangle className="w-4 h-4 text-danger-600 mt-0.5 flex-shrink-0" />
            <p className="text-xs text-danger-700">
              이번 달 사용 기록이 없습니다. 즉시 해지를 권장합니다.
            </p>
          </div>
        )}

        {subscription.trial && subscription.efficiencyRate < 70 && (
          <div className="flex items-start gap-2 p-3 bg-warning-50 border border-warning-200 rounded-lg">
            <Sparkles className="w-4 h-4 text-warning-600 mt-0.5 flex-shrink-0" />
            <p className="text-xs text-warning-700">
              유료 전환 시 연간 {formatCurrency(subscription.potentialAnnualWaste)}의 
              낭비가 예상됩니다.
            </p>
          </div>
        )}
      </div>

      {/* 삭제 확인 모달 */}
      {showDeleteConfirm && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl p-6 max-w-sm w-full animate-fade-in">
            <h3 className="text-lg font-semibold text-gray-900 mb-2">
              구독 삭제
            </h3>
            <p className="text-sm text-gray-600 mb-6">
              {subscription.name}을(를) 삭제하시겠습니까?
              이 작업은 되돌릴 수 없습니다.
            </p>
            <div className="flex gap-3">
              <button
                onClick={() => setShowDeleteConfirm(false)}
                className="btn-secondary flex-1"
                disabled={deleteMutation.isPending}
              >
                취소
              </button>
              <button
                onClick={handleDelete}
                className="btn-danger flex-1"
                disabled={deleteMutation.isPending}
              >
                {deleteMutation.isPending ? '삭제 중...' : '삭제'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
