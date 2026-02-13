import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { X } from 'lucide-react';
import { subscriptionApi } from '@/src/lib/api';
import type { BillingCycle, SubscriptionStatus } from '@/src/types/api';
import { getBillingCycleLabel } from '@/src/lib/utils';

interface AddSubscriptionModalProps {
  onClose: () => void;
  onSuccess: () => void;
}

// 카테고리 목록 (백엔드 초기 데이터와 일치)
const CATEGORIES = [
  { id: 1, name: 'OTT', label: 'OTT' },
  { id: 2, name: 'MUSIC', label: '음악 스트리밍' },
  { id: 3, name: 'EBOOK', label: '전자책' },
  { id: 4, name: 'AI_TOOL', label: 'AI 도구' },
  { id: 5, name: 'WORK_TOOL', label: '업무 도구' },
  { id: 6, name: 'CLOUD', label: '클라우드 스토리지' },
];

export default function AddSubscriptionModal({ onClose, onSuccess }: AddSubscriptionModalProps) {
  const [formData, setFormData] = useState({
    categoryId: '',
    name: '',
    totalCost: '',
    userShareCost: '',
    billingCycle: 'MONTHLY' as BillingCycle,
    status: 'ACTIVE' as SubscriptionStatus,
  });

  const [errors, setErrors] = useState<Record<string, string>>({});

  const mutation = useMutation({
    mutationFn: () => subscriptionApi.create({
      categoryId: Number(formData.categoryId),
      name: formData.name,
      totalCost: Number(formData.totalCost),
      userShareCost: Number(formData.userShareCost),
      billingCycle: formData.billingCycle,
      status: formData.status,
    }),
    onSuccess: () => {
      onSuccess();
    },
    onError: (error: any) => {
      const errorData = error.response?.data?.data;
      if (Array.isArray(errorData)) {
        const newErrors: Record<string, string> = {};
        errorData.forEach((err: any) => {
          newErrors[err.field] = err.message;
        });
        setErrors(newErrors);
      }
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    // 클라이언트 검증
    const newErrors: Record<string, string> = {};
    
    if (!formData.categoryId) newErrors.categoryId = '카테고리를 선택해주세요';
    if (!formData.name.trim()) newErrors.name = '구독 이름을 입력해주세요';
    if (!formData.totalCost || Number(formData.totalCost) < 0) {
      newErrors.totalCost = '총 비용을 입력해주세요';
    }
    if (!formData.userShareCost || Number(formData.userShareCost) < 0) {
      newErrors.userShareCost = '사용자 부담 금액을 입력해주세요';
    }
    if (Number(formData.userShareCost) > Number(formData.totalCost)) {
      newErrors.userShareCost = '사용자 부담 금액은 총 비용보다 클 수 없습니다';
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    mutation.mutate();
  };

  const handleChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    // 에러 클리어
    if (errors[field]) {
      setErrors(prev => {
        const newErrors = { ...prev };
        delete newErrors[field];
        return newErrors;
      });
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto animate-slide-in">
        {/* 헤더 */}
        <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between">
          <h2 className="text-xl font-semibold text-gray-900">구독 추가</h2>
          <button
            onClick={onClose}
            className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* 폼 */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {/* 카테고리 */}
          <div>
            <label className="label">카테고리</label>
            <select
              value={formData.categoryId}
              onChange={(e) => handleChange('categoryId', e.target.value)}
              className="input"
            >
              <option value="">선택해주세요</option>
              {CATEGORIES.map((cat) => (
                <option key={cat.id} value={cat.id}>
                  {cat.label}
                </option>
              ))}
            </select>
            {errors.categoryId && (
              <p className="mt-1 text-xs text-danger-600">{errors.categoryId}</p>
            )}
          </div>

          {/* 구독 이름 */}
          <div>
            <label className="label">구독 이름</label>
            <input
              type="text"
              value={formData.name}
              onChange={(e) => handleChange('name', e.target.value)}
              placeholder="예: Netflix, ChatGPT Plus"
              className="input"
            />
            {errors.name && (
              <p className="mt-1 text-xs text-danger-600">{errors.name}</p>
            )}
          </div>

          {/* 결제 주기 */}
          <div>
            <label className="label">결제 주기</label>
            <div className="grid grid-cols-3 gap-3">
              {(['MONTHLY', 'QUARTERLY', 'ANNUAL'] as BillingCycle[]).map((cycle) => (
                <button
                  key={cycle}
                  type="button"
                  onClick={() => handleChange('billingCycle', cycle)}
                  className={`p-3 rounded-lg border-2 transition-colors ${
                    formData.billingCycle === cycle
                      ? 'border-primary-500 bg-primary-50 text-primary-700'
                      : 'border-gray-200 hover:border-gray-300'
                  }`}
                >
                  {getBillingCycleLabel(cycle)}
                </button>
              ))}
            </div>
          </div>

          {/* 총 비용 */}
          <div>
            <label className="label">총 비용 (원)</label>
            <input
              type="number"
              value={formData.totalCost}
              onChange={(e) => handleChange('totalCost', e.target.value)}
              placeholder="0"
              min="0"
              className="input"
            />
            {errors.totalCost && (
              <p className="mt-1 text-xs text-danger-600">{errors.totalCost}</p>
            )}
            <p className="mt-1 text-xs text-gray-500">
              {formData.billingCycle === 'MONTHLY' && '월간 결제 금액'}
              {formData.billingCycle === 'QUARTERLY' && '분기 결제 금액'}
              {formData.billingCycle === 'ANNUAL' && '연간 결제 금액'}
            </p>
          </div>

          {/* 사용자 부담 금액 */}
          <div>
            <label className="label">사용자 부담 금액 (원)</label>
            <input
              type="number"
              value={formData.userShareCost}
              onChange={(e) => handleChange('userShareCost', e.target.value)}
              placeholder="0"
              min="0"
              className="input"
            />
            {errors.userShareCost && (
              <p className="mt-1 text-xs text-danger-600">{errors.userShareCost}</p>
            )}
            <p className="mt-1 text-xs text-gray-500">
              가족 공유 등으로 비용을 분담하는 경우 실제 부담 금액을 입력하세요
            </p>
          </div>

          {/* 구독 상태 */}
          <div>
            <label className="label">구독 상태</label>
            <div className="grid grid-cols-2 gap-3">
              {(['ACTIVE', 'TRIAL'] as SubscriptionStatus[]).map((status) => (
                <button
                  key={status}
                  type="button"
                  onClick={() => handleChange('status', status)}
                  className={`p-3 rounded-lg border-2 transition-colors ${
                    formData.status === status
                      ? 'border-primary-500 bg-primary-50 text-primary-700'
                      : 'border-gray-200 hover:border-gray-300'
                  }`}
                >
                  {status === 'ACTIVE' ? '정식 구독' : '무료 체험'}
                </button>
              ))}
            </div>
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
              {mutation.isPending ? '추가 중...' : '구독 추가'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
