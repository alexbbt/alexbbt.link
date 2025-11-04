'use client';

import { useState, useEffect } from 'react';
import { api, ShortLink, ShortLinkStats, handleApiError } from '@/lib/api';
import CreateLinkForm from '@/components/admin/CreateLinkForm';
import LinkList from '@/components/admin/LinkList';
import LinkStats from '@/components/admin/LinkStats';

export default function AdminPage() {
  const [links, setLinks] = useState<ShortLink[]>([]);
  const [stats, setStats] = useState<ShortLinkStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

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

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">URL Shortener Admin</h1>
          <p className="mt-2 text-sm text-gray-600">
            Manage your short links and track statistics
          </p>
        </div>

        {error && (
          <div className="mb-4 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
            {error}
          </div>
        )}

        {stats && <LinkStats stats={stats} />}

        <div className="mt-8">
          <CreateLinkForm onLinkCreated={handleLinkCreated} />
        </div>

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
      </div>
    </div>
  );
}
