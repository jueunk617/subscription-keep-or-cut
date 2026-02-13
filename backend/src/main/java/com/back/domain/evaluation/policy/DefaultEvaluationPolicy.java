package com.back.domain.evaluation.policy;

import com.back.domain.evaluation.enums.EvaluationStatus;
import org.springframework.stereotype.Component;

@Component
public class DefaultEvaluationPolicy implements EvaluationPolicy {

    @Override
    public EvaluationStatus calculateStatus(double rate, int usageValue) {
        if (usageValue == 0) {
            return EvaluationStatus.GHOST;
        }

        if (rate >= 100) {
            return EvaluationStatus.EFFICIENT;
        } else if (rate >= 70) {
            return EvaluationStatus.KEEP;
        } else if (rate >= 40) {
            return EvaluationStatus.REVIEW;
        } else {
            return EvaluationStatus.INEFFICIENT;
        }
    }
}