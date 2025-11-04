import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "URL Shortener - alexbbt.link",
  description: "Fast and performant URL shortener with admin interface",
  icons: {
    icon: '/icon.png',
    apple: '/icon.png',
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className="antialiased">
        {children}
      </body>
    </html>
  );
}
