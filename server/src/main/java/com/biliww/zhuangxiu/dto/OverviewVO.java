package com.biliww.zhuangxiu.dto;

import lombok.Data;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import java.math.BigDecimal;
import java.util.List;

/**
 * 概览仪表盘返回：总预算/已花/剩余、阶段预算 vs 实际、分类占比、完成度。
 */
@Data
@RegisterReflectionForBinding
public class OverviewVO {

    private BigDecimal totalBudget;
    private BigDecimal spent;
    private BigDecimal remaining;
    private boolean overBudget;
    private BigDecimal phaseBudgetTotal;
    private BigDecimal phaseSpentTotal;
    private double completionRate;

    private List<PhaseStat> phaseStats;
    private List<CategoryStat> categoryStats;

    @Data
    @RegisterReflectionForBinding
    public static class PhaseStat {
        private String name;
        private BigDecimal budget;
        private BigDecimal actual;
        private boolean over;
    }

    @Data
    @RegisterReflectionForBinding
    public static class CategoryStat {
        private String category;
        private BigDecimal amount;
    }
}
