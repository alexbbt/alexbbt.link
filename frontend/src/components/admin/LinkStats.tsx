'use client';

import { ShortLinkStats } from '@/lib/api';

interface LinkStatsProps {
  stats: ShortLinkStats;
}

export default function LinkStats({ stats }: LinkStatsProps) {
  const statCards = [
    {
      label: 'Total Links',
      value: stats.totalLinks,
      icon: '🔗',
      borderColor: 'border-robin-egg-blue-500',
      iconBg: 'bg-robin-egg-blue-700',
    },
    {
      label: 'Total Clicks',
      value: stats.totalClicks.toLocaleString('en-US'),
      icon: '👆',
      borderColor: 'border-cambridge-blue-500',
      iconBg: 'bg-cambridge-blue-700',
    },
    {
      label: 'Active Links',
      value: stats.activeLinks,
      icon: '✓',
      borderColor: 'border-ash-gray-200',
      iconBg: 'bg-ash-gray-400',
    },
    {
      label: 'Avg Clicks/Link',
      value: stats.averageClicksPerLink.toFixed(1),
      icon: '📊',
      borderColor: 'border-ultra-violet-500',
      iconBg: 'bg-ultra-violet-700',
    },
  ];

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      {statCards.map((stat, index) => (
        <div
          key={index}
          className={`bg-white rounded-xl border ${stat.borderColor} overflow-hidden hover:shadow-md transition-all duration-200`}
        >
          <div className="p-5">
            <div className="flex items-center">
              <div className={`flex-shrink-0 w-12 h-12 ${stat.iconBg} rounded-lg flex items-center justify-center`}>
                <div className="text-xl">{stat.icon}</div>
              </div>
              <div className="ml-4 flex-1 min-w-0">
                <dl>
                  <dt className="text-xs font-semibold text-ultra-violet-500 truncate">
                    {stat.label}
                  </dt>
                  <dd className="text-2xl font-bold text-ultra-violet-600 mt-1">
                    {stat.value}
                  </dd>
                </dl>
              </div>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}
