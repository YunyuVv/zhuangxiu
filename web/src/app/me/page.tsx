"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { isLoggedIn, getUser, logout } from "@/lib/auth";
import BottomNav from "@/components/BottomNav";

export default function MePage() {
  const router = useRouter();
  const [ready, setReady] = useState(false);

  useEffect(() => {
    if (!isLoggedIn()) {
      router.replace("/login");
      return;
    }
    setReady(true);
  }, [router]);

  if (!ready) return null;

  const user = getUser();
  const initial = (user?.nickname || "屋")[0];

  return (
    <div className="container">
      <h1 className="page-title">我的</h1>

      <div className="profile">
        <div className="avatar">{initial}</div>
        <div>
          <p className="name">{user?.nickname || "用户"}</p>
          <p className="uid num">用户ID：{user?.userId}</p>
        </div>
      </div>

      <div className="menu">
        <div className="menu-item" onClick={() => router.push("/projects")}>
          <span className="mi-ico">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
              <rect x="3.5" y="4.5" width="7" height="7" rx="1.6" />
              <rect x="13.5" y="4.5" width="7" height="7" rx="1.6" />
              <rect x="3.5" y="13.5" width="7" height="7" rx="1.6" />
              <rect x="13.5" y="13.5" width="7" height="7" rx="1.6" />
            </svg>
          </span>
          <span>我的项目</span>
          <span className="mi-arrow">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M9 18l6-6-6-6" />
            </svg>
          </span>
        </div>

        <div
          className="menu-item"
          style={{ color: "var(--danger)" }}
          onClick={() => {
            logout();
            router.replace("/login");
          }}
        >
          <span className="mi-ico" style={{ background: "var(--danger-soft)", color: "var(--danger)" }}>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
              <path d="M15 12H4" />
              <path d="M9 7l-5 5 5 5" />
              <path d="M14 4h4a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2h-4" />
            </svg>
          </span>
          <span>退出登录</span>
        </div>
      </div>

      <BottomNav active="me" />
    </div>
  );
}
