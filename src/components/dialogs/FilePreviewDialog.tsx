import React, { useState } from 'react';
import { SyncedFile } from '../../types/models';
import { FileCategoryIcon, SyncStatusBadge } from '../CommonComponents';
import { formatBytes } from '../../services/cacheVaultManager';
import {
  X,
  Edit3,
  Save,
  Pin,
  PinOff,
  Hash,
  Clock,
  HardDrive,
  Cpu,
  Layers,
} from 'lucide-react';

interface FilePreviewDialogProps {
  file: SyncedFile;
  onDismiss: () => void;
  onSaveEdit: (newContent: string) => void;
  onTogglePin: () => void;
}

export const FilePreviewDialog: React.FC<FilePreviewDialogProps> = ({
  file,
  onDismiss,
  onSaveEdit,
  onTogglePin,
}) => {
  const [isEditing, setIsEditing] = useState(false);
  const [content, setContent] = useState(file.textPreview || file.contentData || '');

  const handleSave = () => {
    onSaveEdit(content);
    setIsEditing(false);
  };

  const isTextLike =
    file.category === 'CODE' ||
    file.category === 'DOCUMENT' ||
    file.mimeType.startsWith('text/') ||
    file.mimeType.includes('json') ||
    file.mimeType.includes('xml') ||
    Boolean(file.textPreview);

  const formattedDate = new Date(file.lastModifiedTimestamp).toLocaleString();

  return (
    <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-3 sm:p-4">
      <div className="w-full max-w-xl max-h-[90vh] rounded-2xl bg-[#111D3D] border border-[#1E3A8A] flex flex-col shadow-2xl overflow-hidden">
        {/* Header */}
        <div className="p-4 border-b border-[#1E3A8A] flex items-center justify-between gap-3 bg-[#0B132B]/60">
          <div className="flex items-center gap-3 min-w-0">
            <FileCategoryIcon category={file.category} size={40} />
            <div className="min-w-0">
              <h3 className="font-bold text-sm text-white truncate">{file.name}</h3>
              <div className="flex items-center gap-2 mt-0.5">
                <span className="text-xs text-slate-400 font-mono">
                  {formatBytes(file.sizeBytes)}
                </span>
                <span className="text-slate-600">•</span>
                <SyncStatusBadge status={file.syncStatus} />
              </div>
            </div>
          </div>

          <div className="flex items-center gap-1.5 shrink-0">
            {/* Pin Toggle */}
            <button
              type="button"
              onClick={onTogglePin}
              className={`p-2 rounded-xl border transition-all active:scale-95 ${
                file.isPinned
                  ? 'bg-[#53EFD8]/10 text-[#53EFD8] border-[#53EFD8]/30'
                  : 'bg-[#1C2A4F] text-slate-300 border-[#1E3A8A] hover:bg-[#25386b]'
              }`}
              title={file.isPinned ? 'Unpin File' : 'Pin to Cache'}
            >
              {file.isPinned ? <Pin className="w-4 h-4 fill-current" /> : <PinOff className="w-4 h-4" />}
            </button>

            {/* Edit / Save Button (for text files) */}
            {isTextLike && (
              <button
                type="button"
                onClick={isEditing ? handleSave : () => setIsEditing(true)}
                className={`flex items-center gap-1.5 px-3 py-2 rounded-xl text-xs font-bold transition-all active:scale-95 ${
                  isEditing
                    ? 'bg-[#53EFD8] text-slate-950 shadow-md'
                    : 'bg-[#1C2A4F] text-white border border-[#1E3A8A] hover:bg-[#25386b]'
                }`}
              >
                {isEditing ? (
                  <>
                    <Save className="w-3.5 h-3.5" />
                    <span>Save</span>
                  </>
                ) : (
                  <>
                    <Edit3 className="w-3.5 h-3.5" />
                    <span>Edit</span>
                  </>
                )}
              </button>
            )}

            {/* Close Button */}
            <button
              type="button"
              onClick={onDismiss}
              className="p-2 rounded-xl text-slate-400 hover:text-white hover:bg-[#1C2A4F] transition-colors"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* Content Body */}
        <div className="p-4 flex-1 overflow-y-auto space-y-4">
          {/* Metadata Grid */}
          <div className="grid grid-cols-2 gap-2 text-xs rounded-xl bg-[#0B132B] border border-[#1E3A8A] p-3 font-mono text-slate-300">
            <div>
              <span className="text-[10px] text-slate-500 block uppercase">SHA-256 Hash</span>
              <span className="text-white truncate block" title={file.contentHash}>
                {file.contentHash.slice(0, 16)}...
              </span>
            </div>
            <div>
              <span className="text-[10px] text-slate-500 block uppercase">Lineage Clock</span>
              <span className="text-[#53EFD8] block">
                v{file.versionNumber} (Lamport: {file.lamportTimestamp})
              </span>
            </div>
            <div>
              <span className="text-[10px] text-slate-500 block uppercase">Origin Node</span>
              <span className="text-white truncate block">{file.originDeviceName}</span>
            </div>
            <div>
              <span className="text-[10px] text-slate-500 block uppercase">Last Modified</span>
              <span className="text-slate-400 block">{formattedDate}</span>
            </div>
          </div>

          {/* Vector Clock JSON */}
          <div className="text-xs">
            <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1">
              Vector Clock State Map
            </span>
            <div className="p-2.5 rounded-xl bg-[#0B132B] border border-[#1E3A8A] font-mono text-[11px] text-[#A5B4FC]">
              {file.vectorClockJson}
            </div>
          </div>

          {/* File Viewer / Editor */}
          <div className="space-y-1.5">
            <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">
              {isEditing ? 'Editing Document' : 'Content Preview'}
            </span>

            {isTextLike ? (
              isEditing ? (
                <textarea
                  rows={10}
                  value={content}
                  onChange={(e) => setContent(e.target.value)}
                  className="w-full bg-[#0B132B] border border-[#53EFD8] rounded-xl p-3 text-xs text-white font-mono focus:outline-none resize-none shadow-inner"
                />
              ) : (
                <div className="rounded-xl bg-[#0B132B] border border-[#1E3A8A] p-3 text-xs font-mono text-slate-200 whitespace-pre-wrap max-h-60 overflow-y-auto">
                  {content || '(Empty document)'}
                </div>
              )
            ) : (
              <div className="rounded-xl bg-[#0B132B] border border-[#1E3A8A] p-6 text-center text-xs text-slate-400">
                <HardDrive className="w-8 h-8 text-slate-500 mx-auto mb-2" />
                <p className="font-semibold text-slate-300">{file.mimeType}</p>
                <p className="text-[11px] text-slate-500 mt-1">
                  Binary asset stored with SHA-256 content addressing. Ready for Bluetooth beam transmission.
                </p>
              </div>
            )}
          </div>
        </div>

        {/* Footer */}
        <div className="p-3 bg-[#0B132B]/60 border-t border-[#1E3A8A] flex justify-end">
          <button
            type="button"
            onClick={onDismiss}
            className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-300 hover:bg-[#1C2A4F] transition-colors"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};
