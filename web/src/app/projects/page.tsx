"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { http } from "@/lib/api";
import { isLoggedIn } from "@/lib/auth";
import type { PageVO, ProjectVO } from "@/lib/types";
import BottomNav from "@/components/BottomNav";

export default function ProjectsPage() {
  const router = useRouter();
  const [list, setList] = useState<ProjectVO[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isLoggedIn()) {
      router.replace("/login");
      return;
    }
    http
      .get<PageVO<ProjectVO>>("/projects", { page: 0, size: 50 })
      .then((r) => setList(r.list))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [router]);

  return (
    <div className="container">
      <h1 className="page-title">我的装修项目</h1>
      {loading ? (
        <p className="muted">加载中...</p>
      ) : list.length === 0 ? (
        <div className="empty">
          <div className="empty-ico">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
              <path d="M3 10.5 12 3l9 7.5" />
              <path d="M5 9.5V20a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1V9.5" />
              <path d="M9.5 21v-6h5v6" />
            </svg>
          </div>
          <p>还没有项目<br />点右下角 + 开始记录</p>
        </div>
      ) : (
        list.map((p) => (
          <div
            className="proj"
            key={p.id}
            onClick={() => router.push(`/projects/${p.id}`)}
          >
            <div className="proj-top">
              <span className="pnm">{p.name}</span>
              <span className="tag">{p.status}</span>
            </div>
            <div className="pmt num">
              总预算 ¥{(p.totalBudget / 10000).toFixed(1)}万
            </div>
          </div>
        ))
      )}
      <button
        className="fab"
        aria-label="新建项目"
        onClick={() => router.push("/projects/new")}
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round">
          <path d="M12 5v14M5 12h14" />
        </svg>
      </button>
      <BottomNav active="projects" />
    </div>
  );
}
