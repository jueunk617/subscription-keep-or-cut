import { useState } from 'react';
import type { SubscriptionSummary } from '@/src/types/api';
import SubscriptionCard from './SubscriptionCard';
import UsageInputModal from '../usage/UsageInputModal';

interface SubscriptionListProps {
  subscriptions: SubscriptionSummary[];
  year: number;
  month: number;
  onRefresh: () => void;
}

export default function SubscriptionList({
  subscriptions,
  year,
  month,
  onRefresh,
}: SubscriptionListProps) {
  const [selectedSubscription, setSelectedSubscription] = useState<SubscriptionSummary | null>(null);

  const handleUsageInput = (subscription: SubscriptionSummary) => {
    setSelectedSubscription(subscription);
  };

  const handleCloseModal = () => {
    setSelectedSubscription(null);
  };

  const handleUsageSuccess = () => {
    handleCloseModal();
    onRefresh();
  };

  return (
    <>
      <div className="space-y-4 animate-slide-in">
        <h2 className="text-xl font-bold text-gray-900">구독 목록</h2>
        
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          {subscriptions.map((subscription) => (
            <SubscriptionCard
              key={subscription.id}
              subscription={subscription}
              onUsageInput={handleUsageInput}
              onDelete={onRefresh}
            />
          ))}
        </div>
      </div>

      {selectedSubscription && (
        <UsageInputModal
          subscription={selectedSubscription}
          year={year}
          month={month}
          onClose={handleCloseModal}
          onSuccess={handleUsageSuccess}
        />
      )}
    </>
  );
}
