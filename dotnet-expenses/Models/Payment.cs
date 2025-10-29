using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace ExpensesApi;

[Table("payments")]
public class pmt
{
    [Key]
    public long id { get; set; }
    
    [Column("expense_id")]
    public long expense_id { get; set; }  
    
    [Column("payment_amount")]
    public decimal payment_amount { get; set; }
    
    [Column("payment_date")]
    public DateTime PaymentDate { get; set; }  
    
    [Column("payment_method")]
    public string? paymentMethod { get; set; }
    
    [Column("created_at")]
    public DateTime created_at { get; set; }
}
