"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { Task } from "@/lib/types";
import { api, assetUrl } from "@/lib/api";
import TaskStatusBadge from "@/components/TaskStatusBadge";
import { useTaskTracker } from "@/lib/useTasks";

export default function HistoryPage() {
  const [seed, setSeed] = useState<Task[]>([]);
  const [loaded, setLoaded] = useState(false);
  const { tasks } = useTaskTracker(seed);

  useEffect(() => {
    let alive = true;
    api
      .listTasks()
      .then((list) => alive && setSeed(list))
      .catch(() => {})
      .finally(() => alive && setLoaded(true));
    return () => {
      alive = false;
    };
  }, []);

  return (
    <>
      <h1 className="page-title">历史记录</h1>
      {!loaded && <div className="muted">加载中…</div>}
      {loaded && tasks.length === 0 && (
        <div className="notice">
          还没有制作任务。去{" "}
          <Link href="/" style={{ color: "var(--primary)" }}>
            模板库
          </Link>{" "}
          创建一个吧。
        </div>
      )}

      {tasks.map((t) => (
        <div className="task-row" key={t.id}>
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img className="thumb" src={assetUrl(t.resultUrl || "/demo/xmas-cover.jpg")} alt="" />
          <div className="grow">
            <div style={{ display: "flex", gap: 10, alignItems: "center" }}>
              <strong>{t.templateTitle}</strong>
              <TaskStatusBadge status={t.status} />
            </div>
            <div className="sub">
              {new Date(t.createdAt).toLocaleString()} · 当前阶段：{t.stageDetail || "—"}
            </div>
            {t.status === "FAILED" && <div className="error">失败原因：{t.failReason}</div>}
            {t.status === "SUCCESS" && t.resultUrl && (
              // eslint-disable-next-line @next/next/no-img-element
              <img
                src={assetUrl(t.resultUrl)}
                alt="结果"
                style={{ marginTop: 8, maxWidth: 200, borderRadius: 8, border: "1px solid var(--border)" }}
              />
            )}
          </div>
        </div>
      ))}
    </>
  );
}
