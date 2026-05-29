// ui/src/components/FraudAlerts.tsx
import React, { useState, useEffect } from 'react';
import { AlertTriangle, Shield, XCircle } from 'lucide-react';

interface FraudAlert {
  id: string;
  transactionId: string;
  ruleName: string;
  severity: 'HIGH' | 'MEDIUM' | 'LOW';
  reason: string;
  timestamp: string;
  action: 'BLOCK' | 'REVIEW' | 'ALLOW';
}

export const FraudAlerts: React.FC = () => {
  const [alerts, setAlerts] = useState<FraudAlert[]>([]);
  const [showResolved, setShowResolved] = useState(false);

  useEffect(() => {
    // Poll for fraud alerts (Section 5 - Q18)
    const interval = setInterval(async () => {
      const response = await fetch('/api/fraud/alerts?resolved=false');
      const data = await response.json();
      setAlerts(data);
    }, 3000);
    return () => clearInterval(interval);
  }, []);

  const resolveAlert = async (alertId: string) => {
    await fetch(`/api/fraud/alerts/${alertId}/resolve`, { method: 'POST' });
    setAlerts(alerts.filter(a => a.id !== alertId));
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <div className="flex justify-between items-center mb-3">
        <h3 className="font-bold flex items-center gap-2">
          <AlertTriangle className="text-yellow-500" size={20} />
          Fraud Detection Engine
        </h3>
        <span className="text-xs bg-red-100 text-red-800 px-2 py-1 rounded-full">
          {alerts.length} Active Alerts
        </span>
      </div>

      <div className="space-y-3 max-h-96 overflow-y-auto">
        {alerts.map(alert => (
          <div key={alert.id} className={`border-l-4 p-3 rounded ${
            alert.severity === 'HIGH' ? 'border-red-500 bg-red-50' :
            alert.severity === 'MEDIUM' ? 'border-yellow-500 bg-yellow-50' :
            'border-blue-500 bg-blue-50'
          }`}>
            <div className="flex justify-between items-start">
              <div className="flex-1">
                <div className="font-semibold text-sm">{alert.ruleName}</div>
                <div className="text-xs text-gray-600 mt-1">{alert.reason}</div>
                <div className="text-xs text-gray-400 mt-1">TX: {alert.transactionId}</div>
              </div>
              <button
                onClick={() => resolveAlert(alert.id)}
                className="text-xs bg-gray-200 hover:bg-gray-300 px-2 py-1 rounded"
              >
                Resolve
              </button>
            </div>
            <div className="mt-2 flex gap-2">
              <span className={`text-xs px-2 py-0.5 rounded ${
                alert.action === 'BLOCK' ? 'bg-red-200 text-red-800' :
                alert.action === 'REVIEW' ? 'bg-yellow-200 text-yellow-800' :
                'bg-green-200 text-green-800'
              }`}>
                Action: {alert.action}
              </span>
              <span className="text-xs text-gray-500">{new Date(alert.timestamp).toLocaleTimeString()}</span>
            </div>
          </div>
        ))}

        {alerts.length === 0 && (
          <div className="text-center text-gray-500 py-8">
            <Shield size={40} className="mx-auto mb-2 text-green-500" />
            <p className="text-sm">No active fraud alerts</p>
            <p className="text-xs">All transactions passed dynamic rules (Section 5)</p>
          </div>
        )}
      </div>
    </div>
  );
};