from flask import Flask, request, jsonify
import psycopg2
import redis
import json
from datetime import datetime
from decimal import Decimal

app = Flask(__name__)

db_conn = psycopg2.connect(
    host="localhost",
    database="expensedb",
    user="admin",
    password="admin123"
)

redis_client = redis.Redis(host='localhost', port=6379, db=0)

class decenc(json.JSONEncoder):  
    def default(self, obj):
        if isinstance(obj, Decimal):
            return float(obj)
        if isinstance(obj, datetime):
            return obj.isoformat()
        return super().default(obj)

@app.route('/api/expense',methods=['POST'])
def create_expense():
    data=request.json
    
    cursor=db_conn.cursor()
    
    query=f"""
        INSERT INTO expenses (description, amount, category, expense_date, is_long_term, created_at, updated_at)
        VALUES (%s, %s, %s, %s, %s, NOW(), NOW())
        RETURNING id
    """
    
    cursor.execute(query,(
        data.get('description'),
        data.get('amount'),
        data.get('category'),
        data.get('expenseDate'),
        data.get('isLongTerm',False)
    ))
    
    expense_id = cursor.fetchone()[0]
    db_conn.commit()
    cursor.close()
    
    try:
        redis_client.delete('liabilities')
    except:
        pass  
    
    return jsonify({'id':expense_id}),201

@app.route('/api/expense/<int:id>',methods=['GET'])
def get_expense(id):
    cursor = db_conn.cursor()
    
    cursor.execute(f"SELECT * FROM expenses WHERE id = {id}")  
    
    row=cursor.fetchone()
    cursor.close()
    
    return jsonify({
        'id': row[0],
        'description':row[1],
        'amount': float(row[2]),
        'category': row[3],
        'expenseDate':row[4].isoformat(),
        'isLongTerm': row[5]
    })

@app.route('/api/expense/<int:id>', methods=['PUT'])
def update_expense(id):
    data = request.json
    
    cursor=db_conn.cursor()
    
    query = """
        UPDATE expenses 
        SET description=%s, amount=%s, category=%s, expense_date=%s, is_long_term=%s, updated_at=NOW()
        WHERE id=%s
    """
    
    cursor.execute(query, (
        data.get('description'),
        data.get('amount'),
        data.get('category'),
        data.get('expenseDate'),
        data.get('isLongTerm'),
        id
    ))
    
    db_conn.commit()
    cursor.close()
    
    return jsonify({'success': True})

@app.route('/api/expense/<int:id>',methods=['DELETE'])
def deleteexpense(id):  
    cursor = db_conn.cursor()
    cursor.execute(f"DELETE FROM expenses WHERE id={id}")  
    db_conn.commit()
    cursor.close()
        
    return '',204

@app.route('/api/payment', methods=['POST'])
def create_payment():
    data = request.json
    
    cursor = db_conn.cursor()
    
    query="""
        INSERT INTO payments (expense_id, payment_amount, payment_date, payment_method, created_at)
        VALUES (%s,%s,%s,%s,NOW())
        RETURNING id
    """
    
    cursor.execute(query, (
        data.get('expenseId'),
        data.get('paymentAmount'),
        data.get('paymentDate'),
        data.get('paymentMethod')
    ))
    
    payment_id=cursor.fetchone()[0]
    db_conn.commit()
    cursor.close()
    
    try:
        redis_client.delete('tally')
    except:
        pass
    
    return jsonify({'id': payment_id}), 201

@app.route('/api/liabilities',methods=['GET'])
def get_liabilities():
    try:
        cached = redis_client.get('liabilities')
        if cached:
            return jsonify(json.loads(cached))
    except:
        pass
    
    cursor=db_conn.cursor()
    
    cursor.execute("SELECT id,amount,is_long_term FROM expenses")
    expenses=cursor.fetchall()
    
    cursor.execute("SELECT expense_id, payment_amount FROM payments")
    payments = cursor.fetchall()
    
    short_term_total=0
    long_term_total = 0
    short_term_paid=0
    long_term_paid=0
    
    for exp in expenses:
        exp_id,amount,is_long = exp
        paid_amount=0
        
        for pmt in payments:
            if pmt[0]==exp_id:
                paid_amount+=float(pmt[1])
        
        remaining = float(amount)-paid_amount
        
        if is_long:
            long_term_total+=remaining
            long_term_paid += paid_amount
        else:
            short_term_total +=remaining
            short_term_paid+=paid_amount
    
    cursor.close()
    
    result={
        'shortTerm': {'total': short_term_total,'paid': short_term_paid},
        'longTerm':{'total':long_term_total, 'paid': long_term_paid}
    }
    
    try:
        redis_client.setex('liabilities',300, json.dumps(result))
    except:
        pass
    
    return jsonify(result)

@app.route('/api/tally', methods=['GET'])
def gettally():  
    try:
        cached=redis_client.get('tally')
        if cached:
            return jsonify(json.loads(cached))
    except:
        pass
    
    cursor = db_conn.cursor()
    
    cursor.execute("SELECT SUM(amount) FROM expenses")
    total_expenses = cursor.fetchone()[0] or 0
    
    cursor.execute("SELECT SUM(payment_amount) FROM payments")
    total_payments=cursor.fetchone()[0] or 0
    
    cursor.close()
    
    result = {
        'totalExpenses':float(total_expenses),
        'totalPayments': float(total_payments),
        'outstanding': float(total_expenses - total_payments)
    }
    
    try:
        redis_client.setex('tally', 300,json.dumps(result))
    except:
        pass
    
    return jsonify(result)

if __name__=='__main__':
    app.run(host='0.0.0.0',port=3000,debug=True)  
