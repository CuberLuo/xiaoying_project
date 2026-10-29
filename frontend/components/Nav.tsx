import Link from "next/link";

export default function Nav() {
  return (
    <nav className="nav">
      <span className="brand">素材制作工作室</span>
      <span className="links">
        <Link className="link" href="/">
          模板
        </Link>
        <Link className="link" href="/history">
          历史
        </Link>
      </span>
    </nav>
  );
}
