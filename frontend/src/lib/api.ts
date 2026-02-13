import axios from 'axios';

import type { 
  ApiResponse, 
  DashboardResponse, 
  Subscription,
  CreateSubscriptionRequest,
  UsageRequest,
  Category
} from '@/src/types/api';

const api = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

export const subscriptionApi = {
  // 구독 목록 조회
  getAll: () => 
    api.get<ApiResponse<Subscription[]>>('/subscriptions'),

  // 구독 생성
  create: (data: CreateSubscriptionRequest) => 
    api.post<ApiResponse<Subscription>>('/subscriptions', data),

  // 구독 삭제
  delete: (id: number) => 
    api.delete<ApiResponse<void>>(`/subscriptions/${id}`),
};

export const dashboardApi = {
  // 월별 대시보드 조회
  getMonthly: (year: number, month: number) => 
    api.get<ApiResponse<DashboardResponse>>('/dashboard', {
      params: { year, month },
    }),
};

export const usageApi = {
  // 사용량 기록
  record: (data: UsageRequest) => 
    api.post<ApiResponse<void>>('/usages', data),
};

export const categoryApi = {
  // 카테고리 목록 조회 (초기 데이터에서 사용)
  getAll: () => 
    api.get<ApiResponse<Category[]>>('/categories'),
};

export default api;