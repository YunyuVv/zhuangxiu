package com.biliww.zhuangxiu.controller;

import com.biliww.zhuangxiu.common.ApiResult;
import com.biliww.zhuangxiu.dto.DashboardVO;
import com.biliww.zhuangxiu.dto.OverviewVO;
import com.biliww.zhuangxiu.security.SecurityUtil;
import com.biliww.zhuangxiu.service.OverviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class OverviewController {

    private final OverviewService overviewService;

    @GetMapping("/overview")
    public ApiResult<DashboardVO> dashboard() {
        return ApiResult.success(overviewService.allOverview(SecurityUtil.getCurrentUserId()));
    }

    @GetMapping("/{id}/overview")
    public ApiResult<OverviewVO> overview(@PathVariable Long id) {
        return ApiResult.success(overviewService.get(SecurityUtil.getCurrentUserId(), id));
    }
}
