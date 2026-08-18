"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { http } from "@/lib/api";
import { setToken, setUser, isLoggedIn } from "@/lib/auth";
import type { AuthResponse } from "@/lib/types";

export default function LoginPage() {
  const router = useRouter();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [err, setErr] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isLoggedIn()) router.replace("/projects");
  }, [router]);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setErr("");
    setLoading(true);
    try {
      const r = await http.post<AuthResponse>("/auth/login", { username, password });
      setToken(r.token);
      setUser({ userId: r.userId, nickname: r.nickname });
      router.replace("/projects");
    } catch (e: any) {
      setErr(e?.message || "登录失败");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth">
      <div className="auth-head">
        <div className="auth-logo">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
            <path d="M3 10.5 12 3l9 7.5" />
            <path d="M5 9.5V20a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1V9.5" />
            <path d="M9.5 21v-6h5v6" />
          </svg>
        </div>
        <h1 className="auth-title">欢迎回来</h1>
        <p className="auth-sub">记录你的装修每一步</p>
      </div>

      <div className="auth-card">
        <form onSubmit={submit}>
          <label className="field-label">用户名</label>
          <input className="input" value={username} onChange={(e) => setUsername(e.target.value)} placeholder="用户名" />
          <label className="field-label">密码</label>
          <input className="input" type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="密码" />
          {err && (
            <p className="danger-text" style={{ margin: "4px 0 12px" }}>
              {err}
            </p>
          )}
          <button className="btn btn-block" disabled={loading} type="submit">
            {loading ? "登录中..." : "登录"}
          </button>
        </form>
      </div>

      <p className="auth-foot">
        还没有账号？
        <a onClick={() => router.push("/register")} style={{ cursor: "pointer" }}>
          去注册
        </a>
      </p>
    </div>
  );
}
