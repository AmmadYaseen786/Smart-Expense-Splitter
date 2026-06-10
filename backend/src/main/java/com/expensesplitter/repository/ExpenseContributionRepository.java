package com.expensesplitter.repository;

import com.expensesplitter.model.ExpenseContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseContributionRepository extends JpaRepository<ExpenseContribution, Long> {
    List<ExpenseContribution> findByExpenseId(Long expenseId);
    List<ExpenseContribution> findByUserId(Long userId);
    List<ExpenseContribution> findByExpenseIdIn(List<Long> expenseIds);
}
