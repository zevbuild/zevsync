import React, { useEffect } from 'react';
import { SyncBeamProvider, useSyncBeam } from './context/SyncBeamContext';
import { Header } from './components/Header';
import { BottomNav } from './components/BottomNav';
import { VaultScreen } from './components/VaultScreen';
import { BluetoothRadarScreen } from './components/BluetoothRadarScreen';
import { LiveSyncScreen } from './components/LiveSyncScreen';
import { ConflictsScreen } from './components/ConflictsScreen';
import { StorageMeshScreen } from './components/StorageMeshScreen';
import { FilePreviewDialog } from './components/dialogs/FilePreviewDialog';
import { GitHubHubDialog } from './components/dialogs/GitHubHubDialog';
import { ApkDownloadDialog } from './components/dialogs/ApkDownloadDialog';
import { CheckCircle2, X } from 'lucide-react';

const AppContent: React.FC = () => {
  const {
    currentTab,
    selectedFileForPreview,
    closePreview,
    updateNote,
    togglePin,
    isGitHubHubDialog,
    isApkDownloadDialog,
    snackbarMessage,
    clearSnackbar,
  } = useSyncBeam();

  // Auto-dismiss snackbar after 4 seconds
  useEffect(() => {
    if (snackbarMessage) {
      const timer = setTimeout(() => {
        clearSnackbar();
      }, 4000);
      return () => clearTimeout(timer);
    }
  }, [snackbarMessage, clearSnackbar]);

  return (
    <div className="min-h-screen bg-[#0B132B] text-slate-100 flex flex-col font-sans">
      <Header />

      <main className="flex-1 w-full max-w-4xl mx-auto pt-2">
        {currentTab === 'VAULT' && <VaultScreen />}
        {currentTab === 'RADAR' && <BluetoothRadarScreen />}
        {currentTab === 'LIVE_SYNC' && <LiveSyncScreen />}
        {currentTab === 'CONFLICTS' && <ConflictsScreen />}
        {currentTab === 'STORAGE' && <StorageMeshScreen />}
      </main>

      <BottomNav />

      {/* File Preview Dialog */}
      {selectedFileForPreview && (
        <FilePreviewDialog
          file={selectedFileForPreview}
          onDismiss={closePreview}
          onSaveEdit={(newContent) => updateNote(selectedFileForPreview.id, newContent)}
          onTogglePin={() =>
            togglePin(selectedFileForPreview.id, !selectedFileForPreview.isPinned)
          }
        />
      )}

      {/* GitHub Direct Hub Dialog */}
      {isGitHubHubDialog && <GitHubHubDialog />}

      {/* APK Download & Sideloading Dialog */}
      {isApkDownloadDialog && <ApkDownloadDialog />}

      {/* Snackbar / Toast Notification */}
      {snackbarMessage && (
        <div className="fixed bottom-20 left-4 right-4 sm:left-auto sm:right-6 sm:bottom-20 z-50 animate-in fade-in slide-in-from-bottom-2 duration-200">
          <div className="flex items-center gap-3 py-2.5 px-4 rounded-xl bg-[#111D3D] border border-[#10B981]/50 text-white shadow-2xl text-xs max-w-md">
            <CheckCircle2 className="w-4 h-4 text-[#10B981] shrink-0" />
            <span className="flex-1 font-medium">{snackbarMessage}</span>
            <button
              type="button"
              onClick={clearSnackbar}
              className="text-slate-400 hover:text-white p-0.5"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export const App: React.FC = () => {
  return (
    <SyncBeamProvider>
      <AppContent />
    </SyncBeamProvider>
  );
};

export default App;
