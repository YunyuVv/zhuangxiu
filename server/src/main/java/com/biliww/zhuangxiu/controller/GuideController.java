package com.biliww.zhuangxiu.controller;

import com.biliww.zhuangxiu.common.ApiResult;
import com.biliww.zhuangxiu.dto.GuideVO;
import com.biliww.zhuangxiu.service.GuideService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 装修指南（公开内容，无需登录）。
 */
@RestController
@RequestMapping("/api/guides")
@RequiredArgsConstructor
public class GuideController {

    private final GuideService guideService;

    @GetMapping
    public ApiResult<List<GuideVO>> list() {
        return ApiResult.success(guideService.listGuides());
    }

    @GetMapping("/{stageKey}")
    public ApiResult<GuideVO> get(@PathVariable String stageKey) {
        return ApiResult.success(guideService.getGuide(stageKey));
    }
}
