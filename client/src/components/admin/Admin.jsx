import { useState, useEffect } from 'react';
import axios from 'axios';
import { API_BASE_URL } from '../../config/api';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  LineElement,
  PointElement,
  Title,
  Tooltip,
  Legend,
  ArcElement
} from 'chart.js';
import { Line } from 'react-chartjs-2';

// Register Chart.js components
ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  LineElement,
  PointElement,
  Title,
  Tooltip,
  Legend,
  ArcElement
);

const Admin = ({ user }) => {
  const [stats, setStats] = useState({
    totalUsers: 0,
    totalProjects: 0,
    totalCredits: 0,
    totalTrades: 0,
    averageTradePrice: 0,
    marketDemand: 0,
    marketSupply: 0
  });
  const [marketActivity, setMarketActivity] = useState({
    labels: [],
    trades: [],
    newCredits: []
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const token = localStorage.getItem('token');

        // Fetch system stats
        const statsResponse = await axios.get(`${API_BASE_URL}/admin/stats`, {
          headers: { Authorization: `Bearer ${token}` }
        });

        // Fetch market activity
        const marketActivityResponse = await axios.get(`${API_BASE_URL}/admin/market-activity`, {
          headers: { Authorization: `Bearer ${token}` }
        });

        setStats(statsResponse.data);
        setMarketActivity(marketActivityResponse.data);
      } catch (error) {
        console.error('Error fetching admin data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const marketActivityData = {
    labels: marketActivity.labels,
    datasets: [
      {
        label: 'Trades',
        data: marketActivity.trades,
        fill: false,
        borderColor: 'rgb(75, 192, 192)',
        tension: 0.1,
      },
      {
        label: 'New Credits',
        data: marketActivity.newCredits,
        fill: false,
        borderColor: 'rgb(255, 99, 132)',
        tension: 0.1,
      },
    ],
  };

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-16">
        <div className="flex justify-center items-center h-64">
          <p className="text-lg">Loading admin dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold text-gray-800 mb-6">Admin Dashboard</h1>

      {/* Stats Overview */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div className="bg-white rounded-xl shadow-sm p-6">
          <h2 className="text-lg font-medium text-gray-600 mb-2">Total Users</h2>
          <p className="text-3xl font-bold text-gray-800">{stats.totalUsers}</p>
        </div>
        <div className="bg-white rounded-xl shadow-sm p-6">
          <h2 className="text-lg font-medium text-gray-600 mb-2">Projects</h2>
          <p className="text-3xl font-bold text-gray-800">{stats.totalProjects}</p>
        </div>
        <div className="bg-white rounded-xl shadow-sm p-6">
          <h2 className="text-lg font-medium text-gray-600 mb-2">Credits Issued</h2>
          <p className="text-3xl font-bold text-gray-800">{stats.totalCredits}</p>
        </div>
        <div className="bg-white rounded-xl shadow-sm p-6">
          <h2 className="text-lg font-medium text-gray-600 mb-2">Trades</h2>
          <p className="text-3xl font-bold text-gray-800">{stats.totalTrades}</p>
        </div>
      </div>

      {/* System Overview Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div className="bg-white rounded-xl shadow-sm p-6">
          <h2 className="text-lg font-medium text-gray-600 mb-2">Average Trade Price</h2>
          <p className="text-3xl font-bold text-gray-800">₹{Number(stats.averageTradePrice || 0).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</p>
        </div>
        <div className="bg-white rounded-xl shadow-sm p-6">
          <h2 className="text-lg font-medium text-gray-600 mb-2">Market Demand</h2>
          <p className="text-3xl font-bold text-gray-800">{stats.marketDemand?.toFixed(0) || '0'}</p>
        </div>
        <div className="bg-white rounded-xl shadow-sm p-6">
          <h2 className="text-lg font-medium text-gray-600 mb-2">Market Supply</h2>
          <p className="text-3xl font-bold text-gray-800">{stats.marketSupply?.toFixed(0) || '0'}</p>
        </div>
      </div>

      {/* Market Activity Chart */}
      <div className="bg-white rounded-xl shadow-sm p-6 mb-8">
        <h2 className="text-xl font-semibold mb-4">Market Activity</h2>
        <div className="h-96">
          <Line data={marketActivityData} options={{ maintainAspectRatio: false }} />
        </div>
      </div>
    </div>
  );
};

export default Admin;
