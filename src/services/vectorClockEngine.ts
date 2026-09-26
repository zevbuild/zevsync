export type VectorClockComparison =
  | 'IDENTICAL'
  | 'LOCAL_NEWER'
  | 'REMOTE_NEWER'
  | 'CONCURRENT_CONFLICT';

export class VectorClockEngine {
  private localDeviceId: string;

  constructor(localDeviceId: string) {
    this.localDeviceId = localDeviceId;
  }

  parseClock(json: string | null | undefined): Record<string, number> {
    if (!json || json === '{}' || json.trim() === '') return {};
    try {
      const parsed = JSON.parse(json);
      if (typeof parsed === 'object' && parsed !== null) {
        const result: Record<string, number> = {};
        for (const [k, v] of Object.entries(parsed)) {
          result[k] = Number(v) || 0;
        }
        return result;
      }
      return {};
    } catch {
      return {};
    }
  }

  serializeClock(clock: Record<string, number>): string {
    return JSON.stringify(clock);
  }

  incrementLocal(clockJson: string | null | undefined): [string, number] {
    const clock = { ...this.parseClock(clockJson) };
    const currentLocalVersion = clock[this.localDeviceId] ?? 0;
    const newLocalVersion = currentLocalVersion + 1;
    clock[this.localDeviceId] = newLocalVersion;
    return [this.serializeClock(clock), newLocalVersion];
  }

  compare(
    localJson: string | null | undefined,
    remoteJson: string | null | undefined
  ): VectorClockComparison {
    const local = this.parseClock(localJson);
    const remote = this.parseClock(remoteJson);

    const localKeys = Object.keys(local);
    const remoteKeys = Object.keys(remote);

    if (localKeys.length === 0 && remoteKeys.length === 0) return 'IDENTICAL';
    if (localKeys.length === 0) return 'REMOTE_NEWER';
    if (remoteKeys.length === 0) return 'LOCAL_NEWER';

    let localHasGreater = false;
    let remoteHasGreater = false;

    const allKeys = Array.from(new Set([...localKeys, ...remoteKeys]));
    for (const key of allKeys) {
      const lVal = local[key] ?? 0;
      const rVal = remote[key] ?? 0;
      if (lVal > rVal) {
        localHasGreater = true;
      } else if (rVal > lVal) {
        remoteHasGreater = true;
      }
    }

    if (localHasGreater && !remoteHasGreater) return 'LOCAL_NEWER';
    if (!localHasGreater && remoteHasGreater) return 'REMOTE_NEWER';
    if (!localHasGreater && !remoteHasGreater) return 'IDENTICAL';
    return 'CONCURRENT_CONFLICT';
  }

  merge(
    localJson: string | null | undefined,
    remoteJson: string | null | undefined
  ): string {
    const local = this.parseClock(localJson);
    const remote = this.parseClock(remoteJson);
    const merged: Record<string, number> = {};

    const allKeys = Array.from(new Set([...Object.keys(local), ...Object.keys(remote)]));
    for (const key of allKeys) {
      const lVal = local[key] ?? 0;
      const rVal = remote[key] ?? 0;
      merged[key] = Math.max(lVal, rVal);
    }
    return this.serializeClock(merged);
  }

  nextLamport(localLamport: number, remoteLamport = 0): number {
    return Math.max(localLamport, remoteLamport) + 1;
  }
}
