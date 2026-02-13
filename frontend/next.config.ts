import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  turbopack: {
    // 현재 폴더를 기준으로 모든 패키지를 찾도록 강제 설정
    root: ".",
  },
};

export default nextConfig;