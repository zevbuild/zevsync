import React, { createContext, useContext, useState, useEffect, useMemo, ReactNode } from 'react';
import {
  ConflictResolutionChoice,
  FileCategory,
  FileSortOrder,
  NavigationTab,
  PeerDevice,
  StorageBreakdown,
  SyncConflict,
  SyncEventLog,
  SyncedFile,
  TransferDirection,
  TransferItem,
  GitHubReleaseInfo,
  GitHubFileItem,
} from '../types/models';
import { VectorClockEngine } from '../services/vectorClockEngine';
import {
  calculateSha256,
  calculateStorageBreakdown,
  determineCategory,
  determineMimeType,
} from '../services/cacheVaultManager';
import { GitHubSyncService } from '../services/gitHubSyncService';

interface SyncBeamContextType {
  files: SyncedFile[];
  filteredFiles: SyncedFile[];
  selectedCategory: FileCategory;
  searchQuery: string;
  sortOrder: FileSortOrder;
  pendingConflicts: SyncConflict[];
  resolvedConflicts: SyncConflict[];
  activeTransfers: TransferItem[];
  discoveredPeers: PeerDevice[];
  recentLogs: SyncEventLog[];
  isScanning: boolean;
  isServerListening: boolean;
  storageBreakdown: StorageBreakdown;
  selectedFileForPreview: SyncedFile | null;
  selectedConflictForReview: SyncConflict | null;
  currentTab: NavigationTab;
  isCreatingNoteDialog: boolean;
  isAddPeerDialog: boolean;
  isStorageSettingsDialog: boolean;
  isGitHubHubDialog: boolean;
  isApkDownloadDialog: boolean;
  isGitHubLoading: boolean;
  gitHubDownloadProgress: number;
  gitHubLatestRelease: GitHubReleaseInfo | null;
  gitHubRepoFiles: GitHubFileItem[];
  gitHubGistUrl: string | null;
  quotaLimitGb: number;
  autoSyncOnConnect: boolean;
  meshRelayEnabled: boolean;
  snackbarMessage: string | null;
  localDeviceId: string;
  localDeviceName: string;

  // Actions
  selectTab: (tab: NavigationTab) => void;
  selectCategory: (cat: FileCategory) => void;
  setSearchQuery: (q: string) => void;
  setSortOrder: (order: FileSortOrder) => void;
  openPreview: (file: SyncedFile) => void;
  closePreview: () => void;
  openConflictReview: (conflict: SyncConflict) => void;
  closeConflictReview: () => void;
  showCreateNoteDialog: (show: boolean) => void;
  showAddPeerDialog: (show: boolean) => void;
  showStorageSettingsDialog: (show: boolean) => void;
  showGitHubHubDialog: (show: boolean) => void;
  showApkDownloadDialog: (show: boolean) => void;
  setQuotaLimitGb: (gb: number) => void;
  toggleAutoSync: (enabled: boolean) => void;
  toggleMeshRelay: (enabled: boolean) => void;
  showSnackbar: (msg: string) => void;
  clearSnackbar: () => void;
  createNote: (title: string, content: string) => Promise<void>;
  updateNote: (fileId: string, newContent: string) => Promise<void>;
  importFileFromBlob: (file: File, categoryOverride?: FileCategory) => Promise<void>;
  syncFileWithPeer: (file: SyncedFile, peer: PeerDevice) => void;
  syncAllFilesWithPeer: (peer: PeerDevice) => void;
  simulateConflictTest: (file: SyncedFile, peer: PeerDevice) => void;
  resolveConflict: (
    conflictId: string,
    choice: ConflictResolutionChoice,
    mergedContent?: string | null
  ) => void;
  togglePin: (fileId: string, isPinned: boolean) => void;
  deleteFile: (fileId: string) => void;
  purgeCache: () => number;
  startBluetoothScan: () => void;
  stopBluetoothScan: () => void;
  addVirtualMeshPeer: (name: string, model: string) => void;
  togglePeerConnection: (peerId: string) => void;
  clearCompletedTransfers: () => void;
  exportAppApkToVault: (onSuccess?: (file: SyncedFile) => void) => Promise<void>;
  downloadFromGitHub: (
    urlOrPath: string,
    customName?: string | null,
    onComplete?: (success: boolean) => void
  ) => Promise<void>;
  fetchGitHubReleases: (owner: string, repo: string) => Promise<void>;
  fetchGitHubContents: (owner: string, repo: string, path?: string) => Promise<void>;
  exportFileToGist: (file: SyncedFile, isPublic: boolean, token?: string | null) => Promise<void>;
}

const LOCAL_DEVICE_ID = 'dev-node-' + Math.random().toString(36).substring(2, 8);
const LOCAL_DEVICE_NAME = 'My ZevSync Client';

const gitHubService = new GitHubSyncService();
const vectorClockEngine = new VectorClockEngine(LOCAL_DEVICE_ID);

const SyncBeamContext = createContext<SyncBeamContextType | null>(null);

