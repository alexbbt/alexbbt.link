'use client';

import { useState } from 'react';
import { api, setAuthToken, handleApiError } from '@/lib/api';
import { useRouter } from 'next/navigation';

export default function LoginForm() {
  const router = useRouter();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const response = await api.auth.login({ username, password });
      setAuthToken(response.token);
      router.push('/admin');
      router.refresh();
    } catch (err) {
      setError(handleApiError(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-anti-flash-white-400 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div>
          <div className="flex justify-center mb-6">
            <img src="/logo.svg" alt="alexbbt.link" className="h-16 sm:h-20" />
          </div>
        </div>
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          {error && (
            <div className="bg-red-100 border border-red-300 text-red-800 px-4 py-3 rounded-lg text-sm font-medium">
              {error}
            </div>
          )}
          <div className="rounded-lg shadow-sm -space-y-px">
            <div>
              <label htmlFor="username" className="sr-only">
                Username
              </label>
              <input
                id="username"
                name="username"
                type="text"
                required
                className="appearance-none rounded-t-lg relative block w-full px-3 py-2.5 border border-ash-gray-700 placeholder-ash-gray-500 text-ultra-violet-400 focus:outline-none focus:ring-robin-egg-blue-500 focus:border-robin-egg-blue-500 focus:z-10 sm:text-sm font-medium"
                placeholder="Username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
              />
            </div>
            <div>
              <label htmlFor="password" className="sr-only">
                Password
              </label>
              <input
                id="password"
                name="password"
                type="password"
                required
                className="appearance-none rounded-b-lg relative block w-full px-3 py-2.5 border border-ash-gray-700 placeholder-ash-gray-500 text-ultra-violet-400 focus:outline-none focus:ring-robin-egg-blue-500 focus:border-robin-egg-blue-500 focus:z-10 sm:text-sm font-medium"
                placeholder="Password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>
          </div>

          <div>
            <button
              type="submit"
              disabled={loading}
              className="group relative w-full flex justify-center py-2.5 px-4 border border-transparent text-sm font-semibold rounded-lg text-white bg-robin-egg-blue-500 hover:bg-robin-egg-blue-400 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-robin-egg-blue-500 disabled:bg-ash-gray-600 disabled:cursor-not-allowed transition-colors"
            >
              {loading ? 'Signing in...' : 'Sign in'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
