"use client";

import { useCallback, useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { http } from "@/lib/api";
import {
  CATEGORIES,
  PAY_METHODS,
  PHASE_STATUSES,
  type ExpenseVO,
  type OverviewVO,
  type PhaseVO,
  type ProjectVO,
} from "@/lib/types";
import UploadImage from "@/components/UploadImage";
import BottomNav from "@/components/BottomNav";

type Tab = "timeline" | "expense" | "overview";

export default function ProjectDetailPage() {
  const params = useParams();
  const router = useRouter();
  const projectId = Number(params.id);

  const [tab, setTab] = useState<Tab>("timeline");
  const [project, setProject] = useState<ProjectVO | null>(null);
  const [phases, setPhases] = useState<PhaseVO[]>([]);
  const [expenses, setExpenses] = useState<ExpenseVO[]>([]);
  const [overview, setOverview] = useState<OverviewVO | null>(null);
  const [loading, setLoading] = useState(true);

  const loadPhases = useCallback(async () => {
    const r = await http.get<PhaseVO[]>(`/projects/${projectId}/phases`);
    setPhases(r);
  }, [projectId]);

  const loadExpenses = useCallback(async () => {
    const r = await http.get<{ list: ExpenseVO[] }>(`/projects/${projectId}/expenses`, {
      page: 0,
      size: 200,
    });
    setExpenses(r.list);
  }, [projectId]);

  const loadOverview = useCallback(async () => {
    const r = await http.get<OverviewVO>(`/projects/${projectId}/overview`);
    setOverview(r);
  }, [projectId]);

  useEffect(() => {
    Promise.all([
      http.get<ProjectVO>(`/projects/${projectId}`),
      loadPhases(),
      loadExpenses(),
      loadOverview(),
    ])
      .then(([p]) => setProject(p))
      .catch(() => router.replace("/projects"))
      .finally(() => setLoading(false));
  }, [projectId, router, loadPhases, loadExpenses, loadOverview]);

  if (loading) return <div className="container"><p className="muted">加载中...</p></div>;

  return (
    <div className="container">
      <div className="appbar">
        <span className="appbar-back" onClick={() => router.push("/projects")}>
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M15 18l-6-6 6-6" />
          </svg>
        </span>
        <span className="appbar-title">{project?.name}</span>
        <span className="appbar-spacer" />
        <span className="tag">{project?.status}</span>
      </div>
      <p className="muted num" style={{ margin: "-6px 0 12px 4px" }}>
        总预算 ¥{(project?.totalBudget || 0) / 10000}万
      </p>

      <div className="tabs">
        <div className={`tab ${tab === "timeline" ? "active" : ""}`} onClick={() => setTab("timeline")}>时间线</div>
        <div className={`tab ${tab === "expense" ? "active" : ""}`} onClick={() => setTab("expense")}>记账</div>
        <div className={`tab ${tab === "overview" ? "active" : ""}`} onClick={() => setTab("overview")}>概览</div>
      </div>

      <div>
        {tab === "timeline" && (
          <TimelineTab phases={phases} onSaved={() => { loadPhases(); loadOverview(); }} projectId={projectId} />
        )}
        {tab === "expense" && (
          <ExpenseTab
            projectId={projectId}
            phases={phases}
            expenses={expenses}
            onChanged={() => { loadExpenses(); loadOverview(); }}
          />
        )}
        {tab === "overview" && <OverviewTab overview={overview} />}
      </div>

      <BottomNav active="projects" />
    </div>
  );
}

/* ---------------- 时间线 ---------------- */
function TimelineTab({
  phases,
  onSaved,
  projectId,
}: {
  phases: PhaseVO[];
  onSaved: () => void;
  projectId: number;
}) {
  return (
    <>
      {phases.map((ph) => (
        <PhaseCard key={ph.id} phase={ph} onSaved={onSaved} projectId={projectId} />
      ))}
    </>
  );
}

function PhaseCard({ phase, onSaved, projectId }: { phase: PhaseVO; onSaved: () => void; projectId: number }) {
  const [status, setStatus] = useState(phase.status);
  const [budget, setBudget] = useState(String(phase.budget));
  const [note, setNote] = useState(phase.note || "");
  const [saving, setSaving] = useState(false);

  async function save() {
    setSaving(true);
    try {
      await http.put(`/phases/${phase.id}`, {
        status,
        budget: budget ? Number(budget) : 0,
        note: note || undefined,
      });
      onSaved();
    } catch (e: any) {
      alert(e?.message || "保存失败");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="card">
      <div className="row">
        <b>{phase.name}</b>
        <span className="muted">第 {phase.sortOrder + 1} 步</span>
      </div>
      <label className="field-label" style={{ marginTop: 8 }}>状态</label>
      <select className="input" value={status} onChange={(e) => setStatus(e.target.value)}>
        {PHASE_STATUSES.map((s) => (
          <option key={s} value={s}>{s}</option>
        ))}
      </select>
      <label className="field-label">阶段预算（元）</label>
      <input className="input num" type="number" value={budget} onChange={(e) => setBudget(e.target.value)} />
      <label className="field-label">备注</label>
      <textarea className="textarea" value={note} onChange={(e) => setNote(e.target.value)} />
      <UploadImage projectId={projectId} refType="PHASE" refId={phase.id} label="上传阶段照片" />
      <button className="btn btn-ghost btn-block" disabled={saving} onClick={save}>
        {saving ? "保存中..." : "保存阶段"}
      </button>
    </div>
  );
}

/* ---------------- 记账 ---------------- */
function ExpenseTab({
  projectId,
  phases,
  expenses,
  onChanged,
}: {
  projectId: number;
  phases: PhaseVO[];
  expenses: ExpenseVO[];
  onChanged: () => void;
}) {
  const [category, setCategory] = useState(CATEGORIES[0]);
  const [amount, setAmount] = useState("");
  const [payMethod, setPayMethod] = useState(PAY_METHODS[0]);
  const [expenseDate, setExpenseDate] = useState(new Date().toISOString().slice(0, 10));
  const [phaseId, setPhaseId] = useState<string>("");
  const [note, setNote] = useState("");
  const [adding, setAdding] = useState(false);

  async function add() {
    if (!amount) {
      alert("请填写金额");
      return;
    }
    setAdding(true);
    try {
      await http.post(`/projects/${projectId}/expenses`, {
        category,
        amount: Number(amount),
        payMethod,
        expenseDate,
        phaseId: phaseId ? Number(phaseId) : undefined,
        note: note || undefined,
      });
      setAmount("");
      setNote("");
      onChanged();
    } catch (e: any) {
      alert(e?.message || "添加失败");
    } finally {
      setAdding(false);
    }
  }

  async function remove(id: number) {
    if (!confirm("确认删除这笔花费？")) return;
    try {
      await http.del(`/expenses/${id}`);
      onChanged();
    } catch (e: any) {
      alert(e?.message || "删除失败");
    }
  }

  return (
    <>
      <div className="card">
        <label className="field-label">分类</label>
        <select className="input" value={category} onChange={(e) => setCategory(e.target.value)}>
          {CATEGORIES.map((c) => (<option key={c} value={c}>{c}</option>))}
        </select>
        <label className="field-label">金额（元）</label>
        <input className="input num" type="number" value={amount} onChange={(e) => setAmount(e.target.value)} placeholder="0" />
        <label className="field-label">支付方式</label>
        <select className="input" value={payMethod} onChange={(e) => setPayMethod(e.target.value)}>
          {PAY_METHODS.map((m) => (<option key={m} value={m}>{m}</option>))}
        </select>
        <label className="field-label">归属阶段</label>
        <select className="input" value={phaseId} onChange={(e) => setPhaseId(e.target.value)}>
          <option value="">不关联阶段</option>
          {phases.map((p) => (<option key={p.id} value={p.id}>{p.name}</option>))}
        </select>
        <label className="field-label">日期</label>
        <input className="input" type="date" value={expenseDate} onChange={(e) => setExpenseDate(e.target.value)} />
        <label className="field-label">备注</label>
        <input className="input" value={note} onChange={(e) => setNote(e.target.value)} placeholder="选填" />
        <button className="btn btn-block" disabled={adding} onClick={add}>
          {adding ? "保存中..." : "添加花费"}
        </button>
      </div>

      {expenses.length === 0 ? (
        <p className="muted">还没有花费记录</p>
      ) : (
        expenses.map((e) => (
          <div className="card" key={e.id}>
            <div className="row">
              <b>{e.category}</b>
              <span className="num">¥{e.amount}</span>
            </div>
            <div className="muted num" style={{ marginTop: 4 }}>
              {e.expenseDate} · {e.payMethod || "—"}
              {e.phaseId ? ` · ${phases.find((p) => p.id === e.phaseId)?.name || ""}` : ""}
            </div>
            {e.note && <div className="muted" style={{ marginTop: 4 }}>{e.note}</div>}
            <button className="btn btn-danger btn-block" style={{ marginTop: 10 }} onClick={() => remove(e.id)}>删除</button>
          </div>
        ))
      )}
    </>
  );
}

/* ---------------- 概览 ---------------- */
function OverviewTab({ overview }: { overview: OverviewVO | null }) {
  if (!overview) return <p className="muted">暂无数据</p>;
  const pct = Math.round(overview.completionRate * 100);
  return (
    <>
      <div className="card">
        <div className="row"><span className="muted">总预算</span><b className="num">¥{overview.totalBudget}</b></div>
        <div className="row" style={{ marginTop: 8 }}><span className="muted">已花费</span><b className="num">¥{overview.spent}</b></div>
        <div className="row" style={{ marginTop: 8 }}>
          <span className="muted">剩余</span>
          <b className={"num " + (overview.overBudget ? "danger-text" : "primary-text")}>
            ¥{overview.remaining} {overview.overBudget ? "（已超支）" : ""}
          </b>
        </div>
        <div className="muted" style={{ marginTop: 8 }}>
          完成度 {pct}%
        </div>
      </div>

      <p className="block-title" style={{ margin: "14px 4px 10px" }}>各阶段预算 vs 实际</p>
      {overview.phaseStats.map((s) => (
        <div className="card" key={s.name} style={{ padding: 12 }}>
          <div className="row">
            <span>{s.name}</span>
            <span className={"num " + (s.over ? "danger-text" : "")}>
              ¥{s.budget} / ¥{s.actual} {s.over ? "超支" : ""}
            </span>
          </div>
        </div>
      ))}

      <p className="block-title" style={{ margin: "14px 4px 10px" }}>分类占比</p>
      {overview.categoryStats.length === 0 ? (
        <p className="muted">暂无花费</p>
      ) : (
        overview.categoryStats.map((c) => (
          <div className="card" key={c.category} style={{ padding: 12 }}>
            <div className="row">
              <span>{c.category}</span>
              <b className="num">¥{c.amount}</b>
            </div>
          </div>
        ))
      )}
    </>
  );
}
