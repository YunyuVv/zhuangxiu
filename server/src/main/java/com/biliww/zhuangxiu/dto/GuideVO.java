package com.biliww.zhuangxiu.dto;

import lombok.Data;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import java.math.BigDecimal;
import java.util.List;

/**
 * 装修指南返回：列表接口返回无 details；攻略（单阶段）接口返回带 details。
 */
@Data
@RegisterReflectionForBinding
public class GuideVO {

    private String stageKey;
    private String title;
    private String summary;
    private String duration;
    private BigDecimal costRatio;
    private int sortOrder;

    /** 单阶段攻略明细（列表接口为 null）。 */
    private List<Detail> details;

    @Data
    @RegisterReflectionForBinding
    public static class Detail {
        private String section;
        private String content;
        private int sortOrder;
    }
}
