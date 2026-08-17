package com.biliww.zhuangxiu.controller;

import com.biliww.zhuangxiu.common.ApiResult;
import com.biliww.zhuangxiu.common.PageVO;
import com.biliww.zhuangxiu.dto.ProjectCreateRequest;
import com.biliww.zhuangxiu.dto.ProjectUpdateRequest;
import com.biliww.zhuangxiu.dto.ProjectVO;
import com.biliww.zhuangxiu.security.SecurityUtil;
import com.biliww.zhuangxiu.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ApiResult<ProjectVO> create(@Valid @RequestBody ProjectCreateRequest req) {
        return ApiResult.success(projectService.create(SecurityUtil.getCurrentUserId(), req));
    }

    @GetMapping
    public ApiResult<PageVO<ProjectVO>> list(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        return ApiResult.success(projectService.list(SecurityUtil.getCurrentUserId(), page, size));
    }

    @GetMapping("/{id}")
    public ApiResult<ProjectVO> get(@PathVariable Long id) {
        return ApiResult.success(projectService.get(SecurityUtil.getCurrentUserId(), id));
    }

    @PutMapping("/{id}")
    public ApiResult<ProjectVO> update(@PathVariable Long id,
                                       @Valid @RequestBody ProjectUpdateRequest req) {
        return ApiResult.success(projectService.update(SecurityUtil.getCurrentUserId(), id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        projectService.delete(SecurityUtil.getCurrentUserId(), id);
        return ApiResult.success();
    }
}
