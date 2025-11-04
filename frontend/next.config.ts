import type { NextConfig } from "next";

// Determine if we're building for production static export
const isProduction = process.env.NODE_ENV === 'production';
const isStaticExport = process.env.NEXT_STATIC_EXPORT === 'true';

const nextConfig: NextConfig = {
  // Enable static export for production builds
  ...(isStaticExport && {
    output: 'export',
    trailingSlash: true,
    basePath: '/admin',
    assetPrefix: '/admin',
  }),

  // Enable experimental features for better development experience
  experimental: {
    // Enable server actions (only in development)
    ...(!isStaticExport && {
      serverActions: {
        allowedOrigins: ['localhost:3000'],
      },
    }),
  },

  // Environment variables
  env: {
    NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080',
    NEXT_PUBLIC_API_BASE: process.env.NEXT_PUBLIC_API_BASE || '/api',
  },

  // Rewrites for API proxying (optional - can use direct calls instead)
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/api/:path*`,
      },
    ];
  },

  // Headers for CORS and security
  async headers() {
    return [
      {
        source: '/api/:path*',
        headers: [
          { key: 'Access-Control-Allow-Credentials', value: 'true' },
          { key: 'Access-Control-Allow-Origin', value: '*' },
          { key: 'Access-Control-Allow-Methods', value: 'GET,OPTIONS,PATCH,DELETE,POST,PUT' },
          { key: 'Access-Control-Allow-Headers', value: 'X-CSRF-Token, X-Requested-With, Accept, Accept-Version, Content-Length, Content-MD5, Content-Type, Date, X-Api-Version' },
        ],
      },
    ];
  },

  // Webpack configuration for better development experience
  webpack: (config, { dev, isServer }) => {
    // Add source map support in development
    if (dev && !isServer) {
      config.devtool = 'eval-source-map';
    }

    // Handle font loading errors gracefully for restricted networks
    config.module.rules.push({
      test: /\.(woff|woff2|eot|ttf|otf)$/,
      use: {
        loader: 'file-loader',
        options: {
          publicPath: '/_next/static/fonts/',
          outputPath: 'static/fonts/',
          fallback: 'style-loader',
        },
      },
    });

    return config;
  },

  // Development server configuration
  devIndicators: {
    buildActivity: true,
    buildActivityPosition: 'bottom-right',
  },
};

export default nextConfig;
