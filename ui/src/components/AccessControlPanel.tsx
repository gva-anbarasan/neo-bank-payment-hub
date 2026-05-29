// ui/src/components/AccessControlPanel.tsx
import React, { useState, useEffect } from 'react';

interface Policy {
  id: string;
  name: string;
  type: 'TIME_BASED' | 'LOCATION_BASED' | 'RISK_BASED';
  conditions: any;
  priority: number;
  active: boolean;
}

export const AccessControlPanel: React.FC = () => {
  const [policies, setPolicies] = useState<Policy[]>([]);
  const [selectedUser, setSelectedUser] = useState('');

  useEffect(() => {
    fetchPolicies();
  }, []);

  const fetchPolicies = async () => {
    const response = await fetch('/api/auth/policies');
    const data = await response.json();
    setPolicies(data);
  };

  const testAccess = async () => {
    const response = await fetch('/api/auth/check-access', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        userId: selectedUser,
        resource: 'PAYMENT_APPROVAL',
        action: 'EXECUTE'
      })
    });
    const result = await response.json();
    alert(result.allowed ? '✅ Access Granted' : `❌ Access Denied: ${result.reason}`);
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h3 className="font-bold mb-3">🔐 Dynamic Access Control (ABAC)</h3>

      <div className="mb-4">
        <label className="block text-sm mb-2">Test User Access</label>
        <div className="flex gap-2">
          <input
            type="text"
            placeholder="User ID"
            className="flex-1 p-2 border rounded text-sm"
            value={selectedUser}
            onChange={e => setSelectedUser(e.target.value)}
          />
          <button
            onClick={testAccess}
            className="px-3 py-2 bg-blue-600 text-white rounded text-sm"
          >
            Test Access
          </button>
        </div>
      </div>

      <div className="space-y-2 max-h-64 overflow-y-auto">
        {policies.map(policy => (
          <div key={policy.id} className="border rounded p-2 text-sm">
            <div className="flex justify-between items-center">
              <span className="font-medium">{policy.name}</span>
              <span className={`text-xs px-2 py-0.5 rounded ${policy.active ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'}`}>
                {policy.active ? 'Active' : 'Inactive'}
              </span>
            </div>
            <div className="text-xs text-gray-600 mt-1">
              Type: {policy.type} | Priority: {policy.priority}
            </div>
            <div className="text-xs text-gray-400 mt-1">
              {policy.type === 'TIME_BASED' && '⏰ 9AM - 5PM business hours'}
              {policy.type === 'LOCATION_BASED' && '📍 Blocked countries: XX, YY'}
              {policy.type === 'RISK_BASED' && '⚠️ High-risk transaction threshold'}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};