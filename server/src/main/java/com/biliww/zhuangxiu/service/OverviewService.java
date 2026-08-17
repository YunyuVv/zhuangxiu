package com.biliww.zhuangxiu.service;

import com.biliww.zhuangxiu.common.BusinessException;
import com.biliww.zhuangxiu.common.ErrorCode;
import com.biliww.zhuangxiu.domain.Expense;
import com.biliww.zhuangxiu.domain.Phase;
import com.biliww.zhuangxiu.domain.Project;
import com.biliww.zhuangxiu.dto.OverviewVO;
import com.biliww.zhuangxiu.repository.ExpenseRepository;
import com.biliww.zhuangxiu.repository.PhaseRepository;
import com.biliww.zhuangxiu.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
}
