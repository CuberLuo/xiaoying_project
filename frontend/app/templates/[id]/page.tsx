"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { Template, UploadResult } from "@/lib/types";
import { api, assetUrl } from "@/lib/api";
import UploadSlot from "@/components/UploadSlot";
import TaskStatusBadge from "@/components/TaskStatusBadge";
import { useTaskTracker } from "@/lib/useTasks";

type LoadState = "loading" | "ok" | "notfound" | "error";

export default function TemplateDetailPage() {
  const params = useParams<{ id: string }>();
  const id = params.id;
  const router = useRouter();

  const [tpl, setTpl] = useState<Template | null>(null);
  const [loadState, setLoadState] = useState<LoadState>("loading");
  const [uploads, setUploads] = useState<(UploadResult | null)[]>([]);
  const [submitting, setSubmitting] = useState(false);
  const [slotError, setSlotError] = useState<string | null>(null);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const { tasks, track } = useTaskTracker();

  useEffect(() => {
    let alive = true;
    setLoadState("loading");
    api
      .getTemplate(id)
      .then((t) => {
        if (!alive) return;
        setTpl(t);
        setUploads(Array(t.requiredImageCount).fill(null));
        setLoadState("ok");
      })
      .catch((e) => {
        if (!alive) return;
        const msg = e instanceof Error ? e.message : "";
        setLoadState(msg.includes("不存在") ? "notfound" : "error");
      });
    return () => {
      alive = false;
    };
  }, [id]);

  if (loadState === "loading") return <div className="muted">加载中…</div>;
  if (loadState === "notfound") return <div className="notice">模板不存在。</div>;
  if (loadState === "error") return <div className="notice">加载失败，请稍后重试。</div>;
  if (!tpl) return null;

  const allFilled = uploads.filter(Boolean).length === tpl.requiredImageCount;
  const createdTask = tasks.find((t) => t.templateId === tpl.id);

  async function submit() {
    setSubmitting(true);
    setSubmitError(null);
    try {
      const task = await api.submit(
        tpl!.id,
        uploads.filter(Boolean).map((u) => u!.url)
      );
      // 受理成功后立即交给跟踪器，按钮不再保持“提交中”（R-07）
      track(task);
    } catch (e) {
      setSubmitError(e instanceof Error ? e.message : "提交失败");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <>
      <button className="btn" onClick={() => router.push("/")}>
        ← 返回
      </button>

      <div className="detail-head" style={{ marginTop: 16 }}>
        {/* eslint-disable-next-line @next/next/no-img-element */}
        <img src={assetUrl(tpl.coverUrl)} alt={tpl.title} />
        <div className="detail-info">
          <h1 className="page-title" style={{ margin: 0 }}>
            {tpl.title}
          </h1>
          <div className="muted">{tpl.description}</div>
          <div className="meta" style={{ marginTop: 8 }}>
            <span className="tag">{tpl.category}</span>需要 {tpl.requiredImageCount} 张图片
          </div>
        </div>
      </div>

      <div className="section">
        <h2 className="page-title" style={{ fontSize: 18 }}>
          上传素材
        </h2>
        <div className="slots">
          {uploads.map((u, i) => (
            <UploadSlot
              key={i}
              index={i}
              templateId={tpl.id}
              value={u}
              onChange={(v) => setUploads((prev) => prev.map((p, idx) => (idx === i ? v : p)))}
              onError={setSlotError}
            />
          ))}
        </div>
        {slotError && <div className="error">{slotError}</div>}

        <div style={{ marginTop: 16, display: "flex", gap: 12, alignItems: "center" }}>
          <button className="btn btn-primary" disabled={!allFilled || submitting} onClick={submit}>
            {submitting ? (
              <>
                <span className="spinner" /> 提交中…
              </>
            ) : (
              "提交制作"
            )}
          </button>
          {!allFilled && (
            <span className="muted">请先上传全部 {tpl.requiredImageCount} 张图片</span>
          )}
        </div>
        {submitError && <div className="error">{submitError}</div>}
      </div>

      {createdTask && (
        <div className="section">
          <h2 className="page-title" style={{ fontSize: 18 }}>
            制作任务
          </h2>
          <div className="task-row">
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img className="thumb" src={assetUrl(tpl.coverUrl)} alt="" />
            <div className="grow">
              <div style={{ display: "flex", gap: 10, alignItems: "center" }}>
                <strong>{tpl.title}</strong>
                <TaskStatusBadge status={createdTask.status} />
              </div>
              <div className="sub">{createdTask.stageDetail || "—"}</div>
              {createdTask.status === "FAILED" && (
                <div className="error">失败原因：{createdTask.failReason}</div>
              )}
              {createdTask.status === "SUCCESS" && createdTask.resultUrl && (
                // eslint-disable-next-line @next/next/no-img-element
                <img
                  src={assetUrl(createdTask.resultUrl)}
                  alt="结果"
                  style={{ marginTop: 8, maxWidth: 240, borderRadius: 8, border: "1px solid var(--border)" }}
                />
              )}
            </div>
            <button className="btn" onClick={() => router.push("/history")}>
              查看历史
            </button>
          </div>
        </div>
      )}
    </>
  );
}
