import React, { useState } from 'react';
import { useSyncBeam } from '../context/SyncBeamContext';
import { BluetoothRadarAnimation } from './CommonComponents';
import { formatBytes } from '../services/cacheVaultManager';
import {
  Radio,
  RefreshCw,
  Plus,
  Smartphone,
  Wifi,
  WifiOff,
  Signal,
  ArrowUpRight,
  ArrowDownLeft,
  X,
  Share2,
} from 'lucide-react';

export const BluetoothRadarScreen: React.FC = () => {
  const {
    discoveredPeers,
    isScanning,
    isServerListening,
    startBluetoothScan,
    stopBluetoothScan,
    addVirtualMeshPeer,
    togglePeerConnection,
    syncAllFilesWithPeer,
    isAddPeerDialog,
    showAddPeerDialog,
  } = useSyncBeam();

  const [peerNameInput, setPeerNameInput] = useState('');
  const [peerModelInput, setPeerModelInput] = useState('');

  const connectedCount = discoveredPeers.filter((p) => p.isConnected).length;

  const handleAddPeerSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!peerNameInput.trim()) return;
    addVirtualMeshPeer(peerNameInput.trim(), peerModelInput.trim() || 'Virtual Bluetooth Node');
    setPeerNameInput('');
    setPeerModelInput('');
  };

  return (
    <div className="pb-24 pt-2 px-4 max-w-2xl mx-auto space-y-4">
      {/* Bluetooth Mesh Hero Card */}
      <div className="rounded-2xl bg-[#111D3D] border border-[#1E3A8A] p-4 shadow-lg space-y-4">
        <div className="flex items-center justify-between">
          <div>
            <div className="flex items-center gap-2">
              <Radio className="w-5 h-5 text-[#53EFD8]" />
              <h2 className="font-bold text-base text-white">Bluetooth RFCOMM Mesh</h2>
            </div>
            <p className="text-xs text-slate-400 mt-0.5">
              {isScanning
                ? 'Actively scanning nearby Bluetooth nodes...'
                : 'Listener active on UUID fa87c0d0'}
            </p>
          </div>

          <span
            className={`px-2.5 py-1 rounded-lg text-[10px] font-black tracking-wider uppercase border ${
              isServerListening
                ? 'bg-[#064E3B] text-[#10B981] border-[#10B981]/30'
                : 'bg-[#7F1D1D] text-[#EF4444] border-[#EF4444]/30'
            }`}
          >
            {isServerListening ? 'SERVER READY' : 'OFFLINE'}
          </span>
        </div>

        {/* Center Radar Animation */}
        <div className="flex justify-center py-2">
          <BluetoothRadarAnimation isScanning={isScanning || connectedCount > 0} />
        </div>

        {/* Action Controls */}
        <div className="grid grid-cols-2 gap-2.5">
          <button
            type="button"
            onClick={isScanning ? stopBluetoothScan : startBluetoothScan}
            className={`flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl text-xs font-bold transition-all shadow-md active:scale-95 ${
              isScanning
                ? 'bg-[#78350F] hover:bg-[#92400e] text-[#F59E0B] border border-[#F59E0B]/30'
                : 'bg-[#53EFD8] hover:bg-[#3be5cc] text-slate-950'
            }`}
          >
            <RefreshCw className={`w-4 h-4 ${isScanning ? 'animate-spin' : ''}`} />
            <span>{isScanning ? 'Stop Scanning' : 'Scan for Devices'}</span>
          </button>

          <button
            type="button"
            onClick={() => showAddPeerDialog(true)}
            className="flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl text-xs font-bold bg-[#1C2A4F] hover:bg-[#25386b] text-white border border-[#1E3A8A] transition-all active:scale-95"
          >
            <Plus className="w-4 h-4 text-[#53EFD8]" />
            <span>Add Virtual Node</span>
          </button>
        </div>
      </div>

      {/* Discovered & Paired Peers Section */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <h3 className="font-bold text-sm text-slate-200">
            Discovered Nodes ({discoveredPeers.length})
          </h3>
          <span className="text-xs text-slate-400">
            {connectedCount} connected
          </span>
        </div>

        <div className="space-y-2.5">
          {discoveredPeers.map((peer) => (
            <div
              key={peer.id}
              className={`rounded-xl border p-3.5 transition-all ${
                peer.isConnected
                  ? 'bg-[#111D3D] border-[#1E3A8A] shadow-md'
                  : 'bg-[#0B132B] border-[#1E3A8A]/50 opacity-75'
              }`}
            >
              <div className="flex items-start justify-between gap-3">
                <div className="flex items-start gap-3">
                  <div
                    className={`w-10 h-10 rounded-xl flex items-center justify-center shrink-0 ${
                      peer.isConnected
                        ? 'bg-[#1C2A4F] text-[#53EFD8]'
                        : 'bg-slate-800 text-slate-500'
                    }`}
                  >
                    <Smartphone className="w-5 h-5" />
                  </div>

                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-sm text-white">{peer.name}</span>
                      {peer.isVirtualNode && (
                        <span className="text-[10px] font-bold px-1.5 py-0.2 rounded bg-indigo-900/60 text-indigo-300 border border-indigo-500/30">
                          VIRTUAL
                        </span>
                      )}
                    </div>
                    <p className="text-xs text-slate-400 mt-0.5">{peer.deviceModel}</p>
                    <div className="flex items-center gap-2.5 text-[11px] text-slate-400 mt-1 font-mono">
                      <span>{peer.bluetoothAddress}</span>
                      <span>•</span>
                      <span className="flex items-center gap-1">
                        <Signal className="w-3 h-3 text-[#53EFD8]" />
                        {peer.rssi} dBm
                      </span>
                    </div>
                  </div>
                </div>

                {/* Connection switch / toggle */}
                <button
                  type="button"
                  onClick={() => togglePeerConnection(peer.id)}
                  className={`p-2 rounded-xl border transition-all active:scale-95 ${
                    peer.isConnected
                      ? 'bg-[#064E3B] text-[#10B981] border-[#10B981]/30 hover:bg-[#075e46]'
                      : 'bg-slate-800 text-slate-400 border-slate-700 hover:bg-slate-700'
                  }`}
                  title={peer.isConnected ? 'Disconnect Node' : 'Connect Node'}
                >
                  {peer.isConnected ? (
                    <Wifi className="w-4 h-4" />
                  ) : (
                    <WifiOff className="w-4 h-4" />
                  )}
                </button>
              </div>

              {/* Peer Telemetry & Actions */}
              <div className="mt-3 pt-3 border-t border-[#1E3A8A]/40 flex items-center justify-between text-xs">
                <div className="flex items-center gap-3 text-slate-400">
                  <span className="flex items-center gap-1" title="Bytes Sent">
                    <ArrowUpRight className="w-3.5 h-3.5 text-[#53EFD8]" />
                    {formatBytes(peer.totalBytesSent)}
                  </span>
                  <span className="flex items-center gap-1" title="Bytes Received">
                    <ArrowDownLeft className="w-3.5 h-3.5 text-sky-400" />
                    {formatBytes(peer.totalBytesReceived)}
                  </span>
                </div>

                <button
                  type="button"
                  disabled={!peer.isConnected}
                  onClick={() => syncAllFilesWithPeer(peer)}
                  className={`flex items-center gap-1 px-3 py-1 rounded-lg text-xs font-bold transition-all ${
                    peer.isConnected
                      ? 'bg-[#1C2A4F] hover:bg-[#25386b] text-[#53EFD8] border border-[#1E3A8A] active:scale-95'
                      : 'bg-slate-800 text-slate-500 border border-slate-700 cursor-not-allowed'
                  }`}
                >
                  <Share2 className="w-3.5 h-3.5" />
                  <span>Sync All Files</span>
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Add Virtual Peer Modal */}
      {isAddPeerDialog && (
        <div className="fixed inset-0 z-40 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4">
          <form
            onSubmit={handleAddPeerSubmit}
            className="w-full max-w-sm rounded-2xl bg-[#111D3D] border border-[#1E3A8A] p-5 shadow-2xl space-y-4"
          >
            <div className="flex items-center justify-between">
              <h3 className="font-bold text-white text-base">Add Virtual Node</h3>
              <button
                type="button"
                onClick={() => showAddPeerDialog(false)}
                className="text-slate-400 hover:text-white p-1"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">
                Node Name
              </label>
              <input
                type="text"
                required
                value={peerNameInput}
                onChange={(e) => setPeerNameInput(e.target.value)}
                placeholder="e.g. Pixel 8 (Backpack)"
                className="w-full bg-[#0B132B] border border-[#1E3A8A] rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-[#53EFD8]"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">
                Device Model
              </label>
              <input
                type="text"
                value={peerModelInput}
                onChange={(e) => setPeerModelInput(e.target.value)}
                placeholder="e.g. Google Pixel 8"
                className="w-full bg-[#0B132B] border border-[#1E3A8A] rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-[#53EFD8]"
              />
            </div>

            <div className="flex items-center justify-end gap-2 pt-2">
              <button
                type="button"
                onClick={() => showAddPeerDialog(false)}
                className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-300 hover:bg-[#1C2A4F]"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="px-4 py-2 rounded-xl text-xs font-bold bg-[#53EFD8] hover:bg-[#3be5cc] text-slate-950 shadow-md"
              >
                Connect Virtual Peer
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
};
