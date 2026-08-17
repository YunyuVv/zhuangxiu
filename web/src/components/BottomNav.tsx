"use client";

import Link from "next/link";

export default function BottomNav({ active }: { active: string }) {
  return (
    <div className="bottom-nav">
      <Link href="/projects" className={active === "projects" ? "active" : ""}>
        项目
      </Link>
      <Link href="/login" className={active === "me" ? "active" : ""}>
        我的
      </Link>
    </div>
  );
}
