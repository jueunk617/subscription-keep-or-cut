package com.back.global.init;

import com.back.domain.category.entity.Category;
import com.back.domain.category.enums.UsageUnit;
import com.back.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        // 이미 카테고리가 등록되어 있다면 중복 실행 방지
        if (categoryRepository.count() > 0) {
            return;
        }

        // 기획서 기준 데이터 정의
        List<Category> categories = List.of(
                new Category("OTT", 1800, UsageUnit.MINUTES),     // 월 30시간
                new Category("MUSIC", 1500, UsageUnit.MINUTES),   // 월 25시간
                new Category("EBOOK", 300, UsageUnit.MINUTES),    // 월 5시간
                new Category("AI_TOOL", 12, UsageUnit.DAYS),      // 월 12일
                new Category("WORK_TOOL", 20, UsageUnit.DAYS),    // 월 20일
                new Category("CLOUD", 25, UsageUnit.DAYS)         // 월 25일
        );

        categoryRepository.saveAll(categories);
    }
}