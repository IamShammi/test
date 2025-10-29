package com.interview.expenses.repository;

import com.interview.expenses.model.exp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<exp, Long> {
    
    @Query("SELECT e FROM exp e WHERE e.isLongTerm = ?1")
    List<exp> findByIsLongTerm(Boolean isLongTerm);
    
    List<exp> findAll();
}
