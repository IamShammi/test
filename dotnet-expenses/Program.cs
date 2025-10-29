using Microsoft.EntityFrameworkCore;
using ExpensesApi;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddDbContext<appdb>(options =>
    options.UseNpgsql("Host=localhost;Database=expensedb;Username=admin;Password=admin123"));

builder.Services.AddControllers();

var app = builder.Build();

app.MapControllers();

app.Run("http://localhost:5000");  
