import { useState, useEffect, useMemo } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';
import { API_BASE_URL } from '../../config/api';
import GlassCard from '../ui/GlassCard';
import Button from '../ui/Button';
import Tag from '../ui/Tag';
import PriceChart from '../marketplace/PriceChart';

const Dashboard = ({ user }) => {
  const [wallet, setWallet] = useState({
    walletBalance: 0,
    carbonCreditBalance: 0,
    portfolioValue: 0
  });
  const [dashboardStats, setDashboardStats] = useState({});
  const [userStats, setUserStats] = useState({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        const token = localStorage.getItem('token');
        const [dashboardStatsResponse, statsResponse, walletResponse] = await Promise.all([
          axios.get(`${API_BASE_URL}/dashboard/stats`, {
            headers: { Authorization: `Bearer ${token}` }
          }),
          axios.get(`${API_BASE_URL}/dashboard/user-stats`, {
            headers: { Authorization: `Bearer ${token}` }
          }),
          axios.get(`${API_BASE_URL}/wallet`, {
            headers: { Authorization: `Bearer ${token}` }
          })
        ]);

        setDashboardStats(dashboardStatsResponse.data || {});
        setUserStats(statsResponse.data || {});
        setWallet({
          walletBalance: walletResponse.data.balance,
          carbonCreditBalance: walletResponse.data.carbonCreditBalance,
          portfolioValue: walletResponse.data.carbonCreditBalance * 10
        });
      } catch (error) {
        console.error('Error fetching dashboard data:', error);
      } finally {
        setLoading(false);
      }
    };

    if (user?.id) {
      fetchDashboardData();
    }
  }, [user]);

  const isIssuer = useMemo(() => {
    const roles = (user?.roles || []).map((role) => role.toUpperCase());
    // Admins should see the regular dashboard, not issuer dashboard
    const isAdmin = roles.some((role) => ['ADMIN', 'ROLE_ADMIN'].includes(role));
    if (isAdmin) return false;
    
    return roles.some((role) =>
      ['ISSUER', 'ROLE_ISSUER', 'VERIFIER', 'ROLE_VERIFIER'].includes(role)
    );
  }, [user]);

  if (loading) {
    return (
      <GlassCard title="Loading" subtitle="Fetching trading portfolio, wallet balance, and carbon credit positions.">
        <p className="text-white/70">Please stand by...</p>
      </GlassCard>
    );
  }

  if (isIssuer) {
    return <IssuerDashboard user={user} />;
  }

  const overviewCards = [
    {
      label: 'Wallet balance',
      value: `₹${wallet.walletBalance.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`,
      helper: 'Liquid INR available',
      action: (
        <Button as={Link} to="/wallet" size="sm">
          Add funds
        </Button>
      )
    },
    {
      label: 'Carbon credits',
      value: wallet.carbonCreditBalance.toFixed(2),
      helper: '1 tonne CO₂ per credit',
      action: <Tag variant="accent">{userStats?.preferredCategory || 'Active trader'}</Tag>
    },
    {
      label: 'Portfolio value',
      value: `₹${wallet.portfolioValue.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`,
      helper: 'Market value',
      action: <Tag variant="success">+{userStats?.portfolioGrowth || 0}%</Tag>
    },
    {
      label: 'CO₂ offset',
      value: `${wallet.carbonCreditBalance.toFixed(2)} tCO₂e`,
      helper: 'Total emissions offset',
      action: <Tag variant="neutral">{userStats?.region || 'Global'}</Tag>
    }
  ];

  const quickActions = [
    {
      title: 'Trade carbon credits',
      copy: 'Buy or sell credits through the order matching engine. Low emitters sell excess; high emitters buy to meet caps.',
      to: '/trading',
      accent: '💹'
    },
    {
      title: 'Carbon offset projects',
      copy: 'Explore reforestation and other offset projects. Each credit represents 1 tonne CO₂ reduction.',
      to: '/projects',
      accent: '🌿'
    },
    {
      title: 'Retire credits',
      copy: 'Permanently retire credits to offset emissions. Track your environmental impact.',
      to: '/retirement',
      accent: '🕊️'
    }
  ];

  return (
    <div className="space-y-8">
      <GlassCard
        title="Trading portfolio overview"
        subtitle={`Welcome back, ${user?.name || user?.email || 'trader'}. Monitor your carbon credit holdings, emission cap compliance, and real-time trading activity.`}
        action={
          <Button as={Link} to="/marketplace" variant="secondary">
            View marketplace
          </Button>
        }
      >
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {overviewCards.map((card) => (
            <div
              key={card.label}
              className="rounded-3xl border border-white/10 bg-white/[0.04] px-4 sm:px-5 py-5 sm:py-6"
            >
              <p className="text-xs uppercase tracking-[0.3em] text-white/40">{card.label}</p>
              <p className="mt-3 text-3xl font-semibold">{card.value}</p>
              <p className="text-sm text-white/60">{card.helper}</p>
              <div className="mt-4">{card.action}</div>
            </div>
          ))}
        </div>
      </GlassCard>

      <GlassCard title="Market price trend" subtitle="Real-time carbon credit price movements. Powered by WebSocket updates.">
        <PriceChart />
      </GlassCard>

      <div className="grid gap-6 md:grid-cols-3">
        {quickActions.map((action) => (
          <GlassCard
            key={action.title}
            className="h-full"
            title={action.title}
            subtitle={action.copy}
            action={
              <Button as={Link} to={action.to} size="sm">
                Open
              </Button>
            }
          >
            <div className="text-4xl">{action.accent}</div>
          </GlassCard>
        ))}
      </div>

      <GlassCard
        title="Recent trading activity"
        subtitle="Latest transactions, order executions, and credit movements — tracked in a secure audit ledger."
        action={
          <Button as={Link} to="/wallet" variant="secondary">
            View wallet
          </Button>
        }
      >
        <div className="space-y-3 text-sm text-white/60">
          {Array.isArray(userStats?.recentActivity) && userStats.recentActivity.length > 0 ? (
            userStats.recentActivity.map((entry) => (
              <div
                key={entry.id}
                className="flex items-center justify-between rounded-2xl border border-white/5 bg-white/5 px-4 py-3"
              >
                <span>{entry.description}</span>
                <Tag variant={entry.type === 'CREDIT' ? 'success' : 'accent'}>
                  {entry.amount > 0 ? '+' : ''}
                  {entry.amount}
                </Tag>
              </div>
            ))
          ) : (
            <p>No recent movements captured yet.</p>
          )}
        </div>
      </GlassCard>
    </div>
  );
};

