package com.biliww.zhuangxiu.service;

import com.biliww.zhuangxiu.common.BusinessException;
import com.biliww.zhuangxiu.common.ErrorCode;
import com.biliww.zhuangxiu.domain.Expense;
import com.biliww.zhuangxiu.domain.Phase;
import com.biliww.zhuangxiu.domain.PhaseTask;
import com.biliww.zhuangxiu.domain.Project;
import com.biliww.zhuangxiu.dto.PhaseCreateRequest;
import com.biliww.zhuangxiu.dto.PhaseUpdateRequest;
import com.biliww.zhuangxiu.dto.PhaseVO;
import com.biliww.zhuangxiu.repository.ExpenseRepository;
import com.biliww.zhuangxiu.repository.PhaseRepository;
import com.biliww.zhuangxiu.repository.PhaseTaskRepository;
import com.biliww.zhuangxiu.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PhaseService {

    private final PhaseRepository phaseRepository;
    private final ProjectRepository projectRepository;
    private final ExpenseRepository expenseRepository;
    private final PhaseTaskRepository phaseTaskRepository;

    public List<PhaseVO> list(Long userId, Long projectId) {
        ensureOwned(userId, projectId);
        return phaseRepository.findByProjectIdOrderBySortOrderAsc(projectId).stream()
                .map(PhaseService::toVO).toList();
    }

    /** 新增阶段：追加到项目末尾（sort_order = 当前阶段数），默认未开始、预算 0。 */
    @Transactional
    public PhaseVO create(Long userId, Long projectId, PhaseCreateRequest req) {
        ensureOwned(userId, projectId);
        int nextOrder = phaseRepository.findByProjectIdOrderBySortOrderAsc(projectId).size();
        Phase ph = new Phase();
        ph.setProjectId(projectId);
        ph.setName(req.getName().trim());
        ph.setSortOrder(nextOrder);
        ph.setStatus("未开始");
        ph.setBudget(BigDecimal.ZERO);
        return toVO(phaseRepository.save(ph));
    }

    public PhaseVO update(Long userId, Long phaseId, PhaseUpdateRequest req) {
        Phase ph = phaseRepository.findById(phaseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "阶段不存在"));
        ensureOwned(userId, ph.getProjectId());
        if (req.getName() != null && !req.getName().isBlank()) ph.setName(req.getName().trim());
        if (req.getSortOrder() != null) ph.setSortOrder(req.getSortOrder());
        if (req.getStatus() != null) ph.setStatus(req.getStatus());
        if (req.getStartDate() != null) ph.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) ph.setEndDate(req.getEndDate());
        if (req.getBudget() != null) ph.setBudget(req.getBudget());
        if (req.getNote() != null) ph.setNote(req.getNote());
        return toVO(phaseRepository.save(ph));
    }

    /** 删除阶段：级联清理其下花销与子项，避免外键约束冲突。 */
    @Transactional
    public void delete(Long userId, Long phaseId) {
        Phase ph = phaseRepository.findById(phaseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "阶段不存在"));
        ensureOwned(userId, ph.getProjectId());
        expenseRepository.deleteByPhaseId(phaseId);
        phaseTaskRepository.deleteByPhaseId(phaseId);
        phaseRepository.deleteById(phaseId);
    }

    private void ensureOwned(Long userId, Long projectId) {
        Project p = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "项目不存在"));
        if (!p.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限访问该项目");
        }
    }

    public static PhaseVO toVO(Phase ph) {
        PhaseVO v = new PhaseVO();
        v.setId(ph.getId());
        v.setName(ph.getName());
        v.setSortOrder(ph.getSortOrder());
        v.setStartDate(ph.getStartDate());
        v.setEndDate(ph.getEndDate());
        v.setStatus(ph.getStatus());
        v.setBudget(ph.getBudget());
        v.setNote(ph.getNote());
        return v;
    }
}
