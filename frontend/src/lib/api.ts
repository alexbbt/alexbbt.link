// API utility functions for communicating with Spring Boot backend

// In production (static export served by Spring Boot), use relative URLs (same origin)
// In development, use localhost:3000 which proxies to localhost:8080
const getApiBaseUrl = (): string => {
  // In browser, check if we're on localhost (dev) or production
  if (typeof window !== 'undefined') {
    const origin = window.location.origin;
    // If running on localhost:3000 (dev), use the full URL to backend
    if (origin.includes('localhost:3000')) {
      return 'http://localhost:8080';
    }
    // Otherwise (production), use same origin (relative URL)
    return origin;
  }

  // SSR fallback (shouldn't happen with static export, but just in case)
  return 'http://localhost:8080';
};

const API_BASE_URL = getApiBaseUrl();
const API_BASE_PATH = process.env.NEXT_PUBLIC_API_BASE || '/api';

export const API_URL = `${API_BASE_URL}${API_BASE_PATH}`;

// Auth token management
let authToken: string | null = null;

export function setAuthToken(token: string | null) {
  authToken = token;
  if (token) {
    localStorage.setItem('authToken', token);
  } else {
    localStorage.removeItem('authToken');
  }
}

export function getAuthToken(): string | null {
  if (authToken) {
    return authToken;
  }
  if (typeof window !== 'undefined') {
    authToken = localStorage.getItem('authToken');
    return authToken;
  }
  return null;
}

// Generic API call function
async function apiCall<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const url = `${API_URL}${endpoint}`;
  const token = getAuthToken();

  const defaultOptions: RequestInit = {
    headers: {
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` }),
      ...options.headers,
    },
    credentials: 'include', // Important for CORS with credentials
  };

  const response = await fetch(url, { ...defaultOptions, ...options });

  if (response.status === 401) {
    // Unauthorized - clear token and redirect to login
    setAuthToken(null);
    if (typeof window !== 'undefined') {
      window.location.href = '/admin/login';
    }
    throw new Error('Unauthorized');
  }

  if (!response.ok) {
    throw new Error(`API call failed: ${response.status} ${response.statusText}`);
  }

  return response.json();
}

// ShortLink types
export interface ShortLink {
  id: number;
  slug: string;
  shortUrl: string;
  originalUrl: string;
  clickCount: number;
  createdAt: string;
  updatedAt: string;
  expiresAt: string | null;
  isActive: boolean;
  createdBy?: string;
}

export interface CreateShortLinkRequest {
  url: string;
  slug?: string;
}

export interface ShortLinksResponse {
  content: ShortLink[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  size: number;
}

export interface ShortLinkStats {
  totalLinks: number;
  totalClicks: number;
  activeLinks: number;
  averageClicksPerLink: number;
}

// Auth types
export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  email: string;
  roles: string[];
}

// API functions for Spring Boot endpoints
export const api = {
  // Health check
  health: () => apiCall<{
    status: string;
    service: string;
    version: string;
    timestamp: string;
  }>('/health'),

  // Auth endpoints
  auth: {
    login: (data: LoginRequest) =>
      apiCall<AuthResponse>('/auth/login', {
        method: 'POST',
        body: JSON.stringify(data),
      }),
    me: () => apiCall<AuthResponse>('/auth/me'),
    logout: () => {
      setAuthToken(null);
    },
  },

  // ShortLink endpoints
  shortlinks: {
    // Create a short link
    create: (data: CreateShortLinkRequest) =>
      apiCall<ShortLink>('/shortlinks', {
        method: 'POST',
        body: JSON.stringify(data),
      }),

    // Get user's short links with pagination
    list: (page = 0, size = 20, sortBy = 'createdAt', sortDir: 'asc' | 'desc' = 'desc') =>
      apiCall<ShortLinksResponse>(`/shortlinks?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}`),

    // Get all short links (admin only) with pagination
    listAll: (page = 0, size = 20, sortBy = 'createdAt', sortDir: 'asc' | 'desc' = 'desc') =>
      apiCall<ShortLinksResponse>(`/shortlinks/all?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}`),

    // Get a short link by slug
    get: (slug: string) =>
      apiCall<ShortLink>(`/shortlinks/${slug}`),

    // Delete a short link
    delete: (slug: string) =>
      apiCall<void>(`/shortlinks/${slug}`, {
        method: 'DELETE',
      }),

    // Get statistics for user's links
    stats: () =>
      apiCall<ShortLinkStats>('/shortlinks/stats'),

    // Get statistics for all links (admin only)
    statsAll: () =>
      apiCall<ShortLinkStats>('/shortlinks/stats/all'),
  },
};

// Error handling utility
export function handleApiError(error: unknown): string {
  if (error instanceof Error) {
    return error.message;
  }
  return 'An unexpected error occurred';
}
