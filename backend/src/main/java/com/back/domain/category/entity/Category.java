package com.back.domain.category.entity;

import com.back.domain.category.enums.CategoryType;
import com.back.domain.category.enums.UsageUnit;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private int referenceValue; // 기준값 (분 or 일)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UsageUnit unit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType type;

    public Category(String name, int referenceValue, UsageUnit unit, CategoryType type) {
        this.name = name;
        this.referenceValue = referenceValue;
        this.unit = unit;
        this.type = type;
    }
}