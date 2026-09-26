import React from 'react';
import {
  FileCategory,
  StorageBreakdown,
  SyncStatus,
} from '../types/models';
import {
  Folder,
  Image,
  FileText,
  Music,
  Video,
  Code,
  Archive,
  File as FileIcon,
  CheckCircle2,
  RefreshCw,
  AlertTriangle,
  Radio,
} from 'lucide-react';

export const SyncStatusBadge: React.FC<{ status: SyncStatus; className?: string }> = ({
  status,
  className = '',
}) => {
  const config = {
    SYNCED: {
      bg: 'bg-[#064E3B]/80 text-[#10B981] border-[#10B981]/30',
      label: 'Synced',
      icon: CheckCircle2,
    },
    SYNCING: {
      bg: 'bg-[#0C4A6E]/80 text-[#38BDF8] border-[#38BDF8]/30',
      label: 'Syncing',
      icon: RefreshCw,
      spin: true,
    },
    CONFLICT: {
      bg: 'bg-[#78350F]/80 text-[#F59E0B] border-[#F59E0B]/30',
      label: 'Conflict',
      icon: AlertTriangle,
    },
    LOCAL_ONLY: {
      bg: 'bg-[#312E81]/80 text-[#A5B4FC] border-[#A5B4FC]/30',
      label: 'Local Only',
      icon: FileIcon,
    },
    REMOTE_ONLY: {
      bg: 'bg-[#374151]/80 text-[#9CA3AF] border-[#9CA3AF]/30',
      label: 'Remote',
      icon: Folder,
    },
    QUEUED: {
      bg: 'bg-[#1E293B]/80 text-[#94A3B8] border-[#94A3B8]/30',
      label: 'Queued',
      icon: RefreshCw,
    },
    ERROR: {
      bg: 'bg-[#7F1D1D]/80 text-[#EF4444] border-[#EF4444]/30',
      label: 'Error',
      icon: AlertTriangle,
    },
  }[status] || {
    bg: 'bg-slate-800 text-slate-300 border-slate-600',
    label: status,
    icon: FileIcon,
  };

  const IconComp = config.icon;

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2 py-0.5 rounded-md text-xs font-semibold border ${config.bg} ${className}`}
    >
      <IconComp className={`w-3 h-3 ${config.spin ? 'animate-spin' : ''}`} />
      <span>{config.label}</span>
    </span>
  );
};

export const FileCategoryIcon: React.FC<{
  category: FileCategory;
  size?: number;
  className?: string;
}> = ({ category, size = 44, className = '' }) => {
  const getCategoryConfig = () => {
    switch (category) {
      case 'IMAGE':
        return {
          gradient: 'from-[#EC4899] to-[#BE185D]',
          icon: Image,
        };
      case 'DOCUMENT':
        return {
          gradient: 'from-[#3B82F6] to-[#1D4ED8]',
          icon: FileText,
        };
      case 'AUDIO':
        return {
          gradient: 'from-[#8B5CF6] to-[#6D28D9]',
          icon: Music,
        };
      case 'VIDEO':
        return {
          gradient: 'from-[#F97316] to-[#C2410C]',
          icon: Video,
        };
      case 'CODE':
        return {
          gradient: 'from-[#10B981] to-[#047857]',
          icon: Code,
        };
      case 'ARCHIVE':
        return {
          gradient: 'from-[#EAB308] to-[#A16207]',
          icon: Archive,
        };
      default:
        return {
          gradient: 'from-[#64748B] to-[#334155]',
          icon: Folder,
        };
    }
  };

  const { gradient, icon: IconComp } = getCategoryConfig();

  return (
    <div
      style={{ width: `${size}px`, height: `${size}px` }}
      className={`shrink-0 rounded-xl bg-gradient-to-br ${gradient} flex items-center justify-center shadow-md ${className}`}
    >
      <IconComp className="text-white" style={{ width: `${size * 0.52}px`, height: `${size * 0.52}px` }} />
    </div>
  );
};

export const BluetoothRadarAnimation: React.FC<{
  isScanning?: boolean;
  connectedCount?: number;
}> = ({ isScanning = true }) => {
  return (
    <div className="relative w-48 h-48 flex items-center justify-center">
      {/* Outer static rings */}
      <div className="absolute inset-0 rounded-full border border-sky-400/20" />
      <div className="absolute inset-6 rounded-full border border-sky-400/20" />
      <div className="absolute inset-12 rounded-full border border-sky-400/20" />

      {/* Crosshair lines */}
      <div className="absolute w-full h-[1px] bg-sky-400/10" />
      <div className="absolute h-full w-[1px] bg-sky-400/10" />

      {/* Pulsing waves */}
      {isScanning && (
        <>
          <div className="absolute inset-4 rounded-full border-2 border-[#53EFD8]/60 animate-ping opacity-75" />
          <div
            className="absolute inset-8 rounded-full border-2 border-sky-400/60 animate-ping opacity-60"
            style={{ animationDelay: '1.2s' }}
          />
        </>
      )}

      {/* Central node beacon */}
      <div className="relative z-10 w-14 h-14 rounded-full bg-gradient-to-tr from-[#00D2BA] to-[#4F46E5] flex items-center justify-center shadow-lg shadow-[#00D2BA]/25">
        <Radio className="w-7 h-7 text-white animate-pulse" />
      </div>
    </div>
  );
};

export const StorageProgressBar: React.FC<{
  breakdown: StorageBreakdown;
  className?: string;
}> = ({ breakdown, className = '' }) => {
  const total = Math.max(breakdown.quotaBytes, 1);
  const imgFrac = Math.min(Math.max(breakdown.imagesBytes / total, 0), 1);
  const docFrac = Math.min(Math.max(breakdown.docsBytes / total, 0), 1);
  const codeFrac = Math.min(Math.max(breakdown.codeBytes / total, 0), 1);
  const mediaFrac = Math.min(Math.max((breakdown.audioBytes + breakdown.videoBytes) / total, 0), 1);
  const otherFrac = Math.min(Math.max((breakdown.archiveBytes + breakdown.otherBytes) / total, 0), 1);

  const usedTotal = imgFrac + docFrac + codeFrac + mediaFrac + otherFrac;
  const remainingFrac = Math.max(1 - usedTotal, 0.001);

  return (
    <div className={`w-full ${className}`}>
      <div className="h-3 w-full rounded-full overflow-hidden bg-[#1E293B] flex">
        {imgFrac > 0.005 && (
          <div style={{ flex: imgFrac }} className="h-full bg-[#EC4899]" title="Images" />
        )}
        {docFrac > 0.005 && (
          <div style={{ flex: docFrac }} className="h-full bg-[#3B82F6]" title="Docs & PDF" />
        )}
        {codeFrac > 0.005 && (
          <div style={{ flex: codeFrac }} className="h-full bg-[#10B981]" title="Code & Data" />
        )}
        {mediaFrac > 0.005 && (
          <div style={{ flex: mediaFrac }} className="h-full bg-[#8B5CF6]" title="Media" />
        )}
        {otherFrac > 0.005 && (
          <div style={{ flex: otherFrac }} className="h-full bg-[#EAB308]" title="Archives & Other" />
        )}
        <div style={{ flex: remainingFrac }} className="h-full bg-[#334155]/60" title="Free Space" />
      </div>
    </div>
  );
};
