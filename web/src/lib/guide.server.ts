import type { GuideVO } from "./types";

// 服务端取数：直连后端（不走 /api 代理，代理仅作用于浏览器到 Next 的请求）。
// 开发期后端在 9096；容器/生产通过 BACKEND_URL 环境变量覆盖。
const BASE = process.env.BACKEND_URL || "http://localhost:9096";

async function getJson<T>(path: string): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    cache: "no-store",
    headers: { Accept: "application/json" },
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  const body = (await res.json()) as { code: number; message: string; data: T };
  if (body.code !== 0) throw new Error(body.message || "请求失败");
  return body.data;
}

/** 8 阶段全景（列表页）。失败时抛错由页面层降级处理。 */
export function fetchGuides(): Promise<GuideVO[]> {
  return getJson<GuideVO[]>("/api/guides");
}

/** 单阶段攻略（详情页）。 */
export function fetchGuide(stageKey: string): Promise<GuideVO> {
  return getJson<GuideVO>(`/api/guides/${encodeURIComponent(stageKey)}`);
}
