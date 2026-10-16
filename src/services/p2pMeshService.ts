// Real P2P Mesh Communication Service using BroadcastChannel & local peer signaling

import { SyncedFile } from '../types/models';

export interface P2PPeerPayload {
  nodeId: string;
  nodeName: string;
  deviceModel: string;
  timestamp: number;
}

export type P2PMessage =
  | { type: 'ANNOUNCE'; peer: P2PPeerPayload }
  | { type: 'HEARTBEAT'; peer: P2PPeerPayload }
  | { type: 'LEAVE'; nodeId: string }
  | { type: 'FILE_BEAM'; fromNodeId: string; targetNodeId: string; file: SyncedFile };

export class P2PMeshService {
  private channel: BroadcastChannel | null = null;
  private localNodeId: string;
  private localNodeName: string;
  private localDeviceModel: string;
  private onPeerDiscoveredCallback?: (peer: P2PPeerPayload) => void;
  private onPeerLeftCallback?: (nodeId: string) => void;
  private onFileReceivedCallback?: (file: SyncedFile, fromNodeId: string) => void;
  private heartbeatInterval?: number;

  constructor(localNodeId: string, localNodeName: string, localDeviceModel: string) {
    this.localNodeId = localNodeId;
    this.localNodeName = localNodeName;
    this.localDeviceModel = localDeviceModel;

    if (typeof window !== 'undefined' && 'BroadcastChannel' in window) {
      try {
        this.channel = new BroadcastChannel('zevsync_p2p_mesh');
        this.channel.onmessage = this.handleMessage.bind(this);
      } catch (err) {
        console.warn('BroadcastChannel initialization error:', err);
      }
    }

    if (typeof window !== 'undefined') {
      window.addEventListener('beforeunload', () => {
        this.broadcast({ type: 'LEAVE', nodeId: this.localNodeId });
      });
    }
  }

  start(
    onPeerDiscovered: (peer: P2PPeerPayload) => void,
    onPeerLeft: (nodeId: string) => void,
    onFileReceived: (file: SyncedFile, fromNodeId: string) => void
  ) {
    this.onPeerDiscoveredCallback = onPeerDiscovered;
    this.onPeerLeftCallback = onPeerLeft;
    this.onFileReceivedCallback = onFileReceived;

    // Announce self immediately
    this.broadcastAnnounce();

    // Regular heartbeat every 5 seconds
    if (typeof window !== 'undefined') {
      this.heartbeatInterval = window.setInterval(() => {
        this.broadcastHeartbeat();
      }, 5000);
    }
  }

  stop() {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval);
    }
    this.broadcast({ type: 'LEAVE', nodeId: this.localNodeId });
    if (this.channel) {
      this.channel.close();
      this.channel = null;
    }
  }

  broadcastAnnounce() {
    this.broadcast({
      type: 'ANNOUNCE',
      peer: {
        nodeId: this.localNodeId,
        nodeName: this.localNodeName,
        deviceModel: this.localDeviceModel,
        timestamp: Date.now(),
      },
    });
  }

  broadcastHeartbeat() {
    this.broadcast({
      type: 'HEARTBEAT',
      peer: {
        nodeId: this.localNodeId,
        nodeName: this.localNodeName,
        deviceModel: this.localDeviceModel,
        timestamp: Date.now(),
      },
    });
  }

  beamFile(targetNodeId: string, file: SyncedFile) {
    this.broadcast({
      type: 'FILE_BEAM',
      fromNodeId: this.localNodeId,
      targetNodeId,
      file,
    });
  }

  private broadcast(msg: P2PMessage) {
    try {
      if (this.channel) {
        this.channel.postMessage(msg);
      }
    } catch (err) {
      console.warn('Failed to post message over P2P mesh channel:', err);
    }
  }

  private handleMessage(event: MessageEvent<P2PMessage>) {
    const data = event.data;
    if (!data) return;

    if (data.type === 'ANNOUNCE' || data.type === 'HEARTBEAT') {
      if (data.peer.nodeId !== this.localNodeId) {
        this.onPeerDiscoveredCallback?.(data.peer);

        // If it was an announcement, respond back with our heartbeat so the new peer knows about us immediately
        if (data.type === 'ANNOUNCE') {
          this.broadcastHeartbeat();
        }
      }
    } else if (data.type === 'LEAVE') {
      if (data.nodeId !== this.localNodeId) {
        this.onPeerLeftCallback?.(data.nodeId);
      }
    } else if (data.type === 'FILE_BEAM') {
      if (data.targetNodeId === this.localNodeId) {
        this.onFileReceivedCallback?.(data.file, data.fromNodeId);
      }
    }
  }
}
