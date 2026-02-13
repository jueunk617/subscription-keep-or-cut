package com.back.domain.evaluation.policy;

import com.back.domain.evaluation.enums.EvaluationStatus;

/**
 * 구독 평가 정책 인터페이스
 * 향후 카테고리별로 다른 평가 기준이 필요할 때 이 인터페이스를 구현합니다.
 */
public interface EvaluationPolicy {
    EvaluationStatus calculateStatus(double efficiencyRate, int usageValue);
}