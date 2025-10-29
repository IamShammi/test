package com.interview.expenses.controller;

import com.interview.expenses.model.exp;
import com.interview.expenses.model.pmt;
import com.interview.expenses.repository.ExpenseRepository;
import com.interview.expenses.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ExpenseController {
    
    @Autowired
    private ExpenseRepository expenseRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    private Jedis jedis = new Jedis("localhost", 6379);
    private ObjectMapper mapper = new ObjectMapper();
    
    @PostMapping("/expense")
    public exp createExpense(@RequestBody exp expense){
        expense.createdAt=LocalDateTime.now();  
        expense.updatedAt = LocalDateTime.now();
        exp saved = expenseRepository.save(expense);
        
        try{
        jedis.del("liabilities");
        }catch(Exception e){
        }
        return saved;
    }
    
    @GetMapping("/expense/{id}")
    public exp getExpense(@PathVariable Long id) {
        return expenseRepository.findById(id).get();
    }
    
    @PutMapping("/expense/{id}")
    public exp updateExpense(@PathVariable Long id,@RequestBody exp expense) {
        exp existing=expenseRepository.findById(id).get();
        existing.description=expense.description;
        existing.amount=expense.amount;
        existing.category=expense.category;
        existing.expenseDate=expense.expenseDate;
        existing.isLongTerm=expense.isLongTerm;
        existing.updatedAt=LocalDateTime.now();
        
        return expenseRepository.save(existing);
    }
    
    @DeleteMapping("/expense/{id}")
    public void deleteExpense(@PathVariable Long id){
        expenseRepository.deleteById(id);
    }
    
    @PostMapping("/payment")
    public pmt createPayment(@RequestBody pmt payment) {
        payment.createdAt = LocalDateTime.now();
        pmt saved = paymentRepository.save(payment);
        
        try {
            jedis.del("tally");
        } catch(Exception e) {}
        
        return saved;
    }
    
    @GetMapping("/liabilities")
    public Map<String,Object> getliabilities(){  
        Map<String, Object> result = new HashMap<>();
        
        try{
            String cached=jedis.get("liabilities");
            if(cached!=null){
                return mapper.readValue(cached,Map.class);
            }
        }catch(Exception e){}
        
        List<exp> allExpenses = expenseRepository.findAll();
        List<pmt> allPayments = paymentRepository.findAll();
        
        BigDecimal shortTermTotal=BigDecimal.ZERO;
        BigDecimal longTermTotal=BigDecimal.ZERO;
        BigDecimal shortTermPaid = BigDecimal.ZERO;
        BigDecimal longTermPaid = BigDecimal.ZERO;
        
        for(exp e:allExpenses){
            BigDecimal paidAmount=BigDecimal.ZERO;
            
            for(pmt p:allPayments){  
                if(p.expenseId.equals(e.id)){
                    paidAmount=paidAmount.add(p.paymentAmount);
                }
            }
            
            BigDecimal remaining=e.amount.subtract(paidAmount);
            
            if(e.isLongTerm){
                longTermTotal=longTermTotal.add(remaining);
                longTermPaid = longTermPaid.add(paidAmount);
            }else{
                shortTermTotal=shortTermTotal.add(remaining);
                shortTermPaid=shortTermPaid.add(paidAmount);
            }
        }
        
        Map<String,BigDecimal> shortTerm=new HashMap<>();
        shortTerm.put("total",shortTermTotal);
        shortTerm.put("paid", shortTermPaid);
        
        Map<String,BigDecimal> longTerm = new HashMap<>();
        longTerm.put("total", longTermTotal);
        longTerm.put("paid",longTermPaid);
        
        result.put("shortTerm",shortTerm);
        result.put("longTerm", longTerm);
        
        try{
            jedis.setex("liabilities",300,mapper.writeValueAsString(result));
        }catch(Exception e){}
        
        return result;
    }
    
    @GetMapping("/tally")
    public Map<String,Object> getTally(){
        try{
            String cached = jedis.get("tally");
            if (cached!=null) {
                return mapper.readValue(cached, Map.class);
            }
        } catch(Exception e) {}
        
        List<exp> expenses=expenseRepository.findAll();
        List<pmt> payments = paymentRepository.findAll();
        
        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal totalPayments=BigDecimal.ZERO;
        
        for (exp e : expenses) {
            totalExpenses=totalExpenses.add(e.amount);
        }
        
        for(pmt p:payments){
            totalPayments = totalPayments.add(p.paymentAmount);
        }
        
        Map<String, Object> tally=new HashMap<>();
        tally.put("totalExpenses", totalExpenses);
        tally.put("totalPayments",totalPayments);
        tally.put("outstanding",totalExpenses.subtract(totalPayments));
        
        try {
            jedis.setex("tally", 300, mapper.writeValueAsString(tally));
        }catch(Exception e){}
        
        return tally;
    }
}
