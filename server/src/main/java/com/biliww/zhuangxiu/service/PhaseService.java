package com.biliww.zhuangxiu.service;

import com.biliww.zhuangxiu.common.BusinessException;
import com.biliww.zhuangxiu.common.ErrorCode;
import com.biliww.zhuangxiu.domain.Phase;
import com.biliww.zhuangxiu.domain.Project;
import com.biliww.zhuangxiu.dto.PhaseUpdateRequest;
import com.biliww.zhuangxiu.dto.PhaseVO;
import com.biliww.zhuangxiu.repository.PhaseRepository;
import com.biliww.zhuangxiu.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PhaseService {

    private final PhaseRepository phaseRepository;
    private final ProjectRepository projectRepository;

    public List<PhaseVO> list(Long userId, Long projectId) {
        ensureOwned(userId, projectId);
        return phaseRepository.findByProjectIdOrderBySortOrderAsc(projectId).stream()
                .map(PhaseService::toVO).toList();
    }

    public PhaseVO update(Long userId, Long phaseId, PhaseUpdateRequest req) {
        Phase ph = phaseRepository.findById(phaseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "阶段不存在"));
        ensureOwned(userId, ph.getProjectId());
        if (req.getStatus() != null) ph.setStatus(req.getStatus());
        if (req.getStartDate() != null) ph.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) ph.setEndDate(req.getEndDate());
        if (req.getBudget() != null) ph.setBudget(req.getBudget());
        if (req.getNote() != null) ph.setNote(req.getNote());
        return toVO(phaseRepository.save(ph));
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
