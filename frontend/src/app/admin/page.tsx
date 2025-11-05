'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { api, ShortLink, ShortLinkStats, handleApiError, getAuthToken } from '@/lib/api';
import CreateLinkForm from '@/components/admin/CreateLinkForm';
import LinkList from '@/components/admin/LinkList';
import LinkStats from '@/components/admin/LinkStats';
import NavBar from '@/components/admin/NavBar';

export default function AdminPage() {
  const router = useRouter();
  const [links, setLinks] = useState<ShortLink[]>([]);
  const [stats, setStats] = useState<ShortLinkStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [user, setUser] = useState<{ username: string; email: string; roles: string[] } | null>(null);

  // Check authentication on mount
  useEffect(() => {
    const token = getAuthToken();
    if (!token) {
      router.push('/admin/login');
      return;
    }

    // Verify token by getting current user
    api.auth.me()
      .then((userData) => {
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
      const response = await api.shortlinks.list(page, 20);
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
      const statsData = await api.shortlinks.stats();
      setStats(statsData);
    } catch (err) {
      console.error('Failed to load stats:', err);
    }
  };

  useEffect(() => {
    loadLinks();
    loadStats();
  }, [page]);

  const handleLinkCreated = () => {
    loadLinks();
    loadStats();
  };

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
        title="URL Shortener"
        subtitle="Manage your short links"
        user={user}
        onLogout={handleLogout}
        additionalButtons={
          user.roles.includes('ADMIN') ? (
            <a
              href="/admin/all"
              className="px-3 py-1.5 text-xs sm:text-sm font-semibold text-robin-egg-blue-600 bg-robin-egg-blue-100 border border-robin-egg-blue-400 rounded-lg hover:bg-robin-egg-blue-900 hover:border-robin-egg-blue-700 transition-all duration-200"
            >
              All Links
            </a>
          ) : undefined
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

        {/* Create Link Form */}
        <div className="mt-8">
          <CreateLinkForm onLinkCreated={handleLinkCreated} />
        </div>

        {/* Links List */}
        <div className="mt-8">
          <LinkList
            links={links}
            loading={loading}
            onLinkDeleted={handleLinkDeleted}
            page={page}
            totalPages={totalPages}
            onPageChange={setPage}
          />
        </div>
      </main>
    </div>
  );
}
