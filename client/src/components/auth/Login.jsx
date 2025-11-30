import { useState } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';
import { API_BASE_URL } from '../../config/api';
import GlassCard from '../ui/GlassCard';
import InputField from '../ui/InputField';
import Button from '../ui/Button';
import Tag from '../ui/Tag';

const Login = ({ setAuth, setUser }) => {
  const [formData, setFormData] = useState({
    email: '',
    password: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await axios.post(`${API_BASE_URL}/auth/signin`, formData);
      localStorage.setItem('token', response.data.token);
      localStorage.setItem(
        'user',
        JSON.stringify({
        id: response.data.id,
        email: response.data.email,
        roles: response.data.roles
        })
      );
      setAuth(true);
      setUser({
        id: response.data.id,
        email: response.data.email,
        roles: response.data.roles
      });
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex justify-center">
      <GlassCard
        className="w-full max-w-md"
        title="Welcome back"
        subtitle="Sign in to access the Carbon Trading System. Manage your emission cap, trade carbon credits, and monitor your portfolio."
      >
        {error && (
          <div className="mb-4">
            <Tag variant="danger">{error}</Tag>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-5">
          <InputField
            label="Email address"
              id="email"
            name="email"
              type="email"
            placeholder="nishankkansara@gmail.com"
              value={formData.email}
              onChange={handleChange}
              required
            />

          <InputField
            label="Password"
              id="password"
            name="password"
              type="password"
            placeholder="••••••••"
              value={formData.password}
              onChange={handleChange}
              required
            />

          <Button type="submit" className="w-full" loading={loading}>
            {loading ? 'Logging in...' : 'Sign in'}
          </Button>
        </form>

        <p className="mt-6 text-center text-sm text-white/60">
          Need an account?{' '}
          <Link to="/register" className="text-white">
            Register here
            </Link>
          </p>
      </GlassCard>
    </div>
  );
};

export default Login;
