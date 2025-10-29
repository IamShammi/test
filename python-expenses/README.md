# Python Expenses API

## Running the Application

### Prerequisites
- Python 3.9+
- pip
- Docker (for PostgreSQL and Redis)

### Setup
1. Start containers: `docker-compose up -d` (from root directory)
2. Install dependencies: `pip install -r requirements.txt`
3. Run: `python app.py`

### API Endpoints
- POST /api/expense - Create expense
- GET /api/expense/<id> - Get expense
- PUT /api/expense/<id> - Update expense
- DELETE /api/expense/<id> - Delete expense
- POST /api/payment - Create payment
- GET /api/liabilities - Get liabilities (short/long term)
- GET /api/tally - Get tally report

### Testing
Access the API at: http://localhost:3000
