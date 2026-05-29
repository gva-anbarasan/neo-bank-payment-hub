import React, { useState, useEffect } from 'react'

export const TransactionDashboard: React.FC = () => {
  const [stats, setStats] = useState({ tps: 0, successRate: 100, avgLatency: 0 })
  const [transactions, setTransactions] = useState<any[]>([])

  useEffect(() => {
    // Fetch stats
    fetch('/api/stats')
      .then(res => res.json())
      .then(data => setStats(data))
      .catch(err => console.error('Error fetching stats:', err))

    // WebSocket connection
    const ws = new WebSocket('ws://localhost:8086/ws/stats')
    ws.onmessage = (event) => {
      const data = JSON.parse(event.data)
      setStats(data)
      if (data.transaction) {
        setTransactions(prev => [data.transaction, ...prev].slice(0, 50))
      }
    }
    ws.onerror = (error) => console.error('WebSocket error:', error)

    return () => ws.close()
  }, [])

  return (
    <div style={{ padding: '20px' }}>
      <h1>NEO-BANK Payment Hub</h1>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '20px', marginTop: '20px' }}>
        <div style={{ background: '#fff', padding: '20px', borderRadius: '8px', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
          <h3>TPS (Transactions/sec)</h3>
          <p style={{ fontSize: '32px', fontWeight: 'bold' }}>{stats.tps}</p>
        </div>
        <div style={{ background: '#fff', padding: '20px', borderRadius: '8px', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
          <h3>Success Rate</h3>
          <p style={{ fontSize: '32px', fontWeight: 'bold', color: '#10b981' }}>{stats.successRate}%</p>
        </div>
        <div style={{ background: '#fff', padding: '20px', borderRadius: '8px', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
          <h3>Avg Latency</h3>
          <p style={{ fontSize: '32px', fontWeight: 'bold' }}>{stats.avgLatency}ms</p>
        </div>
      </div>
      <div style={{ marginTop: '20px', background: '#fff', padding: '20px', borderRadius: '8px' }}>
        <h3>System Status</h3>
        <p>‚úÖ All services running</p>
        <p>Ì≥ä Real-time monitoring active</p>
        <p>Ì¥ê Idempotency enabled</p>
      </div>
    </div>
  )
}
