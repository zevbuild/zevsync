import { FileCategory, StorageBreakdown, SyncedFile } from '../types/models';

export async function calculateSha256(content: string | Uint8Array): Promise<string> {
  let buffer: Uint8Array;
  if (typeof content === 'string') {
    buffer = new TextEncoder().encode(content);
  } else {
    buffer = content;
  }
  const hashBuffer = await crypto.subtle.digest('SHA-256', buffer.buffer as ArrayBuffer);
  const hashArray = Array.from(new Uint8Array(hashBuffer));
  return hashArray.map((b) => b.toString(16).padStart(2, '0')).join('');
}

export function determineCategory(fileName: string, mimeType = ''): FileCategory {
  const ext = fileName.split('.').pop()?.toLowerCase() || '';

  if (
    ['jpg', 'jpeg', 'png', 'webp', 'gif', 'svg', 'bmp', 'ico'].includes(ext) ||
    mimeType.startsWith('image/')
  ) {
    return 'IMAGE';
  }

  if (
    ['pdf', 'doc', 'docx', 'txt', 'rtf', 'odt', 'pages'].includes(ext) ||
    mimeType === 'application/pdf'
  ) {
    return 'DOCUMENT';
  }

  if (
    ['mp3', 'wav', 'ogg', 'm4a', 'flac', 'aac', 'wma'].includes(ext) ||
    mimeType.startsWith('audio/')
  ) {
    return 'AUDIO';
  }

  if (
    ['mp4', 'mkv', 'webm', 'avi', 'mov', 'wmv'].includes(ext) ||
    mimeType.startsWith('video/')
  ) {
    return 'VIDEO';
  }

  if (
    [
      'kt',
      'java',
      'ts',
      'tsx',
      'js',
      'jsx',
      'py',
      'json',
      'xml',
      'html',
      'css',
      'scss',
      'rs',
      'go',
      'c',
      'cpp',
      'h',
      'sh',
      'md',
      'yml',
      'yaml',
      'sql',
    ].includes(ext)
  ) {
    return 'CODE';
  }

  if (['zip', 'rar', 'tar', 'gz', '7z', 'bz2'].includes(ext)) {
    return 'ARCHIVE';
  }

  return 'OTHER';
}

export function determineMimeType(fileName: string): string {
  const ext = fileName.split('.').pop()?.toLowerCase() || '';
  const map: Record<string, string> = {
    jpg: 'image/jpeg',
    jpeg: 'image/jpeg',
    png: 'image/png',
    webp: 'image/webp',
    gif: 'image/gif',
    svg: 'image/svg+xml',
    mp4: 'video/mp4',
    mkv: 'video/x-matroska',
    mp3: 'audio/mpeg',
    wav: 'audio/wav',
    pdf: 'application/pdf',
    txt: 'text/plain',
    md: 'text/markdown',
    json: 'application/json',
    xml: 'text/xml',
    kt: 'text/x-kotlin',
    java: 'text/x-java',
    py: 'text/x-python',
    js: 'text/javascript',
    ts: 'text/typescript',
    html: 'text/html',
    css: 'text/css',
    zip: 'application/zip',
    apk: 'application/vnd.android.package-archive',
  };
  return map[ext] || 'application/octet-stream';
}

export function formatBytes(bytes: number): string {
  if (bytes <= 0) return '0 B';
  const kb = bytes / 1024;
  const mb = kb / 1024;
  const gb = mb / 1024;
  if (gb >= 1) return `${gb.toFixed(2)} GB`;
  if (mb >= 1) return `${mb.toFixed(1)} MB`;
  if (kb >= 1) return `${kb.toFixed(1)} KB`;
  return `${bytes} B`;
}

export function formatSpeed(bytesPerSec: number): string {
  if (bytesPerSec <= 0) return '0 B/s';
  const kb = bytesPerSec / 1024;
  const mb = kb / 1024;
  if (mb >= 1) return `${mb.toFixed(2)} MB/s`;
  if (kb >= 1) return `${kb.toFixed(1)} KB/s`;
  return `${bytesPerSec} B/s`;
}

export function calculateStorageBreakdown(
  files: SyncedFile[],
  quotaBytes: number
): StorageBreakdown {
  let imagesBytes = 0;
  let docsBytes = 0;
  let audioBytes = 0;
  let videoBytes = 0;
  let codeBytes = 0;
  let archiveBytes = 0;
  let otherBytes = 0;
  let totalVaultBytes = 0;

  for (const f of files) {
    if (f.isDeleted) continue;
    const size = f.sizeBytes;
    totalVaultBytes += size;
    switch (f.category) {
      case 'IMAGE':
        imagesBytes += size;
        break;
      case 'DOCUMENT':
        docsBytes += size;
        break;
      case 'AUDIO':
        audioBytes += size;
        break;
      case 'VIDEO':
        videoBytes += size;
        break;
      case 'CODE':
        codeBytes += size;
        break;
      case 'ARCHIVE':
        archiveBytes += size;
        break;
      default:
        otherBytes += size;
        break;
    }
  }

  return {
    totalVaultBytes,
    quotaBytes,
    imagesBytes,
    docsBytes,
    audioBytes,
    videoBytes,
    codeBytes,
    archiveBytes,
    otherBytes,
    fileCount: files.filter((f) => !f.isDeleted).length,
    conflictCount: 0,
    cachedPeerCopiesCount: files.filter((f) => f.syncStatus === 'SYNCED').length,
  };
}
