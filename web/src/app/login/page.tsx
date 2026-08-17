"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { http } from "@/lib/api";
import { setToken } from "@/lib/auth";
import type { AuthResponse } from "@/lib/types";

export default function LoginPage() {
  const router = useRouter();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [err, setErr] = useState("");
  const [loading, setLoading] = useState(false);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setErr("");
    setLoading(true);
    try {
      const r = await http.post<AuthResponse>("/auth/login", { username, password });
      setToken(r.token);
      router.replace("/projects");
    } catch (e: any) {
      setErr(e?.message || "登录失败");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="container">
      <h1 className="page-title">登录</h1>
      <form onSubmit={submit}>
        <label className="field-label">用户名</label>
        <input className="input" value={username} onChange={(e) => setUsername(e.target.value)} placeholder="用户名" />
        <label className="field-label">密码</label>
        <input className="input" type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="密码" />
        {err && (
          <p className="danger-text" style={{ marginBottom: 12 }}>
            {err}
          </p>
        )}
        <button className="btn" disabled={loading} type="submit">
          {loading ? "登录中..." : "登录"}
        </button>
      </form>
      <p className="muted" style={{ marginTop: 16, textAlign: "center" }}>
        还没有账号？<a className="primary-text" href="/register">去注册</a>
      </p>
    </div>
  );
}
