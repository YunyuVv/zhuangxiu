package com.biliww.zhuangxiu.controller;

import com.biliww.zhuangxiu.common.ApiResult;
import com.biliww.zhuangxiu.dto.PhaseTaskCreateRequest;
import com.biliww.zhuangxiu.dto.PhaseTaskUpdateRequest;
import com.biliww.zhuangxiu.dto.PhaseTaskVO;
import com.biliww.zhuangxiu.security.SecurityUtil;
import com.biliww.zhuangxiu.service.PhaseTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PhaseTaskController {

    private final PhaseTaskService phaseTaskService;

    @GetMapping("/phases/{phaseId}/tasks")
    public ApiResult<List<PhaseTaskVO>> list(@PathVariable Long phaseId) {
        return ApiResult.success(phaseTaskService.list(SecurityUtil.getCurrentUserId(), phaseId));
    }

    @PostMapping("/phases/{phaseId}/tasks")
    public ApiResult<PhaseTaskVO> create(@PathVariable Long phaseId,
                                         @Valid @RequestBody PhaseTaskCreateRequest req) {
        return ApiResult.success(phaseTaskService.create(SecurityUtil.getCurrentUserId(), phaseId, req));
    }

    @PutMapping("/tasks/{id}")
    public ApiResult<PhaseTaskVO> update(@PathVariable Long id,
                                         @Valid @RequestBody PhaseTaskUpdateRequest req) {
        return ApiResult.success(phaseTaskService.update(SecurityUtil.getCurrentUserId(), id, req));
    }

    @DeleteMapping("/tasks/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        phaseTaskService.delete(SecurityUtil.getCurrentUserId(), id);
        return ApiResult.success();
    }
}
