const TOKEN_KEY = "zx_token";
const USER_KEY = "zx_user";

export interface CurrentUser {
  userId: number;
  nickname: string;
}

export function getToken(): string {
  if (typeof window === "undefined") return "";
  return localStorage.getItem(TOKEN_KEY) || "";
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY);
}

export function isLoggedIn(): boolean {
  return getToken().length > 0;
}

export function setUser(u: CurrentUser): void {
  if (typeof window === "undefined") return;
  localStorage.setItem(USER_KEY, JSON.stringify(u));
}

export function getUser(): CurrentUser | null {
  if (typeof window === "undefined") return null;
  try {
    const s = localStorage.getItem(USER_KEY);
    return s ? (JSON.parse(s) as CurrentUser) : null;
  } catch {
    return null;
  }
}

export function logout(): void {
  clearToken();
  if (typeof window !== "undefined") localStorage.removeItem(USER_KEY);
}
