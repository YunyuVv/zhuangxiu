package com.biliww.zhuangxiu.controller;

import com.biliww.zhuangxiu.common.ApiResult;
import com.biliww.zhuangxiu.common.PageVO;
import com.biliww.zhuangxiu.dto.ExpenseCreateRequest;
import com.biliww.zhuangxiu.dto.ExpenseUpdateRequest;
import com.biliww.zhuangxiu.dto.ExpenseVO;
import com.biliww.zhuangxiu.security.SecurityUtil;
import com.biliww.zhuangxiu.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping("/projects/{projectId}/expenses")
    public ApiResult<ExpenseVO> create(@PathVariable Long projectId,
                                       @Valid @RequestBody ExpenseCreateRequest req) {
        return ApiResult.success(expenseService.create(SecurityUtil.getCurrentUserId(), projectId, req));
    }

    @GetMapping("/projects/{projectId}/expenses")
    public ApiResult<PageVO<ExpenseVO>> list(@PathVariable Long projectId,
                                             @RequestParam(required = false) Long phaseId,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        return ApiResult.success(expenseService.list(SecurityUtil.getCurrentUserId(), projectId, phaseId, page, size));
    }

    @GetMapping("/expenses/{id}")
    public ApiResult<ExpenseVO> get(@PathVariable Long id) {
        return ApiResult.success(expenseService.get(SecurityUtil.getCurrentUserId(), id));
    }

    @PutMapping("/expenses/{id}")
    public ApiResult<ExpenseVO> update(@PathVariable Long id,
                                       @Valid @RequestBody ExpenseUpdateRequest req) {
        return ApiResult.success(expenseService.update(SecurityUtil.getCurrentUserId(), id, req));
    }

    @DeleteMapping("/expenses/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        expenseService.delete(SecurityUtil.getCurrentUserId(), id);
        return ApiResult.success();
    }
}
