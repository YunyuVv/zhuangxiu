import "./globals.css";
import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "装修记录",
  description: "装修从 0 到完工：周期 / 花销 / 资料一站式记录",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="zh-CN">
      <body>{children}</body>
    </html>
  );
}
