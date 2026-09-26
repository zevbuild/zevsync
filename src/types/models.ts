export type FileCategory =
  | 'ALL'
  | 'IMAGE'
  | 'DOCUMENT'
  | 'AUDIO'
  | 'VIDEO'
  | 'CODE'
  | 'ARCHIVE'
  | 'OTHER';

export type SyncStatus =
  | 'SYNCED'
  | 'SYNCING'
  | 'CONFLICT'
  | 'LOCAL_ONLY'
  | 'REMOTE_ONLY'
  | 'QUEUED'
  | 'ERROR';

export type ConflictStatus =
  | 'PENDING'
  | 'RESOLVED_LOCAL'
  | 'RESOLVED_REMOTE'
  | 'RESOLVED_KEEP_BOTH'
  | 'RESOLVED_MERGED';

export interface SyncedFile {
  id: string; // SHA-256 or unique id
  name: string;
  relativePath?: string;
  sizeBytes: number;
  mimeType: string;
  contentHash: string;
  localFilePath?: string;
  contentData?: string; // Text or base64 data for in-memory / storage
  versionNumber: number;
  originDeviceId: string;
  originDeviceName: string;
  lastModifiedTimestamp: number;
  lamportTimestamp: number;
  vectorClockJson: string; // JSON map of deviceId -> version
  syncStatus: SyncStatus;
  isPinned: boolean;
  category: FileCategory;
  conflictWinnerId?: string | null;
  textPreview?: string | null;
  isDeleted: boolean;
  lastSyncPeerName?: string | null;
}

export interface SyncConflict {
  id: string;
  fileId: string;
  fileName: string;
  localVersion: number;
  remoteVersion: number;
  localHash: string;
  remoteHash: string;
  localTimestamp: number;
  remoteTimestamp: number;
  localDeviceId: string;
  remoteDeviceId: string;
  remoteDeviceName: string;
  localSizeBytes: number;
  remoteSizeBytes: number;
  localFilePath?: string;
  remoteFilePath?: string;
  localLamport: number;
  remoteLamport: number;
  localVectorClock: string;
  remoteVectorClock: string;
  localTextSnippet?: string | null;
  remoteTextSnippet?: string | null;
  status: ConflictStatus;
  resolutionTimestamp?: number | null;
  resolutionNote?: string | null;
}

export interface SyncEventLog {
  id: number;
  timestamp: number;
  eventType: string; // "DISCOVERY", "CONNECT", "HANDSHAKE", "TRANSFER_START", "TRANSFER_SUCCESS", "CONFLICT_DETECTED", "CACHE_EVICTED", "CACHE_PIN", etc.
  title: string;
  description: string;
  peerName?: string | null;
  isPositive: boolean;
}

export interface PeerDevice {
  id: string;
  bluetoothAddress: string;
  name: string;
  isConnected: boolean;
  isPaired: boolean;
  rssi: number; // dBm
  lastSeenTimestamp: number;
  totalBytesSent: number;
  totalBytesReceived: number;
  activeTransfers: number;
  isSyncAllowed: boolean;
  isVirtualNode: boolean;
  meshHopCount: number;
  deviceModel: string;
  protocolVersion: number;
}

export type TransferDirection = 'UPLOAD' | 'DOWNLOAD';

export type TransferStatus =
  | 'IDLE'
  | 'TRANSFERRING'
  | 'PAUSED'
  | 'COMPLETED'
  | 'FAILED'
  | 'VERIFYING';

export interface TransferItem {
  id: string;
  fileId: string;
  fileName: string;
  direction: TransferDirection;
  peerId: string;
  peerName: string;
  totalBytes: number;
  bytesTransferred: number;
  speedBytesPerSec: number;
  status: TransferStatus;
  progress: number;
  errorMessage?: string | null;
  startedAt: number;
  finishedAt?: number | null;
}

export interface StorageBreakdown {
  totalVaultBytes: number;
  quotaBytes: number;
  imagesBytes: number;
  docsBytes: number;
  audioBytes: number;
  videoBytes: number;
  codeBytes: number;
  archiveBytes: number;
  otherBytes: number;
  fileCount: number;
  conflictCount: number;
  cachedPeerCopiesCount: number;
}

export type FileSortOrder = 'DATE_DESC' | 'DATE_ASC' | 'NAME_ASC' | 'SIZE_DESC';

export type NavigationTab = 'VAULT' | 'RADAR' | 'LIVE_SYNC' | 'CONFLICTS' | 'STORAGE';

export type ConflictResolutionChoice =
  | 'KEEP_LOCAL_WINNER'
  | 'KEEP_REMOTE_WINNER'
  | 'KEEP_BOTH'
  | 'MERGE_CUSTOM';

export interface GitHubFileItem {
  name: string;
  path: string;
  type: 'file' | 'dir';
  size: number;
  downloadUrl: string | null;
  htmlUrl: string | null;
}

export interface GitHubReleaseAsset {
  name: string;
  size: number;
  downloadUrl: string;
  contentType: string;
}

export interface GitHubReleaseInfo {
  tagName: string;
  name: string;
  body: string;
  publishedAt: string;
  htmlUrl: string;
  assets: GitHubReleaseAsset[];
}

export interface GitHubDownloadResult {
  success: boolean;
  fileName: string;
  bytesDownloaded: number;
  textContent: string | null;
  errorMessage?: string | null;
}
