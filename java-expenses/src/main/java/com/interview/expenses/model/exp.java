package com.interview.expenses.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses")
public class exp {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;  
    
    public String description;
    public BigDecimal amount;
    public String category;
    
    @Column(name = "expense_date")
    public LocalDate expenseDate;
    
    @Column(name = "is_long_term")
    public Boolean isLongTerm;
    
    @Column(name = "created_at")
    public LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
}
