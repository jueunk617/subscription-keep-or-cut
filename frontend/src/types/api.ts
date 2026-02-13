export type BillingCycle = 'MONTHLY' | 'QUARTERLY' | 'ANNUAL';
export type SubscriptionStatus = 'ACTIVE' | 'TRIAL';
export type EvaluationStatus = 'EFFICIENT' | 'KEEP' | 'REVIEW' | 'INEFFICIENT' | 'GHOST';
export type CategoryType = 'CONTENT' | 'PRODUCTIVITY';
export type UsageUnit = 'MINUTES' | 'DAYS';

export interface Category {
  id: number;
  name: string;
  referenceValue: number;
  unit: UsageUnit;
  type: CategoryType;
}

export interface Subscription {
  id: number;
  categoryName: string;
  name: string;
  monthlyShareCost: number;
  billingCycle: BillingCycle;
  status: SubscriptionStatus;
}

export interface SubscriptionSummary {
  id: number;
  categoryName: string;
  name: string;
  efficiencyRate: number;
  status: EvaluationStatus;
  annualWaste: number;
  trial: boolean;
  potentialAnnualWaste: number;
  costPerUnit: number;
}

export interface DashboardResponse {
  totalMonthlyCost: number;
  totalAnnualWasteEstimate: number;
  subscriptions: SubscriptionSummary[];
}

export interface CreateSubscriptionRequest {
  categoryId: number;
  name: string;
  totalCost: number;
  userShareCost: number;
  billingCycle: BillingCycle;
  status: SubscriptionStatus;
}

export interface UsageRequest {
  subscriptionId: number;
  date: string; // YYYY-MM
  usageValue: number;
}

export interface ApiResponse<T> {
  success: boolean;
  code: string;
  message: string;
  data: T;
}