package com.biliww.zhuangxiu.repository;

import com.biliww.zhuangxiu.domain.PhaseTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhaseTaskRepository extends JpaRepository<PhaseTask, Long> {

    List<PhaseTask> findByPhaseIdOrderBySortOrderAsc(Long phaseId);

    List<PhaseTask> findByProjectIdIn(List<Long> projectIds);

    void deleteByPhaseId(Long phaseId);

    void deleteByProjectId(Long projectId);
}
