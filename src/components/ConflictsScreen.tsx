import React, { useState } from 'react';
import { useSyncBeam } from '../context/SyncBeamContext';
import { ConflictResolutionChoice, SyncConflict } from '../types/models';
import {
  AlertTriangle,
  CheckCircle2,
  GitMerge,
  Split,
  GitFork,
  FileText,
  Clock,
  User,
  Radio,
  X,
  Check,
} from 'lucide-react';

export const ConflictsScreen: React.FC = () => {
  const {
    pendingConflicts,
    resolvedConflicts,
    resolveConflict,
    selectedConflictForReview,
    openConflictReview,
    closeConflictReview,
  } = useSyncBeam();

  const [activeTab, setActiveTab] = useState<'active' | 'resolved'>('active');
  const [isManualMergeMode, setIsManualMergeMode] = useState(false);
  const [manualMergedText, setManualMergedText] = useState('');

  const handleOpenReview = (conflict: SyncConflict) => {
    openConflictReview(conflict);
    setIsManualMergeMode(false);
    setManualMergedText(
      `=== LOCAL VERSION ===\n${conflict.localTextSnippet || ''}\n\n=== REMOTE VERSION (${conflict.remoteDeviceName}) ===\n${conflict.remoteTextSnippet || ''}`
    );
  };

  const handleResolve = (choice: ConflictResolutionChoice) => {
    if (!selectedConflictForReview) return;
    resolveConflict(
      selectedConflictForReview.id,
      choice,
      choice === 'MERGE_CUSTOM' ? manualMergedText : null
    );
  };

  const formatDate = (ts: number) => {
    return new Date(ts).toLocaleString([], {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="pb-24 pt-2 px-4 max-w-2xl mx-auto space-y-4">
      {/* Status Card */}
      <div
        className={`rounded-2xl border p-4 shadow-lg flex items-center gap-3.5 transition-colors ${
          pendingConflicts.length > 0
            ? 'bg-[#451A03]/70 border-[#F59E0B]/40'
            : 'bg-[#111D3D] border-[#1E3A8A]'
        }`}
      >
        <div
          className={`w-11 h-11 rounded-full flex items-center justify-center shrink-0 ${
            pendingConflicts.length > 0 ? 'bg-[#F59E0B] text-black' : 'bg-[#10B981] text-black'
          }`}
        >
          {pendingConflicts.length > 0 ? (
            <AlertTriangle className="w-6 h-6" />
          ) : (
            <CheckCircle2 className="w-6 h-6" />
          )}
        </div>

        <div>
          <h2
            className={`font-bold text-sm ${
              pendingConflicts.length > 0 ? 'text-[#F59E0B]' : 'text-white'
            }`}
          >
            {pendingConflicts.length > 0
              ? `${pendingConflicts.length} Conflict(s) Require Resolution`
              : 'All Files Synchronized'}
          </h2>
          <p className="text-xs text-slate-300 mt-0.5">
            Vector clocks & Lamport timestamps track lineage across disconnected devices.
          </p>
        </div>
      </div>

      {/* Tab Switcher */}
      <div className="flex border-b border-[#1E3A8A]">
        <button
          type="button"
          onClick={() => setActiveTab('active')}
          className={`pb-2.5 px-4 text-xs font-bold transition-all relative ${
            activeTab === 'active'
              ? 'text-[#53EFD8]'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <span>Active ({pendingConflicts.length})</span>
          {activeTab === 'active' && (
            <span className="absolute bottom-0 left-0 right-0 h-0.5 bg-[#53EFD8]" />
          )}
        </button>

        <button
          type="button"
          onClick={() => setActiveTab('resolved')}
          className={`pb-2.5 px-4 text-xs font-bold transition-all relative ${
            activeTab === 'resolved'
              ? 'text-[#53EFD8]'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <span>Resolved History ({resolvedConflicts.length})</span>
          {activeTab === 'resolved' && (
            <span className="absolute bottom-0 left-0 right-0 h-0.5 bg-[#53EFD8]" />
          )}
        </button>
      </div>

      {/* Tab Content */}
      {activeTab === 'active' ? (
        <div className="space-y-3">
          {pendingConflicts.length === 0 ? (
            <div className="rounded-xl border border-dashed border-[#1E3A8A] p-8 text-center bg-[#111D3D]/30">
              <CheckCircle2 className="w-10 h-10 text-[#10B981] mx-auto mb-2 opacity-80" />
              <p className="text-sm font-bold text-slate-200">No active conflicts!</p>
              <p className="text-xs text-slate-400 mt-1">
                All files have converged cleanly across known peer vector clocks.
              </p>
            </div>
          ) : (
            pendingConflicts.map((conflict) => (
              <div
                key={conflict.id}
                className="rounded-xl bg-[#111D3D] border border-[#F59E0B]/50 p-4 space-y-3 shadow-md"
              >
                <div className="flex items-start justify-between gap-2">
                  <div>
                    <h3 className="font-bold text-sm text-white">{conflict.fileName}</h3>
                    <p className="text-xs text-[#F59E0B] font-medium mt-0.5">
                      Concurrent branch from {conflict.remoteDeviceName}
                    </p>
                  </div>
                  <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-[#78350F] text-[#F59E0B] border border-[#F59E0B]/30 uppercase">
                    Conflict
                  </span>
                </div>

                {/* Comparison Grid */}
                <div className="grid grid-cols-2 gap-3 p-3 rounded-xl bg-[#0B132B] border border-[#1E3A8A] text-xs">
                  <div>
                    <span className="font-bold text-[10px] uppercase text-[#53EFD8] tracking-wider">
                      LOCAL DEVICE
                    </span>
                    <p className="font-semibold text-white mt-1">Version: v{conflict.localVersion}</p>
                    <p className="text-slate-400 text-[11px]">Lamport: L:{conflict.localLamport}</p>
                    <p className="text-[10px] text-slate-500 font-mono mt-0.5">
                      {formatDate(conflict.localTimestamp)}
                    </p>
                  </div>

                  <div className="border-l border-[#1E3A8A] pl-3 text-right">
                    <span className="font-bold text-[10px] uppercase text-[#A5B4FC] tracking-wider">
                      REMOTE PEER
                    </span>
                    <p className="font-semibold text-white mt-1">Version: v{conflict.remoteVersion}</p>
                    <p className="text-slate-400 text-[11px]">Lamport: L:{conflict.remoteLamport}</p>
                    <p className="text-[10px] text-slate-500 font-mono mt-0.5">
                      {formatDate(conflict.remoteTimestamp)}
                    </p>
                  </div>
                </div>

                {/* Action button */}
                <button
                  type="button"
                  data-testid="resolve_conflict_button"
                  onClick={() => handleOpenReview(conflict)}
                  className="w-full py-2.5 px-4 rounded-xl text-xs font-bold bg-[#53EFD8] hover:bg-[#3be5cc] text-slate-950 flex items-center justify-center gap-2 shadow-md transition-all active:scale-95"
                >
                  <GitMerge className="w-4 h-4" />
                  <span>Resolve Conflict (Side-by-Side)</span>
                </button>
              </div>
            ))
          )}
        </div>
      ) : (
        <div className="space-y-2.5">
          {resolvedConflicts.length === 0 ? (
            <div className="rounded-xl border border-dashed border-[#1E3A8A] p-6 text-center bg-[#111D3D]/30">
              <p className="text-xs text-slate-400">No resolved conflicts yet.</p>
            </div>
          ) : (
            resolvedConflicts.map((res) => (
              <div
                key={res.id}
                className="rounded-xl bg-[#111D3D] border border-[#1E3A8A] p-3 text-xs space-y-1.5"
              >
                <div className="flex items-center justify-between">
                  <span className="font-bold text-white text-xs">{res.fileName}</span>
                  <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-[#064E3B] text-[#10B981] border border-[#10B981]/30">
                    {res.status}
                  </span>
                </div>
                <p className="text-slate-300 text-xs">{res.resolutionNote}</p>
                <p className="text-[10px] text-slate-500">
                  Resolved {res.resolutionTimestamp ? formatDate(res.resolutionTimestamp) : 'recently'}
                </p>
              </div>
            ))
          )}
        </div>
      )}

      {/* Conflict Resolution Dialog */}
      {selectedConflictForReview && (
        <div className="fixed inset-0 z-40 bg-black/75 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="w-full max-w-lg rounded-2xl bg-[#111D3D] border border-[#1E3A8A] p-5 shadow-2xl space-y-4 max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <GitMerge className="w-5 h-5 text-[#F59E0B]" />
                <h3 className="font-bold text-white text-base">
                  Resolve: {selectedConflictForReview.fileName}
                </h3>
              </div>
              <button
                type="button"
                onClick={closeConflictReview}
                className="text-slate-400 hover:text-white p-1"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {!isManualMergeMode ? (
              <>
                <p className="text-xs text-slate-300">
                  Choose how to merge storage state across your offline Bluetooth devices:
                </p>

                {/* Side-by-side snippet preview */}
                <div className="grid grid-cols-2 gap-2 text-xs">
                  <div className="p-3 rounded-xl bg-[#0B132B] border border-[#1E3A8A]">
                    <span className="font-bold text-[10px] text-[#53EFD8] uppercase">
                      Local Version
                    </span>
                    <p className="text-[11px] text-slate-300 font-mono mt-1 line-clamp-4 whitespace-pre-wrap">
                      {selectedConflictForReview.localTextSnippet || '(binary data)'}
                    </p>
                  </div>

                  <div className="p-3 rounded-xl bg-[#0B132B] border border-[#1E3A8A]">
                    <span className="font-bold text-[10px] text-[#A5B4FC] uppercase">
                      {selectedConflictForReview.remoteDeviceName}
                    </span>
                    <p className="text-[11px] text-slate-300 font-mono mt-1 line-clamp-4 whitespace-pre-wrap">
                      {selectedConflictForReview.remoteTextSnippet || '(binary data)'}
                    </p>
                  </div>
                </div>

                {/* 4 Resolution Strategy Buttons */}
                <div className="space-y-2 pt-2">
                  <button
                    type="button"
                    onClick={() => handleResolve('KEEP_LOCAL_WINNER')}
                    className="w-full py-2.5 px-4 rounded-xl text-xs font-bold bg-[#1C2A4F] hover:bg-[#25386b] text-white border border-[#1E3A8A] flex items-center justify-center gap-2 transition-all active:scale-95"
                  >
                    <Check className="w-4 h-4 text-[#53EFD8]" />
                    <span>1. Keep Local Version (Mine)</span>
                  </button>

                  <button
                    type="button"
                    onClick={() => handleResolve('KEEP_REMOTE_WINNER')}
                    className="w-full py-2.5 px-4 rounded-xl text-xs font-bold bg-[#1C2A4F] hover:bg-[#25386b] text-white border border-[#1E3A8A] flex items-center justify-center gap-2 transition-all active:scale-95"
                  >
                    <Radio className="w-4 h-4 text-[#A5B4FC]" />
                    <span>2. Adopt Remote Copy ({selectedConflictForReview.remoteDeviceName})</span>
                  </button>

                  <button
                    type="button"
                    onClick={() => handleResolve('KEEP_BOTH')}
                    className="w-full py-2.5 px-4 rounded-xl text-xs font-bold bg-[#1C2A4F] hover:bg-[#25386b] text-white border border-[#1E3A8A] flex items-center justify-center gap-2 transition-all active:scale-95"
                  >
                    <GitFork className="w-4 h-4 text-amber-400" />
                    <span>3. Keep Both (Fork Duplicate Branch)</span>
                  </button>

                  <button
                    type="button"
                    onClick={() => setIsManualMergeMode(true)}
                    className="w-full py-2 px-4 rounded-xl text-xs font-bold text-[#53EFD8] hover:bg-[#1C2A4F] transition-colors"
                  >
                    4. Interactive Manual Merge Editor →
                  </button>
                </div>
              </>
            ) : (
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <p className="text-xs text-slate-300 font-medium">
                    Edit unified text to resolve both branches:
                  </p>
                  <button
                    type="button"
                    onClick={() => setIsManualMergeMode(false)}
                    className="text-xs text-slate-400 hover:text-white"
                  >
                    ← Back to presets
                  </button>
                </div>

                <textarea
                  rows={8}
                  value={manualMergedText}
                  onChange={(e) => setManualMergedText(e.target.value)}
                  className="w-full bg-[#0B132B] border border-[#1E3A8A] rounded-xl p-3 text-xs text-white font-mono focus:outline-none focus:border-[#53EFD8] resize-none"
                />

                <div className="flex justify-end gap-2 pt-2">
                  <button
                    type="button"
                    onClick={() => setIsManualMergeMode(false)}
                    className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-300 hover:bg-[#1C2A4F]"
                  >
                    Cancel
                  </button>
                  <button
                    type="button"
                    onClick={() => handleResolve('MERGE_CUSTOM')}
                    className="px-4 py-2 rounded-xl text-xs font-bold bg-[#53EFD8] hover:bg-[#3be5cc] text-slate-950 shadow-md"
                  >
                    Save Merged Version
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
