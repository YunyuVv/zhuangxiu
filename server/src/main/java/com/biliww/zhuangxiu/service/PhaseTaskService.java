package com.biliww.zhuangxiu.service;

import com.biliww.zhuangxiu.common.BusinessException;
import com.biliww.zhuangxiu.common.ErrorCode;
import com.biliww.zhuangxiu.domain.Phase;
import com.biliww.zhuangxiu.domain.PhaseTask;
import com.biliww.zhuangxiu.domain.Project;
import com.biliww.zhuangxiu.dto.PhaseTaskCreateRequest;
import com.biliww.zhuangxiu.dto.PhaseTaskUpdateRequest;
import com.biliww.zhuangxiu.dto.PhaseTaskVO;
import com.biliww.zhuangxiu.repository.PhaseRepository;
import com.biliww.zhuangxiu.repository.PhaseTaskRepository;
import com.biliww.zhuangxiu.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PhaseTaskService {

    private final PhaseTaskRepository phaseTaskRepository;
    private final PhaseRepository phaseRepository;
    private final ProjectRepository projectRepository;

    public List<PhaseTaskVO> list(Long userId, Long phaseId) {
        ensureOwned(userId, phaseId);
        return phaseTaskRepository.findByPhaseIdOrderBySortOrderAsc(phaseId).stream()
                .map(PhaseTaskService::toVO).toList();
    }

    @Transactional
    public PhaseTaskVO create(Long userId, Long phaseId, PhaseTaskCreateRequest req) {
        Phase ph = ensureOwned(userId, phaseId);
        int nextOrder = phaseTaskRepository.findByPhaseIdOrderBySortOrderAsc(phaseId).size();
        PhaseTask t = new PhaseTask();
        t.setProjectId(ph.getProjectId());
        t.setPhaseId(phaseId);
        t.setName(req.getName().trim());
        t.setNote(req.getNote());
        t.setDone(false);
        t.setSortOrder(nextOrder);
        return toVO(phaseTaskRepository.save(t));
    }

    public PhaseTaskVO update(Long userId, Long id, PhaseTaskUpdateRequest req) {
        PhaseTask t = phaseTaskRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "子项不存在"));
        ensureOwnedByTask(userId, t);
        if (req.getName() != null && !req.getName().isBlank()) t.setName(req.getName().trim());
        if (req.getDone() != null) t.setDone(req.getDone());
        if (req.getSortOrder() != null) t.setSortOrder(req.getSortOrder());
        if (req.getNote() != null) t.setNote(req.getNote());
        return toVO(phaseTaskRepository.save(t));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        PhaseTask t = phaseTaskRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "子项不存在"));
        ensureOwnedByTask(userId, t);
        phaseTaskRepository.deleteById(id);
    }

    /** 校验阶段归属：返回阶段以便复用 projectId。 */
    private Phase ensureOwned(Long userId, Long phaseId) {
        Phase ph = phaseRepository.findById(phaseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "阶段不存在"));
        Project p = projectRepository.findById(ph.getProjectId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "项目不存在"));
        if (!p.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限访问该项目");
        }
        return ph;
    }

    private void ensureOwnedByTask(Long userId, PhaseTask t) {
        phaseRepository.findById(t.getPhaseId())
                .ifPresent(ph -> ensureOwned(userId, ph.getId()));
    }

    public static PhaseTaskVO toVO(PhaseTask t) {
        PhaseTaskVO v = new PhaseTaskVO();
        v.setId(t.getId());
        v.setPhaseId(t.getPhaseId());
        v.setName(t.getName());
        v.setDone(t.isDone());
        v.setSortOrder(t.getSortOrder());
        v.setNote(t.getNote());
        v.setCreatedAt(t.getCreatedAt());
        return v;
    }
}
