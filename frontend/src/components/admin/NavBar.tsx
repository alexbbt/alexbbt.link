'use client';

interface NavBarProps {
  title: string;
  subtitle?: string;
  user: {
    username: string;
    email: string;
    roles: string[];
  };
  onLogout: () => void;
  additionalButtons?: React.ReactNode;
}

export default function NavBar({ title, subtitle, user, onLogout, additionalButtons }: NavBarProps) {
  return (
    <header className="bg-white border-b border-ash-gray-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-5">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          {/* Logo and Title */}
          <div className="flex items-center gap-3">
            <img src="/logo.svg" alt="alexbbt.link" className="h-10 sm:h-12" />
            <div>
              <h1 className="text-xl sm:text-2xl font-bold text-ultra-violet-400">{title}</h1>
              {subtitle && (
                <p className="text-xs sm:text-sm text-ultra-violet-600 hidden sm:block mt-0.5">
                  {subtitle}
                </p>
              )}
            </div>
          </div>

          {/* User Actions */}
          <div className="flex items-center gap-2.5 sm:gap-3">
            {additionalButtons}
            <div className="text-right hidden sm:block">
              <p className="text-sm font-semibold text-ultra-violet-400">{user.username}</p>
              <p className="text-xs text-ultra-violet-600">{user.email}</p>
            </div>
            <button
              onClick={onLogout}
              className="px-3 py-1.5 text-xs sm:text-sm font-semibold text-ultra-violet-400 bg-white border border-ash-gray-200 rounded-lg hover:bg-ash-gray-100 hover:border-ash-gray-700 transition-all duration-200"
            >
              Logout
            </button>
          </div>
        </div>
      </div>
    </header>
  );
}
