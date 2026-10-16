import React from 'react';
import { useSyncBeam } from '../context/SyncBeamContext';
import { Radio, Download, CloudDownload, Smartphone } from 'lucide-react';

export const Header: React.FC = () => {
  const {
    showApkDownloadDialog,
    showGitHubHubDialog,
    isPhoneProximityConnected,
    proximityDistanceMeters,
    proximityRssi,
    selectTab,
  } = useSyncBeam();

  return (
    <header className="sticky top-0 z-30 bg-[#0B132B]/95 backdrop-blur-md border-b border-[#1E3A8A]/50 px-4 py-2 flex items-center justify-between">
      {/* Brand & Phone Proximity Pill */}
      <div className="flex items-center gap-3">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-[#00D2BA] to-[#4F46E5] flex items-center justify-center shadow-md shadow-[#00D2BA]/20">
            <Radio className="w-4 h-4 text-white" />
          </div>
          <div className="flex flex-col">
            <span className="font-black text-lg tracking-wide bg-gradient-to-r from-white via-slate-100 to-slate-300 bg-clip-text text-transparent">
              ZevSync
            </span>
          </div>
        </div>

        {/* Proximity Phone Live Status Pill */}
        <button
          type="button"
          onClick={() => selectTab('PHONE_EXPLORER')}
          title="Click to open Android Phone Storage"
          className={`hidden sm:flex items-center gap-1.5 py-1 px-2.5 rounded-full text-xs font-semibold border transition-all ${
            isPhoneProximityConnected
              ? 'bg-[#064E3B]/80 hover:bg-[#064E3B] text-[#53EFD8] border-[#10B981]/40'
              : 'bg-amber-950/60 hover:bg-amber-950 text-amber-300 border-amber-700/40'
          }`}
        >
          <span className="relative flex h-2 w-2">
            {isPhoneProximityConnected && (
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-[#10B981] opacity-75" />
            )}
            <span
              className={`relative inline-flex rounded-full h-2 w-2 ${
                isPhoneProximityConnected ? 'bg-[#10B981]' : 'bg-amber-500'
              }`}
            />
          </span>
          <Smartphone className="w-3.5 h-3.5" />
          <span>
            {isPhoneProximityConnected
              ? `Phone Near (~${proximityDistanceMeters}m · ${proximityRssi}dBm)`
              : 'Phone Offline · Connect'}
          </span>
        </button>
      </div>

      {/* Top Actions */}
      <div className="flex items-center gap-2">
        {/* Download APK Top Button */}
        <button
          type="button"
          onClick={() => showApkDownloadDialog(true)}
          data-testid="download_apk_top_btn"
          aria-label="Download APK"
          title="Download Android Companion APK"
          className="w-8 h-8 rounded-full bg-[#047857] hover:bg-[#059669] text-white flex items-center justify-center transition-all duration-200 shadow-sm active:scale-95"
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
          className="w-8 h-8 rounded-full bg-[#1C2A4F] hover:bg-[#25386b] text-[#53EFD8] flex items-center justify-center transition-all duration-200 shadow-sm active:scale-95 border border-[#1E3A8A]"
        >
          <CloudDownload className="w-4 h-4" />
        </button>

        {/* OFFLINE label */}
        <span className="text-[10px] font-semibold text-[#10B981] tracking-wider uppercase ml-1 hidden xs:inline">
          Offline Mesh
        </span>
      </div>
    </header>
  );
};
