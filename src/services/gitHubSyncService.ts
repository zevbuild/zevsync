import {
  GitHubDownloadResult,
  GitHubFileItem,
  GitHubReleaseInfo,
  SyncedFile,
} from '../types/models';

export class GitHubSyncService {
  /**
   * Resolves raw download URL from GitHub blob/tree URLs
   */
  resolveDirectDownloadUrl(url: string): string {
    const trimmed = url.trim();
    if (trimmed.includes('raw.githubusercontent.com')) {
      return trimmed;
    }
    // Convert github.com/owner/repo/blob/branch/path to raw
    const blobMatch = trimmed.match(
      /^https?:\/\/github\.com\/([^/]+)\/([^/]+)\/blob\/([^/]+)\/(.+)$/
    );
    if (blobMatch) {
      const [, owner, repo, branch, path] = blobMatch;
      return `https://raw.githubusercontent.com/${owner}/${repo}/${branch}/${path}`;
    }
    return trimmed;
  }

  extractFileNameFromUrl(url: string): string {
    try {
      const parsed = new URL(url);
      const parts = parsed.pathname.split('/').filter(Boolean);
      return parts[parts.length - 1] || 'downloaded_file';
    } catch {
      const parts = url.split('/');
      return parts[parts.length - 1] || 'downloaded_file';
    }
  }

  async downloadDirectFile(
    rawOrGitHubUrl: string,
    customFileName?: string | null,
    onProgress?: (progress: number) => void
  ): Promise<GitHubDownloadResult> {
    const resolvedUrl = this.resolveDirectDownloadUrl(rawOrGitHubUrl);
    const fileName = customFileName || this.extractFileNameFromUrl(resolvedUrl);

    try {
      onProgress?.(0.2);
      const res = await fetch(resolvedUrl);
      if (!res.ok) {
        throw new Error(`HTTP ${res.status}: ${res.statusText}`);
      }
      onProgress?.(0.6);
      const text = await res.text();
      onProgress?.(1.0);

      return {
        success: true,
        fileName,
        bytesDownloaded: new TextEncoder().encode(text).length,
        textContent: text,
      };
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Unknown download error';
      return {
        success: false,
        fileName,
        bytesDownloaded: 0,
        textContent: null,
        errorMessage: message,
      };
    }
  }

  async fetchReleases(owner: string, repo: string): Promise<GitHubReleaseInfo> {
    const url = `https://api.github.com/repos/${owner}/${repo}/releases/latest`;
    const res = await fetch(url, {
      headers: {
        Accept: 'application/vnd.github.v3+json',
      },
    });

    if (!res.ok) {
      throw new Error(`GitHub API Error: ${res.status} ${res.statusText}`);
    }

    const data = await res.json();
    return {
      tagName: data.tag_name || 'v1.0.0',
      name: data.name || data.tag_name || 'Release',
      body: data.body || 'No release notes provided.',
      publishedAt: data.published_at || new Date().toISOString(),
      htmlUrl: data.html_url || `https://github.com/${owner}/${repo}/releases`,
      assets: (data.assets || []).map((a: { name: string; size: number; browser_download_url: string; content_type: string }) => ({
        name: a.name,
        size: a.size,
        downloadUrl: a.browser_download_url,
        contentType: a.content_type || 'application/octet-stream',
      })),
    };
  }

  async fetchContents(owner: string, repo: string, path = ''): Promise<GitHubFileItem[]> {
    const cleanPath = path.startsWith('/') ? path.slice(1) : path;
    const url = `https://api.github.com/repos/${owner}/${repo}/contents/${cleanPath}`;
    const res = await fetch(url, {
      headers: {
        Accept: 'application/vnd.github.v3+json',
      },
    });

    if (!res.ok) {
      throw new Error(`GitHub API Error: ${res.status} ${res.statusText}`);
    }

    const data = await res.json();
    if (!Array.isArray(data)) {
      // Single file returned
      return [
        {
          name: data.name,
          path: data.path,
          type: data.type === 'dir' ? 'dir' : 'file',
          size: data.size || 0,
          downloadUrl: data.download_url,
          htmlUrl: data.html_url,
        },
      ];
    }

    return data.map((item: { name: string; path: string; type: string; size?: number; download_url?: string; html_url?: string }) => ({
      name: item.name,
      path: item.path,
      type: item.type === 'dir' ? 'dir' : 'file',
      size: item.size || 0,
      downloadUrl: item.download_url || null,
      htmlUrl: item.html_url || null,
    }));
  }

  async exportGist(
    file: SyncedFile,
    isPublic: boolean,
    token?: string | null
  ): Promise<string> {
    const content = file.textPreview || file.contentData || `// File: ${file.name}`;
    const payload = {
      description: `Exported from ZevSync Offline Vault: ${file.name}`,
      public: isPublic,
      files: {
        [file.name]: {
          content,
        },
      },
    };

    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      Accept: 'application/vnd.github.v3+json',
    };
    if (token) {
      headers['Authorization'] = `token ${token}`;
    }

    const res = await fetch('https://api.github.com/gists', {
      method: 'POST',
      headers,
      body: JSON.stringify(payload),
    });

    if (!res.ok) {
      throw new Error(`Gist Creation Failed (${res.status}): ${res.statusText}`);
    }

    const data = await res.json();
    return data.html_url;
  }
}
