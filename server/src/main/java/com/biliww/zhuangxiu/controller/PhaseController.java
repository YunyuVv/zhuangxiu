package com.biliww.zhuangxiu.controller;

import com.biliww.zhuangxiu.common.ApiResult;
import com.biliww.zhuangxiu.dto.PhaseCreateRequest;
import com.biliww.zhuangxiu.dto.PhaseUpdateRequest;
import com.biliww.zhuangxiu.dto.PhaseVO;
import com.biliww.zhuangxiu.security.SecurityUtil;
import com.biliww.zhuangxiu.service.PhaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PhaseController {

    private final PhaseService phaseService;

    @GetMapping("/projects/{projectId}/phases")
    public ApiResult<List<PhaseVO>> list(@PathVariable Long projectId) {
        return ApiResult.success(phaseService.list(SecurityUtil.getCurrentUserId(), projectId));
    }

    @PostMapping("/projects/{projectId}/phases")
    public ApiResult<PhaseVO> create(@PathVariable Long projectId,
                                     @Valid @RequestBody PhaseCreateRequest req) {
        return ApiResult.success(phaseService.create(SecurityUtil.getCurrentUserId(), projectId, req));
    }

    @PutMapping("/phases/{phaseId}")
    public ApiResult<PhaseVO> update(@PathVariable Long phaseId,
                                     @Valid @RequestBody PhaseUpdateRequest req) {
        return ApiResult.success(phaseService.update(SecurityUtil.getCurrentUserId(), phaseId, req));
    }

    @DeleteMapping("/phases/{phaseId}")
    public ApiResult<Void> delete(@PathVariable Long phaseId) {
        phaseService.delete(SecurityUtil.getCurrentUserId(), phaseId);
        return ApiResult.success();
    }
}
