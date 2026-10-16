import React, { useState } from 'react';
import { usePWAInstall } from '../hooks/usePWAInstall';
import { DownloadCloud, Smartphone, X, Check } from 'lucide-react';

export const PWAInstallButton: React.FC<{ compact?: boolean }> = ({ compact = false }) => {
  const { isInstallable, isInstalled, isIOS, install } = usePWAInstall();
  const [showIOSGuide, setShowIOSGuide] = useState(false);
  const [justInstalled, setJustInstalled] = useState(false);

  // If already installed, show small status or hide
  if (isInstalled) {
    return (
      <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-mono text-[#53EFD8] bg-[#53EFD8]/10 border border-[#53EFD8]/30">
        <Check className="w-3.5 h-3.5" />
        <span>Installed App</span>
      </div>
    );
  }

  // Chromium / Android / Desktop flow
  if (isInstallable) {
    return (
      <button
        onClick={async () => {
          const success = await install();
          if (success) {
            setJustInstalled(true);
            setTimeout(() => setJustInstalled(false), 3000);
          }
        }}
        className={`flex items-center gap-2 rounded-lg bg-gradient-to-r from-[#53EFD8] to-cyan-400 font-semibold text-[#0B132B] shadow-md hover:shadow-[#53EFD8]/20 hover:brightness-110 active:scale-95 transition-all ${
          compact ? 'px-2.5 py-1 text-xs' : 'px-3.5 py-1.5 text-xs tracking-wide'
        }`}
        title="Install ZevSync as standalone desktop/mobile app"
      >
        <DownloadCloud className="w-4 h-4 text-[#0B132B]" />
        <span>{justInstalled ? 'Installed!' : 'Install App'}</span>
      </button>
    );
  }

  // iOS Safari flow
  if (isIOS) {
    return (
      <>
        <button
          onClick={() => setShowIOSGuide(true)}
          className={`flex items-center gap-2 rounded-lg border border-cyan-500/40 bg-cyan-950/40 text-cyan-200 hover:bg-cyan-900/40 transition ${
            compact ? 'px-2.5 py-1 text-xs' : 'px-3.5 py-1.5 text-xs'
          }`}
          title="Install on iOS Home Screen"
        >
          <Smartphone className="w-3.5 h-3.5 text-[#53EFD8]" />
          <span>Install iOS</span>
        </button>

        {showIOSGuide && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4">
            <div className="w-full max-w-sm rounded-2xl bg-[#0e1a38] border border-cyan-500/30 p-6 shadow-2xl text-slate-100">
              <div className="flex items-center justify-between pb-3 border-b border-slate-700">
                <div className="flex items-center gap-2">
                  <Smartphone className="w-5 h-5 text-[#53EFD8]" />
                  <h3 className="font-semibold text-white">Install on iPhone / iPad</h3>
                </div>
                <button
                  onClick={() => setShowIOSGuide(false)}
                  className="p-1 rounded-lg hover:bg-slate-800 text-slate-400 hover:text-white"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
              <div className="mt-4 space-y-3 text-xs text-slate-300">
                <p className="flex items-start gap-2">
                  <span className="flex-shrink-0 w-5 h-5 rounded-full bg-cyan-500/20 text-[#53EFD8] flex items-center justify-center font-bold">1</span>
                  <span>Tap the <strong>Share</strong> icon in the Safari navigation bar.</span>
                </p>
                <p className="flex items-start gap-2">
                  <span className="flex-shrink-0 w-5 h-5 rounded-full bg-cyan-500/20 text-[#53EFD8] flex items-center justify-center font-bold">2</span>
                  <span>Scroll down and select <strong>Add to Home Screen</strong>.</span>
                </p>
                <p className="flex items-start gap-2">
                  <span className="flex-shrink-0 w-5 h-5 rounded-full bg-cyan-500/20 text-[#53EFD8] flex items-center justify-center font-bold">3</span>
                  <span>Launch ZevSync directly from your home screen with offline capability!</span>
                </p>
              </div>
              <button
                onClick={() => setShowIOSGuide(false)}
                className="mt-5 w-full rounded-lg bg-[#53EFD8] py-2 text-xs font-bold text-[#0B132B] hover:bg-cyan-300 transition"
              >
                Got It
              </button>
            </div>
          </div>
        )}
      </>
    );
  }

  // Fallback for browsers before prompt or when standalone isn't supported yet
  return (
    <button
      onClick={() => {
        alert(
          'To install ZevSync: click the Install icon in your browser URL bar (or menu > Install ZevSync / Add to Home screen).'
        );
      }}
      className={`hidden sm:flex items-center gap-1.5 rounded-lg border border-slate-700 bg-slate-800/60 px-2.5 py-1 text-xs text-slate-300 hover:border-cyan-500/50 hover:text-white transition`}
      title="Install ZevSync App"
    >
      <DownloadCloud className="w-3.5 h-3.5 text-[#53EFD8]" />
      <span>Install PWA</span>
    </button>
  );
};
