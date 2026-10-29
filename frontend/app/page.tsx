"use client";

import { useEffect, useState } from "react";
import { Template } from "@/lib/types";
import { api } from "@/lib/api";
import TemplateCard from "@/components/TemplateCard";

export default function HomePage() {
  const [categories, setCategories] = useState<string[]>([]);
  const [active, setActive] = useState<string>("ALL");
  const [templates, setTemplates] = useState<Template[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api.listCategories().then(setCategories).catch(() => {});
  }, []);

  useEffect(() => {
    let alive = true;
    setLoading(true);
    setError(null);
    api
      .listTemplates(active)
      .then((list) => alive && setTemplates(list))
      .catch((e) => alive && setError(e instanceof Error ? e.message : "加载失败"))
      .finally(() => alive && setLoading(false));
    return () => {
      alive = false;
    };
  }, [active]);

  return (
    <>
      <h1 className="page-title">模板库</h1>
      <div className="chips">
        <button className={`chip ${active === "ALL" ? "active" : ""}`} onClick={() => setActive("ALL")}>
          全部
        </button>
        {categories.map((c) => (
          <button
            key={c}
            className={`chip ${active === c ? "active" : ""}`}
            onClick={() => setActive(c)}
          >
            {c}
          </button>
        ))}
      </div>

      {loading && <div className="muted">加载中…</div>}
      {error && <div className="error">{error}</div>}
      {!loading && !error && templates.length === 0 && (
        <div className="notice">该分类下暂无模板。</div>
      )}

      <div className="grid">
        {templates.map((t) => (
          <TemplateCard key={t.id} t={t} />
        ))}
      </div>
    </>
  );
}
