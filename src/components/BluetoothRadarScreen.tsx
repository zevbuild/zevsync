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
  Trash2,
  ExternalLink,
  Laptop,
  CheckCircle2,
  Info,
} from 'lucide-react';

export const BluetoothRadarScreen: React.FC = () => {
  const {
    discoveredPeers,
    isScanning,
    isServerListening,
    isWebBluetoothSupported,
    startBluetoothScan,
    stopBluetoothScan,
    addVirtualMeshPeer,
    togglePeerConnection,
    removePeerDevice,
    openNewWindowPeer,
    syncAllFilesWithPeer,
    isAddPeerDialog,
    showAddPeerDialog,
    localDeviceName,
  } = useSyncBeam();

  const [peerNameInput, setPeerNameInput] = useState('');
  const [peerModelInput, setPeerModelInput] = useState('');

  const connectedCount = discoveredPeers.filter((p) => p.isConnected).length;

  const handleAddPeerSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!peerNameInput.trim()) return;
    addVirtualMeshPeer(peerNameInput.trim(), peerModelInput.trim() || 'Manual Node');
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
              <h2 className="font-bold text-base text-white">Bluetooth & P2P Mesh</h2>
            </div>
            <p className="text-xs text-slate-400 mt-0.5">
              {isScanning
                ? 'Actively requesting nearby Bluetooth hardware...'
                : `Local node: ${localDeviceName} · Real hardware & P2P discovery`}
            </p>
          </div>

          <div className="flex items-center gap-2 text-xs">
            <span
              className={`px-2.5 py-1 rounded-lg text-[10px] font-black tracking-wider uppercase border ${
                isServerListening
                  ? 'bg-[#064E3B] text-[#10B981] border-[#10B981]/30'
                  : 'bg-[#7F1D1D] text-[#EF4444] border-[#EF4444]/30'
              }`}
            >
              {isServerListening ? 'MESH ONLINE' : 'OFFLINE'}
            </span>
          </div>
        </div>

        {/* Center Radar Animation */}
        <div className="flex justify-center py-2">
          <BluetoothRadarAnimation isScanning={isScanning || connectedCount > 0} />
        </div>

        {/* Browser Capability Notice */}
        <div className="rounded-xl bg-[#0B132B]/80 border border-[#1E3A8A] p-3 text-xs flex items-start gap-2.5">
          {isWebBluetoothSupported ? (
            <>
              <CheckCircle2 className="w-4 h-4 text-[#10B981] shrink-0 mt-0.5" />
              <div>
                <span className="font-semibold text-slate-200">Native Web Bluetooth Supported</span>
                <p className="text-[11px] text-slate-400 mt-0.5">
                  Scanning triggers your browser's native Bluetooth pairing dialog to discover real physical devices nearby.
                </p>
              </div>
            </>
          ) : (
            <>
              <Info className="w-4 h-4 text-[#53EFD8] shrink-0 mt-0.5" />
              <div>
                <span className="font-semibold text-slate-200">P2P Mesh Fallback Mode Active</span>
                <p className="text-[11px] text-slate-400 mt-0.5">
                  Web Bluetooth is restricted on this browser. Real zero-config P2P sync is active across tabs, windows, and local sessions.
                </p>
              </div>
            </>
          )}
        </div>

        {/* Action Controls */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
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
            <span>{isScanning ? 'Cancel Scan' : 'Scan for Bluetooth Devices'}</span>
          </button>

          <button
            type="button"
            onClick={openNewWindowPeer}
            className="flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl text-xs font-bold bg-[#1C2A4F] hover:bg-[#25386b] text-white border border-[#1E3A8A] transition-all active:scale-95"
            title="Open ZevSync in a new window to test live cross-device P2P mesh synchronization"
          >
            <ExternalLink className="w-4 h-4 text-[#53EFD8]" />
            <span>Open 2nd Node in New Window</span>
          </button>
        </div>
      </div>

      {/* Discovered & Paired Peers Section */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <h3 className="font-bold text-sm text-slate-200">
              Discovered Devices ({discoveredPeers.length})
            </h3>
            <span className="text-xs text-slate-400">· {connectedCount} connected</span>
          </div>

          <button
            type="button"
            onClick={() => showAddPeerDialog(true)}
            className="text-xs font-medium text-[#53EFD8] hover:text-[#3be5cc] flex items-center gap-1 transition-colors"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>Pair Node Manually</span>
          </button>
        </div>

        {/* Empty State when no devices are connected */}
        {discoveredPeers.length === 0 ? (
          <div className="rounded-2xl border border-dashed border-[#1E3A8A] bg-[#0B132B]/60 p-8 text-center space-y-3">
            <div className="w-12 h-12 rounded-2xl bg-[#111D3D] border border-[#1E3A8A] text-[#53EFD8] flex items-center justify-center mx-auto">
              <Laptop className="w-6 h-6" />
            </div>
            <div>
              <h4 className="font-bold text-sm text-white">No Devices Connected</h4>
              <p className="text-xs text-slate-400 mt-1 max-w-md mx-auto leading-relaxed">
                No simulated or fake devices exist. Use the scan button to discover actual Bluetooth hardware nearby, or open ZevSync in another tab or window to build your live peer mesh.
              </p>
            </div>
            <div className="pt-2 flex flex-wrap items-center justify-center gap-2">
              <button
                type="button"
                onClick={startBluetoothScan}
                className="py-2 px-3.5 rounded-xl text-xs font-bold bg-[#53EFD8] hover:bg-[#3be5cc] text-slate-950 shadow-sm"
              >
                Scan Nearby Bluetooth
              </button>
              <button
                type="button"
                onClick={openNewWindowPeer}
                className="py-2 px-3.5 rounded-xl text-xs font-semibold bg-[#1C2A4F] hover:bg-[#25386b] text-slate-200 border border-[#1E3A8A]"
              >
                Open 2nd Node in New Tab
              </button>
            </div>
          </div>
        ) : (
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
                      {peer.connectionType === 'P2P_MESH' ? (
                        <Laptop className="w-5 h-5" />
                      ) : (
                        <Smartphone className="w-5 h-5" />
                      )}
                    </div>

                    <div>
                      <div className="flex items-center gap-2">
                        <span className="font-bold text-sm text-white">{peer.name}</span>
                        {peer.connectionType === 'BLUETOOTH' ? (
                          <span className="text-[10px] text-teal-400">
                            · Hardware Bluetooth
                          </span>
                        ) : peer.connectionType === 'P2P_MESH' ? (
                          <span className="text-[10px] text-sky-400">
                            · Local P2P Mesh
                          </span>
                        ) : peer.isVirtualNode ? (
                          <span className="text-[10px] text-indigo-400">
                            · Manual Node
                          </span>
                        ) : null}
                      </div>

                      <p className="text-xs text-slate-400 mt-0.5">{peer.deviceModel}</p>

                      <div className="flex items-center gap-2.5 text-[11px] text-slate-400 mt-1 font-mono">
                        <span>{peer.bluetoothAddress}</span>
                        <span>·</span>
                        <span className="flex items-center gap-1">
                          <Signal className="w-3 h-3 text-[#53EFD8]" />
                          {peer.rssi} dBm
                        </span>
                      </div>
                    </div>
                  </div>

                  {/* Actions: Connect/Disconnect toggle and Remove */}
                  <div className="flex items-center gap-1.5">
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

                    <button
                      type="button"
                      onClick={() => removePeerDevice(peer.id)}
                      className="p-2 rounded-xl border border-slate-800 bg-[#0B132B] text-slate-400 hover:text-rose-400 hover:border-rose-900/40 transition-all active:scale-95"
                      title="Remove device from mesh"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>

                {/* Peer Telemetry & Sync Action */}
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
                    className={`flex items-center gap-1 px-3 py-1.5 rounded-lg text-xs font-bold transition-all ${
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
        )}
      </div>

      {/* Add Peer Modal */}
      {isAddPeerDialog && (
        <div className="fixed inset-0 z-40 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4">
          <form
            onSubmit={handleAddPeerSubmit}
            className="w-full max-w-sm rounded-2xl bg-[#111D3D] border border-[#1E3A8A] p-5 shadow-2xl space-y-4"
          >
            <div className="flex items-center justify-between">
              <h3 className="font-bold text-white text-base">Pair Node Manually</h3>
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
                placeholder="e.g. Workshop Phone or Field Tablet"
                className="w-full bg-[#0B132B] border border-[#1E3A8A] rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-[#53EFD8]"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">
                Device Model or Type
              </label>
              <input
                type="text"
                value={peerModelInput}
                onChange={(e) => setPeerModelInput(e.target.value)}
                placeholder="e.g. Bluetooth Node / Hardware Device"
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
                Add to Mesh
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
};
