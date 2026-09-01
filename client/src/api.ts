import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

let isRefreshing = false;
let failedQueue: any[] = [];

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });

  failedQueue = [];
};

api.interceptors.request.use((config) => {
  console.log(`[DEBUG] Outgoing Request: ${config.method?.toUpperCase()} ${config.url}`, config);
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, (error) => {
  console.error('[DEBUG] Request Error:', error);
  return Promise.reject(error);
});

api.interceptors.response.use(
  (response) => {
    console.log(`[DEBUG] Incoming Response: ${response.status} ${response.config.url}`, response);
    return response;
  },
  async (error) => {
    console.log("DEBUG: Interceptor caught error:", error.response?.status, error.message, error.config?.url);
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      console.log("DEBUG: 401 caught, attempting refresh...");
      if (isRefreshing) {
        console.log("DEBUG: Already refreshing, queuing request...");
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return api(originalRequest);
          })
          .catch((err) => {
            return Promise.reject(err);
          });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const refreshToken = localStorage.getItem('refreshToken');
      console.log("DEBUG: Refresh token found:", !!refreshToken);
      if (!refreshToken) {
        console.log("DEBUG: No refresh token, logging out.");
        processQueue(error);
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        window.location.href = '/login';
        return Promise.reject(error);
      }

      try {
        console.log("DEBUG: Sending refresh request...");
        const response = await axios.post('http://localhost:8080/auth/refresh', refreshToken, {
          headers: { 'Content-Type': 'text/plain' },
        });
        const newAccessToken = response.data;
        console.log("DEBUG: Refresh successful!");

        localStorage.setItem('accessToken', newAccessToken);
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;

        processQueue(null, newAccessToken);
        isRefreshing = false;

        return api(originalRequest);
      } catch (refreshError) {
        console.log("DEBUG: Refresh failed:", refreshError);
        processQueue(refreshError);
        isRefreshing = false;
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    console.log("DEBUG: Error caught in interceptor, status:", error.response?.status);
    return Promise.reject(error);
  }
);

export default api;
