import React, { useState } from 'react';
import { useSyncBeam } from '../context/SyncBeamContext';
import { StorageProgressBar } from './CommonComponents';
import { formatBytes } from '../services/cacheVaultManager';
import {
  HardDrive,
  Trash2,
  Image,
  FileText,
  Code,
  Music,
  Archive,
  Folder,
  Radio,
  Sliders,
  Shield,
  Layers,
  Cpu,
  AlertTriangle,
  X,
} from 'lucide-react';

export const StorageMeshScreen: React.FC = () => {
  const {
    storageBreakdown,
    quotaLimitGb,
    setQuotaLimitGb,
    purgeCache,
    autoSyncOnConnect,
    toggleAutoSync,
    meshRelayEnabled,
    toggleMeshRelay,
    localDeviceId,
    localDeviceName,
  } = useSyncBeam();

  const [showPurgeConfirm, setShowPurgeConfirm] = useState(false);

  const categories = [
    { label: 'Images', bytes: storageBreakdown.imagesBytes, icon: Image, color: 'text-[#EC4899]', bg: 'bg-[#EC4899]/10' },
    { label: 'Docs & PDF', bytes: storageBreakdown.docsBytes, icon: FileText, color: 'text-[#3B82F6]', bg: 'bg-[#3B82F6]/10' },
    { label: 'Code & Data', bytes: storageBreakdown.codeBytes, icon: Code, color: 'text-[#10B981]', bg: 'bg-[#10B981]/10' },
    { label: 'Media (Audio/Video)', bytes: storageBreakdown.audioBytes + storageBreakdown.videoBytes, icon: Music, color: 'text-[#8B5CF6]', bg: 'bg-[#8B5CF6]/10' },
    { label: 'Archives', bytes: storageBreakdown.archiveBytes, icon: Archive, color: 'text-[#EAB308]', bg: 'bg-[#EAB308]/10' },
    { label: 'Other Cache Files', bytes: storageBreakdown.otherBytes, icon: Folder, color: 'text-slate-400', bg: 'bg-slate-400/10' },
  ];

  const quotaBytes = quotaLimitGb * 1024 * 1024 * 1024;
  const usedPercent = Math.min((storageBreakdown.totalVaultBytes / quotaBytes) * 100, 100);

  return (
    <div className="pb-24 pt-2 px-4 max-w-2xl mx-auto space-y-4">
      {/* Storage Quota Overview Card */}
      <div className="rounded-2xl bg-[#111D3D] border border-[#1E3A8A] p-4 shadow-lg space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <HardDrive className="w-5 h-5 text-[#53EFD8]" />
            <h2 className="font-bold text-base text-white">Cache Storage Quota</h2>
          </div>
          <span className="font-bold text-xs text-[#53EFD8] font-mono">
            {formatBytes(storageBreakdown.totalVaultBytes)} / {quotaLimitGb.toFixed(1)} GB
          </span>
        </div>

        {/* Progress bar */}
        <StorageProgressBar breakdown={storageBreakdown} />

        {/* Quota slider */}
        <div className="space-y-1.5 pt-1">
          <div className="flex justify-between text-xs text-slate-400">
            <span>Storage Threshold</span>
            <span className="font-semibold text-white">{quotaLimitGb.toFixed(1)} GB</span>
          </div>
          <input
            type="range"
            min="0.5"
            max="10.0"
            step="0.5"
            value={quotaLimitGb}
            onChange={(e) => setQuotaLimitGb(parseFloat(e.target.value))}
            data-testid="quota_slider"
            className="w-full accent-[#53EFD8] bg-[#0B132B] h-2 rounded-lg cursor-pointer"
          />
        </div>

        <div className="flex justify-between items-center pt-1 border-t border-[#1E3A8A]/50">
          <span className="text-xs text-slate-400">
            Used {usedPercent.toFixed(1)}% of allocated device cache
          </span>
          <button
            type="button"
            data-testid="purge_cache_button"
            onClick={() => setShowPurgeConfirm(true)}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold text-[#EF4444] bg-[#7F1D1D]/30 border border-[#EF4444]/30 hover:bg-[#7F1D1D]/50 transition-all active:scale-95"
          >
            <Trash2 className="w-3.5 h-3.5" />
            <span>Purge Cache Storage</span>
          </button>
        </div>
      </div>

      {/* Storage Category Breakdown Grid */}
      <div className="space-y-2.5">
        <h3 className="font-bold text-sm text-slate-200">Category Breakdown</h3>
        <div className="grid grid-cols-2 gap-2.5">
          {categories.map((cat) => {
            const Icon = cat.icon;
            return (
              <div
                key={cat.label}
                className="rounded-xl bg-[#111D3D] border border-[#1E3A8A] p-3 flex items-center gap-3"
              >
                <div className={`w-9 h-9 rounded-xl ${cat.bg} ${cat.color} flex items-center justify-center shrink-0`}>
                  <Icon className="w-4 h-4" />
                </div>
                <div className="min-w-0">
                  <p className="font-semibold text-xs text-white truncate">{cat.label}</p>
                  <p className="font-mono text-xs text-slate-400 mt-0.5">{formatBytes(cat.bytes)}</p>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Mesh Configuration & Protocol Status */}
      <div className="space-y-2.5">
        <h3 className="font-bold text-sm text-slate-200">Mesh & Protocol Configuration</h3>
        <div className="rounded-xl bg-[#111D3D] border border-[#1E3A8A] divide-y divide-[#1E3A8A]/50 text-xs">
          {/* Auto-Sync Toggle */}
          <div className="p-3.5 flex items-center justify-between">
            <div>
              <p className="font-bold text-white">Auto-Sync On Node Connect</p>
              <p className="text-slate-400 text-[11px] mt-0.5">
                Automatically triggers handshake and delta updates when paired Bluetooth devices come into range.
              </p>
            </div>
            <button
              type="button"
              onClick={() => toggleAutoSync(!autoSyncOnConnect)}
              className={`w-11 h-6 rounded-full transition-colors relative shrink-0 ${
                autoSyncOnConnect ? 'bg-[#53EFD8]' : 'bg-slate-700'
              }`}
            >
              <div
                className={`w-4 h-4 rounded-full bg-slate-950 transition-transform absolute top-1 ${
                  autoSyncOnConnect ? 'left-6' : 'left-1'
                }`}
              />
            </button>
          </div>

          {/* Mesh Relay Toggle */}
          <div className="p-3.5 flex items-center justify-between">
            <div>
              <p className="font-bold text-white">Mesh Relay Node (Hop Forwarder)</p>
              <p className="text-slate-400 text-[11px] mt-0.5">
                Forwards encrypted chunks across intermediate Bluetooth devices to extend peer mesh range.
              </p>
            </div>
            <button
              type="button"
              onClick={() => toggleMeshRelay(!meshRelayEnabled)}
              className={`w-11 h-6 rounded-full transition-colors relative shrink-0 ${
                meshRelayEnabled ? 'bg-[#53EFD8]' : 'bg-slate-700'
              }`}
            >
              <div
                className={`w-4 h-4 rounded-full bg-slate-950 transition-transform absolute top-1 ${
                  meshRelayEnabled ? 'left-6' : 'left-1'
                }`}
              />
            </button>
          </div>

          {/* Node Info */}
          <div className="p-3.5 space-y-1.5 font-mono text-[11px] text-slate-400">
            <div className="flex justify-between">
              <span>Local Device Node ID</span>
              <span className="text-white">{localDeviceId}</span>
            </div>
            <div className="flex justify-between">
              <span>Bluetooth Service UUID</span>
              <span className="text-[#53EFD8]">fa87c0d0-afac-11de-8a39-0800200c9a66</span>
            </div>
            <div className="flex justify-between">
              <span>RFCOMM Chunk Block Size</span>
              <span className="text-white">64 KB (SHA-256 Verified)</span>
            </div>
            <div className="flex justify-between">
              <span>Protocol Version</span>
              <span className="text-white">v2.0 (Vector Clock & Lamport)</span>
            </div>
          </div>
        </div>
      </div>

      {/* Purge Cache Confirmation Modal */}
      {showPurgeConfirm && (
        <div className="fixed inset-0 z-40 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="w-full max-w-sm rounded-2xl bg-[#111D3D] border border-[#1E3A8A] p-5 shadow-2xl space-y-4">
            <div className="flex items-center gap-2 text-[#EF4444]">
              <AlertTriangle className="w-5 h-5" />
              <h3 className="font-bold text-white text-base">Purge Cache Storage?</h3>
            </div>
            <p className="text-xs text-slate-300">
              This will remove all unpinned files from the offline cache vault to free device storage.
              Pinned files will remain safely stored.
            </p>
            <div className="flex justify-end gap-2 pt-2">
              <button
                type="button"
                onClick={() => setShowPurgeConfirm(false)}
                className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-300 hover:bg-[#1C2A4F]"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={() => {
                  purgeCache();
                  setShowPurgeConfirm(false);
                }}
                className="px-4 py-2 rounded-xl text-xs font-bold bg-[#EF4444] hover:bg-[#dc2626] text-white shadow-md"
              >
                Confirm Purge
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
