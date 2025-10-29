# Java Expenses API

## Running the Application

### Prerequisites
- Java 21+
- Maven 3.6+
- Docker (for PostgreSQL and Redis)

### Setup
1. Start containers: `docker-compose up -d` (from root directory)
2. Build: `mvn clean package`
3. Run: `java -jar target/expenses-api.jar`

### API Endpoints
- POST /api/expense - Create expense
- GET /api/expense/{id} - Get expense
- PUT /api/expense/{id} - Update expense
- DELETE /api/expense/{id} - Delete expense
- POST /api/payment - Create payment
- GET /api/liabilities - Get liabilities (short/long term)
- GET /api/tally - Get tally report

### Testing
Access the API at: http://localhost:8080

