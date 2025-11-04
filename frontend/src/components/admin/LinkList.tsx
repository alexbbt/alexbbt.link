'use client';

import { useState } from 'react';
import { api, ShortLink, handleApiError } from '@/lib/api';

// Format date consistently to avoid hydration mismatches
function formatDate(dateString: string): string {
  const date = new Date(dateString);
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  });
}

interface LinkListProps {
  links: ShortLink[];
  loading: boolean;
  onLinkDeleted: () => void;
  page: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  showCreatedBy?: boolean;
}

export default function LinkList({
  links,
  loading,
  onLinkDeleted,
  page,
  totalPages,
  onPageChange,
  showCreatedBy = false,
}: LinkListProps) {
  const [deleting, setDeleting] = useState<string | null>(null);
  const [copied, setCopied] = useState<string | null>(null);

  const handleDelete = async (slug: string) => {
    if (!confirm('Are you sure you want to delete this short link?')) {
      return;
    }

    setDeleting(slug);
    try {
      await api.shortlinks.delete(slug);
      onLinkDeleted();
    } catch (err) {
      alert(handleApiError(err));
    } finally {
      setDeleting(null);
    }
  };

  const copyToClipboard = async (text: string, slug: string) => {
    try {
      await navigator.clipboard.writeText(text);
      setCopied(slug);
      setTimeout(() => setCopied(null), 2000);
    } catch (err) {
      alert('Failed to copy to clipboard');
    }
  };

  if (loading) {
    return (
      <div className="bg-white rounded-xl border border-ash-gray-200 p-8 sm:p-12">
        <div className="text-center py-12">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-2 border-robin-egg-blue-200 border-t-robin-egg-blue-500"></div>
          <p className="mt-4 text-ultra-violet-500">Loading links...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-xl border border-ash-gray-200 overflow-hidden">
      <div className="px-6 py-4 border-b border-ash-gray-900">
        <h2 className="text-lg sm:text-xl font-semibold text-ultra-violet-400">
          {showCreatedBy ? 'All Short Links' : 'My Short Links'}
        </h2>
      </div>

      {links.length === 0 ? (
        <div className="px-6 py-16 text-center">
          <p className="text-sm text-ultra-violet-600">No short links created yet.</p>
          <p className="text-xs text-ultra-violet-700 mt-1">Create your first one above!</p>
        </div>
      ) : (
        <>
          {/* Mobile Card View */}
          <div className="block sm:hidden divide-y divide-ash-gray-900">
            {links.map((link) => (
              <div key={link.id} className="p-5 space-y-3">
                <div className="flex items-start justify-between gap-3">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-2.5">
                      <code className="text-xs font-mono bg-robin-egg-blue-100 text-robin-egg-blue-300 px-2.5 py-1.5 rounded-md border border-robin-egg-blue-200 truncate max-w-[60%] font-semibold">
                        {link.slug}
                      </code>
                      <button
                        onClick={() => copyToClipboard(link.shortUrl, link.slug)}
                        className={`p-1.5 rounded-md transition-all ${
                          copied === link.slug
                            ? 'text-cambridge-blue-300 bg-cambridge-blue-900'
                            : 'text-robin-egg-blue-400 hover:text-robin-egg-blue-300 hover:bg-robin-egg-blue-100'
                        }`}
                        title="Copy URL"
                        aria-label="Copy URL"
                      >
                        {copied === link.slug ? (
                          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                          </svg>
                        ) : (
                          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
                          </svg>
                        )}
                      </button>
                    </div>
                    <a
                      href={link.originalUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-xs text-robin-egg-blue-200 hover:text-robin-egg-blue-100 hover:underline break-all block font-semibold"
                    >
                      {link.originalUrl}
                    </a>
                  </div>
                  <button
                    onClick={() => handleDelete(link.slug)}
                    disabled={deleting === link.slug}
                    className="px-3 py-1.5 text-xs font-semibold text-red-700 hover:text-red-800 hover:bg-red-100 rounded-lg border border-red-300 disabled:opacity-100 disabled:cursor-not-allowed transition-all"
                  >
                    {deleting === link.slug ? 'Deleting...' : 'Delete'}
                  </button>
                </div>

                <div className="flex items-center justify-between text-xs text-ultra-violet-500 pt-2 border-t border-ash-gray-900">
                  <div className="flex items-center gap-4">
                    <span className="font-semibold text-ultra-violet-400">{link.clickCount} clicks</span>
                    {showCreatedBy && (
                      <span className="text-ultra-violet-600">by {link.createdBy || 'N/A'}</span>
                    )}
                  </div>
                  <span className="text-ultra-violet-600">{formatDate(link.createdAt)}</span>
                </div>
              </div>
            ))}
          </div>

          {/* Desktop Table View */}
          <div className="hidden sm:block overflow-x-auto">
            <table className="min-w-full divide-y divide-ash-gray-900">
              <thead className="bg-ash-gray-100">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-ultra-violet-400 uppercase tracking-wider">
                    Slug
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-ultra-violet-400 uppercase tracking-wider">
                    Original URL
                  </th>
                  {showCreatedBy && (
                    <th className="px-6 py-3 text-left text-xs font-semibold text-ultra-violet-400 uppercase tracking-wider">
                      Created By
                    </th>
                  )}
                  <th className="px-6 py-3 text-left text-xs font-semibold text-ultra-violet-400 uppercase tracking-wider">
                    Clicks
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-ultra-violet-400 uppercase tracking-wider">
                    Created
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-ultra-violet-400 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-ash-gray-900">
                {links.map((link) => (
                  <tr key={link.id} className="hover:bg-ash-gray-100/50 transition-colors">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center gap-2">
                        <code className="text-sm font-mono bg-robin-egg-blue-100 text-robin-egg-blue-300 px-2.5 py-1.5 rounded-md border border-robin-egg-blue-200 font-semibold">
                          {link.slug}
                        </code>
                        <button
                          onClick={() => copyToClipboard(link.shortUrl, link.slug)}
                          className={`p-1.5 rounded-md transition-all ${
                            copied === link.slug
                              ? 'text-cambridge-blue-300 bg-cambridge-blue-900'
                              : 'text-robin-egg-blue-400 hover:text-robin-egg-blue-300 hover:bg-robin-egg-blue-100'
                          }`}
                          title="Copy URL"
                          aria-label="Copy URL"
                        >
                          {copied === link.slug ? (
                            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                            </svg>
                          ) : (
                            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
                            </svg>
                          )}
                        </button>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <a
                        href={link.originalUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="text-sm text-robin-egg-blue-200 hover:text-robin-egg-blue-100 hover:underline truncate max-w-md block font-semibold"
                      >
                        {link.originalUrl}
                      </a>
                    </td>
                    {showCreatedBy && (
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-ultra-violet-500">
                        {link.createdBy || 'N/A'}
                      </td>
                    )}
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-semibold text-ultra-violet-400">
                      {link.clickCount}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-ultra-violet-500">
                      {formatDate(link.createdAt)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                      <button
                        onClick={() => handleDelete(link.slug)}
                        disabled={deleting === link.slug}
                        className="px-3 py-1.5 text-xs font-semibold text-red-700 hover:text-red-800 hover:bg-red-100 rounded-lg border border-red-300 disabled:opacity-100 disabled:cursor-not-allowed transition-all"
                      >
                        {deleting === link.slug ? 'Deleting...' : 'Delete'}
                      </button>
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
                onClick={() => onPageChange(page - 1)}
                disabled={page === 0}
                className="w-full sm:w-auto px-4 py-2 border border-ash-gray-700 rounded-lg text-sm font-semibold text-ultra-violet-400 bg-white hover:bg-ash-gray-100 hover:border-ash-gray-600 disabled:opacity-100 disabled:cursor-not-allowed transition-all"
              >
                Previous
              </button>
              <span className="text-sm text-ultra-violet-500">
                Page {page + 1} of {totalPages}
              </span>
              <button
                onClick={() => onPageChange(page + 1)}
                disabled={page >= totalPages - 1}
                className="w-full sm:w-auto px-4 py-2 border border-ash-gray-700 rounded-lg text-sm font-semibold text-ultra-violet-400 bg-white hover:bg-ash-gray-100 hover:border-ash-gray-600 disabled:opacity-100 disabled:cursor-not-allowed transition-all"
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
