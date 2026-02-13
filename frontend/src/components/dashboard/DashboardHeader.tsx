export default function DashboardHeader() {
  return (
    <div className="animate-fade-in text-center">
      <h1 className="text-4xl sm:text-5xl font-extrabold tracking-tight text-gray-900">
        Keep Or Cut
      </h1>
      <p className="mt-4 text-base sm:text-lg text-gray-600">
        구독을 계속 가져갈지, 과감히 정리할지
        <br className="hidden sm:block" />
        한눈에 비교해서 결정해 보세요.
      </p>
    </div>
  );
}