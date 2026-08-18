package com.biliww.zhuangxiu.service;

import com.biliww.zhuangxiu.common.BusinessException;
import com.biliww.zhuangxiu.common.Constants;
import com.biliww.zhuangxiu.common.ErrorCode;
import com.biliww.zhuangxiu.domain.Expense;
import com.biliww.zhuangxiu.domain.Phase;
import com.biliww.zhuangxiu.domain.Project;
import com.biliww.zhuangxiu.dto.DashboardVO;
import com.biliww.zhuangxiu.dto.OverviewVO;
import com.biliww.zhuangxiu.repository.ExpenseRepository;
import com.biliww.zhuangxiu.repository.PhaseRepository;
import com.biliww.zhuangxiu.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OverviewService {

    private final ProjectRepository projectRepository;
    private final PhaseRepository phaseRepository;
    private final ExpenseRepository expenseRepository;

    public OverviewVO get(Long userId, Long projectId) {
        Project p = getOwned(userId, projectId);
        List<Phase> phases = phaseRepository.findByProjectIdOrderBySortOrderAsc(projectId);
        List<Expense> expenses = expenseRepository.findByProjectId(projectId);

        BigDecimal spent = expenses.stream().map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalBudget = p.getTotalBudget() == null ? BigDecimal.ZERO : p.getTotalBudget();
        BigDecimal remaining = totalBudget.subtract(spent);
        boolean overBudget = remaining.signum() < 0;

        // 按阶段聚合实际花费
        Map<Long, BigDecimal> phaseActual = expenses.stream()
                .filter(e -> e.getPhaseId() != null)
                .collect(Collectors.groupingBy(Expense::getPhaseId,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)));

        List<OverviewVO.PhaseStat> phaseStats = phases.stream().map(ph -> {
            OverviewVO.PhaseStat s = new OverviewVO.PhaseStat();
            s.setName(ph.getName());
            s.setBudget(ph.getBudget());
            BigDecimal actual = phaseActual.getOrDefault(ph.getId(), BigDecimal.ZERO);
            s.setActual(actual);
            s.setOver(ph.getBudget().signum() > 0 && actual.compareTo(ph.getBudget()) > 0);
            return s;
        }).toList();

        // 分类占比
        Map<String, BigDecimal> cat = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)));
        List<OverviewVO.CategoryStat> categoryStats = cat.entrySet().stream()
                .map(en -> {
                    OverviewVO.CategoryStat s = new OverviewVO.CategoryStat();
                    s.setCategory(en.getKey());
                    s.setAmount(en.getValue());
                    return s;
                })
                .sorted(Comparator.comparing(OverviewVO.CategoryStat::getAmount).reversed())
                .toList();

        long done = phases.stream().filter(ph -> "已完成".equals(ph.getStatus())).count();
        double completionRate = phases.isEmpty() ? 0.0 : (double) done / phases.size();

        BigDecimal phaseBudgetTotal = phases.stream().map(Phase::getBudget)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        OverviewVO vo = new OverviewVO();
        vo.setTotalBudget(totalBudget);
        vo.setSpent(spent);
        vo.setRemaining(remaining);
        vo.setOverBudget(overBudget);
        vo.setPhaseBudgetTotal(phaseBudgetTotal);
        vo.setPhaseSpentTotal(spent);
        vo.setCompletionRate(completionRate);
        vo.setPhaseStats(phaseStats);
        vo.setCategoryStats(categoryStats);
        return vo;
    }

    private Project getOwned(Long userId, Long projectId) {
        Project p = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "项目不存在"));
        if (!p.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限访问该项目");
        }
        return p;
    }

    /** 当前用户所有项目的总览聚合：整体预算/花费/完成度、分类占比、分阶段完成度、各项目卡片。 */
    public DashboardVO allOverview(Long userId) {
        List<Project> projects = projectRepository.findByUserId(userId, Pageable.unpaged()).getContent();
        List<Long> ids = projects.stream().map(Project::getId).toList();
        List<Phase> phases = ids.isEmpty() ? List.of() : phaseRepository.findByProjectIdIn(ids);
        List<Expense> expenses = ids.isEmpty() ? List.of() : expenseRepository.findByProjectIdIn(ids);

        BigDecimal totalBudget = projects.stream()
                .map(p -> p.getTotalBudget() == null ? BigDecimal.ZERO : p.getTotalBudget())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal spent = expenses.stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remaining = totalBudget.subtract(spent);
        boolean overBudget = remaining.signum() < 0;

        // 各项目完成度
        Map<Long, List<Phase>> byProject = phases.stream()
                .collect(Collectors.groupingBy(Phase::getProjectId));
        List<DashboardVO.ProjectCard> cards = projects.stream().map(p -> {
            List<Phase> ps = byProject.getOrDefault(p.getId(), List.of());
            long done = ps.stream().filter(ph -> "已完成".equals(ph.getStatus())).count();
            double cr = ps.isEmpty() ? 0.0 : (double) done / ps.size();
            DashboardVO.ProjectCard c = new DashboardVO.ProjectCard();
            c.setId(p.getId());
            c.setName(p.getName());
            c.setStatus(p.getStatus());
            c.setArea(p.getArea());
            c.setTotalBudget(p.getTotalBudget());
            c.setCompletionRate(cr);
            return c;
        }).toList();

        long doneAll = phases.stream().filter(ph -> "已完成".equals(ph.getStatus())).count();
        double completionRate = phases.isEmpty() ? 0.0 : (double) doneAll / phases.size();

        // 分类花费占比（跨项目聚合）
        Map<String, BigDecimal> cat = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)));
        List<OverviewVO.CategoryStat> categoryStats = cat.entrySet().stream().map(en -> {
            OverviewVO.CategoryStat s = new OverviewVO.CategoryStat();
            s.setCategory(en.getKey());
            s.setAmount(en.getValue());
            return s;
        }).sorted(Comparator.comparing(OverviewVO.CategoryStat::getAmount).reversed()).toList();

        // 分阶段完成度（按标准工序顺序，仅展示有数据的阶段）
        List<DashboardVO.PhaseProgress> phaseProgress = new ArrayList<>();
        for (String name : Constants.STANDARD_PHASES) {
            long total = phases.stream().filter(ph -> name.equals(ph.getName())).count();
            if (total == 0) continue;
            long done = phases.stream()
                    .filter(ph -> name.equals(ph.getName()) && "已完成".equals(ph.getStatus()))
                    .count();
            DashboardVO.PhaseProgress pp = new DashboardVO.PhaseProgress();
            pp.setName(name);
            pp.setTotal(total);
            pp.setDone(done);
            pp.setRate((double) done / total);
            phaseProgress.add(pp);
        }

        DashboardVO vo = new DashboardVO();
        vo.setTotalBudget(totalBudget);
        vo.setSpent(spent);
        vo.setRemaining(remaining);
        vo.setOverBudget(overBudget);
        vo.setCompletionRate(completionRate);
        vo.setCategoryStats(categoryStats);
        vo.setPhaseProgress(phaseProgress);
        vo.setProjects(cards);
        return vo;
    }
}
