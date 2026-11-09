package com.biliww.zhuangxiu.service;

import com.biliww.zhuangxiu.common.BusinessException;
import com.biliww.zhuangxiu.common.ErrorCode;
import com.biliww.zhuangxiu.common.PageVO;
import com.biliww.zhuangxiu.domain.Expense;
import com.biliww.zhuangxiu.domain.Project;
import com.biliww.zhuangxiu.dto.ExpenseCreateRequest;
import com.biliww.zhuangxiu.dto.ExpenseUpdateRequest;
import com.biliww.zhuangxiu.dto.ExpenseVO;
import com.biliww.zhuangxiu.repository.ExpenseRepository;
import com.biliww.zhuangxiu.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ProjectRepository projectRepository;

    public ExpenseVO create(Long userId, Long projectId, ExpenseCreateRequest req) {
        ensureOwned(userId, projectId);
        Expense e = new Expense();
        e.setProjectId(projectId);
        e.setPhaseId(req.getPhaseId());
        e.setName(req.getName());
        e.setCategory(req.getCategory());
        e.setAmount(req.getAmount());
        e.setPayMethod(req.getPayMethod());
        e.setExpenseDate(req.getExpenseDate());
        e.setNote(req.getNote());
        return toVO(expenseRepository.save(e));
    }

    public ExpenseVO update(Long userId, Long id, ExpenseUpdateRequest req) {
        Expense e = expenseRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "花销记录不存在"));
        ensureOwned(userId, e.getProjectId());
        if (req.getPhaseId() != null) e.setPhaseId(req.getPhaseId());
        if (req.getName() != null) e.setName(req.getName());
        if (req.getCategory() != null) e.setCategory(req.getCategory());
        if (req.getAmount() != null) e.setAmount(req.getAmount());
        if (req.getPayMethod() != null) e.setPayMethod(req.getPayMethod());
        if (req.getExpenseDate() != null) e.setExpenseDate(req.getExpenseDate());
        if (req.getNote() != null) e.setNote(req.getNote());
        return toVO(expenseRepository.save(e));
    }

    public ExpenseVO get(Long userId, Long id) {
        Expense e = expenseRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "花销记录不存在"));
        ensureOwned(userId, e.getProjectId());
        return toVO(e);
    }

    public PageVO<ExpenseVO> list(Long userId, Long projectId, Long phaseId, int page, int size) {
        ensureOwned(userId, projectId);
        Page<Expense> pg;
        if (phaseId != null) {
            pg = expenseRepository.findByProjectIdAndPhaseId(projectId, phaseId, PageRequest.of(page, size));
        } else {
            pg = expenseRepository.findByProjectId(projectId, PageRequest.of(page, size));
        }
        List<ExpenseVO> list = pg.getContent().stream().map(ExpenseService::toVO).toList();
        return PageVO.of(pg.getTotalElements(), page, size, list);
    }

    public void delete(Long userId, Long id) {
        Expense e = expenseRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "花销记录不存在"));
        ensureOwned(userId, e.getProjectId());
        expenseRepository.deleteById(id);
    }

    private void ensureOwned(Long userId, Long projectId) {
        Project p = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "项目不存在"));
        if (!p.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限访问该项目");
        }
    }

    public static ExpenseVO toVO(Expense e) {
        ExpenseVO v = new ExpenseVO();
        v.setId(e.getId());
        v.setPhaseId(e.getPhaseId());
        v.setName(e.getName());
        v.setCategory(e.getCategory());
        v.setAmount(e.getAmount());
        v.setPayMethod(e.getPayMethod());
        v.setExpenseDate(e.getExpenseDate());
        v.setNote(e.getNote());
        v.setCreatedAt(e.getCreatedAt());
        return v;
    }
}
