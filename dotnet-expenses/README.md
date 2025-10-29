# .NET Expenses API

## Running the Application

### Prerequisites
- .NET 8.0 SDK or later
- Docker (for PostgreSQL and Redis)

### Setup
1. Start containers: `docker-compose up -d` (from root directory)
2. Restore packages: `dotnet restore`
3. Run: `dotnet run`

### API Endpoints
- POST /api/expense - Create expense
- GET /api/expense/{id} - Get expense
- PUT /api/expense/{id} - Update expense
- DELETE /api/expense/{id} - Delete expense
- POST /api/payment - Create payment
- GET /api/liabilities - Get liabilities (short/long term)
- GET /api/tally - Get tally report

### Testing
Access the API at: http://localhost:5000
