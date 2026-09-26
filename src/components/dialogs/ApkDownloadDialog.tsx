import React, { useState } from 'react';
import { useSyncBeam } from '../../context/SyncBeamContext';
import {
  Download,
  Smartphone,
  X,
  ShieldCheck,
  Radio,
  FileCheck,
  Share2,
  Copy,
  Check,
  ExternalLink,
  HelpCircle,
} from 'lucide-react';

export const ApkDownloadDialog: React.FC = () => {
  const {
    showApkDownloadDialog,
    exportAppApkToVault,
    showGitHubHubDialog,
    showSnackbar,
    files,
  } = useSyncBeam();

  const [isExtracting, setIsExtracting] = useState(false);
  const [copiedLink, setCopiedLink] = useState(false);
  const [activeTab, setActiveTab] = useState<'steps' | 'bluetooth' | 'links'>('steps');

  const existingApk = files.find((f) => f.name.endsWith('.apk') && !f.isDeleted);

  const handleExtractApk = async () => {
    setIsExtracting(true);
    await exportAppApkToVault(() => {
      setIsExtracting(false);
    });
  };

  const handleCopyLink = () => {
    const url = 'https://github.com/zevbuild/zevsync/releases';
    navigator.clipboard.writeText(url);
    setCopiedLink(true);
    setTimeout(() => setCopiedLink(false), 2000);
    showSnackbar('Copied APK releases URL to clipboard');
  };

  const handleShare = () => {
    if (navigator.share) {
      navigator.share({
        title: 'ZevSync - Offline Bluetooth P2P Sync',
        text: 'Download and sideload ZevSync offline mesh APK for multi-device sync without internet:',
        url: 'https://github.com/zevbuild/zevsync',
      });
    } else {
      handleCopyLink();
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-end sm:items-center justify-center p-0 sm:p-4">
      <div className="w-full max-w-lg max-h-[90vh] rounded-t-3xl sm:rounded-2xl bg-[#111D3D] border border-[#1E3A8A] flex flex-col shadow-2xl overflow-hidden animate-in slide-in-from-bottom duration-200">
        {/* Header */}
        <div className="p-4 border-b border-[#1E3A8A] flex items-center justify-between bg-[#0B132B]/70">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-[#047857] text-white flex items-center justify-center shadow-md">
              <Smartphone className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-bold text-sm text-white">ZevSync Android APK</h3>
              <p className="text-xs text-slate-400">Offline package extraction & installation guide</p>
            </div>
          </div>

          <button
            type="button"
            onClick={() => showApkDownloadDialog(false)}
            className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-[#1C2A4F] transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Tab switch */}
        <div className="flex border-b border-[#1E3A8A] bg-[#0B132B]/40 px-3">
          {[
            { id: 'steps', label: 'Installation Steps' },
            { id: 'bluetooth', label: 'P2P Bluetooth Beam' },
            { id: 'links', label: 'Releases & Share' },
          ].map((tab) => (
            <button
              key={tab.id}
              type="button"
              onClick={() => setActiveTab(tab.id as typeof activeTab)}
              className={`py-2.5 px-3 text-xs font-semibold border-b-2 transition-all ${
                activeTab === tab.id
                  ? 'border-[#53EFD8] text-[#53EFD8] font-bold'
                  : 'border-transparent text-slate-400 hover:text-slate-200'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {/* Body */}
        <div className="p-4 flex-1 overflow-y-auto space-y-4">
          {/* Quick Extract Action Card */}
          <div className="rounded-xl bg-gradient-to-br from-[#064E3B] to-[#0B132B] border border-[#10B981]/40 p-4 space-y-3">
            <div className="flex items-start justify-between">
              <div>
                <span className="font-bold text-sm text-white block">
                  Self-Hosting Vault Extraction
                </span>
                <p className="text-xs text-slate-300 mt-0.5">
                  Generate the standalone ZevSync APK directly into your local offline Vault.
                </p>
              </div>
              <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-[#10B981]/20 text-[#10B981] border border-[#10B981]/30">
                OFFLINE
              </span>
            </div>

            <button
              type="button"
              disabled={isExtracting}
              onClick={handleExtractApk}
              className="w-full py-2.5 px-4 rounded-xl text-xs font-bold bg-[#10B981] hover:bg-[#059669] text-slate-950 flex items-center justify-center gap-2 shadow-md transition-all active:scale-95"
            >
              {isExtracting ? (
                <span>Extracting APK Package...</span>
              ) : existingApk ? (
                <>
                  <FileCheck className="w-4 h-4" />
                  <span>Re-Extract Latest ZevSync APK (14.7 MB)</span>
                </>
              ) : (
                <>
                  <Download className="w-4 h-4" />
                  <span>Extract ZevSync APK to Vault (14.7 MB)</span>
                </>
              )}
            </button>

            {existingApk && (
              <p className="text-[11px] text-[#10B981] flex items-center gap-1 font-mono">
                <FileCheck className="w-3.5 h-3.5" />
                <span>APK is in Vault ({existingApk.name})</span>
              </p>
            )}
          </div>

          {activeTab === 'steps' && (
            <div className="space-y-3">
              <span className="text-xs font-bold text-slate-300 block uppercase tracking-wider">
                Step-by-Step Sideloading Instructions:
              </span>

              {[
                {
                  step: 1,
                  title: 'Extract or Download APK',
                  desc: 'Tap the button above to extract ZevSync into your local vault, or download from GitHub releases.',
                },
                {
                  step: 2,
                  title: 'Enable Unknown Sources',
                  desc: 'On your Android device, go to Settings → Security & Privacy → Install Unknown Apps, and toggle permission for your browser or file manager.',
                },
                {
                  step: 3,
                  title: 'Install Package',
                  desc: 'Open your device Downloads or File Manager, locate ZevSync.apk, and tap "Install". Accept the offline permission prompt.',
                },
                {
                  step: 4,
                  title: 'Start Bluetooth Mesh',
                  desc: 'Launch ZevSync, permit Bluetooth Scan & Connect, and discover other nearby Android devices with zero internet!',
                },
              ].map((item) => (
                <div
                  key={item.step}
                  className="rounded-xl bg-[#0B132B] border border-[#1E3A8A] p-3 flex items-start gap-3"
                >
                  <div className="w-6 h-6 rounded-full bg-[#1C2A4F] text-[#53EFD8] flex items-center justify-center shrink-0 font-bold text-xs">
                    {item.step}
                  </div>
                  <div>
                    <h4 className="font-bold text-xs text-white">{item.title}</h4>
                    <p className="text-[11px] text-slate-400 mt-0.5 leading-relaxed">
                      {item.desc}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          )}

          {activeTab === 'bluetooth' && (
            <div className="space-y-3 text-xs text-slate-300 leading-relaxed">
              <div className="rounded-xl bg-[#0B132B] border border-[#1E3A8A] p-3.5 space-y-2">
                <div className="flex items-center gap-2 text-[#53EFD8]">
                  <Radio className="w-4 h-4" />
                  <span className="font-bold text-sm">Autonomous Air-Gapped Beaming</span>
                </div>
                <p>
                  Because ZevSync can extract its own compiled APK file into the vault, it can
                  beam its own installer to nearby friends and colleagues over Bluetooth RFCOMM!
                </p>
                <p className="text-slate-400 text-[11px]">
                  1. Extract APK to Vault.<br />
                  2. Select the APK in the Vault tab and tap "Sync with Peer".<br />
                  3. The receiving device receives the complete APK file into its vault for direct installation.
                </p>
              </div>
            </div>
          )}

          {activeTab === 'links' && (
            <div className="space-y-3">
              <div className="p-3.5 rounded-xl bg-[#0B132B] border border-[#1E3A8A] space-y-3">
                <span className="font-bold text-xs text-white block">Official GitHub Release</span>
                <p className="text-xs text-slate-400">
                  Always download verified APK binaries with cryptographic release checksums.
                </p>

                <div className="flex gap-2">
                  <button
                    type="button"
                    onClick={handleCopyLink}
                    className="flex-1 py-2 px-3 rounded-lg text-xs font-bold bg-[#1C2A4F] hover:bg-[#25386b] text-white border border-[#1E3A8A] flex items-center justify-center gap-1.5"
                  >
                    {copiedLink ? <Check className="w-3.5 h-3.5 text-[#10B981]" /> : <Copy className="w-3.5 h-3.5" />}
                    <span>{copiedLink ? 'Copied Link' : 'Copy Release Link'}</span>
                  </button>

                  <button
                    type="button"
                    onClick={handleShare}
                    className="py-2 px-3 rounded-lg text-xs font-bold bg-[#53EFD8] hover:bg-[#3be5cc] text-slate-950 flex items-center justify-center gap-1.5"
                  >
                    <Share2 className="w-3.5 h-3.5" />
                    <span>Share</span>
                  </button>
                </div>
              </div>

              <button
                type="button"
                onClick={() => {
                  showApkDownloadDialog(false);
                  showGitHubHubDialog(true);
                }}
                className="w-full py-2.5 px-4 rounded-xl text-xs font-bold text-[#53EFD8] bg-[#172554] border border-[#1E3A8A] flex items-center justify-center gap-2 hover:bg-[#1E3A8A] transition-colors"
              >
                <span>Open GitHub Direct Hub Dialog →</span>
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
