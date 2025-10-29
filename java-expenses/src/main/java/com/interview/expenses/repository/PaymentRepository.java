package com.interview.expenses.repository;

import com.interview.expenses.model.pmt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentRepository extends JpaRepository<pmt, Long> {
    List<pmt> findByExpenseId(Long expenseId);
}
