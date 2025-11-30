import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { API_BASE_URL } from '../../config/api';
import GlassCard from '../ui/GlassCard';
import InputField from '../ui/InputField';
import Button from '../ui/Button';
import Tag from '../ui/Tag';

const Register = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    organization: '',
    country: ''
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
      const response = await axios.post(`${API_BASE_URL}/auth/signup`, formData);
      alert(response.data.message);
      navigate('/login');
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex justify-center">
      <GlassCard
        className="w-full max-w-2xl"
        title="Join Carbon Trading System"
        subtitle="Register to participate in carbon credit trading. Set your emission cap and start trading credits representing 1 tonne CO₂ equivalent."
      >
        {error && (
          <div className="mb-4">
            <Tag variant="danger">{error}</Tag>
          </div>
        )}

        <form onSubmit={handleSubmit} className="grid gap-5 md:grid-cols-2">
          <InputField
            label="Full name"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleChange}
            placeholder="Harmit Khimani"
              required
            />

          <InputField
            label="Email"
              id="email"
            name="email"
              type="email"
              value={formData.email}
              onChange={handleChange}
            placeholder="harmit.khimani@example.com"
              required
            />

          <InputField
            label="Password"
              id="password"
            name="password"
              type="password"
              value={formData.password}
              onChange={handleChange}
            placeholder="Choose a strong passphrase"
              required
            />

          <InputField
            label="Organisation"
              id="organization"
              name="organization"
              value={formData.organization}
              onChange={handleChange}
            placeholder="Dhirubhai Ambani University"
              required
            />

          <InputField
            label="Country"
              id="country"
              name="country"
              value={formData.country}
              onChange={handleChange}
            placeholder="India"
              required
            />

          <div className="md:col-span-2">
            <Button type="submit" className="w-full" loading={loading}>
              {loading ? 'Creating workspace...' : 'Create workspace'}
            </Button>
          </div>
        </form>

        <p className="mt-6 text-center text-sm text-white/60">
          Already have an account?{' '}
          <Link to="/login" className="text-white">
            Sign in instead
            </Link>
          </p>
      </GlassCard>
    </div>
  );
};

export default Register;
