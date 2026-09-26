import React from 'react';
import { useSyncBeam } from '../context/SyncBeamContext';
import { Radio, Download, CloudDownload } from 'lucide-react';

export const Header: React.FC = () => {
  const { showApkDownloadDialog, showGitHubHubDialog } = useSyncBeam();

  return (
    <header className="sticky top-0 z-30 bg-[#0B132B]/95 backdrop-blur-md border-b border-[#1E3A8A]/50 px-4 py-2.5 flex items-center justify-between">
      {/* Brand */}
      <div className="flex items-center gap-2.5">
        <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-[#00D2BA] to-[#4F46E5] flex items-center justify-center shadow-md shadow-[#00D2BA]/20">
          <Radio className="w-4 h-4 text-white" />
        </div>
        <div className="flex flex-col">
          <span className="font-black text-lg tracking-wide bg-gradient-to-r from-white via-slate-100 to-slate-300 bg-clip-text text-transparent">
            ZevSync
          </span>
        </div>
      </div>

      {/* Top Actions */}
      <div className="flex items-center gap-2">
        {/* Download APK Top Button */}
        <button
          type="button"
          onClick={() => showApkDownloadDialog(true)}
          data-testid="download_apk_top_btn"
          aria-label="Download APK"
          title="Download Android APK"
          className="w-9 h-9 rounded-full bg-[#047857] hover:bg-[#059669] text-white flex items-center justify-center transition-all duration-200 shadow-sm active:scale-95"
        >
          <Download className="w-4 h-4" />
        </button>

        {/* GitHub Direct Hub Top Button */}
        <button
          type="button"
          onClick={() => showGitHubHubDialog(true)}
          data-testid="github_hub_top_btn"
          aria-label="GitHub Direct Hub"
          title="GitHub Direct Sync Hub"
          className="w-9 h-9 rounded-full bg-[#1C2A4F] hover:bg-[#25386b] text-[#53EFD8] flex items-center justify-center transition-all duration-200 shadow-sm active:scale-95 border border-[#1E3A8A]"
        >
          <CloudDownload className="w-4 h-4" />
        </button>

        {/* 100% OFFLINE badge */}
        <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-[#064E3B] border border-[#10B981]/30">
          <span className="w-2 h-2 rounded-full bg-[#10B981] animate-pulse" />
          <span className="text-[10px] font-bold text-[#10B981] tracking-wider uppercase">
            100% OFFLINE
          </span>
        </div>
      </div>
    </header>
  );
};
