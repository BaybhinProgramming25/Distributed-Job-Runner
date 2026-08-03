import axios from 'axios';
import { getToken, clearAuth } from '../auth';

const api = axios.create();

api.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (res) => res,
  (err) => {
    const status = err.response?.status;
    const url = err.config?.url || '';
    const isAuthCall = url.includes('/api/users/signup') || url.includes('/api/users/login');
    if ((status === 401 || status === 403) && !isAuthCall && window.location.pathname !== '/login') {
      clearAuth();
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

export default api;
