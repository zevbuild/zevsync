import React, { useState } from 'react';
import { useSyncBeam } from '../../context/SyncBeamContext';
import { SyncedFile } from '../../types/models';
import { formatBytes } from '../../services/cacheVaultManager';
import {
  CloudDownload,
  Download,
  X,
  FileCode,
  Package,
  FolderGit2,
  Share2,
  BookOpen,
  ExternalLink,
  Copy,
  Check,
  RefreshCw,
  Search,
  FileText,
} from 'lucide-react';

const PRESETS = [
  {
    title: 'SyncBeam Architecture Spec',
    description: 'Complete specification of the vector-clock P2P Bluetooth protocol',
    url: 'https://raw.githubusercontent.com/zevbuild/zevsync/main/README.md',
    suggestedName: 'SyncBeam_README.md',
    tag: 'DOCS',
  },
  {
    title: 'Mesh Network Schema',
    description: 'Protocol buffer & JSON schema for RFCOMM packet headers',
    url: 'https://raw.githubusercontent.com/zevbuild/zevsync/main/metadata.json',
    suggestedName: 'mesh_schema.json',
    tag: 'CONFIG',
  },
  {
    title: 'Offline Python P2P Tool',
    description: 'Python script for headless node synchronization',
    url: 'https://raw.githubusercontent.com/zevbuild/zevsync/main/tools/sync_bridge.py',
    suggestedName: 'sync_bridge.py',
    tag: 'PYTHON',
  },
];

type HubTab = 'DIRECT' | 'RELEASES' | 'REPO' | 'GIST' | 'GUIDE';

