import React, { useState } from 'react'
import axios from 'axios'

export const PaymentForm: React.FC = () => {
  const [amount, setAmount] = useState('')
  const [userId, setUserId] = useState('')
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setMessage('')

    try {
      const response = await axios.post('/api/payments', {
        userId,
        amount: parseFloat(amount),
        currency: 'USD',
        cardNumber: '4111111111111111'
      }, {
        headers: { 'Idempotency-Key': `test-${Date.now()}` }
      })
      setMessage(`✅ ${response.data.status}: ${response.data.message || 'Success'}`)
    } catch (error: any) {
      setMessage(`❌ Error: ${error.response?.data?.message || error.message}`)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ background: '#fff', padding: '20px', borderRadius: '8px', marginTop: '20px' }}>
      <h3>Make a Payment</h3>
      <form onSubmit={handleSubmit}>
        <input
          type="text"
          placeholder="User ID"
          value={userId}
          onChange={(e) => setUserId(e.target.value)}
          style={{ display: 'block', width: '100%', padding: '10px', margin: '10px 0', border: '1px solid #ddd', borderRadius: '4px' }}
          required
        />
        <input
          type="number"
          placeholder="Amount"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
          style={{ display: 'block', width: '100%', padding: '10px', margin: '10px 0', border: '1px solid #ddd', borderRadius: '4px' }}
          required
        />
        <button type="submit" disabled={loading} style={{ background: '#3b82f6', color: '#fff', padding: '10px 20px', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
          {loading ? 'Processing...' : 'Pay Now'}
        </button>
      </form>
      {message && <p style={{ marginTop: '10px' }}>{message}</p>}
    </div>
  )
}
