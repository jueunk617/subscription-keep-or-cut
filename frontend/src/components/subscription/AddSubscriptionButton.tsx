'use client';

import { useState } from 'react';
import { Plus } from 'lucide-react';
import AddSubscriptionModal from './AddSubscriptionModal';

interface AddSubscriptionButtonProps {
  onSuccess: () => void;
}

export default function AddSubscriptionButton({ onSuccess }: AddSubscriptionButtonProps) {
  const [isOpen, setIsOpen] = useState(false);

  const handleSuccess = () => {
    setIsOpen(false);
    onSuccess();
  };

  return (
    <>
      <button
        onClick={() => setIsOpen(true)}
        className="btn-primary flex items-center gap-2"
        data-add-subscription
      >
        <Plus className="w-4 h-4" />
        구독 추가
      </button>

      {isOpen && (
        <AddSubscriptionModal
          onClose={() => setIsOpen(false)}
          onSuccess={handleSuccess}
        />
      )}
    </>
  );
}
