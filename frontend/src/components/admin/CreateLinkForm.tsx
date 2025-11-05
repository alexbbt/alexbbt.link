'use client';

import { useState } from 'react';
import { api, handleApiError } from '@/lib/api';

interface CreateLinkFormProps {
  onLinkCreated: () => void;
}

export default function CreateLinkForm({ onLinkCreated }: CreateLinkFormProps) {
  const [url, setUrl] = useState('');
  const [customSlug, setCustomSlug] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      const result = await api.shortlinks.create({
        url,
        slug: customSlug.trim() || undefined,
      });
      setSuccess(`Short link created: ${result.shortUrl}`);
      setUrl('');
      setCustomSlug('');
      onLinkCreated();
    } catch (err) {
      setError(handleApiError(err));
    } finally {
      setLoading(false);
    }
  };

  const generateRandomSlug = () => {
    const chars = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
    let slug = '';
    for (let i = 0; i < 6; i++) {
      slug += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    setCustomSlug(slug);
  };

  return (
    <div className="bg-white rounded-xl border border-ash-gray-200 p-6 sm:p-8">
      <h2 className="text-lg sm:text-xl font-semibold text-ultra-violet-400 mb-6">
        Create Short Link
      </h2>
      <form onSubmit={handleSubmit} className="space-y-5">
        <div>
          <label htmlFor="url" className="block text-sm font-semibold text-ultra-violet-400 mb-2">
            URL to Shorten <span className="text-red-600">*</span>
          </label>
          <input
            type="url"
            id="url"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            required
            className="block w-full rounded-lg border-ash-gray-700 text-ultra-violet-400 placeholder-ultra-violet-700 focus:border-robin-egg-blue-500 focus:ring-2 focus:ring-robin-egg-blue-500/20 text-sm px-4 py-2.5 transition-all"
            placeholder="https://example.com"
          />
        </div>

        <div>
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 mb-2">
            <label htmlFor="slug" className="block text-sm font-semibold text-ultra-violet-400">
              Custom Slug <span className="text-ultra-violet-600 font-normal">(optional)</span>
            </label>
            <button
              type="button"
              onClick={generateRandomSlug}
              className="text-xs sm:text-sm text-robin-egg-blue-800 hover:text-robin-egg-blue-900 font-bold self-start sm:self-auto transition-colors underline"
            >
              Generate Random
            </button>
          </div>
          <input
            type="text"
            id="slug"
            value={customSlug}
            onChange={(e) => setCustomSlug(e.target.value)}
            className="block w-full rounded-lg border-ash-gray-700 text-ultra-violet-400 placeholder-ultra-violet-700 focus:border-robin-egg-blue-500 focus:ring-2 focus:ring-robin-egg-blue-500/20 text-sm px-4 py-2.5 transition-all"
            placeholder="my-link (leave empty for random)"
            pattern="[a-zA-Z0-9_-]+"
            maxLength={50}
          />
          <p className="mt-2 text-xs text-ultra-violet-600">
            Use alphanumeric characters, hyphens, or underscores
          </p>
        </div>

        {error && (
          <div className="bg-red-100 border border-red-300 text-red-800 px-4 py-3 rounded-lg text-sm font-medium">
            {error}
          </div>
        )}

        {success && (
          <div className="bg-cambridge-blue-100 border border-cambridge-blue-700 text-cambridge-blue-100 px-4 py-3 rounded-lg text-sm font-medium">
            {success}
          </div>
        )}

        <button
          type="submit"
          disabled={loading || !url}
          className="w-full bg-robin-egg-blue-500 text-white py-2.5 px-4 rounded-lg font-semibold hover:bg-robin-egg-blue-400 disabled:bg-ash-gray-700 disabled:cursor-not-allowed transition-all duration-200 text-sm shadow-sm hover:shadow disabled:shadow-none"
        >
          {loading ? 'Creating...' : 'Create Short Link'}
        </button>
      </form>
    </div>
  );
}
