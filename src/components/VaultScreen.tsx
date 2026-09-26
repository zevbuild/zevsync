import React, { useRef, useState } from 'react';
import { useSyncBeam } from '../context/SyncBeamContext';
import { FileCategory, FileSortOrder, PeerDevice, SyncedFile } from '../types/models';
import { formatBytes } from '../services/cacheVaultManager';
import { FileCategoryIcon, SyncStatusBadge } from './CommonComponents';
import {
  Search,
  ArrowUpDown,
  Pin,
  PinOff,
  MoreVertical,
  Plus,
  Upload,
  Radio,
  FileText,
  AlertTriangle,
  FolderOpen,
  Eye,
  Trash2,
  HardDrive,
  X,
  Share2,
} from 'lucide-react';

const CATEGORIES: { id: FileCategory; label: string }[] = [
  { id: 'ALL', label: 'All Files' },
  { id: 'IMAGE', label: 'Images' },
  { id: 'DOCUMENT', label: 'Docs & PDF' },
  { id: 'AUDIO', label: 'Audio' },
  { id: 'VIDEO', label: 'Videos' },
  { id: 'CODE', label: 'Code & Data' },
  { id: 'ARCHIVE', label: 'Archives' },
  { id: 'OTHER', label: 'Other' },
];

export const VaultScreen: React.FC = () => {
  const {
    filteredFiles,
    files,
    selectedCategory,
    selectCategory,
    searchQuery,
    setSearchQuery,
    sortOrder,
    setSortOrder,
    storageBreakdown,
    discoveredPeers,
    selectTab,
    openPreview,
    togglePin,
    deleteFile,
    syncFileWithPeer,
    simulateConflictTest,
    isCreatingNoteDialog,
    showCreateNoteDialog,
    createNote,
    importFileFromBlob,
    showSnackbar,
  } = useSyncBeam();

  const [activeMenuFileId, setActiveMenuFileId] = useState<string | null>(null);
  const [peerPickerFile, setPeerPickerFile] = useState<SyncedFile | null>(null);
  const [newNoteTitle, setNewNoteTitle] = useState('');
  const [newNoteContent, setNewNoteContent] = useState('');
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  const activePeersCount = discoveredPeers.filter((p) => p.isConnected).length;

  const handleCreateNoteSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newNoteTitle.trim()) {
      showSnackbar('Please enter a note title');
      return;
    }
    await createNote(newNoteTitle.trim(), newNoteContent);
    setNewNoteTitle('');
    setNewNoteContent('');
  };

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const uploaded = e.target.files?.[0];
    if (uploaded) {
      importFileFromBlob(uploaded);
    }
    if (e.target) {
      e.target.value = '';
    }
  };

  return (
    <div className="pb-24 pt-2 px-4 max-w-2xl mx-auto space-y-3.5">
      {/* Header Banner */}
      <div className="rounded-2xl bg-gradient-to-br from-[#111D3D] to-[#1C2A4F] border border-[#1E3A8A] p-4 shadow-lg flex items-center justify-between">
        <div>
          <div className="flex items-center gap-2">
            <FolderOpen className="w-5 h-5 text-[#53EFD8]" />
            <h2 className="font-bold text-base text-white">Offline Cache Vault</h2>
          </div>
          <p className="text-xs text-slate-300 mt-1">
            <span className="font-semibold text-white">{files.length}</span> file(s) ·{' '}
            <span className="font-semibold text-[#53EFD8]">
              {formatBytes(storageBreakdown.totalVaultBytes)}
            </span>{' '}
            cached
          </p>
        </div>

        <button
          type="button"
          onClick={() => selectTab('RADAR')}
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-[#172554] hover:bg-[#1E3A8A] border border-[#1E3A8A] text-xs font-semibold text-sky-300 transition-all active:scale-95"
        >
          <Radio className="w-3.5 h-3.5 text-[#53EFD8]" />
          <span>{activePeersCount} Connected</span>
        </button>
      </div>

      {/* Search Bar & Sort Menu */}
      <div className="flex items-center gap-2">
        <div className="relative flex-1">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search vault & code contents..."
            className="w-full bg-[#111D3D] border border-[#1E3A8A] rounded-xl pl-9 pr-3 py-2 text-xs text-white placeholder-slate-400 focus:outline-none focus:border-[#53EFD8] transition-colors"
          />
          {searchQuery && (
            <button
              type="button"
              onClick={() => setSearchQuery('')}
              className="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-white"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          )}
        </div>

        {/* Sort Select */}
        <div className="relative">
          <select
            value={sortOrder}
            onChange={(e) => setSortOrder(e.target.value as FileSortOrder)}
            className="appearance-none bg-[#111D3D] border border-[#1E3A8A] rounded-xl pl-3 pr-8 py-2 text-xs font-semibold text-slate-200 focus:outline-none focus:border-[#53EFD8] cursor-pointer"
          >
            <option value="DATE_DESC">Newest First</option>
            <option value="DATE_ASC">Oldest First</option>
            <option value="NAME_ASC">Name (A-Z)</option>
            <option value="SIZE_DESC">Largest Size</option>
          </select>
          <ArrowUpDown className="w-3.5 h-3.5 text-slate-400 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
        </div>
      </div>

      {/* Category Filter Chips */}
      <div className="flex items-center gap-1.5 overflow-x-auto pb-1 scrollbar-none">
        {CATEGORIES.map((cat) => {
          const isSelected = selectedCategory === cat.id;
          return (
            <button
              key={cat.id}
              type="button"
              onClick={() => selectCategory(cat.id)}
              className={`px-3 py-1.5 rounded-full text-xs font-semibold whitespace-nowrap transition-all ${
                isSelected
                  ? 'bg-[#53EFD8] text-slate-950 shadow-md font-bold'
                  : 'bg-[#111D3D] text-slate-300 hover:bg-[#1C2A4F] border border-[#1E3A8A]'
              }`}
            >
              {cat.label}
            </button>
          );
        })}
      </div>

      {/* Files List */}
      <div className="space-y-2.5">
        {filteredFiles.length === 0 ? (
          <div className="rounded-2xl border border-dashed border-[#1E3A8A] p-8 text-center bg-[#111D3D]/40">
            <HardDrive className="w-10 h-10 text-slate-500 mx-auto mb-2" />
            <p className="text-sm font-semibold text-slate-300">No files found in Vault</p>
            <p className="text-xs text-slate-400 mt-1">
              {searchQuery
                ? 'Try matching a different keyword or file name.'
                : 'Import files from your device or create an offline text note.'}
            </p>
          </div>
        ) : (
          filteredFiles.map((file) => (
            <div
              key={file.id}
              className="rounded-xl bg-[#111D3D] border border-[#1E3A8A] hover:border-[#38BDF8]/50 p-3 transition-all relative group"
            >
              <div className="flex items-start gap-3">
                {/* Category Icon */}
                <FileCategoryIcon category={file.category} size={42} />

                {/* File info */}
                <div
                  className="flex-1 min-w-0 cursor-pointer"
                  onClick={() => openPreview(file)}
                >
                  <div className="flex items-center gap-1.5">
                    <span className="font-bold text-sm text-white truncate max-w-[200px] sm:max-w-xs">
                      {file.name}
                    </span>
                    {file.isPinned && (
                      <Pin className="w-3.5 h-3.5 text-[#53EFD8] shrink-0 fill-[#53EFD8]" />
                    )}
                  </div>

                  <div className="flex items-center gap-2 mt-1 flex-wrap">
                    <span className="text-xs text-slate-400 font-medium">
                      {formatBytes(file.sizeBytes)}
                    </span>
                    <span className="text-slate-600 text-xs">•</span>
                    <span className="text-xs text-slate-400">
                      v{file.versionNumber} (L:{file.lamportTimestamp})
                    </span>
                    <span className="text-slate-600 text-xs">•</span>
                    <SyncStatusBadge status={file.syncStatus} />
                  </div>

                  {file.textPreview && (
                    <p className="text-xs text-slate-400 mt-1 line-clamp-1 font-mono bg-[#0B132B]/50 px-2 py-0.5 rounded border border-[#1E3A8A]/40">
                      {file.textPreview}
                    </p>
                  )}
                </div>

                {/* File Quick Actions & Menu */}
                <div className="flex items-center gap-1 shrink-0 relative">
                  {/* Sync with Peer Button */}
                  <button
                    type="button"
                    title="Sync with nearby peer"
                    onClick={() => setPeerPickerFile(file)}
                    className="p-1.5 rounded-lg bg-[#1C2A4F] hover:bg-[#25386b] text-[#53EFD8] border border-[#1E3A8A] transition-all"
                  >
                    <Share2 className="w-3.5 h-3.5" />
                  </button>

                  {/* Simulate Conflict Button */}
                  <button
                    type="button"
                    title="Simulate concurrent edit conflict"
                    onClick={() => {
                      const peer = discoveredPeers[0];
                      if (peer) {
                        simulateConflictTest(file, peer);
                      } else {
                        showSnackbar('No peers discovered to simulate conflict with');
                      }
                    }}
                    className="p-1.5 rounded-lg bg-[#451A03] hover:bg-[#78350F] text-[#F59E0B] border border-[#F59E0B]/30 transition-all"
                  >
                    <AlertTriangle className="w-3.5 h-3.5" />
                  </button>

                  {/* More Menu Toggle */}
                  <button
                    type="button"
                    onClick={() =>
                      setActiveMenuFileId(activeMenuFileId === file.id ? null : file.id)
                    }
                    className="p-1.5 rounded-lg hover:bg-[#1C2A4F] text-slate-300 transition-colors"
                  >
                    <MoreVertical className="w-4 h-4" />
                  </button>

                  {/* Dropdown Menu */}
                  {activeMenuFileId === file.id && (
                    <div className="absolute right-0 top-8 z-20 w-44 rounded-xl bg-[#172554] border border-[#1E3A8A] shadow-xl py-1 text-xs">
                      <button
                        type="button"
                        onClick={() => {
                          openPreview(file);
                          setActiveMenuFileId(null);
                        }}
                        className="w-full text-left px-3 py-2 hover:bg-[#1E3A8A] text-slate-200 flex items-center gap-2"
                      >
                        <Eye className="w-3.5 h-3.5 text-sky-400" />
                        <span>Preview & Edit</span>
                      </button>

                      <button
                        type="button"
                        onClick={() => {
                          togglePin(file.id, !file.isPinned);
                          setActiveMenuFileId(null);
                        }}
                        className="w-full text-left px-3 py-2 hover:bg-[#1E3A8A] text-slate-200 flex items-center gap-2"
                      >
                        {file.isPinned ? (
                          <>
                            <PinOff className="w-3.5 h-3.5 text-amber-400" />
                            <span>Unpin File</span>
                          </>
                        ) : (
                          <>
                            <Pin className="w-3.5 h-3.5 text-[#53EFD8]" />
                            <span>Pin to Cache</span>
                          </>
                        )}
                      </button>

                      <button
                        type="button"
                        onClick={() => {
                          deleteFile(file.id);
                          setActiveMenuFileId(null);
                        }}
                        className="w-full text-left px-3 py-2 hover:bg-[#7F1D1D] text-rose-300 flex items-center gap-2 border-t border-[#1E3A8A]/50"
                      >
                        <Trash2 className="w-3.5 h-3.5 text-rose-400" />
                        <span>Delete from Vault</span>
                      </button>
                    </div>
                  )}
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Floating Action Buttons */}
      <div className="fixed bottom-20 right-4 z-20 flex flex-col gap-2.5">
        {/* Hidden File Input */}
        <input
          type="file"
          ref={fileInputRef}
          onChange={handleFileUpload}
          className="hidden"
        />

        {/* Upload File FAB */}
        <button
          type="button"
          onClick={() => fileInputRef.current?.click()}
          title="Import File into Vault"
          className="w-12 h-12 rounded-full bg-[#1C2A4F] hover:bg-[#25386b] text-[#53EFD8] border border-[#1E3A8A] shadow-xl flex items-center justify-center transition-all active:scale-95"
        >
          <Upload className="w-5 h-5" />
        </button>

        {/* Create Note FAB */}
        <button
          type="button"
          onClick={() => showCreateNoteDialog(true)}
          title="Create Offline Text Note"
          className="w-12 h-12 rounded-full bg-gradient-to-tr from-[#00D2BA] to-[#4F46E5] hover:opacity-95 text-white shadow-xl flex items-center justify-center transition-all active:scale-95 shadow-[#00D2BA]/25"
        >
          <Plus className="w-6 h-6" />
        </button>
      </div>

      {/* Peer Picker Modal for File Sync */}
      {peerPickerFile && (
        <div className="fixed inset-0 z-40 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="w-full max-w-sm rounded-2xl bg-[#111D3D] border border-[#1E3A8A] p-5 shadow-2xl space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="font-bold text-white text-base">Select Peer Node</h3>
                <p className="text-xs text-slate-400">
                  Transmit '{peerPickerFile.name}' over Bluetooth RFCOMM
                </p>
              </div>
              <button
                type="button"
                onClick={() => setPeerPickerFile(null)}
                className="text-slate-400 hover:text-white p-1"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="space-y-2 max-h-60 overflow-y-auto pr-1">
              {discoveredPeers.map((peer) => (
                <div
                  key={peer.id}
                  className="rounded-xl bg-[#172554] border border-[#1E3A8A] p-3 flex items-center justify-between"
                >
                  <div className="min-w-0 pr-2">
                    <p className="font-semibold text-xs text-white truncate">{peer.name}</p>
                    <p className="text-[10px] text-slate-400">
                      {peer.deviceModel} • {peer.rssi} dBm
                    </p>
                  </div>
                  <button
                    type="button"
                    disabled={!peer.isConnected}
                    onClick={() => {
                      syncFileWithPeer(peerPickerFile, peer);
                      setPeerPickerFile(null);
                    }}
                    className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all ${
                      peer.isConnected
                        ? 'bg-[#53EFD8] hover:bg-[#3be5cc] text-slate-950 active:scale-95'
                        : 'bg-slate-700 text-slate-400 cursor-not-allowed'
                    }`}
                  >
                    Sync
                  </button>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* Create Note Modal */}
      {isCreatingNoteDialog && (
        <div className="fixed inset-0 z-40 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4">
          <form
            onSubmit={handleCreateNoteSubmit}
            className="w-full max-w-md rounded-2xl bg-[#111D3D] border border-[#1E3A8A] p-5 shadow-2xl space-y-4"
          >
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <FileText className="w-5 h-5 text-[#53EFD8]" />
                <h3 className="font-bold text-white text-base">New Offline Note</h3>
              </div>
              <button
                type="button"
                onClick={() => showCreateNoteDialog(false)}
                className="text-slate-400 hover:text-white p-1"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">
                Document Title
              </label>
              <input
                type="text"
                required
                value={newNoteTitle}
                onChange={(e) => setNewNoteTitle(e.target.value)}
                placeholder="e.g. Field_Notes.md"
                className="w-full bg-[#0B132B] border border-[#1E3A8A] rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-[#53EFD8]"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">
                Markdown / Text Content
              </label>
              <textarea
                rows={6}
                value={newNoteContent}
                onChange={(e) => setNewNoteContent(e.target.value)}
                placeholder="Type or paste markdown content..."
                className="w-full bg-[#0B132B] border border-[#1E3A8A] rounded-xl p-3 text-xs text-white font-mono focus:outline-none focus:border-[#53EFD8] resize-none"
              />
            </div>

            <div className="flex items-center justify-end gap-2 pt-2">
              <button
                type="button"
                onClick={() => showCreateNoteDialog(false)}
                className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-300 hover:bg-[#1C2A4F]"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="px-4 py-2 rounded-xl text-xs font-bold bg-[#53EFD8] hover:bg-[#3be5cc] text-slate-950 shadow-md"
              >
                Save to Vault
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
};
