"use client";

import type { ReactElement } from "react";
import Link from "next/link";

const ICONS: Record<string, ReactElement> = {
  home: (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <path d="M3 10.5 12 3l9 7.5" />
      <path d="M5 9.5V20a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1V9.5" />
      <path d="M9.5 21v-6h5v6" />
    </svg>
  ),
  projects: (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <rect x="3.5" y="4.5" width="7" height="7" rx="1.6" />
      <rect x="13.5" y="4.5" width="7" height="7" rx="1.6" />
      <rect x="3.5" y="13.5" width="7" height="7" rx="1.6" />
      <rect x="13.5" y="13.5" width="7" height="7" rx="1.6" />
    </svg>
  ),
  me: (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="8" r="3.6" />
      <path d="M4.5 20a7.5 7.5 0 0 1 15 0" />
    </svg>
  ),
};

export default function BottomNav({ active }: { active: string }) {
  return (
    <div className="bottom-nav">
      <Link href="/" className={active === "home" ? "active" : ""} aria-label="首页">
        {ICONS.home}
        <span>首页</span>
      </Link>
      <Link href="/projects" className={active === "projects" ? "active" : ""} aria-label="项目">
        {ICONS.projects}
        <span>项目</span>
      </Link>
      <Link href="/me" className={active === "me" ? "active" : ""} aria-label="我的">
        {ICONS.me}
        <span>我的</span>
      </Link>
    </div>
  );
}
