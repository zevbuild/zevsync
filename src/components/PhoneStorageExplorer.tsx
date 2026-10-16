import React, { useState, useRef } from 'react';
import { useSyncBeam } from '../context/SyncBeamContext';
import { PhoneFileItem } from '../types/models';
import { formatBytes } from '../services/cacheVaultManager';
import {
  Smartphone,
  Folder,
  File,
  Image as ImageIcon,
  FileText,
  Music,
  Video,
  Download,
  Upload,
  FolderPlus,
  Trash2,
  RefreshCw,
  ChevronRight,
  ArrowLeft,
  CheckCircle2,
  Share2,
  QrCode,
  Radio,
  HardDrive,
  Copy,
  Check,
  X,
  Eye,
  Search,
  Archive,
  FolderDown,
  Compass,
  Bluetooth,
  Save,
} from 'lucide-react';

export const PhoneStorageExplorer: React.FC = () => {
  const {
    phoneFiles,
    currentPhonePath,
    navigateToPhoneFolder,
    uploadFileToPhone,
    downloadFileFromPhone,
    downloadFolderAsZip,
    deletePhoneFile,
    createPhoneFolder,
    syncAllPhoneFilesToVault,
    isPhoneProximityConnected,
    proximityAutoSyncEnabled,
    toggleProximityAutoSync,
    connectPhoneProximityBridge,
    disconnectPhoneProximityBridge,
    proximityDistanceMeters,
    proximityRssi,
    connectedPhoneName,
    simulateProximityRange,
    mountLocalDirectoryPicker,
    startPhoneBluetoothPairing,
    showSnackbar,
  } = useSyncBeam();

  const [showNewFolderModal, setShowNewFolderModal] = useState(false);
  const [newFolderName, setNewFolderName] = useState('');
  const [showQrModal, setShowQrModal] = useState(false);
  const [previewFile, setPreviewFile] = useState<PhoneFileItem | null>(null);
  const [editedContent, setEditedContent] = useState('');
  const [isEditingText, setIsEditingText] = useState(false);
  const [copiedLink, setCopiedLink] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  // Filter files in current directory or search query
  const currentItems = phoneFiles.filter((item) => {
    if (searchQuery.trim()) {
      return item.name.toLowerCase().includes(searchQuery.toLowerCase().trim());
    }
    return item.parentPath === currentPhonePath;
  });

  // Compute breadcrumbs
  const breadcrumbs =
    currentPhonePath === '/'
      ? ['Internal Storage']
      : ['Internal Storage', ...currentPhonePath.split('/').filter(Boolean)];

  const handleBreadcrumbClick = (index: number) => {
    if (index === 0) {
      navigateToPhoneFolder('/');
      return;
    }
    const parts = currentPhonePath.split('/').filter(Boolean).slice(0, index);
    navigateToPhoneFolder('/' + parts.join('/'));
  };

  const handleCreateFolder = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newFolderName.trim()) return;
    createPhoneFolder(currentPhonePath, newFolderName.trim());
    setNewFolderName('');
    setShowNewFolderModal(false);
  };

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      uploadFileToPhone(file, currentPhonePath);
    }
    if (e.target) {
      e.target.value = '';
    }
  };

  const handleCopyPairLink = () => {
    const url = typeof window !== 'undefined' ? window.location.href : 'https://zevsync.local';
    navigator.clipboard.writeText(url);
    setCopiedLink(true);
    setTimeout(() => setCopiedLink(false), 2000);
    showSnackbar('Copied phone pairing link to clipboard');
  };

  const openPreview = (file: PhoneFileItem) => {
    setPreviewFile(file);
    setEditedContent(file.contentSnippet || '');
    setIsEditingText(false);
  };

  const handleSaveTextEdit = () => {
    if (!previewFile) return;
    previewFile.contentSnippet = editedContent;
    setIsEditingText(false);
    showSnackbar(`Saved changes to '${previewFile.name}' on phone`);
  };

  const getFileIcon = (item: PhoneFileItem) => {
    if (item.isDirectory) return <Folder className="w-5 h-5 text-amber-400" />;
    const mime = item.mimeType;
    if (mime.startsWith('image/')) return <ImageIcon className="w-5 h-5 text-pink-400" />;
    if (mime.startsWith('video/')) return <Video className="w-5 h-5 text-purple-400" />;
    if (mime.startsWith('audio/')) return <Music className="w-5 h-5 text-indigo-400" />;
    if (mime.includes('markdown') || mime.includes('text') || mime.includes('pdf')) {
      return <FileText className="w-5 h-5 text-sky-400" />;
    }
    return <File className="w-5 h-5 text-slate-400" />;
  };

  return (
    <div className="pb-24 pt-2 px-4 max-w-3xl mx-auto space-y-4">
      {/* Proximity Bridge Status & Control Card */}
      <div className="rounded-2xl bg-[#111D3D] border border-[#1E3A8A] p-4 shadow-lg space-y-4">
        <div className="flex items-start justify-between gap-3">
          <div className="flex items-center gap-3">
            <div
              className={`w-12 h-12 rounded-2xl flex items-center justify-center shrink-0 transition-all ${
                isPhoneProximityConnected
                  ? 'bg-[#00D2BA]/20 text-[#53EFD8] border border-[#53EFD8]/40 shadow-lg shadow-[#00D2BA]/10'
                  : 'bg-slate-800 text-slate-400 border border-slate-700'
              }`}
            >
              <Smartphone className="w-6 h-6" />
            </div>

            <div>
              <div className="flex items-center gap-2">
                <h2 className="font-bold text-base text-white">{connectedPhoneName}</h2>
                {isPhoneProximityConnected && (
                  <span className="flex h-2 w-2 relative">
                    <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-[#10B981] opacity-75" />
                    <span className="relative inline-flex rounded-full h-2 w-2 bg-[#10B981]" />
                  </span>
                )}
              </div>
              <p className="text-xs text-slate-300 mt-0.5">
                {isPhoneProximityConnected
                  ? `Phone near laptop · Distance: ~${proximityDistanceMeters}m · Signal: ${proximityRssi} dBm · Full access unlocked`
                  : 'Phone is currently out of range or disconnected. Move closer or connect below.'}
              </p>
            </div>
          </div>

          <span
            className={`px-2.5 py-1 rounded-lg text-[10px] font-black tracking-wider uppercase border shrink-0 ${
              isPhoneProximityConnected
                ? 'bg-[#064E3B] text-[#10B981] border-[#10B981]/30'
                : 'bg-amber-950 text-amber-400 border-amber-800/40'
            }`}
          >
            {isPhoneProximityConnected ? 'PHONE CONNECTED' : 'OUT OF RANGE'}
          </span>
        </div>

        {/* Proximity Auto-Access & Range Simulator */}
        <div className="pt-2 border-t border-[#1E3A8A]/50 space-y-2.5">
          <div className="flex flex-wrap items-center justify-between gap-2 text-xs">
            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => toggleProximityAutoSync(!proximityAutoSyncEnabled)}
                className={`w-9 h-5 rounded-full p-0.5 transition-colors relative ${
                  proximityAutoSyncEnabled ? 'bg-[#10B981]' : 'bg-slate-700'
                }`}
              >
                <div
                  className={`w-4 h-4 rounded-full bg-white transition-transform ${
                    proximityAutoSyncEnabled ? 'translate-x-4' : 'translate-x-0'
                  }`}
                />
              </button>
              <span className="text-slate-300 font-medium">Auto-access files when near phone</span>
            </div>

            {/* Proximity Range Test Buttons */}
            <div className="flex items-center gap-1 text-[11px]">
              <span className="text-slate-400 mr-1 hidden sm:inline">Test Distance:</span>
              <button
                type="button"
                onClick={() => simulateProximityRange('NEAR')}
                className={`px-2 py-0.5 rounded-lg border transition-all ${
                  isPhoneProximityConnected && proximityDistanceMeters < 1
                    ? 'bg-[#10B981]/20 text-[#53EFD8] border-[#10B981]'
                    : 'bg-[#0B132B] text-slate-400 border-[#1E3A8A] hover:text-white'
                }`}
              >
                Near (&lt;1m)
              </button>
              <button
                type="button"
                onClick={() => simulateProximityRange('MID')}
                className={`px-2 py-0.5 rounded-lg border transition-all ${
                  isPhoneProximityConnected && proximityDistanceMeters >= 1 && proximityDistanceMeters < 5
                    ? 'bg-[#38BDF8]/20 text-[#38BDF8] border-[#38BDF8]'
                    : 'bg-[#0B132B] text-slate-400 border-[#1E3A8A] hover:text-white'
                }`}
              >
                Mid (2.5m)
              </button>
              <button
                type="button"
                onClick={() => simulateProximityRange('FAR')}
                className={`px-2 py-0.5 rounded-lg border transition-all ${
                  isPhoneProximityConnected && proximityDistanceMeters >= 5
                    ? 'bg-amber-500/20 text-amber-300 border-amber-500'
                    : 'bg-[#0B132B] text-slate-400 border-[#1E3A8A] hover:text-white'
                }`}
              >
                Far (7.5m)
              </button>
              <button
                type="button"
                onClick={() => simulateProximityRange('DISCONNECTED')}
                className={`px-2 py-0.5 rounded-lg border transition-all ${
                  !isPhoneProximityConnected
                    ? 'bg-rose-500/20 text-rose-300 border-rose-500'
                    : 'bg-[#0B132B] text-slate-400 border-[#1E3A8A] hover:text-white'
                }`}
              >
                Away
              </button>
            </div>
          </div>

          {/* Quick Action Connectivity Tools */}
          <div className="flex flex-wrap items-center justify-between gap-2 pt-1 border-t border-[#1E3A8A]/30">
            <div className="flex flex-wrap items-center gap-2">
              <button
                type="button"
                onClick={startPhoneBluetoothPairing}
                className="py-1.5 px-3 rounded-xl text-xs font-semibold bg-[#1C2A4F] hover:bg-[#25386b] text-[#53EFD8] border border-[#1E3A8A] flex items-center gap-1.5 transition-all"
                title="Pair phone using browser Web Bluetooth"
              >
                <Bluetooth className="w-3.5 h-3.5" />
                <span>Pair Web Bluetooth</span>
              </button>

              <button
                type="button"
                onClick={mountLocalDirectoryPicker}
                className="py-1.5 px-3 rounded-xl text-xs font-semibold bg-[#1C2A4F] hover:bg-[#25386b] text-slate-200 border border-[#1E3A8A] flex items-center gap-1.5 transition-all"
                title="Directly select and mount an Android storage folder or SD card"
              >
                <FolderDown className="w-3.5 h-3.5 text-amber-400" />
                <span>Mount Phone SD / Folder</span>
              </button>

              <button
                type="button"
                onClick={() => setShowQrModal(true)}
                className="py-1.5 px-3 rounded-xl text-xs font-semibold bg-[#1C2A4F] hover:bg-[#25386b] text-slate-200 border border-[#1E3A8A] flex items-center gap-1.5"
              >
                <QrCode className="w-3.5 h-3.5 text-[#53EFD8]" />
                <span>QR Link</span>
              </button>
            </div>

            <div className="flex items-center gap-2">
              {!isPhoneProximityConnected ? (
                <button
                  type="button"
                  onClick={connectPhoneProximityBridge}
                  className="py-1.5 px-3 rounded-xl text-xs font-bold bg-[#53EFD8] hover:bg-[#3be5cc] text-slate-950 flex items-center gap-1.5 transition-all shadow-sm active:scale-95"
                >
                  <Radio className="w-3.5 h-3.5" />
                  <span>Connect Storage Bridge</span>
                </button>
              ) : (
                <button
                  type="button"
                  onClick={syncAllPhoneFilesToVault}
                  className="py-1.5 px-3 rounded-xl text-xs font-bold bg-[#047857] hover:bg-[#059669] text-white flex items-center gap-1.5 transition-all shadow-sm active:scale-95"
                  title="Copy all phone files into laptop offline cache"
                >
                  <Download className="w-3.5 h-3.5" />
                  <span>Backup All to Laptop</span>
                </button>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Category Fast-Jumps */}
      <div className="grid grid-cols-3 sm:grid-cols-6 gap-2">
        {[
          { label: 'Camera / DCIM', path: '/DCIM/Camera', icon: ImageIcon },
          { label: 'Downloads', path: '/Download', icon: Download },
          { label: 'Documents', path: '/Documents', icon: FileText },
          { label: 'Pictures', path: '/Pictures', icon: Folder },
          { label: 'Music', path: '/Music', icon: Music },
          { label: 'Root Storage', path: '/', icon: HardDrive },
        ].map((cat) => {
          const Icon = cat.icon;
          const isActive = currentPhonePath === cat.path;
          return (
            <button
              key={cat.path}
              type="button"
              onClick={() => {
                setSearchQuery('');
                navigateToPhoneFolder(cat.path);
              }}
              className={`py-2 px-2.5 rounded-xl border text-left flex flex-col items-center justify-center gap-1 transition-all ${
                isActive
                  ? 'bg-[#1C2A4F] border-[#53EFD8] text-[#53EFD8]'
                  : 'bg-[#111D3D] border-[#1E3A8A] text-slate-300 hover:bg-[#172554]'
              }`}
            >
              <Icon className="w-4 h-4" />
              <span className="text-[10px] font-medium text-center truncate w-full">{cat.label}</span>
            </button>
          );
        })}
      </div>

      {/* Directory Browser Header & Toolbar */}
      <div className="rounded-2xl bg-[#111D3D] border border-[#1E3A8A] p-4 shadow-lg space-y-3">
        {/* Search Bar & Actions */}
        <div className="flex flex-wrap items-center justify-between gap-2">
          <div className="relative flex-1 min-w-[200px]">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search phone files and folders..."
              className="w-full bg-[#0B132B] border border-[#1E3A8A] rounded-xl pl-9 pr-3 py-1.5 text-xs text-white focus:outline-none focus:border-[#53EFD8]"
            />
            {searchQuery && (
              <button
                type="button"
                onClick={() => setSearchQuery('')}
                className="absolute right-2.5 top-2 text-slate-400 hover:text-white"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            )}
          </div>

          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => downloadFolderAsZip(currentPhonePath)}
              className="py-1.5 px-3 rounded-xl text-xs font-semibold bg-[#1C2A4F] hover:bg-[#25386b] text-sky-300 border border-[#1E3A8A] flex items-center gap-1.5 transition-all"
              title="Download this folder and subfolders as a .ZIP file onto your laptop"
            >
              <Archive className="w-3.5 h-3.5" />
              <span>Download ZIP</span>
            </button>

            <input
              type="file"
              ref={fileInputRef}
              onChange={handleFileUpload}
              className="hidden"
            />

            <button
              type="button"
              onClick={() => fileInputRef.current?.click()}
              className="py-1.5 px-3 rounded-xl text-xs font-bold bg-[#1C2A4F] hover:bg-[#25386b] text-[#53EFD8] border border-[#1E3A8A] flex items-center gap-1.5 transition-all active:scale-95"
              title="Upload file from laptop to this folder on phone"
            >
              <Upload className="w-3.5 h-3.5" />
              <span>Upload to Phone</span>
            </button>

            <button
              type="button"
              onClick={() => setShowNewFolderModal(true)}
              className="py-1.5 px-3 rounded-xl text-xs font-semibold bg-[#0B132B] hover:bg-[#172554] text-slate-300 border border-[#1E3A8A] flex items-center gap-1.5"
              title="Create new folder on phone"
            >
              <FolderPlus className="w-3.5 h-3.5" />
              <span className="hidden sm:inline">New Folder</span>
            </button>
          </div>
        </div>

        {/* Breadcrumb Path */}
        <div className="flex items-center gap-1 overflow-x-auto text-xs py-1.5 border-t border-[#1E3A8A]/50">
          {currentPhonePath !== '/' && (
            <button
              type="button"
              onClick={() => {
                const parts = currentPhonePath.split('/').filter(Boolean);
                parts.pop();
                navigateToPhoneFolder(parts.length === 0 ? '/' : '/' + parts.join('/'));
              }}
              className="p-1 rounded-lg text-slate-400 hover:text-white hover:bg-[#1C2A4F] mr-1 shrink-0"
              title="Go up one folder"
            >
              <ArrowLeft className="w-4 h-4" />
            </button>
          )}

          {breadcrumbs.map((segment, idx) => (
            <React.Fragment key={idx}>
              {idx > 0 && <ChevronRight className="w-3.5 h-3.5 text-slate-500 shrink-0" />}
              <button
                type="button"
                onClick={() => handleBreadcrumbClick(idx)}
                className={`px-1.5 py-0.5 rounded font-mono hover:text-[#53EFD8] transition-colors whitespace-nowrap ${
                  idx === breadcrumbs.length - 1
                    ? 'text-white font-bold'
                    : 'text-slate-400'
                }`}
              >
                {segment}
              </button>
            </React.Fragment>
          ))}
        </div>

        {/* Directory Item List */}
        <div className="space-y-1.5 pt-1 max-h-[500px] overflow-y-auto">
          {currentItems.length === 0 ? (
            <div className="text-center py-12 text-slate-400 space-y-3">
              <Folder className="w-10 h-10 mx-auto text-slate-600" />
              <p className="text-xs">
                {searchQuery ? `No files matching "${searchQuery}"` : 'This folder on your phone is empty.'}
              </p>
              <button
                type="button"
                onClick={() => fileInputRef.current?.click()}
                className="text-xs text-[#53EFD8] hover:underline"
              >
                Upload a file from your computer here
              </button>
            </div>
          ) : (
            currentItems.map((item) => (
              <div
                key={item.id}
                className="group flex items-center justify-between p-2.5 rounded-xl bg-[#0B132B]/80 hover:bg-[#172554]/60 border border-[#1E3A8A]/40 transition-colors"
              >
                {/* Item Details (Click to open directory or preview) */}
                <div
                  className="flex items-center gap-3 min-w-0 flex-1 cursor-pointer"
                  onClick={() => {
                    if (item.isDirectory) {
                      setSearchQuery('');
                      navigateToPhoneFolder(item.path);
                    } else {
                      openPreview(item);
                    }
                  }}
                >
                  <div className="p-2 rounded-lg bg-[#111D3D] border border-[#1E3A8A]/50 shrink-0">
                    {item.previewUrl ? (
                      <img
                        src={item.previewUrl}
                        alt={item.name}
                        className="w-5 h-5 object-cover rounded"
                      />
                    ) : (
                      getFileIcon(item)
                    )}
                  </div>

                  <div className="min-w-0 flex-1">
                    <p className="font-semibold text-xs text-white truncate group-hover:text-[#53EFD8] transition-colors">
                      {item.name}
                    </p>
                    <p className="text-[11px] text-slate-400 mt-0.5">
                      {item.isDirectory ? (
                        <span>Folder · {phoneFiles.filter((f) => f.parentPath === item.path).length} items</span>
                      ) : (
                        <span>
                          {formatBytes(item.sizeBytes)} · {new Date(item.modifiedTimestamp).toLocaleDateString()}
                        </span>
                      )}
                    </p>
                  </div>
                </div>

                {/* File Action Triggers */}
                <div className="flex items-center gap-1 ml-2">
                  {!item.isDirectory ? (
                    <>
                      <button
                        type="button"
                        onClick={() => openPreview(item)}
                        className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-[#1C2A4F] transition-colors"
                        title="Preview File"
                      >
                        <Eye className="w-3.5 h-3.5" />
                      </button>

                      <button
                        type="button"
                        onClick={() => downloadFileFromPhone(item)}
                        className="p-1.5 rounded-lg text-[#53EFD8] hover:bg-[#1C2A4F] transition-colors"
                        title="Download to Laptop"
                      >
                        <Download className="w-3.5 h-3.5" />
                      </button>
                    </>
                  ) : (
                    <button
                      type="button"
                      onClick={() => downloadFolderAsZip(item.path)}
                      className="p-1.5 rounded-lg text-sky-400 hover:bg-[#1C2A4F] transition-colors"
                      title="Download folder as ZIP"
                    >
                      <Archive className="w-3.5 h-3.5" />
                    </button>
                  )}

                  <button
                    type="button"
                    onClick={() => deletePhoneFile(item.id)}
                    className="p-1.5 rounded-lg text-slate-500 hover:text-rose-400 hover:bg-[#1C2A4F] transition-colors"
                    title="Delete on Phone"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      </div>

      {/* New Folder Modal */}
      {showNewFolderModal && (
        <div className="fixed inset-0 z-50 bg-black/75 backdrop-blur-sm flex items-center justify-center p-4">
          <form
            onSubmit={handleCreateFolder}
            className="w-full max-w-sm rounded-2xl bg-[#111D3D] border border-[#1E3A8A] p-5 shadow-2xl space-y-4"
          >
            <div className="flex items-center justify-between">
              <h3 className="font-bold text-white text-base">New Phone Folder</h3>
              <button
                type="button"
                onClick={() => setShowNewFolderModal(false)}
                className="text-slate-400 hover:text-white p-1"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">
                Folder Name
              </label>
              <input
                type="text"
                required
                autoFocus
                value={newFolderName}
                onChange={(e) => setNewFolderName(e.target.value)}
                placeholder="e.g. Work_Documents"
                className="w-full bg-[#0B132B] border border-[#1E3A8A] rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-[#53EFD8]"
              />
            </div>

            <div className="flex justify-end gap-2 pt-2">
              <button
                type="button"
                onClick={() => setShowNewFolderModal(false)}
                className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-300 hover:bg-[#1C2A4F]"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="px-4 py-2 rounded-xl text-xs font-bold bg-[#53EFD8] hover:bg-[#3be5cc] text-slate-950 shadow-md"
              >
                Create Folder
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Phone Pair & QR Modal */}
      {showQrModal && (
        <div className="fixed inset-0 z-50 bg-black/75 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="w-full max-w-sm rounded-2xl bg-[#111D3D] border border-[#1E3A8A] p-5 shadow-2xl space-y-4 text-center">
            <div className="flex items-center justify-between">
              <h3 className="font-bold text-white text-base">Pair Android Phone</h3>
              <button
                type="button"
                onClick={() => setShowQrModal(false)}
                className="text-slate-400 hover:text-white p-1"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <p className="text-xs text-slate-300">
              Open ZevSync on your phone to link directly to this computer via offline proximity mesh:
            </p>

            <div className="p-4 bg-white rounded-2xl w-48 h-48 mx-auto flex items-center justify-center shadow-inner">
              <QrCode className="w-36 h-36 text-slate-950" />
            </div>

            <div className="pt-2 flex gap-2">
              <button
                type="button"
                onClick={handleCopyPairLink}
                className="flex-1 py-2 px-3 rounded-xl text-xs font-bold bg-[#1C2A4F] hover:bg-[#25386b] text-white border border-[#1E3A8A] flex items-center justify-center gap-1.5"
              >
                {copiedLink ? <Check className="w-3.5 h-3.5 text-[#10B981]" /> : <Copy className="w-3.5 h-3.5" />}
                <span>{copiedLink ? 'Copied' : 'Copy Pairing URL'}</span>
              </button>

              <button
                type="button"
                onClick={() => {
                  connectPhoneProximityBridge();
                  setShowQrModal(false);
                }}
                className="py-2 px-4 rounded-xl text-xs font-bold bg-[#53EFD8] hover:bg-[#3be5cc] text-slate-950 shadow-md"
              >
                Connect Bridge
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Rich File Preview Modal */}
      {previewFile && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="w-full max-w-xl rounded-2xl bg-[#111D3D] border border-[#1E3A8A] p-5 shadow-2xl space-y-4 max-h-[88vh] flex flex-col">
            <div className="flex items-center justify-between pb-2 border-b border-[#1E3A8A]">
              <div className="flex items-center gap-2 min-w-0">
                {getFileIcon(previewFile)}
                <h3 className="font-bold text-white text-sm truncate">{previewFile.name}</h3>
                <span className="text-[10px] text-slate-400 font-mono">
                  ({formatBytes(previewFile.sizeBytes)})
                </span>
              </div>
              <button
                type="button"
                onClick={() => setPreviewFile(null)}
                className="text-slate-400 hover:text-white p-1 shrink-0"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="flex-1 overflow-y-auto p-3 bg-[#0B132B] rounded-xl border border-[#1E3A8A]/50 text-xs text-slate-200 font-mono">
              {/* Image Preview */}
              {previewFile.mimeType.startsWith('image/') ? (
                <div className="text-center py-4 space-y-3">
                  {previewFile.previewUrl ? (
                    <img
                      src={previewFile.previewUrl}
                      alt={previewFile.name}
                      className="max-h-72 mx-auto rounded-lg object-contain shadow-lg"
                    />
                  ) : (
                    <div className="py-12 bg-slate-900 rounded-lg flex flex-col items-center justify-center gap-2">
                      <ImageIcon className="w-12 h-12 text-pink-400" />
                      <p className="text-slate-300 font-sans font-medium">{previewFile.name}</p>
                      <p className="text-[11px] text-slate-500 font-sans">
                        High-resolution photo on phone ({formatBytes(previewFile.sizeBytes)})
                      </p>
                    </div>
                  )}
                </div>
              ) : previewFile.mimeType.startsWith('video/') ? (
                /* Video Preview */
                <div className="py-8 bg-slate-900 rounded-lg flex flex-col items-center justify-center gap-3 font-sans">
                  <Video className="w-12 h-12 text-purple-400" />
                  <p className="text-slate-300 font-medium">{previewFile.name}</p>
                  <p className="text-xs text-slate-400">Video media file from phone storage</p>
                  <button
                    type="button"
                    onClick={() => downloadFileFromPhone(previewFile)}
                    className="py-1.5 px-3 rounded-lg text-xs font-bold bg-[#53EFD8] text-slate-950 flex items-center gap-1.5"
                  >
                    <Download className="w-3.5 h-3.5" />
                    <span>Stream / Download to Laptop</span>
                  </button>
                </div>
              ) : previewFile.mimeType.startsWith('audio/') ? (
                /* Audio Preview */
                <div className="py-8 bg-slate-900 rounded-lg flex flex-col items-center justify-center gap-3 font-sans">
                  <Music className="w-12 h-12 text-indigo-400" />
                  <p className="text-slate-300 font-medium">{previewFile.name}</p>
                  <p className="text-xs text-slate-400">Audio recording on phone</p>
                  <button
                    type="button"
                    onClick={() => downloadFileFromPhone(previewFile)}
                    className="py-1.5 px-3 rounded-lg text-xs font-bold bg-[#53EFD8] text-slate-950 flex items-center gap-1.5"
                  >
                    <Download className="w-3.5 h-3.5" />
                    <span>Download Audio File</span>
                  </button>
                </div>
              ) : (
                /* Text / Document Preview & Edit */
                <div className="space-y-2">
                  <div className="flex items-center justify-between pb-1 border-b border-[#1E3A8A]/40 text-slate-400 font-sans text-[11px]">
                    <span>Phone File Content Preview</span>
                    <button
                      type="button"
                      onClick={() => setIsEditingText(!isEditingText)}
                      className="text-[#53EFD8] hover:underline"
                    >
                      {isEditingText ? 'Cancel Edit' : 'Edit on Phone'}
                    </button>
                  </div>

                  {isEditingText ? (
                    <div className="space-y-2">
                      <textarea
                        value={editedContent}
                        onChange={(e) => setEditedContent(e.target.value)}
                        rows={12}
                        className="w-full bg-[#111D3D] text-white p-3 rounded-xl border border-[#53EFD8]/50 focus:outline-none font-mono text-xs"
                      />
                      <button
                        type="button"
                        onClick={handleSaveTextEdit}
                        className="py-1.5 px-3 rounded-lg text-xs font-bold bg-[#10B981] hover:bg-[#059669] text-white flex items-center gap-1.5"
                      >
                        <Save className="w-3.5 h-3.5" />
                        <span>Save Back to Phone</span>
                      </button>
                    </div>
                  ) : previewFile.contentSnippet ? (
                    <pre className="whitespace-pre-wrap">{previewFile.contentSnippet}</pre>
                  ) : (
                    <div className="text-center py-8 text-slate-400 font-sans">
                      <p>Binary File: {previewFile.name}</p>
                      <p className="text-[11px] text-slate-500 mt-1">
                        Ready to download to your laptop storage.
                      </p>
                    </div>
                  )}
                </div>
              )}
            </div>

            <div className="flex justify-end gap-2 pt-2">
              <button
                type="button"
                onClick={() => setPreviewFile(null)}
                className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-300 hover:bg-[#1C2A4F]"
              >
                Close
              </button>
              <button
                type="button"
                onClick={() => {
                  downloadFileFromPhone(previewFile);
                  setPreviewFile(null);
                }}
                className="px-4 py-2 rounded-xl text-xs font-bold bg-[#53EFD8] hover:bg-[#3be5cc] text-slate-950 flex items-center gap-1.5 shadow-md"
              >
                <Download className="w-3.5 h-3.5" />
                <span>Save to Laptop</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
