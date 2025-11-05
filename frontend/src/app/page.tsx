'use client';

import { useState, useEffect } from 'react';
import { getAuthToken } from '@/lib/api';

export default function HomePage() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    const token = getAuthToken();
    setIsAuthenticated(!!token);
  }, []);

  return (
    <div className="min-h-screen bg-gradient-to-br from-robin-egg-blue-100 via-anti-flash-white-400 to-ultra-violet-100 flex items-center justify-center px-4">
      <div className="max-w-2xl mx-auto text-center">
        {/* Logo */}
        <img src="/logo.svg" alt="alexbbt.link" className="h-20 sm:h-24 mx-auto mb-8" />

        {/* Main Message */}
        <p className="text-lg text-ultra-violet-600 mb-8">
          A personal URL shortening service.
        </p>

        {/* Admin Link - Only show if authenticated */}
        {isAuthenticated && (
          <a
            href="/admin"
            className="inline-block px-6 py-3 text-base font-semibold text-white bg-robin-egg-blue-500 rounded-lg hover:bg-robin-egg-blue-600 transition-all duration-200 shadow-md hover:shadow-lg"
          >
            Admin Dashboard
          </a>
        )}

        {/* Footer */}
        <div className="mt-16 text-sm text-ultra-violet-600">
          <p>&copy; 2025 alexbbt.link</p>
        </div>
      </div>
    </div>
  );
}
