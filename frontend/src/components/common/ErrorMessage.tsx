import { AlertCircle } from 'lucide-react';

interface ErrorMessageProps {
  message: string;
}

export default function ErrorMessage({ message }: ErrorMessageProps) {
  return (
    <div className="card max-w-md">
      <div className="card-body text-center py-8">
        <div className="flex justify-center mb-4">
          <div className="bg-danger-50 p-4 rounded-full">
            <AlertCircle className="w-8 h-8 text-danger-600" />
          </div>
        </div>
        <h3 className="text-lg font-semibold text-gray-900 mb-2">오류가 발생했습니다</h3>
        <p className="text-gray-600">{message}</p>
      </div>
    </div>
  );
}