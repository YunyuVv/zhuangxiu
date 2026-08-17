"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { http } from "@/lib/api";
import { setToken } from "@/lib/auth";
import type { AuthResponse } from "@/lib/types";

export default function RegisterPage() {
  const router = useRouter();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [nickname, setNickname] = useState("");
  const [err, setErr] = useState("");
  const [loading, setLoading] = useState(false);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setErr("");
    setLoading(true);
    try {
      const r = await http.post<AuthResponse>("/auth/register", {
        username,
        password,
        nickname: nickname || undefined,
      });
      setToken(r.token);
      router.replace("/projects");
    } catch (e: any) {
      setErr(e?.message || "注册失败");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="container">
      <h1 className="page-title">注册</h1>
      <form onSubmit={submit}>
        <label className="field-label">用户名</label>
        <input className="input" value={username} onChange={(e) => setUsername(e.target.value)} placeholder="用于登录" />
        <label className="field-label">密码（至少 6 位）</label>
        <input className="input" type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="密码" />
        <label className="field-label">昵称（可选）</label>
        <input className="input" value={nickname} onChange={(e) => setNickname(e.target.value)} placeholder="昵称" />
        {err && (
          <p className="danger-text" style={{ marginBottom: 12 }}>
            {err}
          </p>
        )}
        <button className="btn" disabled={loading} type="submit">
          {loading ? "注册中..." : "注册并登录"}
        </button>
      </form>
      <p className="muted" style={{ marginTop: 16, textAlign: "center" }}>
        已有账号？<a className="primary-text" href="/login">去登录</a>
      </p>
    </div>
  );
}
