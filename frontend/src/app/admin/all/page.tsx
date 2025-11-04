'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { api, ShortLink, ShortLinkStats, handleApiError, getAuthToken } from '@/lib/api';
import LinkList from '@/components/admin/LinkList';
import LinkStats from '@/components/admin/LinkStats';

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
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          <p className="mt-4 text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8 flex justify-between items-center">
          <div className="flex items-center gap-4">
            <img src="/logo.svg" alt="alexbbt.link" className="h-12" />
            <div>
              <h1 className="text-3xl font-bold text-gray-900">All Short Links</h1>
              <p className="mt-2 text-sm text-gray-600">
                View and manage all short links across all users (Admin Only)
              </p>
            </div>
          </div>
          <div className="flex items-center gap-4">
            <a
              href="/admin"
              className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
            >
              My Links
            </a>
            <div className="text-right">
              <p className="text-sm font-medium text-gray-900">{user.username}</p>
              <p className="text-xs text-gray-500">{user.email}</p>
            </div>
            <button
              onClick={handleLogout}
              className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
            >
              Logout
            </button>
          </div>
        </div>

        {error && (
          <div className="mb-4 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
            {error}
          </div>
        )}

        {stats && <LinkStats stats={stats} />}

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
      </div>
    </div>
  );
}
