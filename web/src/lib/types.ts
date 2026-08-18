// 与后端 DTO 对齐的前端类型定义

export interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

export interface PageVO<T> {
  total: number;
  page: number;
  size: number;
  list: T[];
}

export interface AuthResponse {
  token: string;
  userId: number;
  nickname: string;
}

export interface ProjectVO {
  id: number;
  name: string;
  address?: string;
  area?: number;
  totalBudget: number;
  startDate?: string;
  expectedEndDate?: string;
  status: string;
  createdAt?: string;
}

export interface PhaseVO {
  id: number;
  name: string;
  sortOrder: number;
  startDate?: string;
  endDate?: string;
  status: string;
  budget: number;
  note?: string;
}

export interface ExpenseVO {
  id: number;
  phaseId?: number;
  category: string;
  amount: number;
  payMethod?: string;
  expenseDate: string;
  note?: string;
  createdAt?: string;
}

export interface UploadKeyResponse {
  apiKey: string;
  uploadUrl: string;
  expiresInSeconds: number;
}

export interface AttachmentSaveRequest {
  projectId: number;
  refType: "PROJECT" | "PHASE" | "EXPENSE";
  refId?: number;
  url: string;
  thumbUrl?: string;
}

export interface OverviewVO {
  totalBudget: number;
  spent: number;
  remaining: number;
  overBudget: boolean;
  phaseBudgetTotal: number;
  phaseSpentTotal: number;
  completionRate: number;
  phaseStats: { name: string; budget: number; actual: number; over: boolean }[];
  categoryStats: { category: string; amount: number }[];
}

export interface DashboardVO {
  totalBudget: number;
  spent: number;
  remaining: number;
  overBudget: boolean;
  completionRate: number;
  categoryStats: { category: string; amount: number }[];
  phaseProgress: { name: string; done: number; total: number; rate: number }[];
  projects: {
    id: number;
    name: string;
    status: string;
    area?: number;
    totalBudget: number;
    completionRate: number;
  }[];
}

export const CATEGORIES = ["设计费", "材料", "人工", "家具家电", "其他"];
export const PAY_METHODS = ["微信", "支付宝", "银行卡", "现金", "其他"];
export const PHASE_STATUSES = ["未开始", "进行中", "已完成"];
