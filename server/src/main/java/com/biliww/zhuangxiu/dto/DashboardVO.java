package com.biliww.zhuangxiu.dto;

import lombok.Data;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import java.math.BigDecimal;
import java.util.List;

/**
 * 首页总览仪表盘：当前用户所有项目的汇总。含整体预算/花费/完成度、
 * 分类花费占比、分阶段完成度，以及各项目卡片（含各自完成度）。
 */
@Data
@RegisterReflectionForBinding
public class DashboardVO {

    private BigDecimal totalBudget;
    private BigDecimal spent;
    private BigDecimal remaining;
    private boolean overBudget;
    private double completionRate;

    private List<OverviewVO.CategoryStat> categoryStats;
    private List<PhaseProgress> phaseProgress;
    private List<ProjectCard> projects;

    @Data
    @RegisterReflectionForBinding
    public static class ProjectCard {
        private Long id;
        private String name;
        private String status;
        private BigDecimal area;
        private BigDecimal totalBudget;
        private double completionRate;
    }

    @Data
    @RegisterReflectionForBinding
    public static class PhaseProgress {
        private String name;
        private long done;
        private long total;
        private double rate;
    }
}
