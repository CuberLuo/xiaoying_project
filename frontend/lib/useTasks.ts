"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { Task } from "./types";
import { api } from "./api";

/**
 * 前端异步状态跟踪（R-07/R-08/R-09）：
 *  - track(task) 加入一个新受理的任务；
 *  - 仅当存在“未完成任务”时才轮询（1.5s），全部完成即停止轮询；
 *  - 查询异常时保留已有任务信息，不把后台任务标记为失败；
 *  - 组件卸载时清理定时器，避免旧请求覆盖其他任务/用户数据；
 *  - 刷新页面后用 listTasks() 重新 seed，可恢复历史并继续跟踪未结束任务。
 *  - 轮询间隔可配置（pollMs），生产默认 1.5s，测试用短间隔。
 */
export function useTaskTracker(seed: Task[] = [], pollMs = 1500) {
  const [tasks, setTasks] = useState<Record<string, Task>>(() =>
    Object.fromEntries(seed.map((t) => [t.id, t] as [string, Task]))
  );
  const tasksRef = useRef(tasks);
  tasksRef.current = tasks;

  const track = useCallback((t: Task) => {
    setTasks((prev) => (prev[t.id] ? prev : { ...prev, [t.id]: t }));
  }, []);

  const hasUnfinished = Object.values(tasks).some(
    (t) => t.status === "PENDING" || t.status === "PROCESSING"
  );

  useEffect(() => {
    if (!hasUnfinished) return;
    let cancelled = false;
    const id = setInterval(async () => {
      const list = Object.values(tasksRef.current).filter(
        (t) => t.status === "PENDING" || t.status === "PROCESSING"
      );
      for (const t of list) {
        try {
          const updated = await api.getTask(t.id);
          // 防御：后端返回空/异常数据时不写入，避免污染已有任务状态
          if (!cancelled && updated && updated.id) {
            setTasks((prev) => ({ ...prev, [t.id]: updated }));
          }
        } catch {
          // 查询异常：保留已有任务信息，不覆盖、不标记失败（R-07）
        }
      }
    }, pollMs);
    return () => {
      cancelled = true;
      clearInterval(id);
    };
  }, [hasUnfinished, pollMs]);

  const list = Object.values(tasks).sort((a, b) => b.createdAt.localeCompare(a.createdAt));
  return { tasks: list, track };
}
