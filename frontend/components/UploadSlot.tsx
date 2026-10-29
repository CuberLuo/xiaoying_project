"use client";

import { useRef, useState } from "react";
import { UploadResult } from "@/lib/types";
import { api, assetUrl } from "@/lib/api";

export default function UploadSlot({
  index,
  templateId,
  value,
  onChange,
  onError
}: {
  index: number;
  templateId: string;
  value: UploadResult | null;
  onChange: (v: UploadResult | null) => void;
  onError: (msg: string | null) => void;
}) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [uploading, setUploading] = useState(false);

  async function handleFile(file: File) {
    setUploading(true);
    onError(null);
    try {
      const res = await api.upload(file, templateId);
      onChange(res);
    } catch (e) {
      onError(e instanceof Error ? e.message : "上传失败");
      onChange(null);
    } finally {
      setUploading(false);
    }
  }

  return (
    <div
      className="slot"
      role="button"
      tabIndex={0}
      aria-label={`上传第 ${index + 1} 张图片`}
      onClick={() => !uploading && inputRef.current?.click()}
      onKeyDown={(e) => {
        if (e.key === "Enter" || e.key === " ") inputRef.current?.click();
      }}
    >
      <input
        ref={inputRef}
        type="file"
        accept="image/*"
        style={{ display: "none" }}
        onChange={(e) => {
          const f = e.target.files?.[0];
          if (f) handleFile(f);
          e.target.value = "";
        }}
      />
      {value ? (
        <>
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img src={assetUrl(value.url)} alt={value.name} />
          <button
            className="remove"
            aria-label="移除图片"
            onClick={(e) => {
              e.stopPropagation();
              onChange(null);
            }}
          >
            ×
          </button>
        </>
      ) : uploading ? (
        <div className="overlay">
          <span className="spinner" /> 上传中…
        </div>
      ) : (
        <div className="placeholder">点击上传第 {index + 1} 张图片</div>
      )}
    </div>
  );
}
