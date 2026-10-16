import React from 'react';
import { useSyncBeam } from '../context/SyncBeamContext';
import { formatBytes, formatSpeed } from '../services/cacheVaultManager';
import {
  Activity,
  ArrowUpRight,
  ArrowDownLeft,
  CheckCircle2,
  Trash2,
  Clock,
  Radio,
  FileText,
  AlertTriangle,
  FolderOpen,
} from 'lucide-react';

export const LiveSyncScreen: React.FC = () => {
  const { activeTransfers, clearCompletedTransfers, recentLogs } = useSyncBeam();

  const activeTransfersCount = activeTransfers.filter(
    (t) => t.status === 'TRANSFERRING' || t.status === 'VERIFYING'
  ).length;

  const totalSpeed = activeTransfers
    .filter((t) => t.status === 'TRANSFERRING')
    .reduce((acc, curr) => acc + curr.speedBytesPerSec, 0);

  const formatLogTime = (ts: number) => {
    const d = new Date(ts);
    return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  };

  return (
    <div className="pb-24 pt-2 px-4 max-w-2xl mx-auto space-y-4">
      {/* Throughput & Live Status Banner */}
      <div className="rounded-2xl bg-[#111D3D] border border-[#1E3A8A] p-4 shadow-lg flex items-center justify-between">
        <div>
          <div className="flex items-center gap-2">
            <Activity className="w-5 h-5 text-[#53EFD8]" />
            <h2 className="font-bold text-base text-white">Live RFCOMM Throughput</h2>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            {activeTransfersCount > 0
              ? `${activeTransfersCount} active streaming channel(s)`
              : 'Mesh idle · Ready for real-time peer sync'}
          </p>
        </div>

        <div className="px-3.5 py-1.5 rounded-xl bg-[#0B132B] border border-[#1E3A8A]">
          <span className="font-mono font-bold text-sm text-[#53EFD8]">
            {formatSpeed(totalSpeed)}
          </span>
        </div>
      </div>

      {/* File Streaming Queue */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <h3 className="font-bold text-sm text-slate-200">
            Streaming Queue ({activeTransfers.length})
          </h3>

          {activeTransfers.length > 0 && (
            <button
              type="button"
              onClick={clearCompletedTransfers}
              className="flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-xs font-semibold text-slate-400 hover:text-white hover:bg-[#1C2A4F] transition-colors"
            >
              <Trash2 className="w-3.5 h-3.5" />
              <span>Clear Done</span>
            </button>
          )}
        </div>

        {activeTransfers.length === 0 ? (
          <div className="rounded-xl border border-dashed border-[#1E3A8A] p-6 text-center bg-[#111D3D]/30">
            <Radio className="w-8 h-8 text-slate-500 mx-auto mb-2 opacity-50" />
            <p className="text-xs text-slate-400">No active file transfers in progress.</p>
            <p className="text-[11px] text-slate-500 mt-0.5">
              Sync a file from the Vault tab to watch live block streaming.
            </p>
          </div>
        ) : (
          <div className="space-y-2">
            {activeTransfers.map((item) => (
              <div
                key={item.id}
                className="rounded-xl bg-[#111D3D] border border-[#1E3A8A] p-3.5 space-y-2.5 shadow-sm"
              >
                <div className="flex items-start justify-between gap-2">
                  <div className="flex items-center gap-2 min-w-0">
                    <div
                      className={`w-7 h-7 rounded-lg flex items-center justify-center shrink-0 ${
                        item.direction === 'UPLOAD'
                          ? 'bg-[#006A60]/60 text-[#53EFD8]'
                          : 'bg-[#1C2A4F] text-[#38BDF8]'
                      }`}
                    >
                      {item.direction === 'UPLOAD' ? (
                        <ArrowUpRight className="w-4 h-4" />
                      ) : (
                        <ArrowDownLeft className="w-4 h-4" />
                      )}
                    </div>
                    <div className="truncate">
                      <p className="font-bold text-xs text-white truncate">{item.fileName}</p>
                      <p className="text-[11px] text-slate-400">
                        {item.direction === 'UPLOAD' ? 'To' : 'From'} {item.peerName}
                      </p>
                    </div>
                  </div>

                  {/* Status Badge */}
                  <div className="shrink-0 text-right">
                    <span
                      className={`text-[10px] font-bold px-2 py-0.5 rounded-full uppercase tracking-wider ${
                        item.status === 'COMPLETED'
                          ? 'bg-[#064E3B] text-[#10B981]'
                          : item.status === 'VERIFYING'
                          ? 'bg-[#78350F] text-[#F59E0B]'
                          : item.status === 'FAILED'
                          ? 'bg-[#7F1D1D] text-[#EF4444]'
                          : 'bg-[#0C4A6E] text-[#38BDF8]'
                      }`}
                    >
                      {item.status}
                    </span>
                    <p className="text-[11px] font-mono text-slate-400 mt-1">
                      {formatSpeed(item.speedBytesPerSec)}
                    </p>
                  </div>
                </div>

                {/* Progress bar */}
                <div className="space-y-1">
                  <div className="w-full h-2 rounded-full bg-[#0B132B] overflow-hidden">
                    <div
                      style={{ width: `${Math.round(item.progress * 100)}%` }}
                      className={`h-full transition-all duration-200 rounded-full ${
                        item.status === 'COMPLETED'
                          ? 'bg-[#10B981]'
                          : item.status === 'VERIFYING'
                          ? 'bg-[#F59E0B]'
                          : 'bg-gradient-to-r from-[#00D2BA] to-[#4F46E5]'
                      }`}
                    />
                  </div>
                  <div className="flex justify-between text-[10px] text-slate-400 font-mono">
                    <span>
                      {formatBytes(item.bytesTransferred)} / {formatBytes(item.totalBytes)}
                    </span>
                    <span>{Math.round(item.progress * 100)}%</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Live Mesh Event Logs */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <h3 className="font-bold text-sm text-slate-200">Mesh Event Logs & Lineage</h3>
          <span className="text-xs text-slate-400 font-mono">
            {recentLogs.length} events
          </span>
        </div>

        <div className="rounded-xl bg-[#111D3D] border border-[#1E3A8A] divide-y divide-[#1E3A8A]/40 max-h-80 overflow-y-auto">
          {recentLogs.map((log) => (
            <div key={log.id} className="p-3 text-xs flex items-start gap-2.5">
              <span
                className={`w-2 h-2 rounded-full mt-1.5 shrink-0 ${
                  log.isPositive ? 'bg-[#10B981]' : 'bg-[#EF4444]'
                }`}
              />
              <div className="flex-1 min-w-0">
                <div className="flex items-center justify-between gap-2">
                  <p className="font-bold text-white truncate">{log.title}</p>
                  <span className="text-[10px] font-mono text-slate-500 shrink-0">
                    {formatLogTime(log.timestamp)}
                  </span>
                </div>
                <p className="text-slate-400 mt-0.5 break-words">{log.description}</p>
                {log.peerName && (
                  <span className="inline-block mt-1 text-[10px] font-semibold text-[#53EFD8] bg-[#172554] px-1.5 py-0.2 rounded border border-[#1E3A8A]">
                    {log.peerName}
                  </span>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
