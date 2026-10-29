import { Template, Task, UploadResult } from "./types";

const BASE = process.env.NEXT_PUBLIC_API_BASE || "http://localhost:8080";

/** 简易用户身份：每个浏览器一个稳定 UUID，存 localStorage，用作 X-User-Id（R-10）。 */
export function getUserId(): string {
  if (typeof window === "undefined") return "demo-user";
  let id = localStorage.getItem("ms_user_id");
  if (!id) {
    id = "u-" + crypto.randomUUID();
    localStorage.setItem("ms_user_id", id);
  }
  return id;
}

/**
 * 把后端返回的相对资源地址（/demo/..、/files/..）拼成可访问的绝对地址。
 * 绝对地址（http(s)://）原样返回。避免 <img> 被请求到前端 3000 端口而 404。
 */
export function assetUrl(path: string | null | undefined): string {
  if (!path) return "";
  if (/^https?:\/\//i.test(path)) return path;
  return BASE + path;
}

async function req<T>(path: string, init?: RequestInit): Promise<T> {
  const headers: Record<string, string> = { ...(init?.headers as Record<string, string> | undefined) };
  headers["X-User-Id"] = getUserId();
  const res = await fetch(BASE + path, { ...init, headers });
  if (!res.ok) {
    let msg = res.statusText;
    try {
      const e = await res.json();
      msg = e.detail || e.error || msg;
    } catch {
      /* ignore */
    }
    throw new Error(msg);
  }
  return res.json() as Promise<T>;
}

export const api = {
  listTemplates: (category?: string) =>
    req<Template[]>(
      `/api/templates${category && category !== "ALL" ? `?category=${encodeURIComponent(category)}` : ""}`
    ),
  listCategories: () => req<string[]>(`/api/templates/categories`),
  getTemplate: (id: string) => req<Template>(`/api/templates/${id}`),

  upload: async (file: File, templateId?: string): Promise<UploadResult> => {
    const form = new FormData();
    form.append("file", file);
    if (templateId) form.append("templateId", templateId);
    const headers: Record<string, string> = { "X-User-Id": getUserId() };
    const res = await fetch(BASE + "/api/upload", { method: "POST", body: form, headers });
    if (!res.ok) {
      let msg = res.statusText;
      try {
        const e = await res.json();
        msg = e.detail || e.error || msg;
      } catch {
        /* ignore */
      }
      throw new Error(msg);
    }
    return (await res.json()) as UploadResult;
  },

  submit: (templateId: string, fileUrls: string[]) =>
    req<Task>(`/api/templates/${templateId}/tasks`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ fileUrls })
    }),

  getTask: (id: string) => req<Task>(`/api/tasks/${id}`),
  listTasks: () => req<Task[]>(`/api/tasks`)
};
