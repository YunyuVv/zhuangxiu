"use client";

import { useRef, useState } from "react";
import { http } from "@/lib/api";
import type { AttachmentSaveRequest, UploadKeyResponse } from "@/lib/types";

interface Props {
  projectId: number;
  refType: "PROJECT" | "PHASE" | "EXPENSE";
  refId?: number;
  onUploaded?: (url: string) => void;
  label?: string;
}

const KEY_CACHE = "zx_imgbb";

function getCachedKey(): { key: string; url: string; exp: number } | null {
  try {
    const s = localStorage.getItem(KEY_CACHE);
    if (!s) return null;
    const o = JSON.parse(s);
    if (o.exp < Date.now()) return null;
    return o;
  } catch {
    return null;
  }
}

/**
 * 图片上传组件：
 * 1. 前端 Canvas 压缩（最长边 1920，质量 0.8）
 * 2. 取后端下发的 imgbb Key（缓存 24h）
 * 3. 直传 imgbb，拿回 URL
 * 4. 回传后端 /attachment 持久化
 */
export default function UploadImage({ projectId, refType, refId, onUploaded, label = "上传图片" }: Props) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [busy, setBusy] = useState(false);
  const [preview, setPreview] = useState("");

  async function ensureKey(): Promise<UploadKeyResponse> {
    const cached = getCachedKey();
    if (cached) {
      return {
        apiKey: cached.key,
        uploadUrl: cached.url,
        expiresInSeconds: (cached.exp - Date.now()) / 1000,
      };
    }
    const r = await http.get<UploadKeyResponse>("/attachment/upload-key");
    localStorage.setItem(
      KEY_CACHE,
      JSON.stringify({ key: r.apiKey, url: r.uploadUrl, exp: Date.now() + r.expiresInSeconds * 1000 })
    );
    return r;
  }

  async function compress(file: File): Promise<Blob> {
    const img = new Image();
    const url = URL.createObjectURL(file);
    await new Promise<void>((res, rej) => {
      img.onload = () => res();
      img.onerror = rej;
      img.src = url;
    });
    URL.revokeObjectURL(url);
    const max = 1920;
    let { width, height } = img;
    if (width > max || height > max) {
      const ratio = Math.min(max / width, max / height);
      width = Math.round(width * ratio);
      height = Math.round(height * ratio);
    }
    const canvas = document.createElement("canvas");
    canvas.width = width;
    canvas.height = height;
    canvas.getContext("2d")!.drawImage(img, 0, 0, width, height);
    return await new Promise<Blob>((res) => canvas.toBlob((b) => res(b!), "image/jpeg", 0.8));
  }

  async function onChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;
    setBusy(true);
    try {
      const blob = await compress(file);
      const { apiKey, uploadUrl } = await ensureKey();
      const fd = new FormData();
      fd.append("key", apiKey);
      fd.append("image", blob, file.name || "image.jpg");
      const resp = await fetch(uploadUrl, { method: "POST", body: fd });
      const json = await resp.json();
      if (!json.success) throw new Error(json.error?.message || "上传失败");
      const url: string = json.data.url;
      const thumb: string = json.data.thumb?.url || url;
      setPreview(url);
      const body: AttachmentSaveRequest = { projectId, refType, refId, url, thumbUrl: thumb };
      await http.post("/attachment", body);
      onUploaded?.(url);
    } catch (err: any) {
      alert(err?.message || "上传失败");
    } finally {
      setBusy(false);
      if (inputRef.current) inputRef.current.value = "";
    }
  }

  return (
    <div>
      <button type="button" className="btn btn-ghost" disabled={busy} onClick={() => inputRef.current?.click()}>
        {busy ? "上传中..." : label}
      </button>
      <input ref={inputRef} type="file" accept="image/*" hidden onChange={onChange} />
      {preview && (
        // eslint-disable-next-line @next/next/no-img-element
        <img src={preview} alt="预览" style={{ width: "100%", marginTop: 8, borderRadius: "var(--r-sm)" }} />
      )}
    </div>
  );
}
