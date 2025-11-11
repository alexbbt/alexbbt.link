'use client';

import { useState, useEffect } from 'react';
import { api, LinkVisit, LinkVisitsResponse, handleApiError } from '@/lib/api';

interface VisitLogsProps {
  slug: string;
  onClose: () => void;
}

function formatDateTime(dateString: string): string {
  const date = new Date(dateString);
  return date.toLocaleString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

function formatDate(dateString: string): string {
  const date = new Date(dateString);
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
}

export default function VisitLogs({ slug, onClose }: VisitLogsProps) {
  const [visits, setVisits] = useState<LinkVisit[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const loadVisits = async () => {
    try {
      setLoading(true);
      setError(null);
      console.log('Loading visits for slug:', slug);
      const response = await api.visits.getForLink(slug, page, 20);
      console.log('Visit logs response:', response);
      setVisits(response.content);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
    } catch (err) {
      console.error('Error loading visits:', err);
      setError(handleApiError(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadVisits();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [slug, page]);

  // Handle Escape key to close modal
  useEffect(() => {
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        onClose();
      }
    };

    window.addEventListener('keydown', handleEscape);
    return () => window.removeEventListener('keydown', handleEscape);
  }, [onClose]);

  // Handle click on background to close modal
  const handleBackgroundClick = (e: React.MouseEvent<HTMLDivElement>) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  return (
    <div
      className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4"
      onClick={handleBackgroundClick}
    >
      <div
        className="bg-white rounded-xl border border-ash-gray-200 max-w-6xl w-full max-h-[90vh] flex flex-col shadow-2xl"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="px-6 py-4 border-b border-ash-gray-900 flex items-center justify-between">
          <div>
            <h2 className="text-lg sm:text-xl font-semibold text-ultra-violet-400">
              Visit Logs for <code className="text-robin-egg-blue-600">{slug}</code>
            </h2>
            <p className="text-sm text-ultra-violet-500 mt-1">
              {totalElements} total visit{totalElements !== 1 ? 's' : ''}
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-2 text-ultra-violet-400 hover:text-ultra-violet-600 hover:bg-ash-gray-100 rounded-lg transition-all"
            aria-label="Close"
          >
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto p-6">
          {error && (
            <div className="mb-4 bg-red-100 border border-red-300 text-red-800 px-4 py-3 rounded-lg text-sm">
              {error}
            </div>
          )}

          {loading ? (
            <div className="text-center py-12">
              <div className="inline-block animate-spin rounded-full h-8 w-8 border-2 border-robin-egg-blue-400 border-t-robin-egg-blue-600"></div>
              <p className="mt-4 text-ultra-violet-500">Loading visits...</p>
            </div>
          ) : visits.length === 0 ? (
            <div className="text-center py-12">
              <p className="text-sm text-ultra-violet-600">No visits recorded yet.</p>
            </div>
          ) : (
            <>
              {/* Mobile Card View */}
              <div className="block sm:hidden space-y-4">
                {visits.map((visit) => (
                  <div key={visit.id} className="bg-ash-gray-100 rounded-lg p-4 space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="text-xs font-semibold text-ultra-violet-400">
                        {formatDateTime(visit.createdAt)}
                      </span>
                    </div>
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
                    {visit.referrer && (
                      <div className="text-xs text-ultra-violet-600 truncate">
                        <span className="font-semibold">Referrer:</span>{' '}
                        <a
                          href={visit.referrer}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="text-robin-egg-blue-400 hover:underline"
                        >
                          {visit.referrer}
                        </a>
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
                <div className="mt-6 flex flex-col sm:flex-row items-center justify-between gap-4">
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
      </div>
    </div>
  );
}
