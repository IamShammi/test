package com.interview.expenses.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class pmt {  
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    
    @Column(name = "expense_id")
    public Long expenseId;  
    
    @Column(name = "payment_amount")
    public BigDecimal paymentAmount;
    
    @Column(name = "payment_date")
    public LocalDate paymentDate;
    
    @Column(name = "payment_method")
    public String paymentMethod;
    
    @Column(name = "created_at")
    public LocalDateTime createdAt;
}
