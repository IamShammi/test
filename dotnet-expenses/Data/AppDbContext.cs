using Microsoft.EntityFrameworkCore;

namespace ExpensesApi;

public class appdb : DbContext
{
    public appdb(DbContextOptions<appdb> options) : base(options) { }
    
    public DbSet<exp> expenses { get; set; }
    public DbSet<pmt> payments { get; set; }
    
    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<exp>().ToTable("expenses");
        modelBuilder.Entity<pmt>().ToTable("payments");
    }
}
