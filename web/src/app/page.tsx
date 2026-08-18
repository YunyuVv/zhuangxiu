"use client";

import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { http } from "@/lib/api";
import { isLoggedIn, getUser } from "@/lib/auth";
import type { DashboardVO } from "@/lib/types";
import BottomNav from "@/components/BottomNav";

function greet(): string {
  const h = new Date().getHours();
  if (h < 6) return "凌晨好";
  if (h < 12) return "早上好";
  if (h < 14) return "中午好";
  if (h < 18) return "下午好";
  return "晚上好";
}

const RING_C = 289.0;
const wan = (yuan: number) =>
  "¥" + (Math.abs(yuan) / 10000).toFixed(1) + "万";

export default function Home() {
  const router = useRouter();
  const [data, setData] = useState<DashboardVO | null>(null);
  const [loading, setLoading] = useState(true);
  const [failed, setFailed] = useState(false);
  const rootRef = useRef<HTMLDivElement>(null);
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    if (!isLoggedIn()) {
      router.replace("/login");
      return;
    }
    http
      .get<DashboardVO>("/projects/overview")
      .then(setData)
      .catch(() => setFailed(true))
      .finally(() => setLoading(false));
  }, [router]);

  // 数据就绪后播放进场与数字动画
  useEffect(() => {
    if (!data || !rootRef.current) return;
    const reduce = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    const root = rootRef.current;

    root.querySelectorAll<HTMLElement>(".reveal").forEach((el, i) =>
      setTimeout(() => el.classList.add("show"), reduce ? 0 : i * 90)
    );
    root.querySelectorAll<HTMLElement>("[data-w]").forEach((el) => {
      el.style.width = el.getAttribute("data-w") + "%";
    });

    const ring = root.querySelector<SVGCircleElement>("#ring");
    const setRing = (p: number) =>
      ring?.setAttribute("stroke-dashoffset", String(RING_C * (1 - p / 100)));

    const animateNum = (
      sel: string,
      to: number,
      render: (v: number) => string
    ) => {
      const el = root.querySelector(sel);
      if (!el) return;
      if (reduce) {
        el.textContent = render(to);
        return;
      }
      const start = performance.now();
      const step = (now: number) => {
        const t = Math.min(1, (now - start) / 1100);
        const v = to * (1 - Math.pow(1 - t, 3));
        el.textContent = render(v);
        if (t < 1) requestAnimationFrame(step);
        else el.textContent = render(to);
      };
      requestAnimationFrame(step);
    };

    const pct = Math.round(data.completionRate * 100);
    if (reduce) {
      setRing(pct);
      const comp = root.querySelector("#comp");
      if (comp) comp.textContent = pct + "%";
    } else {
      const start = performance.now();
      const step = (now: number) => {
        const t = Math.min(1, (now - start) / 1200);
        const p = pct * (1 - Math.pow(1 - t, 3));
        setRing(p);
        const comp = root.querySelector("#comp");
        if (comp) comp.textContent = Math.round(p) + "%";
        if (t < 1) requestAnimationFrame(step);
      };
      requestAnimationFrame(step);
    }

    animateNum("#budget", data.totalBudget, wan);
    animateNum("#spent", data.spent, wan);
    animateNum(
      "#remain",
      data.remaining,
      (v) => (data.remaining < 0 ? "超支 " + wan(v) : wan(v))
    );
  }, [data]);

  const user = mounted ? getUser() : null;
  const nickname = user?.nickname || "屋主";
  const greeting = mounted ? greet() : "你好";
  const overBudget = data ? data.overBudget : false;

  return (
    <div className="container" ref={rootRef}>
      <div className="greet reveal">
        <p className="h">
          {greeting}，{nickname}
        </p>
        <p className="s">你的装修正在稳步推进</p>
      </div>

      {loading && !data ? (
        <p className="muted" style={{ padding: 16 }}>
          加载中...
        </p>
      ) : failed || !data ? (
        <p className="muted" style={{ padding: 16 }}>
          加载失败，请下拉刷新
        </p>
      ) : (
        <>
          <div className="hero reveal">
            <div className="ring">
              <svg width="108" height="108" viewBox="0 0 104 104">
                <circle cx="52" cy="52" r="46" fill="none" stroke="rgba(255,255,255,0.22)" strokeWidth="10" />
                <circle
                  id="ring"
                  cx="52"
                  cy="52"
                  r="46"
                  fill="none"
                  stroke="#ffffff"
                  strokeWidth="10"
                  strokeLinecap="round"
                  strokeDasharray="289.0"
                  strokeDashoffset="289.0"
                />
              </svg>
              <div className="ring-center">
                <div className="rn num" id="comp">
                  0%
                </div>
                <div className="rl">装修完成度</div>
              </div>
            </div>
            <div className="hero-meta">
              <p className="t">总预算</p>
              <p className="b num" id="budget">
                0
              </p>
            </div>
          </div>

          <div className="stats reveal">
            <div className="stat">
              <div className="sn num" id="spent">
                0
              </div>
              <div className="sl">已花费</div>
            </div>
            <div className={"stat" + (overBudget ? " warn" : "")}>
              <div className="sn num" id="remain">
                0
              </div>
              <div className="sl">{overBudget ? "已超支" : "剩余预算"}</div>
            </div>
          </div>

          <div className="block reveal">
            <p className="block-title">
              我的项目 <span className="count">{data.projects.length} 个</span>
            </p>
            {data.projects.length === 0 ? (
              <div className="empty">
                <div className="empty-ico">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M3 10.5 12 3l9 7.5" />
                    <path d="M5 9.5V20a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1V9.5" />
                  </svg>
                </div>
                <p>还没有项目<br />点右下角 + 开始记录</p>
              </div>
            ) : (
              data.projects.map((p) => (
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
                    {(p.area || 0)}㎡ · 总预算 ¥{(p.totalBudget / 10000).toFixed(1)}万
                  </div>
                  <div className="bar">
                    <i data-w={Math.round(p.completionRate * 100)} />
                  </div>
                </div>
              ))
            )}
          </div>

          {data.phaseProgress.length > 0 && (
            <div className="block reveal">
              <p className="block-title">阶段进度</p>
              {data.phaseProgress.map((ph) => (
                <div className="prow" key={ph.name}>
                  <span className="pname">{ph.name}</span>
                  <span className="ptrack">
                    <i data-w={Math.round(ph.rate * 100)} />
                  </span>
                  <span className="ppct num">{Math.round(ph.rate * 100)}%</span>
                </div>
              ))}
            </div>
          )}

          {data.categoryStats.length > 0 && (
            <div className="block reveal">
              <p className="block-title">支出构成</p>
              {data.categoryStats.map((c) => {
                const max = data.categoryStats[0]?.amount || 1;
                const w = Math.max(4, Math.round((c.amount / max) * 100));
                return (
                  <div className="prow" key={c.category}>
                    <span className="pname" style={{ width: 72 }}>
                      {c.category}
                    </span>
                    <span className="ptrack">
                      <i data-w={w} />
                    </span>
                    <span className="ppct num">¥{(c.amount / 10000).toFixed(1)}万</span>
                  </div>
                );
              })}
            </div>
          )}

          <BottomNav active="home" />
        </>
      )}
    </div>
  );
}
