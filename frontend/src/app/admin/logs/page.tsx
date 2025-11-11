'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { api, LinkVisit, handleApiError, getAuthToken } from '@/lib/api';
import NavBar from '@/components/admin/NavBar';

function formatDateTime(dateString: string): string {
  const date = new Date(dateString);
  return date.toLocaleString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  });
}

export default function LogsPage() {
  const router = useRouter();
  const [visits, setVisits] = useState<LinkVisit[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
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
        // Check if user is admin
        if (!userData.roles.includes('ADMIN')) {
          router.push('/admin');
        }
      })
      .catch(() => {
        router.push('/admin/login');
      });
  }, [router]);

  const loadVisits = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await api.visits.getRedirectRequests(page, 20);
      setVisits(response.content);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
    } catch (err) {
      setError(handleApiError(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (user?.roles.includes('ADMIN')) {
      loadVisits();
    }
  }, [page, user]);

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

  if (!user.roles.includes('ADMIN')) {
    return null; // Will redirect
  }

  return (
    <div className="min-h-screen bg-anti-flash-white-400">
      <NavBar
        title="Request Logs"
        subtitle="All redirect controller requests"
        user={user}
        onLogout={handleLogout}
        additionalButtons={
          <button
            onClick={() => router.push('/admin')}
            className="px-3 py-1.5 text-xs sm:text-sm font-semibold text-robin-egg-blue-600 bg-robin-egg-blue-100 border border-robin-egg-blue-400 rounded-lg hover:bg-robin-egg-blue-900 hover:border-robin-egg-blue-700 transition-all duration-200"
          >
            Back to Admin
          </button>
        }
      />

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 sm:py-10">
        {error && (
          <div className="mb-6 bg-red-100 border border-red-300 text-red-800 px-4 py-3 rounded-lg text-sm font-medium">
            {error}
          </div>
        )}

        <div className="bg-white rounded-xl border border-ash-gray-200 overflow-hidden">
          <div className="px-6 py-4 border-b border-ash-gray-900">
            <h2 className="text-lg sm:text-xl font-semibold text-ultra-violet-400">
              All Redirect Requests
            </h2>
            <p className="text-sm text-ultra-violet-500 mt-1">
              {totalElements} total request{totalElements !== 1 ? 's' : ''} (including 302, 404, and other status codes)
            </p>
          </div>

          {loading ? (
            <div className="text-center py-12">
              <div className="inline-block animate-spin rounded-full h-8 w-8 border-2 border-robin-egg-blue-400 border-t-robin-egg-blue-600"></div>
              <p className="mt-4 text-ultra-violet-500">Loading logs...</p>
            </div>
          ) : visits.length === 0 ? (
            <div className="text-center py-12">
              <p className="text-sm text-ultra-violet-600">No requests logged yet.</p>
            </div>
          ) : (
            <>
              {/* Mobile Card View */}
              <div className="block sm:hidden space-y-4 p-4">
                {visits.map((visit) => (
                  <div key={visit.id} className="bg-ash-gray-100 rounded-lg p-4 space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="text-xs font-semibold text-ultra-violet-400">
                        {formatDateTime(visit.createdAt)}
                      </span>
                      {visit.statusCode && (
                        <span
                          className={`text-xs px-2 py-1 rounded font-semibold ${
                            visit.statusCode === 302
                              ? 'bg-green-100 text-green-700'
                              : visit.statusCode === 404
                              ? 'bg-yellow-100 text-yellow-700'
                              : visit.statusCode >= 400
                              ? 'bg-red-100 text-red-700'
                              : 'bg-blue-100 text-blue-700'
                          }`}
                        >
                          {visit.statusCode}
                        </span>
                      )}
                    </div>
                    {visit.slug && (
                      <div className="text-xs text-ultra-violet-600">
                        <span className="font-semibold">Slug:</span>{' '}
                        <code className="bg-robin-egg-blue-100 text-robin-egg-blue-600 px-1.5 py-0.5 rounded">
                          {visit.slug}
                        </code>
                      </div>
                    )}
                    {visit.ipAddress && (
                      <div className="text-xs text-ultra-violet-600">
                        <span className="font-semibold">IP:</span> {visit.ipAddress}
                      </div>
                    )}
                    {visit.deviceType && (
                      <div className="text-xs text-ultra-violet-600">
                        <span className="font-semibold">Device:</span> {visit.deviceType}
                        {visit.browser && ` • ${visit.browser}`}
                        {visit.operatingSystem && ` • ${visit.operatingSystem}`}
                      </div>
                    )}
                    {visit.countryCode && (
                      <div className="text-xs text-ultra-violet-600">
                        <span className="font-semibold">Country:</span> {visit.countryCode}
                      </div>
                    )}
                  </div>
                ))}
              </div>

              {/* Desktop Table View */}
              <div className="hidden sm:block overflow-x-auto">
                <table className="min-w-full divide-y divide-ash-gray-900">
                  <thead className="bg-ash-gray-100">
                    <tr>
                      <th className="px-4 py-3 text-left text-xs font-semibold text-ultra-violet-400 uppercase tracking-wider">
                        Date & Time
                      </th>
                      <th className="px-4 py-3 text-left text-xs font-semibold text-ultra-violet-400 uppercase tracking-wider">
                        Slug
                      </th>
                      <th className="px-4 py-3 text-left text-xs font-semibold text-ultra-violet-400 uppercase tracking-wider">
                        Status
                      </th>
                      <th className="px-4 py-3 text-left text-xs font-semibold text-ultra-violet-400 uppercase tracking-wider">
                        IP Address
                      </th>
                      <th className="px-4 py-3 text-left text-xs font-semibold text-ultra-violet-400 uppercase tracking-wider">
                        Device
                      </th>
                      <th className="px-4 py-3 text-left text-xs font-semibold text-ultra-violet-400 uppercase tracking-wider">
                        Browser
                      </th>
                      <th className="px-4 py-3 text-left text-xs font-semibold text-ultra-violet-400 uppercase tracking-wider">
                        OS
                      </th>
                      <th className="px-4 py-3 text-left text-xs font-semibold text-ultra-violet-400 uppercase tracking-wider">
                        Country
                      </th>
                      <th className="px-4 py-3 text-left text-xs font-semibold text-ultra-violet-400 uppercase tracking-wider">
                        Referrer
                      </th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-ash-gray-900">
                    {visits.map((visit) => (
                      <tr key={visit.id} className="hover:bg-ash-gray-100/50 transition-colors">
                        <td className="px-4 py-3 whitespace-nowrap text-sm text-ultra-violet-500">
                          {formatDateTime(visit.createdAt)}
                        </td>
                        <td className="px-4 py-3 whitespace-nowrap text-sm">
                          {visit.slug ? (
                            <code className="bg-robin-egg-blue-100 text-robin-egg-blue-600 px-2 py-1 rounded font-mono text-xs">
                              {visit.slug}
                            </code>
                          ) : (
                            '—'
                          )}
                        </td>
                        <td className="px-4 py-3 whitespace-nowrap">
                          {visit.statusCode ? (
                            <span
                              className={`text-xs px-2 py-1 rounded font-semibold ${
                                visit.statusCode === 302
                                  ? 'bg-green-100 text-green-700'
                                  : visit.statusCode === 404
                                  ? 'bg-yellow-100 text-yellow-700'
                                  : visit.statusCode >= 400
                                  ? 'bg-red-100 text-red-700'
                                  : 'bg-blue-100 text-blue-700'
                              }`}
                            >
                              {visit.statusCode}
                            </span>
                          ) : (
                            '—'
                          )}
                        </td>
                        <td className="px-4 py-3 whitespace-nowrap text-sm text-ultra-violet-600 font-mono">
                          {visit.ipAddress || '—'}
                        </td>
                        <td className="px-4 py-3 whitespace-nowrap text-sm text-ultra-violet-600">
                          {visit.deviceType || '—'}
                        </td>
                        <td className="px-4 py-3 whitespace-nowrap text-sm text-ultra-violet-600">
                          {visit.browser || '—'}
                        </td>
                        <td className="px-4 py-3 whitespace-nowrap text-sm text-ultra-violet-600">
                          {visit.operatingSystem || '—'}
                        </td>
                        <td className="px-4 py-3 whitespace-nowrap text-sm text-ultra-violet-600">
                          {visit.countryCode || '—'}
                        </td>
                        <td className="px-4 py-3 text-sm text-ultra-violet-600 max-w-xs truncate">
                          {visit.referrer ? (
                            <a
                              href={visit.referrer}
                              target="_blank"
                              rel="noopener noreferrer"
                              className="text-robin-egg-blue-400 hover:underline"
                              title={visit.referrer}
                            >
                              {visit.referrer}
                            </a>
                          ) : (
                            '—'
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Pagination */}
              {totalPages > 1 && (
                <div className="px-6 py-4 border-t border-ash-gray-900 flex flex-col sm:flex-row items-center justify-between gap-4 bg-ash-gray-100/50">
                  <button
                    onClick={() => setPage(page - 1)}
                    disabled={page === 0}
                    className="w-full sm:w-auto px-4 py-2 border border-ash-gray-700 rounded-lg text-sm font-semibold text-ultra-violet-400 bg-white hover:bg-ash-gray-100 hover:border-ash-gray-600 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
                  >
                    Previous
                  </button>
                  <span className="text-sm text-ultra-violet-500">
                    Page {page + 1} of {totalPages}
                  </span>
                  <button
                    onClick={() => setPage(page + 1)}
                    disabled={page >= totalPages - 1}
                    className="w-full sm:w-auto px-4 py-2 border border-ash-gray-700 rounded-lg text-sm font-semibold text-ultra-violet-400 bg-white hover:bg-ash-gray-100 hover:border-ash-gray-600 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
                  >
                    Next
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      </main>
    </div>
  );
}

