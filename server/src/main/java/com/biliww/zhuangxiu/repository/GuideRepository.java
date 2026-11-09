package com.biliww.zhuangxiu.repository;

import com.biliww.zhuangxiu.domain.Guide;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuideRepository extends JpaRepository<Guide, Long> {

    Optional<Guide> findByStageKey(String stageKey);

    List<Guide> findAllByOrderBySortOrderAsc();
}
