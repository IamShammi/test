using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace ExpensesApi;

[Table("expenses")]
public class exp
{
    [Key]
    public long id { get; set; }  
    
    public string? description { get; set; }
    
    public decimal amount { get; set; }
    
    public string? category { get; set; }
    
    [Column("expense_date")]
    public DateTime expenseDate { get; set; }  
    
    [Column("is_long_term")]
    public bool isLongTerm { get; set; }
    
    [Column("created_at")]
    public DateTime CreatedAt { get; set; }
    
    [Column("updated_at")]
    public DateTime UpdatedAt { get; set; }
}
