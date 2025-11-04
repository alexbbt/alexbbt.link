// API utility functions for communicating with Spring Boot backend

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
const API_BASE_PATH = process.env.NEXT_PUBLIC_API_BASE || '/api';

export const API_URL = `${API_BASE_URL}${API_BASE_PATH}`;

// Generic API call function
async function apiCall<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const url = `${API_URL}${endpoint}`;

  const defaultOptions: RequestInit = {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    credentials: 'include', // Important for CORS with credentials
  };

  const response = await fetch(url, { ...defaultOptions, ...options });

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

// API functions for Spring Boot endpoints
export const api = {
  // Health check
  health: () => apiCall<{
    status: string;
    service: string;
    version: string;
    timestamp: string;
  }>('/health'),

  // ShortLink endpoints
  shortlinks: {
    // Create a short link
    create: (data: CreateShortLinkRequest) =>
      apiCall<ShortLink>('/shortlinks', {
        method: 'POST',
        body: JSON.stringify(data),
      }),

    // Get all short links with pagination
    list: (page = 0, size = 20, sortBy = 'createdAt', sortDir: 'asc' | 'desc' = 'desc') =>
      apiCall<ShortLinksResponse>(`/shortlinks?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}`),

    // Get a short link by slug
    get: (slug: string) =>
      apiCall<ShortLink>(`/shortlinks/${slug}`),

    // Delete a short link
    delete: (slug: string) =>
      apiCall<void>(`/shortlinks/${slug}`, {
        method: 'DELETE',
      }),

    // Get statistics
    stats: () =>
      apiCall<ShortLinkStats>('/shortlinks/stats'),
  },
};

// Error handling utility
export function handleApiError(error: unknown): string {
  if (error instanceof Error) {
    return error.message;
  }
  return 'An unexpected error occurred';
}
