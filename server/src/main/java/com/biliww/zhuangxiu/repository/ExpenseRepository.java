package com.biliww.zhuangxiu.repository;

import com.biliww.zhuangxiu.domain.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Page<Expense> findByProjectId(Long projectId, Pageable pageable);

    List<Expense> findByProjectId(Long projectId);

    Page<Expense> findByProjectIdAndPhaseId(Long projectId, Long phaseId, Pageable pageable);
}
