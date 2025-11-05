'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { api, ShortLink, ShortLinkStats, handleApiError, getAuthToken } from '@/lib/api';
import LinkList from '@/components/admin/LinkList';
import LinkStats from '@/components/admin/LinkStats';
import NavBar from '@/components/admin/NavBar';

export default function AllLinksPage() {
  const router = useRouter();
  const [links, setLinks] = useState<ShortLink[]>([]);
  const [stats, setStats] = useState<ShortLinkStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [user, setUser] = useState<{ username: string; email: string; roles: string[] } | null>(null);

  // Check authentication and admin role on mount
  useEffect(() => {
    const token = getAuthToken();
    if (!token) {
      router.push('/admin/login');
      return;
    }

    // Verify token and check if user is admin
    api.auth.me()
      .then((userData) => {
        if (!userData.roles.includes('ADMIN')) {
          // Not an admin, redirect to home
          router.push('/admin');
          return;
        }
        setUser({ username: userData.username, email: userData.email, roles: userData.roles });
      })
      .catch(() => {
        router.push('/admin/login');
      });
  }, [router]);

  const loadLinks = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await api.shortlinks.listAll(page, 20);
      setLinks(response.content);
      setTotalPages(response.totalPages);
    } catch (err) {
      setError(handleApiError(err));
    } finally {
      setLoading(false);
    }
  };

  const loadStats = async () => {
    try {
      const statsData = await api.shortlinks.statsAll();
      setStats(statsData);
    } catch (err) {
      console.error('Failed to load stats:', err);
    }
  };

  useEffect(() => {
    if (user) {
      loadLinks();
      loadStats();
    }
  }, [page, user]);

  const handleLinkDeleted = () => {
    loadLinks();
    loadStats();
  };

  const handleLogout = () => {
    api.auth.logout();
    router.push('/admin/login');
  };

  if (!user) {
    return (
      <div className="min-h-screen bg-anti-flash-white-400 flex items-center justify-center p-4">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-2 border-robin-egg-blue-200 border-t-robin-egg-blue-500"></div>
          <p className="mt-4 text-ultra-violet-500">Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-anti-flash-white-400">
      <NavBar
        title="All Short Links"
        subtitle="View and manage all short links across all users (Admin Only)"
        user={user}
        onLogout={handleLogout}
        additionalButtons={
          <a
            href="/admin"
            className="px-3 py-1.5 text-xs sm:text-sm font-semibold text-ultra-violet-400 bg-white border border-ash-gray-200 rounded-lg hover:bg-ash-gray-100 hover:border-ash-gray-700 transition-all duration-200"
          >
            My Links
          </a>
        }
      />

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 sm:py-10">
        {error && (
          <div className="mb-6 bg-red-100 border border-red-300 text-red-800 px-4 py-3 rounded-lg text-sm font-medium">
            {error}
          </div>
        )}

        {/* Stats */}
        {stats && <LinkStats stats={stats} />}

        {/* Links List */}
        <div className="mt-8">
          <LinkList
            links={links}
            loading={loading}
            onLinkDeleted={handleLinkDeleted}
            page={page}
            totalPages={totalPages}
            onPageChange={setPage}
            showCreatedBy={true}
          />
        </div>
      </main>
    </div>
  );
}
