package com.biliww.zhuangxiu.repository;

import com.biliww.zhuangxiu.domain.Phase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhaseRepository extends JpaRepository<Phase, Long> {

    List<Phase> findByProjectIdOrderBySortOrderAsc(Long projectId);

    List<Phase> findByProjectIdIn(List<Long> projectIds);
}
