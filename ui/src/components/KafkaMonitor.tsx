// ui/src/components/KafkaMonitor.tsx
import React, { useEffect, useState } from 'react';
import { LineChart, Line, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

interface KafkaMetrics {
  consumerLag: number;
  messagesPerSecond: number;
  partitionHealth: { [key: string]: { lag: number; offset: number } };
  lastCommitTimestamp: string;
}

export const KafkaMonitor: React.FC = () => {
  const [metrics, setMetrics] = useState<KafkaMetrics | null>(null);
  const [history, setHistory] = useState<any[]>([]);

  useEffect(() => {
    const eventSource = new EventSource('/api/kafka/metrics/stream');
    eventSource.onmessage = (event) => {
      const data = JSON.parse(event.data);
      setMetrics(data);
      setHistory(prev => [...prev, { time: new Date().toLocaleTimeString(), lag: data.consumerLag }].slice(-20));
    };
    return () => eventSource.close();
  }, []);

  if (!metrics) return <div className="bg-white rounded-lg shadow-md p-6">Loading Kafka metrics...</div>;

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h3 className="font-bold mb-3">📊 Kafka Consumer Health</h3>

      <div className="grid grid-cols-2 gap-3 mb-4">
        <MetricBadge label="Consumer Lag" value={metrics.consumerLag} color={metrics.consumerLag > 1000 ? 'red' : 'green'} />
        <MetricBadge label="Msg/Sec" value={metrics.messagesPerSecond} unit="msg/s" />
        <MetricBadge label="Last Commit" value={metrics.lastCommitTimestamp} small />
        <MetricBadge label="Active Partitions" value={Object.keys(metrics.partitionHealth).length} />
      </div>

      <ResponsiveContainer width="100%" height={200}>
        <LineChart data={history}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="time" />
          <YAxis />
          <Tooltip />
          <Line type="monotone" dataKey="lag" stroke="#8884d8" strokeWidth={2} dot={false} />
          <Area type="monotone" dataKey="lag" fill="#8884d8" fillOpacity={0.1} />
        </LineChart>
      </ResponsiveContainer>

      <div className="mt-3 text-xs text-gray-500">
        <strong>Q9 - Consumer Groups:</strong> Ordering guaranteed per partition | {Object.keys(metrics.partitionHealth).length} partitions active
      </div>
    </div>
  );
};

const MetricBadge: React.FC<{ label: string; value: string | number; unit?: string; color?: string; small?: boolean }> =
  ({ label, value, unit, color = 'blue', small }) => (
    <div className={`bg-${color}-50 rounded p-2 ${small ? 'text-xs' : ''}`}>
      <div className={`text-${color}-600 text-xs`}>{label}</div>
      <div className="font-bold">{value}{unit && <span className="text-sm font-normal"> {unit}</span>}</div>
    </div>
  );