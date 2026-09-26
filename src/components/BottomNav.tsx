import React from 'react';
import { useSyncBeam } from '../context/SyncBeamContext';
import { NavigationTab } from '../types/models';
import {
  Folder,
  Radio,
  RefreshCw,
  AlertTriangle,
  HardDrive,
} from 'lucide-react';

interface TabItem {
  id: NavigationTab;
  label: string;
  testTag: string;
  icon: React.ComponentType<{ className?: string }>;
  getBadgeCount: (ui: ReturnType<typeof useSyncBeam>) => number;
  badgeColor?: string;
}

const TABS: TabItem[] = [
  {
    id: 'VAULT',
    label: 'Vault',
    testTag: 'nav_vault',
    icon: Folder,
    getBadgeCount: () => 0,
  },
  {
    id: 'RADAR',
    label: 'Radar',
    testTag: 'nav_radar',
    icon: Radio,
    getBadgeCount: (ui) => ui.discoveredPeers.filter((p) => p.isConnected).length,
    badgeColor: 'bg-[#53EFD8] text-black',
  },
  {
    id: 'LIVE_SYNC',
    label: 'Live Sync',
    testTag: 'nav_transfers',
    icon: RefreshCw,
    getBadgeCount: (ui) => ui.activeTransfers.length,
    badgeColor: 'bg-[#38BDF8] text-black',
  },
  {
    id: 'CONFLICTS',
    label: 'Conflicts',
    testTag: 'nav_conflicts',
    icon: AlertTriangle,
    getBadgeCount: (ui) => ui.pendingConflicts.length,
    badgeColor: 'bg-[#F59E0B] text-black',
  },
  {
    id: 'STORAGE',
    label: 'Storage & Mesh',
    testTag: 'nav_storage',
    icon: HardDrive,
    getBadgeCount: () => 0,
  },
];

export const BottomNav: React.FC = () => {
  const syncBeam = useSyncBeam();
  const { currentTab, selectTab } = syncBeam;

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-30 bg-[#111D3D]/95 backdrop-blur-lg border-t border-[#1E3A8A] px-2 py-1 shadow-2xl">
      <div className="max-w-md mx-auto grid grid-cols-5 gap-1">
        {TABS.map((tab) => {
          const isSelected = currentTab === tab.id;
          const Icon = tab.icon;
          const badgeCount = tab.getBadgeCount(syncBeam);

          return (
            <button
              key={tab.id}
              type="button"
              data-testid={tab.testTag}
              onClick={() => selectTab(tab.id)}
              className={`flex flex-col items-center justify-center py-2 px-1 rounded-xl transition-all duration-150 relative ${
                isSelected
                  ? 'text-[#53EFD8] font-bold bg-[#1C2A4F]/60'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-[#1C2A4F]/30'
              }`}
            >
              <div className="relative">
                <Icon
                  className={`w-5 h-5 transition-transform duration-200 ${
                    isSelected ? 'scale-110' : ''
                  }`}
                />
                {badgeCount > 0 && (
                  <span
                    className={`absolute -top-2 -right-3 text-[10px] font-black px-1.5 py-0.2 rounded-full ring-2 ring-[#111D3D] shadow-sm ${
                      tab.badgeColor || 'bg-[#53EFD8] text-black'
                    }`}
                  >
                    {badgeCount}
                  </span>
                )}
              </div>
              <span className="text-[10px] mt-1 tracking-tight truncate max-w-full">
                {tab.label}
              </span>
            </button>
          );
        })}
      </div>
    </nav>
  );
};
