import "./globals.css";
import Nav from "@/components/Nav";

export const metadata = {
  title: "素材制作工作室",
  description: "AI 素材制作演示（模拟生成，无真实 AI 服务）"
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="zh-CN">
      <body>
        <Nav />
        <main className="container">{children}</main>
      </body>
    </html>
  );
}
