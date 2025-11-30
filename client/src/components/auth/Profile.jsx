import { useState, useEffect } from 'react';
import axios from 'axios';
import { API_BASE_URL } from '../../config/api';
import GlassCard from '../ui/GlassCard';
import InputField from '../ui/InputField';
import Button from '../ui/Button';
import Tag from '../ui/Tag';

const Profile = ({ user }) => {
  const [profileData, setProfileData] = useState({
    name: '',
    email: '',
    organization: '',
    country: '',
    profileImage: ''
  });
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);

  useEffect(() => {
    const fetchProfileData = async () => {
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get(`${API_BASE_URL}/user/profile`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        
        setProfileData(response.data);
      } catch (error) {
        console.error('Error fetching profile data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchProfileData();
  }, []);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setProfileData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setUpdating(true);
    
    try {
      const token = localStorage.getItem('token');
      const response = await axios.put(`${API_BASE_URL}/user/profile`, profileData, {
        headers: { Authorization: `Bearer ${token}` }
      });
      
      setProfileData(response.data);
      alert('Profile updated successfully!');
    } catch (error) {
      console.error('Error updating profile:', error);
      alert('Failed to update profile. Please try again.');
    } finally {
      setUpdating(false);
    }
  };

  if (loading) {
    return (
      <GlassCard title="Loading profile" subtitle="Fetching your account data.">
        <p className="text-white/70">One moment…</p>
      </GlassCard>
    );
  }

  const roleTags = profileData.roles?.length
    ? profileData.roles.map((role) => role.replace('ROLE_', ''))
    : ['USER'];

  return (
    <div className="space-y-8">
      <GlassCard
        title="User profile"
        subtitle="Manage your account details, organization information, and trading preferences for the Carbon Trading System."
        action={<Tag variant="accent">{user?.email}</Tag>}
      >
        <div className="flex flex-col gap-8 md:flex-row">
          <div className="md:w-1/3">
            <div className="flex flex-col items-center">
              <div className="flex h-32 w-32 items-center justify-center rounded-full border border-white/20 bg-white/10 text-4xl font-semibold text-white">
                {profileData.name?.charAt(0).toUpperCase() || 'U'}
              </div>
              <Button variant="secondary" size="sm" className="mt-4">
                Change photo
              </Button>
            </div>
            <div className="mt-6 space-y-2 text-sm text-white/70">
              <p>
                <span className="text-white">Email:</span> {profileData.email}
              </p>
              <p className="flex flex-wrap gap-2">
                <span className="text-white">Roles:</span>
                {roleTags.map((role) => (
                  <Tag key={role}>{role}</Tag>
                ))}
              </p>
              <p>
                <span className="text-white">Member since:</span> {new Date().getFullYear()}
              </p>
            </div>
          </div>
          
          <div className="md:w-2/3 space-y-8">
            <form onSubmit={handleSubmit} className="grid gap-5 md:grid-cols-2">
              <InputField
                label="Full name"
                    name="name"
                    value={profileData.name}
                    onChange={handleInputChange}
                    placeholder="Enter your full name"
                  />
              <InputField
                label="Email"
                    name="email"
                    value={profileData.email}
                    disabled
                inputClassName="opacity-70 cursor-not-allowed"
                  />
              <InputField
                label="Organization"
                    name="organization"
                    value={profileData.organization || ''}
                    onChange={handleInputChange}
                    placeholder="Enter your organization"
                  />
              <InputField
                label="Country"
                    name="country"
                    value={profileData.country || ''}
                    onChange={handleInputChange}
                    placeholder="Enter your country"
                  />
              <div className="md:col-span-2 flex justify-end">
                <Button type="submit" loading={updating}>
                  {updating ? 'Updating...' : 'Update profile'}
                </Button>
              </div>
            </form>
            
            <div className="rounded-3xl border border-white/10 bg-white/5 p-5">
              <div className="flex items-center justify-between">
                  <div>
                  <p className="text-white font-semibold">Security</p>
                  <p className="text-sm text-white/60">Password last rotated 3 months ago</p>
                </div>
                <Button variant="secondary" size="sm">
                  Change password
                </Button>
              </div>
            </div>
          </div>
        </div>
      </GlassCard>
    </div>
  );
};

export default Profile;