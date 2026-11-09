import Link from "next/link";
import { fetchGuide, fetchGuides } from "@/lib/guide.server";
import type { GuideVO } from "@/lib/types";

export const dynamic = "force-dynamic";

const pct = (r: number) => Math.round((r || 0) * 100);

export async function generateMetadata({
  params,
}: {
  params: Promise<{ stageKey: string }>;
}) {
  const { stageKey } = await params;
  try {
    const g = await fetchGuide(stageKey);
    return { title: `${g.title} · 装修攻略` };
  } catch {
    return { title: "装修攻略" };
  }
}

export default async function GuideDetailPage({
  params,
}: {
  params: Promise<{ stageKey: string }>;
}) {
  const { stageKey } = await params;

  let guide: GuideVO | null = null;
  let error: string | null = null;
  try {
    guide = await fetchGuide(stageKey);
  } catch {
    error = "攻略加载失败，请检查网络后重试";
  }

  let all: GuideVO[] = [];
  try {
    all = await fetchGuides();
  } catch {
    /* 上一步/下一步依赖列表，失败则隐藏导航 */
  }

  if (error || !guide) {
    return (
      <div className="container">
        <div className="appbar">
          <Link href="/guide" className="appbar-back" aria-label="返回">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M15 5l-7 7 7 7" />
            </svg>
          </Link>
          <span className="appbar-title">装修攻略</span>
        </div>
        <div className="empty">
          <div className="empty-ico">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
              <path d="M12 3 2 20h20L12 3z" />
              <path d="M12 10v4" />
              <path d="M12 17.5v.5" />
            </svg>
          </div>
          <p>{error || "攻略不存在"}</p>
          <a className="btn btn-ghost" href={`/guide/${stageKey}`} style={{ marginTop: 16 }}>
            重试
          </a>
        </div>
      </div>
    );
  }

  const idx = all.findIndex((g) => g.stageKey === guide!.stageKey);
  const total = all.length || 8;
  const prev = idx > 0 ? all[idx - 1] : null;
  const next = idx >= 0 && idx < all.length - 1 ? all[idx + 1] : null;

  return (
    <div className="container">
      <div className="appbar">
        <Link href="/guide" className="appbar-back" aria-label="返回">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M15 5l-7 7 7 7" />
          </svg>
        </Link>
        <span className="appbar-title">{guide.title}</span>
      </div>

      <div className="guide-detail-hero">
        <div className="gdh-index">
          {String(idx + 1).padStart(2, "0")} / {String(total).padStart(2, "0")}
        </div>
        <h1 className="gdh-name">{guide.title}</h1>
        <div className="gdh-meta">
          {guide.duration && <span className="tag">{guide.duration}</span>}
          <span className="tag tag-warn">占总预算 ~{pct(guide.costRatio)}%</span>
        </div>
        {guide.summary && <p className="gdh-sum">{guide.summary}</p>}
      </div>

      {guide.details && guide.details.length > 0 ? (
        <div className="guide-sections">
          {guide.details.map((d) => (
            <div className="card gsection" key={d.section}>
              <div className="gs-sec-label">{d.section}</div>
              <p className="gs-sec-content">{d.content}</p>
            </div>
          ))}
        </div>
      ) : (
        <div className="empty">
          <p>该阶段完整攻略筹备中，先看看上方阶段说明。</p>
        </div>
      )}

      {(prev || next) && (
        <div className="guide-nav">
          {prev ? (
            <Link className="gn-item" href={`/guide/${prev.stageKey}`}>
              <span className="gn-dir">‹ 上一步</span>
              <span className="gn-name">{prev.title}</span>
            </Link>
          ) : (
            <span />
          )}
          {next ? (
            <Link className="gn-item gn-right" href={`/guide/${next.stageKey}`}>
              <span className="gn-dir">下一步 ›</span>
              <span className="gn-name">{next.title}</span>
            </Link>
          ) : (
            <span />
          )}
        </div>
      )}

      <div className="guide-cta card">
        <p className="gc-title">开始「{guide.title}」阶段</p>
        <p className="gc-sub">建项目后按阶段记录花销与资料，对照攻略不踩坑。</p>
        <Link className="btn" href="/login">
          记录这一阶段的花费
        </Link>
      </div>
    </div>
  );
}
