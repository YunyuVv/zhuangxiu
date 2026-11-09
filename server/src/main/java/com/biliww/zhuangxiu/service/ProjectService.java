package com.biliww.zhuangxiu.service;

import com.biliww.zhuangxiu.common.BusinessException;
import com.biliww.zhuangxiu.common.Constants;
import com.biliww.zhuangxiu.common.ErrorCode;
import com.biliww.zhuangxiu.common.PageVO;
import com.biliww.zhuangxiu.domain.Phase;
import com.biliww.zhuangxiu.domain.Project;
import com.biliww.zhuangxiu.dto.ProjectCreateRequest;
import com.biliww.zhuangxiu.dto.ProjectUpdateRequest;
import com.biliww.zhuangxiu.dto.ProjectVO;
import com.biliww.zhuangxiu.repository.ExpenseRepository;
import com.biliww.zhuangxiu.repository.PhaseRepository;
import com.biliww.zhuangxiu.repository.PhaseTaskRepository;
import com.biliww.zhuangxiu.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final PhaseRepository phaseRepository;
    private final ExpenseRepository expenseRepository;
    private final PhaseTaskRepository phaseTaskRepository;

    @Transactional
    public ProjectVO create(Long userId, ProjectCreateRequest req) {
        Project p = new Project();
        p.setUserId(userId);
        p.setName(req.getName());
        p.setAddress(req.getAddress());
        p.setArea(req.getArea());
        p.setTotalBudget(req.getTotalBudget() == null ? BigDecimal.ZERO : req.getTotalBudget());
        p.setStartDate(req.getStartDate());
        p.setExpectedEndDate(req.getExpectedEndDate());
        p.setStatus("进行中");
        p = projectRepository.save(p);
        initPhases(p.getId());
        return toVO(p);
    }

    /** 新建项目时自动初始化 8 个标准阶段（阶段预算默认 0）。 */
    private void initPhases(Long projectId) {
        List<Phase> phases = new ArrayList<>();
        int i = 0;
        for (String name : Constants.STANDARD_PHASES) {
            Phase ph = new Phase();
            ph.setProjectId(projectId);
            ph.setName(name);
            ph.setSortOrder(i++);
            ph.setStatus("未开始");
            ph.setBudget(BigDecimal.ZERO);
            phases.add(ph);
        }
        phaseRepository.saveAll(phases);
    }

    public ProjectVO update(Long userId, Long projectId, ProjectUpdateRequest req) {
        Project p = getOwned(userId, projectId);
        if (req.getName() != null) p.setName(req.getName());
        if (req.getAddress() != null) p.setAddress(req.getAddress());
        if (req.getArea() != null) p.setArea(req.getArea());
        if (req.getTotalBudget() != null) p.setTotalBudget(req.getTotalBudget());
        if (req.getStartDate() != null) p.setStartDate(req.getStartDate());
        if (req.getExpectedEndDate() != null) p.setExpectedEndDate(req.getExpectedEndDate());
        if (req.getStatus() != null) p.setStatus(req.getStatus());
        return toVO(projectRepository.save(p));
    }

    public ProjectVO get(Long userId, Long projectId) {
        return toVO(getOwned(userId, projectId));
    }

    public PageVO<ProjectVO> list(Long userId, int page, int size) {
        Page<Project> pg = projectRepository.findByUserId(userId, PageRequest.of(page, size));
        List<ProjectVO> list = pg.getContent().stream().map(ProjectService::toVO).toList();
        return PageVO.of(pg.getTotalElements(), page, size, list);
    }

    @Transactional
    public void delete(Long userId, Long projectId) {
        getOwned(userId, projectId);
        // 级联清理，避免外键约束冲突（expenses.phase_id、phase_tasks.phase_id/project_id）
        expenseRepository.deleteByProjectId(projectId);
        phaseTaskRepository.deleteByProjectId(projectId);
        phaseRepository.findByProjectIdOrderBySortOrderAsc(projectId)
                .forEach(ph -> phaseRepository.deleteById(ph.getId()));
        projectRepository.deleteById(projectId);
    }

    private Project getOwned(Long userId, Long projectId) {
        Project p = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "项目不存在"));
        if (!p.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限访问该项目");
        }
        return p;
    }

    public static ProjectVO toVO(Project p) {
        ProjectVO v = new ProjectVO();
        v.setId(p.getId());
        v.setName(p.getName());
        v.setAddress(p.getAddress());
        v.setArea(p.getArea());
        v.setTotalBudget(p.getTotalBudget());
        v.setStartDate(p.getStartDate());
        v.setExpectedEndDate(p.getExpectedEndDate());
        v.setStatus(p.getStatus());
        v.setCreatedAt(p.getCreatedAt());
        return v;
    }
}
