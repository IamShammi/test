-- Expense Accounting Database Schema

CREATE TABLE IF NOT EXISTS expenses (
    id SERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    category VARCHAR(100),
    expense_date DATE NOT NULL,
    is_long_term BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS payments (
    id SERIAL PRIMARY KEY,
    expense_id INTEGER REFERENCES expenses(id) ON DELETE CASCADE,
    payment_amount DECIMAL(10, 2) NOT NULL,
    payment_date DATE NOT NULL,
    payment_method VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_expenses_date ON expenses(expense_date);
CREATE INDEX idx_expenses_category ON expenses(category);
CREATE INDEX idx_payments_expense ON payments(expense_id);
CREATE INDEX idx_payments_date ON payments(payment_date);

-- Sample data for testing
INSERT INTO expenses (description, amount, category, expense_date, is_long_term) VALUES
('Office Supplies', 150.00, 'Operations', '2025-01-15', FALSE),
('Software License', 2400.00, 'Technology', '2025-01-01', TRUE),
('Travel Expenses', 800.00, 'Travel', '2025-02-10', FALSE),
('Equipment Lease', 5000.00, 'Assets', '2025-01-01', TRUE);

INSERT INTO payments (expense_id, payment_amount, payment_date, payment_method) VALUES
(1, 150.00, '2025-01-20', 'Credit Card'),
(2, 200.00, '2025-01-15', 'Bank Transfer'),
(3, 400.00, '2025-02-15', 'Credit Card');
