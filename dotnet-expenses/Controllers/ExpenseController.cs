using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using StackExchange.Redis;
using Newtonsoft.Json;

namespace ExpensesApi.Controllers;

[ApiController]
[Route("api")]
public class ExpenseController : ControllerBase
{
    private readonly appdb _context;
    
    private static ConnectionMultiplexer redis = ConnectionMultiplexer.Connect("localhost:6379");
    private static IDatabase cache = redis.GetDatabase();
    
    public ExpenseController(appdb context)
    {
        _context = context;
    }
    
    [HttpPost("expense")]
    public async Task<exp> createexpense([FromBody] exp expense)
    {
        expense.CreatedAt=DateTime.UtcNow;  
        expense.UpdatedAt = DateTime.UtcNow;
        
        _context.expenses.Add(expense);
        await _context.SaveChangesAsync();
        
        try{
        cache.KeyDelete("liabilities");
        }catch{}  

        return expense;
    }
    
    [HttpGet("expense/{id}")]
    public async Task<exp> GetExpense(long id)
    {
        return await _context.expenses.FindAsync(id);
    }
    
    [HttpPut("expense/{id}")]
    public async Task<exp> UpdateExpense(long id,[FromBody] exp expense)
    {
        var existing = await _context.expenses.FindAsync(id);
        
        existing.description=expense.description;
        existing.amount = expense.amount;
        existing.category=expense.category;
        existing.expenseDate = expense.expenseDate;
        existing.isLongTerm=expense.isLongTerm;
        existing.UpdatedAt= DateTime.UtcNow;
        
        await _context.SaveChangesAsync();
        
        return existing;
    }
    
    [HttpDelete("expense/{id}")]
    public async Task deleteexpense(long id)
    {
        var expense=await _context.expenses.FindAsync(id);
        if(expense!=null){
            _context.expenses.Remove(expense);
            await _context.SaveChangesAsync();
        }
    }
    
    [HttpPost("payment")]
    public async Task<pmt> CreatePayment([FromBody] pmt payment)
    {
        payment.created_at = DateTime.UtcNow;
        
        _context.payments.Add(payment);
        await _context.SaveChangesAsync();
        
        try{
            cache.KeyDelete("tally");
        }catch{}
        
        return payment;
    }
    
    [HttpGet("liabilities")]
    public async Task<object> getliabilities()
    {
        try{
            var cached=cache.StringGet("liabilities");
            if(cached.HasValue){
                return JsonConvert.DeserializeObject(cached);
            }
        }catch{}
        
        var expenses=await _context.expenses.ToListAsync();
        var payments = await _context.payments.ToListAsync();
        
        decimal shortTermTotal=0;
        decimal longTermTotal = 0;
        decimal shortTermPaid=0;
        decimal longTermPaid = 0;
        
        foreach(var e in expenses)
        {
            decimal paidAmount = 0;
            
            foreach(var p in payments) 
            {
                if(p.expense_id==e.id)
                {
                    paidAmount+=p.payment_amount;
                }
            }
            
            var remaining=e.amount - paidAmount;
            
            if(e.isLongTerm)
            {
                longTermTotal+=remaining;
                longTermPaid +=paidAmount;
            }
            else
            {
                shortTermTotal += remaining;
                shortTermPaid+=paidAmount;
            }
        }
        
        var result=new
        {
            shortTerm = new { total = shortTermTotal, paid=shortTermPaid },
            longTerm=new{total=longTermTotal,paid = longTermPaid}
        };
        
        try{
            cache.StringSet("liabilities",JsonConvert.SerializeObject(result),TimeSpan.FromMinutes(5));
        }catch{}
        
        return result;
    }
    
    [HttpGet("tally")]
    public async Task<object> GetTally()
    {
        try
        {
            var cached = cache.StringGet("tally");
            if (cached.HasValue)
            {
                return JsonConvert.DeserializeObject(cached);
            }
        }
        catch { }
        
        var expenses = await _context.expenses.ToListAsync();
        var payments=await _context.payments.ToListAsync();
        
        decimal totalExpenses=0;
        decimal totalPayments = 0;
        
        foreach(var e in expenses)
        {
            totalExpenses+=e.amount;
        }
        
        foreach (var p in payments)
        {
            totalPayments += p.payment_amount;
        }
        
        var tally = new
        {
            totalExpenses = totalExpenses,
            totalPayments=totalPayments,
            outstanding = totalExpenses - totalPayments
        };
        
        try
        {
            cache.StringSet("tally", JsonConvert.SerializeObject(tally), TimeSpan.FromMinutes(5));
        }
        catch { }
        
        return tally;
    }
}