const IssuerDashboard = ({ user }) => {
  const duties = [
    'Review project documentation & baselines',
    'Verify MRV methodology compliance',
    'Approve, reject, or flag follow-ups',
    'Issue credits for verified projects'
  ];

  const systemStatus = [
    { label: 'Trading market', value: 'Active', variant: 'success' },
    { label: 'Order matching engine', value: 'Running · Real-time', variant: 'accent' },
    { label: 'Reviews queue', value: '4 pending files', variant: 'warning' }
  ];

  return (
    <div className="space-y-8">
      <GlassCard
        title="Verifier workspace"
        subtitle={`Welcome back ${user?.name || 'Verifier'}. Review and verify carbon offset projects before credits can be issued and traded on the marketplace.`}
        action={
          <Button as={Link} to="/projects">
            Review queue
          </Button>
        }
      />

      <div className="grid gap-6 md:grid-cols-2">
        <GlassCard title="Verification tasks">
          <ul className="space-y-3 text-white/70">
            {duties.map((item) => (
              <li key={item} className="flex items-start gap-3">
                <span>•</span>
                <span>{item}</span>
              </li>
            ))}
          </ul>
        </GlassCard>

        <GlassCard title="Trading system status">
          <div className="space-y-4">
            {systemStatus.map((status) => (
              <div
                key={status.label}
                className="flex items-center justify-between rounded-2xl border border-white/5 bg-white/5 px-4 py-3"
              >
                <p>{status.label}</p>
                <Tag variant={status.variant}>{status.value}</Tag>
              </div>
            ))}
          </div>
        </GlassCard>
      </div>
    </div>
  );
};

export default Dashboard;
