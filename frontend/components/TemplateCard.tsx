import Link from "next/link";
import { Template } from "@/lib/types";
import { assetUrl } from "@/lib/api";

export default function TemplateCard({ t }: { t: Template }) {
  return (
    <Link href={`/templates/${t.id}`} className="card">
      {/* eslint-disable-next-line @next/next/no-img-element */}
      <img className="cover" src={assetUrl(t.coverUrl)} alt={t.title} />
      <div className="body">
        <div className="title">{t.title}</div>
        <div className="meta">
          <span className="tag">{t.category}</span>需 {t.requiredImageCount} 张图
        </div>
      </div>
    </Link>
  );
}
