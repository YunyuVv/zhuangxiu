package com.biliww.zhuangxiu.repository;

import com.biliww.zhuangxiu.domain.GuideDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuideDetailRepository extends JpaRepository<GuideDetail, Long> {

    List<GuideDetail> findByGuideIdOrderBySortOrderAsc(Long guideId);
}
