import Link from "next/link";
import { fetchGuides } from "@/lib/guide.server";
import type { GuideVO } from "@/lib/types";

// 公开内容，按请求实时渲染（SSR），无需登录
export const dynamic = "force-dynamic";

export const metadata = {
  title: "装修指南",
  description: "装修分几步走？从拆改到入住的 8 个标准阶段一文看懂。",
};

const pct = (r: number) => Math.round((r || 0) * 100);

export default async function GuideListPage() {
  let guides: GuideVO[] = [];
  let error: string | null = null;
  try {
    guides = await fetchGuides();
  } catch {
    error = "攻略加载失败，请检查网络后重试";
  }

  return (
    <div className="container">
      <div className="appbar">
        <span className="appbar-title">装修指南</span>
      </div>

      <div className="guide-hero">
        <h1 className="gh-title">装修分几步走？</h1>
        <p className="gh-sub">
          从拆改到入住，8 个标准阶段一文看懂。先搞清楚流程，再动手不慌。
        </p>
      </div>

      {error ? (
        <div className="empty">
          <div className="empty-ico">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
              <path d="M12 3 2 20h20L12 3z" />
              <path d="M12 10v4" />
              <path d="M12 17.5v.5" />
            </svg>
          </div>
          <p>{error}</p>
          <a className="btn btn-ghost" href="/guide" style={{ marginTop: 16 }}>
            重试
          </a>
        </div>
      ) : guides.length === 0 ? (
        <div className="empty">
          <p>攻略筹备中…</p>
        </div>
      ) : (
        <div className="guide-list">
          {guides.map((g, i) => (
            <Link href={`/guide/${g.stageKey}`} className="guide-stage" key={g.stageKey}>
              <div className="gs-index">{String(i + 1).padStart(2, "0")}</div>
              <div className="gs-body">
                <div className="gs-top">
                  <span className="gs-name">{g.title}</span>
                  {g.duration && <span className="tag">{g.duration}</span>}
                </div>
                {g.summary && <p className="gs-sum">{g.summary}</p>}
                <div className="gs-foot">
                  <span className="gs-cost">占总预算 ~{pct(g.costRatio)}%</span>
                  <span className="gs-go">看攻略 ›</span>
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}

      <div className="guide-cta card">
        <p className="gc-title">准备好开工了？</p>
        <p className="gc-sub">建一个装修项目，按阶段记录花销与进度，对照攻略不踩坑。</p>
        <Link className="btn" href="/login">
          开始记录我的装修
        </Link>
      </div>
    </div>
  );
}