export const SyncBeamProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [currentTab, setCurrentTab] = useState<NavigationTab>('VAULT');
  const [selectedCategory, setSelectedCategory] = useState<FileCategory>('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [sortOrder, setSortOrder] = useState<FileSortOrder>('DATE_DESC');
  const [quotaLimitGb, setQuotaLimitGb] = useState(2.0);
  const [autoSyncOnConnect, setAutoSyncOnConnect] = useState(true);
  const [meshRelayEnabled, setMeshRelayEnabled] = useState(true);
  const [isScanning, setIsScanning] = useState(false);
  const [isServerListening, setIsServerListening] = useState(true);
  const [snackbarMessage, setSnackbarMessage] = useState<string | null>(null);

  // Dialogs
  const [selectedFileForPreview, setSelectedFileForPreview] = useState<SyncedFile | null>(null);
  const [selectedConflictForReview, setSelectedConflictForReview] = useState<SyncConflict | null>(null);
  const [isCreatingNoteDialog, setIsCreatingNoteDialog] = useState(false);
  const [isAddPeerDialog, setIsAddPeerDialog] = useState(false);
  const [isStorageSettingsDialog, setIsStorageSettingsDialog] = useState(false);
  const [isGitHubHubDialog, setIsGitHubHubDialog] = useState(false);
  const [isApkDownloadDialog, setIsApkDownloadDialog] = useState(false);

  // GitHub state
  const [isGitHubLoading, setIsGitHubLoading] = useState(false);
  const [gitHubDownloadProgress, setGitHubDownloadProgress] = useState(0);
  const [gitHubLatestRelease, setGitHubLatestRelease] = useState<GitHubReleaseInfo | null>(null);
  const [gitHubRepoFiles, setGitHubRepoFiles] = useState<GitHubFileItem[]>([]);
  const [gitHubGistUrl, setGitHubGistUrl] = useState<string | null>(null);

  // Core Data
  const [files, setFiles] = useState<SyncedFile[]>(() => {
    // Initial Seed Data matching Android Demo Repository
    const guideContent = `# 🚀 SyncBeam Offline Bluetooth Mesh

Welcome to SyncBeam! This application creates an autonomous offline peer-to-peer sharing and cache environment without needing any internet connection, cellular data, or central servers.

## Key Features:
- **Automatic Bluetooth Handshake**: Automatically detects nearby paired and discoverable Bluetooth devices.
- **Vector Clock & Lamport Lineage**: Seamless multi-device causality tracking.
- **Conflict Management Engine**: Intelligent LWW, side-by-side interactive merging, and branch-forking.
- **Chunked Content-Addressable Storage**: SHA-256 integrity checks with resumable block transfers.
- **Smart Cache & Quota Controller**: Configurable storage thresholds with LRU auto-eviction and pin protection.`;

    const configContent = `{
  "mesh_name": "SyncBeam-Local-Mesh",
  "rfcomm_channel": 1,
  "protocol_version": 2,
  "chunk_size_kb": 64,
  "auto_sync_on_connect": true,
  "conflict_policy": "PROMPT_INTERACTIVE",
  "cache_quota_gb": 2.0,
  "eviction_strategy": "LRU_UNPINNED"
}`;

    const resolverContent = `package sync.mesh

// Vector Clock Conflict Resolver
fun resolveLineage(local: Map<String, Long>, remote: Map<String, Long>): ConflictResult {
    val lGreater = local.any { (k, v) -> v > (remote[k] ?: 0L) }
    val rGreater = remote.any { (k, v) -> v > (local[k] ?: 0L) }
    return when {
        lGreater && !rGreater -> ConflictResult.LOCAL_DOMINATES
        !lGreater && rGreater -> ConflictResult.REMOTE_DOMINATES
        !lGreater && !rGreater -> ConflictResult.EQUAL
        else -> ConflictResult.CONCURRENT_CONFLICT
    }
}`;

    const now = Date.now();
    return [
      {
        id: 'f1-guide',
        name: 'Mesh_Architecture_Guide.md',
        relativePath: '',
        sizeBytes: 1250,
        mimeType: 'text/markdown',
        contentHash: 'a7b3c901e4f5d6',
        versionNumber: 1,
        originDeviceId: LOCAL_DEVICE_ID,
        originDeviceName: LOCAL_DEVICE_NAME,
        lastModifiedTimestamp: now - 3600000,
        lamportTimestamp: 1,
        vectorClockJson: JSON.stringify({ [LOCAL_DEVICE_ID]: 1 }),
        syncStatus: 'CONFLICT',
        isPinned: true,
        category: 'DOCUMENT',
        textPreview: guideContent,
        contentData: guideContent,
        isDeleted: false,
      },
      {
        id: 'f2-config',
        name: 'mesh_config.json',
        relativePath: '',
        sizeBytes: 310,
        mimeType: 'application/json',
        contentHash: 'f4e8d2a1b9c3e0',
        versionNumber: 2,
        originDeviceId: LOCAL_DEVICE_ID,
        originDeviceName: LOCAL_DEVICE_NAME,
        lastModifiedTimestamp: now - 7200000,
        lamportTimestamp: 2,
        vectorClockJson: JSON.stringify({ [LOCAL_DEVICE_ID]: 2 }),
        syncStatus: 'SYNCED',
        isPinned: false,
        category: 'CODE',
        textPreview: configContent,
        contentData: configContent,
        isDeleted: false,
      },
      {
        id: 'f3-resolver',
        name: 'ConflictResolver.kt',
        relativePath: '',
        sizeBytes: 620,
        mimeType: 'text/x-kotlin',
        contentHash: 'd3c2b1a0e9f8d7',
        versionNumber: 1,
        originDeviceId: LOCAL_DEVICE_ID,
        originDeviceName: LOCAL_DEVICE_NAME,
        lastModifiedTimestamp: now - 1800000,
        lamportTimestamp: 3,
        vectorClockJson: JSON.stringify({ [LOCAL_DEVICE_ID]: 1 }),
        syncStatus: 'LOCAL_ONLY',
        isPinned: false,
        category: 'CODE',
        textPreview: resolverContent,
        contentData: resolverContent,
        isDeleted: false,
      },
    ];
  });

  const [discoveredPeers, setDiscoveredPeers] = useState<PeerDevice[]>([
    {
      id: 'node-pixel9',
      bluetoothAddress: '78:BD:BC:54:11:02',
      name: 'Pixel 9 Pro (Living Room)',
      isConnected: true,
      isPaired: true,
      rssi: -45,
      lastSeenTimestamp: Date.now(),
      totalBytesSent: 2048500,
      totalBytesReceived: 1450200,
      activeTransfers: 0,
      isSyncAllowed: true,
      isVirtualNode: false,
      meshHopCount: 1,
      deviceModel: 'Google Pixel 9 Pro',
      protocolVersion: 2,
    },
    {
      id: 'node-tabs9',
      bluetoothAddress: '9C:20:7B:A8:44:91',
      name: 'Galaxy Tab S9 (Studio)',
      isConnected: true,
      isPaired: true,
      rssi: -62,
      lastSeenTimestamp: Date.now() - 30000,
      totalBytesSent: 850000,
      totalBytesReceived: 420000,
      activeTransfers: 0,
      isSyncAllowed: true,
      isVirtualNode: false,
      meshHopCount: 1,
      deviceModel: 'Samsung Galaxy Tab S9',
      protocolVersion: 2,
    },
  ]);

  const [activeTransfers, setActiveTransfers] = useState<TransferItem[]>([]);

  const [pendingConflicts, setPendingConflicts] = useState<SyncConflict[]>([
    {
      id: 'conflict-init-1',
      fileId: 'f1-guide',
      fileName: 'Mesh_Architecture_Guide.md',
      localVersion: 1,
      remoteVersion: 2,
      localHash: 'a7b3c901e4f5d6',
      remoteHash: 'b8c4d012f5e7a9',
      localTimestamp: Date.now() - 3600000,
      remoteTimestamp: Date.now() - 600000,
      localDeviceId: LOCAL_DEVICE_ID,
      remoteDeviceId: 'node-pixel9',
      remoteDeviceName: 'Pixel 9 Pro (Living Room)',
      localSizeBytes: 1250,
      remoteSizeBytes: 1420,
      localLamport: 1,
      remoteLamport: 2,
      localVectorClock: JSON.stringify({ [LOCAL_DEVICE_ID]: 1 }),
      remoteVectorClock: JSON.stringify({ 'node-pixel9': 2 }),
      localTextSnippet: `# 🚀 SyncBeam Offline Bluetooth Mesh
Welcome to SyncBeam! This application creates an autonomous offline peer-to-peer sharing and cache environment without needing any internet connection.`,
      remoteTextSnippet: `# 🚀 SyncBeam Offline Bluetooth Mesh (Pixel 9 Updated)
Welcome to SyncBeam! Enhanced with multi-hop Bluetooth forwarding, chunk encryption, and live battery-aware sync thresholds.`,
      status: 'PENDING',
    },
  ]);

  const [resolvedConflicts, setResolvedConflicts] = useState<SyncConflict[]>([]);

  const [recentLogs, setRecentLogs] = useState<SyncEventLog[]>([
    {
      id: 1,
      timestamp: Date.now() - 10000,
      eventType: 'SERVER',
      title: 'Bluetooth Sync Listener Active',
      description: 'Listening on RFCOMM UUID fa87c0d0-afac-11de-8a39-0800200c9a66',
      isPositive: true,
    },
    {
      id: 2,
      timestamp: Date.now() - 8000,
      eventType: 'CONNECT',
      title: 'Mesh Peer Connected',
      description: 'Pixel 9 Pro connected via Bluetooth channel 1',
      peerName: 'Pixel 9 Pro (Living Room)',
      isPositive: true,
    },
    {
      id: 3,
      timestamp: Date.now() - 5000,
      eventType: 'CONFLICT_DETECTED',
      title: 'Sync Conflict on Mesh_Architecture_Guide.md',
      description: 'Concurrent edits from Pixel 9 Pro vs local device detected',
      peerName: 'Pixel 9 Pro (Living Room)',
      isPositive: false,
    },
  ]);

  const logEvent = (
    type: string,
    title: string,
    description: string,
    peerName?: string | null,
    isPositive = true
  ) => {
    const newLog: SyncEventLog = {
      id: Date.now() + Math.random(),
      timestamp: Date.now(),
      eventType: type,
      title,
      description,
      peerName,
      isPositive,
    };
    setRecentLogs((prev) => [newLog, ...prev.slice(0, 50)]);
  };

  const showSnackbar = (msg: string) => {
    setSnackbarMessage(msg);
  };

  const clearSnackbar = () => {
    setSnackbarMessage(null);
  };

  // Filter and sort files
  const filteredFiles = useMemo(() => {
    return files
      .filter((file) => {
        if (file.isDeleted) return false;
        const matchesCategory =
          selectedCategory === 'ALL' || file.category === selectedCategory;
        const query = searchQuery.trim().toLowerCase();
        const matchesQuery =
          query === '' ||
          file.name.toLowerCase().includes(query) ||
          (file.textPreview && file.textPreview.toLowerCase().includes(query));
        return matchesCategory && matchesQuery;
      })
      .sort((a, b) => {
        switch (sortOrder) {
          case 'DATE_DESC':
            return b.lastModifiedTimestamp - a.lastModifiedTimestamp;
          case 'DATE_ASC':
            return a.lastModifiedTimestamp - b.lastModifiedTimestamp;
          case 'NAME_ASC':
            return a.name.localeCompare(b.name);
          case 'SIZE_DESC':
            return b.sizeBytes - a.sizeBytes;
        }
      });
  }, [files, selectedCategory, searchQuery, sortOrder]);

  const storageBreakdown = useMemo(() => {
    const quotaBytes = quotaLimitGb * 1024 * 1024 * 1024;
    const base = calculateStorageBreakdown(files, quotaBytes);
    return {
      ...base,
      conflictCount: pendingConflicts.length,
    };
  }, [files, quotaLimitGb, pendingConflicts.length]);

  // Actions
  const createNote = async (title: string, content: string) => {
    const fileName = title.endsWith('.md') || title.endsWith('.txt') ? title : `${title}.md`;
    const hash = await calculateSha256(content);
    const size = new TextEncoder().encode(content).length;
    const [clock, ver] = vectorClockEngine.incrementLocal('{}');

    const newFile: SyncedFile = {
      id: hash,
      name: fileName,
      relativePath: '',
      sizeBytes: size,
      mimeType: 'text/markdown',
      contentHash: hash,
      versionNumber: ver,
      originDeviceId: LOCAL_DEVICE_ID,
      originDeviceName: LOCAL_DEVICE_NAME,
      lastModifiedTimestamp: Date.now(),
      lamportTimestamp: 1,
      vectorClockJson: clock,
      syncStatus: 'LOCAL_ONLY',
      isPinned: false,
      category: 'DOCUMENT',
      textPreview: content.slice(0, 1000),
      contentData: content,
      isDeleted: false,
    };

    setFiles((prev) => [newFile, ...prev]);
    setIsCreatingNoteDialog(false);
    logEvent('NOTE_CREATED', 'Offline Note Created', `${fileName} (${size} bytes) saved to cache`);
    showSnackbar(`Created note '${fileName}'`);
  };

  const updateNote = async (fileId: string, newContent: string) => {
    const existing = files.find((f) => f.id === fileId);
    if (!existing) return;

    const hash = await calculateSha256(newContent);
    const size = new TextEncoder().encode(newContent).length;
    const [clock, ver] = vectorClockEngine.incrementLocal(existing.vectorClockJson);
    const nextLamport = vectorClockEngine.nextLamport(existing.lamportTimestamp);

    const updated: SyncedFile = {
      ...existing,
      id: hash,
      contentHash: hash,
      sizeBytes: size,
      versionNumber: ver,
      lastModifiedTimestamp: Date.now(),
      lamportTimestamp: nextLamport,
      vectorClockJson: clock,
      syncStatus: 'LOCAL_ONLY',
      textPreview: newContent.slice(0, 1000),
      contentData: newContent,
    };

    setFiles((prev) => prev.map((f) => (f.id === fileId ? updated : f)));
    setSelectedFileForPreview(null);
    logEvent(
      'FILE_UPDATED',
      'Document Modified',
      `${existing.name} edited locally · Vector clock updated`
    );
    showSnackbar(`Saved changes to '${existing.name}' & updated vector clock`);
  };

  const importFileFromBlob = async (file: File, categoryOverride?: FileCategory) => {
    const arrayBuffer = await file.arrayBuffer();
    const bytes = new Uint8Array(arrayBuffer);
    const hash = await calculateSha256(bytes);
    const [clock, ver] = vectorClockEngine.incrementLocal('{}');
    const category = categoryOverride || determineCategory(file.name, file.type);

    let textPreview: string | null = null;
    let contentData: string | undefined = undefined;

    if (file.type.startsWith('text/') || file.name.match(/\.(txt|md|json|js|ts|tsx|html|css|py|kt)$/i)) {
      try {
        const text = new TextDecoder().decode(bytes);
        textPreview = text.slice(0, 1000);
        contentData = text;
      } catch {
        textPreview = null;
      }
    }

    const newFile: SyncedFile = {
      id: hash,
      name: file.name,
      relativePath: '',
      sizeBytes: file.size,
      mimeType: file.type || determineMimeType(file.name),
      contentHash: hash,
      versionNumber: ver,
      originDeviceId: LOCAL_DEVICE_ID,
      originDeviceName: LOCAL_DEVICE_NAME,
      lastModifiedTimestamp: Date.now(),
      lamportTimestamp: 1,
      vectorClockJson: clock,
      syncStatus: 'LOCAL_ONLY',
      isPinned: false,
      category,
      textPreview,
      contentData,
      isDeleted: false,
    };

    setFiles((prev) => [newFile, ...prev]);
    logEvent('FILE_ADDED', 'File Imported to Vault', `${file.name} added to local offline vault`);
    showSnackbar(`Imported '${file.name}' into offline vault`);
  };

  const syncFileWithPeer = (file: SyncedFile, peer: PeerDevice) => {
    const transferId = 'xfer-' + Math.random().toString(36).substring(2, 9);
    const initialItem: TransferItem = {
      id: transferId,
      fileId: file.id,
      fileName: file.name,
      direction: 'UPLOAD',
      peerId: peer.id,
      peerName: peer.name,
      totalBytes: file.sizeBytes,
      bytesTransferred: 0,
      speedBytesPerSec: 0,
      status: 'TRANSFERRING',
      progress: 0,
      startedAt: Date.now(),
    };

    setActiveTransfers((prev) => [initialItem, ...prev]);
    logEvent(
      'TRANSFER_START',
      'Starting Upload Transfer',
      `${file.name} with ${peer.name}`,
      peer.name
    );
    showSnackbar(`Syncing '${file.name}' with ${peer.name}...`);

    // Simulate multi-chunk streaming with progress updates
    const totalBytes = file.sizeBytes;
    const stepSize = Math.max(Math.floor(totalBytes / 10), 1024);
    let transferred = 0;
    const startTime = Date.now();

    const interval = setInterval(() => {
      transferred = Math.min(transferred + stepSize, totalBytes);
      const elapsed = Math.max((Date.now() - startTime) / 1000, 0.1);
      const speed = Math.floor(transferred / elapsed);
      const progress = transferred / totalBytes;

      setActiveTransfers((prev) =>
        prev.map((item) =>
          item.id === transferId
            ? {
                ...item,
                bytesTransferred: transferred,
                speedBytesPerSec: speed,
                progress,
                status: transferred >= totalBytes ? 'VERIFYING' : 'TRANSFERRING',
              }
            : item
        )
      );

      if (transferred >= totalBytes) {
        clearInterval(interval);
        setTimeout(() => {
          setActiveTransfers((prev) =>
            prev.map((item) =>
              item.id === transferId
                ? {
                    ...item,
                    status: 'COMPLETED',
                    progress: 1,
                    finishedAt: Date.now(),
                  }
                : item
            )
          );

          // Update file sync status
          setFiles((prev) =>
            prev.map((f) =>
              f.id === file.id
                ? { ...f, syncStatus: 'SYNCED', lastSyncPeerName: peer.name }
                : f
            )
          );

          // Update peer sent bytes
          setDiscoveredPeers((prev) =>
            prev.map((p) =>
              p.id === peer.id ? { ...p, totalBytesSent: p.totalBytesSent + totalBytes } : p
            )
          );

          logEvent(
            'TRANSFER_SUCCESS',
            `Sync Complete: ${file.name}`,
            `Successfully verified SHA-256 match with ${peer.name}`,
            peer.name
          );
          showSnackbar(`'${file.name}' synced with ${peer.name}`);
        }, 350);
      }
    }, 180);
  };

  const syncAllFilesWithPeer = (peer: PeerDevice) => {
    const unSynced = files.filter((f) => !f.isDeleted);
    if (unSynced.length === 0) {
      showSnackbar('No files in vault to sync');
      return;
    }
    showSnackbar(`Queuing sync for ${unSynced.length} file(s) with ${peer.name}...`);
    unSynced.forEach((file, index) => {
      setTimeout(() => {
        syncFileWithPeer(file, peer);
      }, index * 400);
    });
  };

  const simulateConflictTest = (file: SyncedFile, peer: PeerDevice) => {
    const remoteVersion = file.versionNumber + 1;
    const remoteClock = JSON.stringify({
      ...vectorClockEngine.parseClock(file.vectorClockJson),
      [peer.id]: remoteVersion,
    });

    const conflict: SyncConflict = {
      id: 'conflict-' + Math.random().toString(36).substring(2, 8),
      fileId: file.id,
      fileName: file.name,
      localVersion: file.versionNumber,
      remoteVersion,
      localHash: file.contentHash,
      remoteHash: 'rem-' + Math.random().toString(36).substring(2, 10),
      localTimestamp: file.lastModifiedTimestamp,
      remoteTimestamp: Date.now(),
      localDeviceId: LOCAL_DEVICE_ID,
      remoteDeviceId: peer.id,
      remoteDeviceName: peer.name,
      localSizeBytes: file.sizeBytes,
      remoteSizeBytes: file.sizeBytes + 150,
      localLamport: file.lamportTimestamp,
      remoteLamport: file.lamportTimestamp + 2,
      localVectorClock: file.vectorClockJson,
      remoteVectorClock: remoteClock,
      localTextSnippet: file.textPreview || file.contentData || 'Local document content',
      remoteTextSnippet: `// Branch from ${peer.name} [v${remoteVersion}]\n` + (file.textPreview || 'Remote changes'),
      status: 'PENDING',
    };

    setPendingConflicts((prev) => [conflict, ...prev]);
    setFiles((prev) =>
      prev.map((f) => (f.id === file.id ? { ...f, syncStatus: 'CONFLICT' } : f))
    );

    logEvent(
      'CONFLICT_DETECTED',
      `Sync Conflict on ${file.name}`,
      `Concurrent branch from ${peer.name} detected`,
      peer.name,
      false
    );
    showSnackbar(`Simulated concurrent edit conflict from ${peer.name}`);
  };

  const resolveConflict = (
    conflictId: string,
    choice: ConflictResolutionChoice,
    mergedContent?: string | null
  ) => {
    const conflict = pendingConflicts.find((c) => c.id === conflictId);
    if (!conflict) return;

    const existingFile = files.find((f) => f.id === conflict.fileId);

    if (choice === 'KEEP_LOCAL_WINNER') {
      const [mergedClock] = vectorClockEngine.incrementLocal(
        vectorClockEngine.merge(conflict.localVectorClock, conflict.remoteVectorClock)
      );
      if (existingFile) {
        setFiles((prev) =>
          prev.map((f) =>
            f.id === existingFile.id
              ? {
                  ...f,
                  syncStatus: 'SYNCED',
                  vectorClockJson: mergedClock,
                  lastModifiedTimestamp: Date.now(),
                }
              : f
          )
        );
      }
      logEvent('CONFLICT_RESOLVED', 'Conflict Resolved: Local Winner', `${conflict.fileName} local copy set as winner`, conflict.remoteDeviceName);
      showSnackbar('Resolved: Local copy set as authoritative winner');
    } else if (choice === 'KEEP_REMOTE_WINNER') {
      const mergedClock = vectorClockEngine.merge(conflict.localVectorClock, conflict.remoteVectorClock);
      if (existingFile) {
        setFiles((prev) =>
          prev.map((f) =>
            f.id === existingFile.id
              ? {
                  ...f,
                  syncStatus: 'SYNCED',
                  versionNumber: conflict.remoteVersion,
                  vectorClockJson: mergedClock,
                  lastModifiedTimestamp: conflict.remoteTimestamp,
                  textPreview: conflict.remoteTextSnippet || f.textPreview,
                  contentData: conflict.remoteTextSnippet || f.contentData,
                }
              : f
          )
        );
      }
      logEvent('CONFLICT_RESOLVED', 'Conflict Resolved: Remote Adopted', `${conflict.fileName} replaced with ${conflict.remoteDeviceName}'s copy`, conflict.remoteDeviceName);
      showSnackbar(`Resolved: Adopted remote copy from ${conflict.remoteDeviceName}`);
    } else if (choice === 'KEEP_BOTH') {
      const ext = conflict.fileName.includes('.') ? conflict.fileName.split('.').pop() : 'txt';
      const base = conflict.fileName.includes('.')
        ? conflict.fileName.substring(0, conflict.fileName.lastIndexOf('.'))
        : conflict.fileName;
      const branchName = `${base}_(From_${conflict.remoteDeviceName.replace(/\s+/g, '_')}).${ext}`;

      const branchFile: SyncedFile = {
        id: 'branch-' + Math.random().toString(36).substring(2, 9),
        name: branchName,
        relativePath: '',
        sizeBytes: conflict.remoteSizeBytes,
        mimeType: determineMimeType(branchName),
        contentHash: conflict.remoteHash,
        versionNumber: conflict.remoteVersion,
        originDeviceId: conflict.remoteDeviceId,
        originDeviceName: conflict.remoteDeviceName,
        lastModifiedTimestamp: conflict.remoteTimestamp,
        lamportTimestamp: conflict.remoteLamport,
        vectorClockJson: conflict.remoteVectorClock,
        syncStatus: 'SYNCED',
        isPinned: false,
        category: determineCategory(branchName),
        textPreview: conflict.remoteTextSnippet || null,
        contentData: conflict.remoteTextSnippet || undefined,
        isDeleted: false,
      };

      if (existingFile) {
        setFiles((prev) => [
          branchFile,
          ...prev.map((f) => (f.id === existingFile.id ? { ...f, syncStatus: 'SYNCED' as const } : f)),
        ]);
      }
      logEvent('CONFLICT_RESOLVED', 'Conflict Resolved: Forked Branch', `Created branch copy '${branchName}'`, conflict.remoteDeviceName);
      showSnackbar(`Resolved: Created duplicate branch copy '${branchName}'`);
    } else if (choice === 'MERGE_CUSTOM') {
      const mergedText = mergedContent || conflict.localTextSnippet || '';
      const [mergedClock, newVer] = vectorClockEngine.incrementLocal(
        vectorClockEngine.merge(conflict.localVectorClock, conflict.remoteVectorClock)
      );

      if (existingFile) {
        setFiles((prev) =>
          prev.map((f) =>
            f.id === existingFile.id
              ? {
                  ...f,
                  syncStatus: 'SYNCED' as const,
                  versionNumber: newVer,
                  vectorClockJson: mergedClock,
                  lastModifiedTimestamp: Date.now(),
                  textPreview: mergedText.slice(0, 1000),
                  contentData: mergedText,
                }
              : f
          )
        );
      }
      logEvent('CONFLICT_RESOLVED', 'Conflict Resolved: Interactive Merge', `${conflict.fileName} merged changes`, conflict.remoteDeviceName);
      showSnackbar('Resolved: Merged changes successfully');
    }

    const resolvedItem: SyncConflict = {
      ...conflict,
      status:
        choice === 'KEEP_LOCAL_WINNER'
          ? 'RESOLVED_LOCAL'
          : choice === 'KEEP_REMOTE_WINNER'
          ? 'RESOLVED_REMOTE'
          : choice === 'KEEP_BOTH'
          ? 'RESOLVED_KEEP_BOTH'
          : 'RESOLVED_MERGED',
      resolutionTimestamp: Date.now(),
      resolutionNote: `Resolved via ${choice}`,
    };

    setPendingConflicts((prev) => prev.filter((c) => c.id !== conflictId));
    setResolvedConflicts((prev) => [resolvedItem, ...prev]);
    setSelectedConflictForReview(null);
  };

  const togglePin = (fileId: string, isPinned: boolean) => {
    setFiles((prev) =>
      prev.map((f) => (f.id === fileId ? { ...f, isPinned } : f))
    );
    const target = files.find((f) => f.id === fileId);
    logEvent('CACHE_PIN', isPinned ? 'File Pinned' : 'File Unpinned', `${target?.name || fileId} ${isPinned ? 'protected from LRU eviction' : 'eligible for eviction'}`);
    showSnackbar(isPinned ? 'File pinned in offline cache' : 'File unpinned');
  };

  const deleteFile = (fileId: string) => {
    const target = files.find((f) => f.id === fileId);
    setFiles((prev) => prev.filter((f) => f.id !== fileId));
    logEvent('FILE_DELETED', 'File Removed', `${target?.name || fileId} purged from vault`);
    showSnackbar('File removed from cache vault');
  };

  const purgeCache = () => {
    const unpinned = files.filter((f) => !f.isPinned);
    setFiles((prev) => prev.filter((f) => f.isPinned));
    logEvent('CACHE_PURGED', 'Cache Storage Purged', `Evicted ${unpinned.length} unpinned files`);
    showSnackbar(`Purged ${unpinned.length} unpinned files from cache`);
    return unpinned.length;
  };

  const startBluetoothScan = () => {
    setIsScanning(true);
    logEvent('SCAN', 'Bluetooth Scan Started', 'Searching for nearby Bluetooth RFCOMM mesh nodes...');
    showSnackbar('Searching for nearby Bluetooth sync nodes...');

    setTimeout(() => {
      // Simulate finding a new peer if not already present
      setDiscoveredPeers((prev) => {
        if (prev.some((p) => p.name.includes('OnePlus 12'))) return prev;
        const newPeer: PeerDevice = {
          id: 'node-oneplus-' + Math.random().toString(36).substring(2, 6),
          bluetoothAddress: 'AA:BB:CC:DD:EE:12',
          name: 'OnePlus 12 (Field Unit)',
          isConnected: false,
          isPaired: false,
          rssi: -58,
          lastSeenTimestamp: Date.now(),
          totalBytesSent: 0,
          totalBytesReceived: 0,
          activeTransfers: 0,
          isSyncAllowed: true,
          isVirtualNode: false,
          meshHopCount: 1,
          deviceModel: 'OnePlus 12 5G',
          protocolVersion: 2,
        };
        logEvent('DISCOVERY', 'Discovered Bluetooth Peer', `${newPeer.name} (RSSI: -58 dBm)`, newPeer.name);
        return [...prev, newPeer];
      });
      setIsScanning(false);
    }, 2800);
  };

  const stopBluetoothScan = () => {
    setIsScanning(false);
  };

  const addVirtualMeshPeer = (name: string, model: string) => {
    const id = 'node-sim-' + Math.random().toString(36).substring(2, 8);
    const peer: PeerDevice = {
      id,
      bluetoothAddress: 'VI:RT:UA:L' + Math.floor(10 + Math.random() * 89) + ':' + Math.floor(10 + Math.random() * 89),
      name: name.trim() || 'Virtual Peer',
      isConnected: true,
      isPaired: true,
      rssi: -42,
      lastSeenTimestamp: Date.now(),
      totalBytesSent: 0,
      totalBytesReceived: 0,
      activeTransfers: 0,
      isSyncAllowed: true,
      isVirtualNode: true,
      meshHopCount: 1,
      deviceModel: model.trim() || 'Virtual Mesh Node',
      protocolVersion: 2,
    };

    setDiscoveredPeers((prev) => [peer, ...prev]);
    setIsAddPeerDialog(false);
    logEvent('CONNECT', 'Mesh Peer Joined', `${peer.name} connected via Bluetooth RFCOMM channel`, peer.name);
    showSnackbar(`Added virtual Bluetooth peer '${peer.name}'`);
  };

  const togglePeerConnection = (peerId: string) => {
    setDiscoveredPeers((prev) =>
      prev.map((p) => {
        if (p.id === peerId) {
          const nextState = !p.isConnected;
          logEvent(
            nextState ? 'CONNECT' : 'DISCONNECT',
            nextState ? 'Peer Connected' : 'Peer Disconnected',
            `${p.name} is now ${nextState ? 'online' : 'offline'}`,
            p.name,
            nextState
          );
          showSnackbar(`${p.name} is now ${nextState ? 'connected' : 'disconnected'}`);
          return { ...p, isConnected: nextState };
        }
        return p;
      })
    );
  };

  const clearCompletedTransfers = () => {
    setActiveTransfers((prev) =>
      prev.filter((t) => t.status !== 'COMPLETED' && t.status !== 'FAILED')
    );
    showSnackbar('Cleared completed transfers');
  };

  const exportAppApkToVault = async (onSuccess?: (file: SyncedFile) => void) => {
    const apkFileName = 'ZevSync_v1.0_OfflineMesh.apk';
    const apkDummyContent = 'PK\x03\x04ZevSync-Android-Standalone-Release-Package-Payload-Signature-OfflineMesh';
    const hash = await calculateSha256(apkDummyContent);
    const [clock, ver] = vectorClockEngine.incrementLocal('{}');

    const apkFile: SyncedFile = {
      id: hash,
      name: apkFileName,
      relativePath: '',
      sizeBytes: 15420000, // ~14.7 MB APK size
      mimeType: 'application/vnd.android.package-archive',
      contentHash: hash,
      versionNumber: ver,
      originDeviceId: LOCAL_DEVICE_ID,
      originDeviceName: LOCAL_DEVICE_NAME,
      lastModifiedTimestamp: Date.now(),
      lamportTimestamp: 1,
      vectorClockJson: clock,
      syncStatus: 'LOCAL_ONLY',
      isPinned: true,
      category: 'ARCHIVE',
      textPreview: 'Binary Android Application Package (.apk) with offline manifest & Bluetooth RFCOMM capabilities.',
      contentData: apkDummyContent,
      isDeleted: false,
    };

    setFiles((prev) => [apkFile, ...prev]);
    logEvent('APK_EXTRACTED', 'ZevSync APK Extracted', 'Ready for direct Bluetooth sideloading to other devices');
    showSnackbar('Extracted ZevSync APK (14.7 MB) into Vault!');
    onSuccess?.(apkFile);
  };

  const downloadFromGitHub = async (
    urlOrPath: string,
    customName?: string | null,
    onComplete?: (success: boolean) => void
  ) => {
    if (!urlOrPath.trim()) {
      showSnackbar('Please enter a GitHub URL or file path');
      return;
    }

    setIsGitHubLoading(true);
    setGitHubDownloadProgress(0.1);

    try {
      const result = await gitHubService.downloadDirectFile(
        urlOrPath.trim(),
        customName,
        (p) => setGitHubDownloadProgress(p)
      );

      setIsGitHubLoading(false);
      setGitHubDownloadProgress(1.0);

      if (result.success && result.textContent) {
        const hash = await calculateSha256(result.textContent);
        const [clock, ver] = vectorClockEngine.incrementLocal('{}');
        const category = determineCategory(result.fileName);

        const newFile: SyncedFile = {
          id: hash,
          name: result.fileName,
          relativePath: '',
          sizeBytes: result.bytesDownloaded,
          mimeType: determineMimeType(result.fileName),
          contentHash: hash,
          versionNumber: ver,
          originDeviceId: LOCAL_DEVICE_ID,
          originDeviceName: LOCAL_DEVICE_NAME,
          lastModifiedTimestamp: Date.now(),
          lamportTimestamp: 1,
          vectorClockJson: clock,
          syncStatus: 'LOCAL_ONLY',
          isPinned: false,
          category,
          textPreview: result.textContent.slice(0, 1000),
          contentData: result.textContent,
          isDeleted: false,
        };

        setFiles((prev) => [newFile, ...prev]);
        logEvent('GITHUB_DOWNLOAD', 'Downloaded from GitHub', `${result.fileName} stored in vault`);
        showSnackbar(`Downloaded '${result.fileName}' into Offline Vault!`);
        onComplete?.(true);
      } else {
        showSnackbar(`Download failed: ${result.errorMessage || 'Could not fetch file'}`);
        onComplete?.(false);
      }
    } catch (err: unknown) {
      setIsGitHubLoading(false);
      const msg = err instanceof Error ? err.message : 'Unknown download error';
      showSnackbar(`Error downloading from GitHub: ${msg}`);
      onComplete?.(false);
    }
  };

  const fetchGitHubReleases = async (owner: string, repo: string) => {
    setIsGitHubLoading(true);
    try {
      const release = await gitHubService.fetchReleases(owner.trim(), repo.trim());
      setGitHubLatestRelease(release);
      setIsGitHubLoading(false);
      showSnackbar(`Loaded latest release for ${owner}/${repo}: ${release.tagName}`);
    } catch (err: unknown) {
      setIsGitHubLoading(false);
      const msg = err instanceof Error ? err.message : 'Could not fetch releases';
      showSnackbar(`Error fetching releases: ${msg}`);
    }
  };

  const fetchGitHubContents = async (owner: string, repo: string, path = '') => {
    setIsGitHubLoading(true);
    try {
      const items = await gitHubService.fetchContents(owner.trim(), repo.trim(), path.trim());
      setGitHubRepoFiles(items);
      setIsGitHubLoading(false);
      showSnackbar(`Loaded ${items.length} files from ${owner}/${repo}`);
    } catch (err: unknown) {
      setIsGitHubLoading(false);
      const msg = err instanceof Error ? err.message : 'Could not fetch repository';
      showSnackbar(`Error fetching repository: ${msg}`);
    }
  };

  const exportFileToGist = async (file: SyncedFile, isPublic: boolean, token?: string | null) => {
    setIsGitHubLoading(true);
    try {
      const gistUrl = await gitHubService.exportGist(file, isPublic, token);
      setGitHubGistUrl(gistUrl);
      setIsGitHubLoading(false);
      logEvent('GIST_EXPORT', 'Gist Created', gistUrl);
      showSnackbar(`Gist created: ${gistUrl}`);
    } catch (err: unknown) {
      setIsGitHubLoading(false);
      const msg = err instanceof Error ? err.message : 'Gist export failed';
      showSnackbar(`Gist export failed: ${msg}`);
    }
  };

  return (
    <SyncBeamContext.Provider
      value={{
        files,
        filteredFiles,
        selectedCategory,
        searchQuery,
        sortOrder,
        pendingConflicts,
        resolvedConflicts,
        activeTransfers,
        discoveredPeers,
        recentLogs,
        isScanning,
        isServerListening,
        storageBreakdown,
        selectedFileForPreview,
        selectedConflictForReview,
        currentTab,
        isCreatingNoteDialog,
        isAddPeerDialog,
        isStorageSettingsDialog,
        isGitHubHubDialog,
        isApkDownloadDialog,
        isGitHubLoading,
        gitHubDownloadProgress,
        gitHubLatestRelease,
        gitHubRepoFiles,
        gitHubGistUrl,
        quotaLimitGb,
        autoSyncOnConnect,
        meshRelayEnabled,
        snackbarMessage,
        localDeviceId: LOCAL_DEVICE_ID,
        localDeviceName: LOCAL_DEVICE_NAME,

        selectTab: setCurrentTab,
        selectCategory: setSelectedCategory,
        setSearchQuery,
        setSortOrder,
        openPreview: setSelectedFileForPreview,
        closePreview: () => setSelectedFileForPreview(null),
        openConflictReview: setSelectedConflictForReview,
        closeConflictReview: () => setSelectedConflictForReview(null),
        showCreateNoteDialog: setIsCreatingNoteDialog,
        showAddPeerDialog: setIsAddPeerDialog,
        showStorageSettingsDialog: setIsStorageSettingsDialog,
        showGitHubHubDialog: setIsGitHubHubDialog,
        showApkDownloadDialog: setIsApkDownloadDialog,
        setQuotaLimitGb,
        toggleAutoSync: setAutoSyncOnConnect,
        toggleMeshRelay: setMeshRelayEnabled,
        showSnackbar,
        clearSnackbar,
        createNote,
        updateNote,
        importFileFromBlob,
        syncFileWithPeer,
        syncAllFilesWithPeer,
        simulateConflictTest,
        resolveConflict,
        togglePin,
        deleteFile,
        purgeCache,
        startBluetoothScan,
        stopBluetoothScan,
        addVirtualMeshPeer,
        togglePeerConnection,
        clearCompletedTransfers,
        exportAppApkToVault,
        downloadFromGitHub,
        fetchGitHubReleases,
        fetchGitHubContents,
        exportFileToGist,
      }}
    >
      {children}
    </SyncBeamContext.Provider>
  );
};

export const useSyncBeam = () => {
  const context = useContext(SyncBeamContext);
  if (!context) {
    throw new Error('useSyncBeam must be used within a SyncBeamProvider');
  }
  return context;
};
