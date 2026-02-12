package com.back.global.init;

import com.back.domain.category.entity.Category;
import com.back.domain.category.enums.CategoryType;
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

        if (categoryRepository.count() > 0) {
            return;
        }

        List<Category> categories = List.of(

                // 콘텐츠형
                new Category("OTT", 1800, UsageUnit.MINUTES, CategoryType.CONTENT),
                new Category("MUSIC", 1500, UsageUnit.MINUTES, CategoryType.CONTENT),
                new Category("EBOOK", 300, UsageUnit.MINUTES, CategoryType.CONTENT),

                // 생산성형
                new Category("AI_TOOL", 12, UsageUnit.DAYS, CategoryType.PRODUCTIVITY),
                new Category("WORK_TOOL", 20, UsageUnit.DAYS, CategoryType.PRODUCTIVITY),
                new Category("CLOUD", 25, UsageUnit.DAYS, CategoryType.PRODUCTIVITY)
        );

        categoryRepository.saveAll(categories);
    }
}