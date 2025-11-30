import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

// Create axios instance with base URL
const api = axios.create({
  baseURL: API_URL,
});

// Add request interceptor to include auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Auth services
export const authService = {
  login: (credentials) => api.post('/auth/signin', credentials),
  register: (userData) => api.post('/auth/signup', userData),
};

// Carbon credit services
export const creditService = {
  getAll: () => api.get('/credits'),
  getById: (id) => api.get(`/credits/${id}`),
  getMyCarbonCredits: () => api.get('/credits/my'),
  create: (creditData) => api.post('/credits', creditData),
};

// Marketplace services
export const marketplaceService = {
  getOrders: () => api.get('/marketplace/orders'),
  createOrder: (orderData) => api.post('/marketplace/orders', orderData),
  cancelOrder: (orderId) => api.delete(`/marketplace/orders/${orderId}`),
  getTrades: () => api.get('/marketplace/trades'),
};

// Wallet services
export const walletService = {
  getBalance: () => api.get('/wallet'),
};

export default {
  authService,
  creditService,
  marketplaceService,
  walletService,
};