export const GitHubHubDialog: React.FC = () => {
  const {
    showGitHubHubDialog,
    downloadFromGitHub,
    fetchGitHubReleases,
    fetchGitHubContents,
    exportFileToGist,
    gitHubLatestRelease,
    gitHubRepoFiles,
    gitHubGistUrl,
    isGitHubLoading,
    gitHubDownloadProgress,
    files,
    showSnackbar,
  } = useSyncBeam();

  const [activeTab, setActiveTab] = useState<HubTab>('DIRECT');

  // Direct download inputs
  const [downloadUrl, setDownloadUrl] = useState('');
  const [customFileName, setCustomFileName] = useState('');

  // Releases inputs
  const [releaseOwner, setReleaseOwner] = useState('zevbuild');
  const [releaseRepo, setReleaseRepo] = useState('zevsync');

  // Repo browser inputs
  const [repoOwner, setRepoOwner] = useState('zevbuild');
  const [repoRepo, setRepoRepo] = useState('zevsync');
  const [repoPath, setRepoPath] = useState('');

  // Gist export inputs
  const [selectedGistFileId, setSelectedGistFileId] = useState<string>(files[0]?.id || '');
  const [isGistPublic, setIsGistPublic] = useState(false);
  const [gistToken, setGistToken] = useState('');
  const [copiedGist, setCopiedGist] = useState(false);

  const handleDirectDownload = async () => {
    if (!downloadUrl.trim()) {
      showSnackbar('Please enter a GitHub URL or file path');
      return;
    }
    await downloadFromGitHub(downloadUrl.trim(), customFileName.trim() || null);
  };

  const handleCopyGist = () => {
    if (gitHubGistUrl) {
      navigator.clipboard.writeText(gitHubGistUrl);
      setCopiedGist(true);
      setTimeout(() => setCopiedGist(false), 2000);
      showSnackbar('Copied Gist URL to clipboard');
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-3 sm:p-4">
      <div className="w-full max-w-2xl max-h-[92vh] rounded-2xl bg-[#111D3D] border border-[#1E3A8A] flex flex-col shadow-2xl overflow-hidden">
        {/* Header */}
        <div className="p-4 border-b border-[#1E3A8A] flex items-center justify-between bg-[#0B132B]/70">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-xl bg-[#1C2A4F] text-[#53EFD8] flex items-center justify-center border border-[#1E3A8A]">
              <CloudDownload className="w-4 h-4" />
            </div>
            <div>
              <h3 className="font-bold text-sm text-white flex items-center gap-1.5">
                <span>GitHub Direct Hub</span>
                <span className="text-[10px] font-mono px-1.5 py-0.2 rounded bg-indigo-900/60 text-indigo-300 border border-indigo-500/30">
                  ONLINE/OFFLINE BRIDGE
                </span>
              </h3>
              <p className="text-xs text-slate-400">
                Pull files, releases, and scripts into offline vault
              </p>
            </div>
          </div>

          <button
            type="button"
            onClick={() => showGitHubHubDialog(false)}
            className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-[#1C2A4F] transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Tab Navigation */}
        <div className="flex border-b border-[#1E3A8A] bg-[#0B132B]/40 px-3 overflow-x-auto scrollbar-none">
          {[
            { id: 'DIRECT', label: 'Direct Download', icon: Download },
            { id: 'RELEASES', label: 'Releases & APK', icon: Package },
            { id: 'REPO', label: 'Repo Files', icon: FolderGit2 },
            { id: 'GIST', label: 'Export Gist', icon: Share2 },
            { id: 'GUIDE', label: 'GitHub Guide', icon: BookOpen },
          ].map((tab) => {
            const Icon = tab.icon;
            const isSelected = activeTab === tab.id;
            return (
              <button
                key={tab.id}
                type="button"
                onClick={() => setActiveTab(tab.id as HubTab)}
                className={`flex items-center gap-1.5 py-2.5 px-3 text-xs font-semibold whitespace-nowrap border-b-2 transition-all ${
                  isSelected
                    ? 'border-[#53EFD8] text-[#53EFD8] font-bold bg-[#1C2A4F]/30'
                    : 'border-transparent text-slate-400 hover:text-slate-200'
                }`}
              >
                <Icon className="w-3.5 h-3.5" />
                <span>{tab.label}</span>
              </button>
            );
          })}
        </div>

        {/* Body */}
        <div className="p-4 flex-1 overflow-y-auto space-y-4">
          {/* TAB 1: DIRECT DOWNLOAD */}
          {activeTab === 'DIRECT' && (
            <div className="space-y-4">
              <div className="space-y-3 p-3.5 rounded-xl bg-[#0B132B] border border-[#1E3A8A]">
                <div>
                  <label className="block text-xs font-medium text-slate-300 mb-1">
                    GitHub URL or Raw Content URL
                  </label>
                  <input
                    type="text"
                    value={downloadUrl}
                    onChange={(e) => setDownloadUrl(e.target.value)}
                    placeholder="https://github.com/owner/repo/blob/main/file.txt"
                    className="w-full bg-[#111D3D] border border-[#1E3A8A] rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-[#53EFD8]"
                  />
                </div>

                <div>
                  <label className="block text-xs font-medium text-slate-300 mb-1">
                    Custom File Name (Optional)
                  </label>
                  <input
                    type="text"
                    value={customFileName}
                    onChange={(e) => setCustomFileName(e.target.value)}
                    placeholder="e.g. imported_config.json"
                    className="w-full bg-[#111D3D] border border-[#1E3A8A] rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-[#53EFD8]"
                  />
                </div>

                <button
                  type="button"
                  disabled={isGitHubLoading}
                  onClick={handleDirectDownload}
                  className="w-full py-2.5 px-4 rounded-xl text-xs font-bold bg-[#53EFD8] hover:bg-[#3be5cc] text-slate-950 flex items-center justify-center gap-2 shadow-md transition-all active:scale-95 disabled:opacity-50"
                >
                  {isGitHubLoading ? (
                    <RefreshCw className="w-4 h-4 animate-spin" />
                  ) : (
                    <Download className="w-4 h-4" />
                  )}
                  <span>{isGitHubLoading ? 'Fetching from GitHub...' : 'Download File into Vault'}</span>
                </button>
              </div>

              {/* Presets */}
              <div className="space-y-2">
                <span className="text-xs font-bold text-slate-300 block">
                  Quick Repository Presets
                </span>
                <div className="space-y-2">
                  {PRESETS.map((preset) => (
                    <div
                      key={preset.title}
                      className="rounded-xl bg-[#111D3D] border border-[#1E3A8A] p-3 flex items-center justify-between gap-3"
                    >
                      <div className="min-w-0">
                        <div className="flex items-center gap-2">
                          <span className="font-bold text-xs text-white">{preset.title}</span>
                          <span className="text-[10px] font-mono px-1.5 py-0.2 rounded bg-sky-900/40 text-sky-300 border border-sky-500/30">
                            {preset.tag}
                          </span>
                        </div>
                        <p className="text-[11px] text-slate-400 mt-0.5 truncate">
                          {preset.description}
                        </p>
                      </div>

                      <button
                        type="button"
                        onClick={() => {
                          setDownloadUrl(preset.url);
                          setCustomFileName(preset.suggestedName);
                          downloadFromGitHub(preset.url, preset.suggestedName);
                        }}
                        className="px-3 py-1.5 rounded-lg text-xs font-bold bg-[#1C2A4F] hover:bg-[#25386b] text-[#53EFD8] border border-[#1E3A8A] shrink-0 active:scale-95 transition-all"
                      >
                        Import
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}

          {/* TAB 2: RELEASES */}
          {activeTab === 'RELEASES' && (
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="block text-xs font-medium text-slate-300 mb-1">Owner</label>
                  <input
                    type="text"
                    value={releaseOwner}
                    onChange={(e) => setReleaseOwner(e.target.value)}
                    className="w-full bg-[#0B132B] border border-[#1E3A8A] rounded-xl px-3 py-2 text-xs text-white"
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-slate-300 mb-1">Repository</label>
                  <input
                    type="text"
                    value={releaseRepo}
                    onChange={(e) => setReleaseRepo(e.target.value)}
                    className="w-full bg-[#0B132B] border border-[#1E3A8A] rounded-xl px-3 py-2 text-xs text-white"
                  />
                </div>
              </div>

              <button
                type="button"
                disabled={isGitHubLoading}
                onClick={() => fetchGitHubReleases(releaseOwner, releaseRepo)}
                className="w-full py-2 px-4 rounded-xl text-xs font-bold bg-[#1C2A4F] hover:bg-[#25386b] text-white border border-[#1E3A8A] flex items-center justify-center gap-2"
              >
                {isGitHubLoading ? (
                  <RefreshCw className="w-3.5 h-3.5 animate-spin" />
                ) : (
                  <Search className="w-3.5 h-3.5 text-[#53EFD8]" />
                )}
                <span>Fetch Latest Release</span>
              </button>

              {gitHubLatestRelease && (
                <div className="rounded-xl bg-[#0B132B] border border-[#1E3A8A] p-4 space-y-3">
                  <div className="flex items-center justify-between">
                    <div>
                      <span className="font-bold text-sm text-white">
                        {gitHubLatestRelease.name || gitHubLatestRelease.tagName}
                      </span>
                      <p className="text-xs text-[#53EFD8] font-mono mt-0.5">
                        {gitHubLatestRelease.tagName}
                      </p>
                    </div>
                    <a
                      href={gitHubLatestRelease.htmlUrl}
                      target="_blank"
                      rel="noreferrer"
                      className="text-xs text-slate-400 hover:text-white flex items-center gap-1"
                    >
                      <span>View</span>
                      <ExternalLink className="w-3 h-3" />
                    </a>
                  </div>

                  <p className="text-xs text-slate-300 whitespace-pre-wrap max-h-36 overflow-y-auto font-mono bg-[#111D3D] p-2.5 rounded-lg border border-[#1E3A8A]">
                    {gitHubLatestRelease.body || 'No release description.'}
                  </p>

                  <div className="space-y-1.5">
                    <span className="text-xs font-bold text-slate-400 uppercase">
                      Release Assets ({gitHubLatestRelease.assets.length})
                    </span>
                    {gitHubLatestRelease.assets.map((asset) => (
                      <div
                        key={asset.name}
                        className="rounded-lg bg-[#111D3D] border border-[#1E3A8A] p-2.5 flex items-center justify-between"
                      >
                        <div className="min-w-0 pr-2">
                          <p className="font-semibold text-xs text-white truncate">{asset.name}</p>
                          <p className="text-[11px] text-slate-400">{formatBytes(asset.size)}</p>
                        </div>
                        <button
                          type="button"
                          onClick={() => downloadFromGitHub(asset.downloadUrl, asset.name)}
                          className="px-3 py-1 rounded-md text-xs font-bold bg-[#53EFD8] text-slate-950"
                        >
                          Download
                        </button>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

          {/* TAB 3: REPO FILES */}
          {activeTab === 'REPO' && (
            <div className="space-y-3">
              <div className="grid grid-cols-3 gap-2">
                <input
                  type="text"
                  value={repoOwner}
                  onChange={(e) => setRepoOwner(e.target.value)}
                  placeholder="Owner"
                  className="bg-[#0B132B] border border-[#1E3A8A] rounded-xl px-2.5 py-1.5 text-xs text-white"
                />
                <input
                  type="text"
                  value={repoRepo}
                  onChange={(e) => setRepoRepo(e.target.value)}
                  placeholder="Repo"
                  className="bg-[#0B132B] border border-[#1E3A8A] rounded-xl px-2.5 py-1.5 text-xs text-white"
                />
                <input
                  type="text"
                  value={repoPath}
                  onChange={(e) => setRepoPath(e.target.value)}
                  placeholder="Path"
                  className="bg-[#0B132B] border border-[#1E3A8A] rounded-xl px-2.5 py-1.5 text-xs text-white"
                />
              </div>

              <button
                type="button"
                disabled={isGitHubLoading}
                onClick={() => fetchGitHubContents(repoOwner, repoRepo, repoPath)}
                className="w-full py-2 px-4 rounded-xl text-xs font-bold bg-[#1C2A4F] hover:bg-[#25386b] text-white border border-[#1E3A8A] flex items-center justify-center gap-2"
              >
                {isGitHubLoading ? (
                  <RefreshCw className="w-3.5 h-3.5 animate-spin" />
                ) : (
                  <FolderGit2 className="w-3.5 h-3.5 text-[#53EFD8]" />
                )}
                <span>Browse Repository Tree</span>
              </button>

              <div className="space-y-1.5 max-h-72 overflow-y-auto">
                {gitHubRepoFiles.map((item) => (
                  <div
                    key={item.path}
                    className="rounded-lg bg-[#0B132B] border border-[#1E3A8A] p-2.5 flex items-center justify-between"
                  >
                    <div className="min-w-0 pr-2">
                      <p className="font-semibold text-xs text-white truncate">{item.name}</p>
                      <p className="text-[10px] text-slate-400 font-mono">
                        {item.type === 'dir' ? 'DIRECTORY' : formatBytes(item.size)}
                      </p>
                    </div>

                    {item.type === 'file' && item.downloadUrl && (
                      <button
                        type="button"
                        onClick={() => downloadFromGitHub(item.downloadUrl!, item.name)}
                        className="px-2.5 py-1 rounded text-xs font-bold bg-[#1C2A4F] text-[#53EFD8] border border-[#1E3A8A]"
                      >
                        Import
                      </button>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* TAB 4: EXPORT GIST */}
          {activeTab === 'GIST' && (
            <div className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1">
                  Select File from Offline Vault to Export
                </label>
                <select
                  value={selectedGistFileId}
                  onChange={(e) => setSelectedGistFileId(e.target.value)}
                  className="w-full bg-[#0B132B] border border-[#1E3A8A] rounded-xl px-3 py-2 text-xs text-white"
                >
                  {files.map((f) => (
                    <option key={f.id} value={f.id}>
                      {f.name} ({formatBytes(f.sizeBytes)})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1">
                  GitHub Personal Access Token (Optional)
                </label>
                <input
                  type="password"
                  value={gistToken}
                  onChange={(e) => setGistToken(e.target.value)}
                  placeholder="ghp_..."
                  className="w-full bg-[#0B132B] border border-[#1E3A8A] rounded-xl px-3 py-2 text-xs text-white"
                />
              </div>

              <div className="flex items-center justify-between p-3 rounded-xl bg-[#0B132B] border border-[#1E3A8A]">
                <div>
                  <span className="font-semibold text-xs text-white">Public Gist</span>
                  <p className="text-[11px] text-slate-400">Discoverable on GitHub gist search</p>
                </div>
                <input
                  type="checkbox"
                  checked={isGistPublic}
                  onChange={(e) => setIsGistPublic(e.target.checked)}
                  className="w-4 h-4 accent-[#53EFD8]"
                />
              </div>

              <button
                type="button"
                disabled={isGitHubLoading || !selectedGistFileId}
                onClick={() => {
                  const target = files.find((f) => f.id === selectedGistFileId);
                  if (target) {
                    exportFileToGist(target, isGistPublic, gistToken || null);
                  }
                }}
                className="w-full py-2.5 px-4 rounded-xl text-xs font-bold bg-[#53EFD8] hover:bg-[#3be5cc] text-slate-950 flex items-center justify-center gap-2"
              >
                {isGitHubLoading ? (
                  <RefreshCw className="w-4 h-4 animate-spin" />
                ) : (
                  <Share2 className="w-4 h-4" />
                )}
                <span>Export Vault File as Gist</span>
              </button>

              {gitHubGistUrl && (
                <div className="rounded-xl bg-[#064E3B] border border-[#10B981]/40 p-3.5 space-y-2">
                  <span className="text-xs font-bold text-[#10B981] block">
                    Gist Published Successfully!
                  </span>
                  <div className="flex items-center gap-2">
                    <input
                      type="text"
                      readOnly
                      value={gitHubGistUrl}
                      className="flex-1 bg-[#0B132B] border border-[#10B981]/30 rounded-lg px-2.5 py-1 text-xs text-white font-mono"
                    />
                    <button
                      type="button"
                      onClick={handleCopyGist}
                      className="px-2.5 py-1 rounded-lg text-xs font-bold bg-[#10B981] text-black flex items-center gap-1"
                    >
                      {copiedGist ? <Check className="w-3.5 h-3.5" /> : <Copy className="w-3.5 h-3.5" />}
                      <span>{copiedGist ? 'Copied' : 'Copy'}</span>
                    </button>
                  </div>
                </div>
              )}
            </div>
          )}

          {/* TAB 5: GUIDE */}
          {activeTab === 'GUIDE' && (
            <div className="space-y-3 text-xs text-slate-300 leading-relaxed">
              <h4 className="font-bold text-white text-sm">
                How ZevSync Bridges GitHub with Offline Bluetooth Mesh:
              </h4>
              <p>
                1. <strong>Direct Download</strong> fetches source files, specs, or datasets directly
                into your browser or Android device cache without needing git CLI or terminal.
              </p>
              <p>
                2. Once downloaded, files are hashed via <strong>SHA-256</strong> and registered into
                the local <strong>Vector Clock Engine</strong> with lamport lineage.
              </p>
              <p>
                3. The file can then be beamed to nearby peer devices over <strong>Bluetooth RFCOMM</strong>{' '}
                even when there is zero Internet connectivity or cellular connection in the field.
              </p>
              <p>
                4. Any offline modifications will trigger the <strong>Side-by-Side Conflict Resolver</strong>{' '}
                when peers reconnect, preventing silent overwrites.
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
