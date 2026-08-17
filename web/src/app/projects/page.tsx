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
        <p className="muted">还没有项目，点右下角 + 新建</p>
      ) : (
        list.map((p) => (
          <div
            className="card"
            key={p.id}
            style={{ cursor: "pointer" }}
            onClick={() => router.push(`/projects/${p.id}`)}
          >
            <div className="row">
              <b>{p.name}</b>
              <span className="muted">{p.status}</span>
            </div>
            <div className="muted" style={{ marginTop: 6 }}>
              总预算 ¥{p.totalBudget}
            </div>
          </div>
        ))
      )}
      <button className="fab" onClick={() => router.push("/projects/new")}>
        +
      </button>
      <BottomNav active="projects" />
    </div>
  );
}
