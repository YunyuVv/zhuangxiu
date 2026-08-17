"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { http } from "@/lib/api";
import type { ProjectVO } from "@/lib/types";
import BottomNav from "@/components/BottomNav";

export default function NewProjectPage() {
  const router = useRouter();
  const [form, setForm] = useState({
    name: "",
    address: "",
    area: "",
    totalBudget: "",
    startDate: "",
    expectedEndDate: "",
  });
  const [err, setErr] = useState("");
  const [loading, setLoading] = useState(false);

  function set(k: string, v: string) {
    setForm((f) => ({ ...f, [k]: v }));
  }

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setErr("");
    if (!form.name || !form.totalBudget) {
      setErr("请填写项目名称与总预算");
      return;
    }
    setLoading(true);
    try {
      const r = await http.post<ProjectVO>("/projects", {
        name: form.name,
        address: form.address || undefined,
        area: form.area ? Number(form.area) : undefined,
        totalBudget: Number(form.totalBudget),
        startDate: form.startDate || undefined,
        expectedEndDate: form.expectedEndDate || undefined,
      });
      router.replace(`/projects/${r.id}`);
    } catch (e: any) {
      setErr(e?.message || "创建失败");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="container">
      <h1 className="page-title">新建装修项目</h1>
      <form onSubmit={submit}>
        <label className="field-label">项目名称 *</label>
        <input className="input" value={form.name} onChange={(e) => set("name", e.target.value)} placeholder="如：中海·锦城 3 室 1 厅" />
        <label className="field-label">房屋地址</label>
        <input className="input" value={form.address} onChange={(e) => set("address", e.target.value)} placeholder="选填" />
        <label className="field-label">面积（㎡）</label>
        <input className="input" type="number" value={form.area} onChange={(e) => set("area", e.target.value)} placeholder="选填" />
        <label className="field-label">总预算（元）*</label>
        <input className="input" type="number" value={form.totalBudget} onChange={(e) => set("totalBudget", e.target.value)} placeholder="如 200000" />
        <label className="field-label">开工日期</label>
        <input className="input" type="date" value={form.startDate} onChange={(e) => set("startDate", e.target.value)} />
        <label className="field-label">预计完工日期</label>
        <input className="input" type="date" value={form.expectedEndDate} onChange={(e) => set("expectedEndDate", e.target.value)} />
        {err && (
          <p className="danger-text" style={{ marginBottom: 12 }}>
            {err}
          </p>
        )}
        <button className="btn" disabled={loading} type="submit">
          {loading ? "创建中..." : "创建（自动初始化 8 个阶段）"}
        </button>
      </form>
      <BottomNav active="projects" />
    </div>
  );
}
