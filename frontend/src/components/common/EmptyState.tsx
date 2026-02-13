import { PackageOpen } from 'lucide-react';

interface EmptyStateProps {
  title: string;
  description: string;
  actionLabel?: string;
  onAction?: () => void;
}

export default function EmptyState({
  title,
  description,
  actionLabel,
  onAction,
}: EmptyStateProps) {
  return (
    <div className="card">
      <div className="card-body py-16 text-center">
        <div className="flex justify-center mb-4">
          <div className="bg-gray-100 p-6 rounded-full">
            <PackageOpen className="w-12 h-12 text-gray-400" />
          </div>
        </div>
        <h3 className="text-lg font-semibold text-gray-900 mb-2">{title}</h3>
        <p className="text-gray-600 mb-6 max-w-sm mx-auto">{description}</p>
        {actionLabel && onAction && (
          <button onClick={onAction} className="btn-primary">
            {actionLabel}
          </button>
        )}
      </div>
    </div>
  );
}