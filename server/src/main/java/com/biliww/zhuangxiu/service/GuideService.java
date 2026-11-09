package com.biliww.zhuangxiu.service;

import com.biliww.zhuangxiu.common.BusinessException;
import com.biliww.zhuangxiu.common.ErrorCode;
import com.biliww.zhuangxiu.domain.Guide;
import com.biliww.zhuangxiu.domain.GuideDetail;
import com.biliww.zhuangxiu.dto.GuideVO;
import com.biliww.zhuangxiu.repository.GuideDetailRepository;
import com.biliww.zhuangxiu.repository.GuideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 装修指南服务：公开内容，无需登录。
 */
@Service
@RequiredArgsConstructor
public class GuideService {

    private final GuideRepository guideRepository;
    private final GuideDetailRepository guideDetailRepository;

    /** 8 阶段全景（轻量说明，列表接口不带 details）。 */
    public List<GuideVO> listGuides() {
        return guideRepository.findAllByOrderBySortOrderAsc().stream()
                .map(this::toVO)
                .toList();
    }

    /** 单阶段攻略（含 P1 明细）。 */
    public GuideVO getGuide(String stageKey) {
        Guide guide = guideRepository.findByStageKey(stageKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "攻略不存在"));
        GuideVO vo = toVO(guide);
        List<GuideVO.Detail> details = guideDetailRepository.findByGuideIdOrderBySortOrderAsc(guide.getId())
                .stream().map(d -> {
                    GuideVO.Detail dt = new GuideVO.Detail();
                    dt.setSection(d.getSection());
                    dt.setContent(d.getContent());
                    dt.setSortOrder(d.getSortOrder());
                    return dt;
                }).toList();
        vo.setDetails(details);
        return vo;
    }

    private GuideVO toVO(Guide g) {
        GuideVO vo = new GuideVO();
        vo.setStageKey(g.getStageKey());
        vo.setTitle(g.getTitle());
        vo.setSummary(g.getSummary());
        vo.setDuration(g.getDuration());
        vo.setCostRatio(g.getCostRatio());
        vo.setSortOrder(g.getSortOrder());
        return vo;
    }
}
