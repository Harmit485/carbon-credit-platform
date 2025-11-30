import { useState, useEffect } from 'react';
import axios from 'axios';
import { API_BASE_URL } from '../../config/api';
import GlassCard from '../ui/GlassCard';
import Button from '../ui/Button';
import Modal from '../ui/Modal';
import InputField from '../ui/InputField';
import Table from '../ui/Table';
import Tag from '../ui/Tag';

const Wallet = ({ user }) => {
  const [walletData, setWalletData] = useState({
    balance: 0,
    carbonCreditBalance: 0,
    netRemainingCredit: 0
  });
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showDepositForm, setShowDepositForm] = useState(false);
  const [depositAmount, setDepositAmount] = useState('');

  useEffect(() => {
    const fetchWalletData = async () => {
      try {
        const token = localStorage.getItem('token');

        const [walletResponse, transactionsResponse, usageSummaryResponse] = await Promise.all([
          axios.get(`${API_BASE_URL}/wallet`, {
            headers: { Authorization: `Bearer ${token}` }
          }),
          axios.get(`${API_BASE_URL}/wallet/transactions`, {
            headers: { Authorization: `Bearer ${token}` }
          }),
          axios.get(`${API_BASE_URL}/usage/summary`, {
            headers: { Authorization: `Bearer ${token}` }
          }).catch(() => ({ data: { netRemainingCredits: 0 } })) // Graceful fallback
        ]);

        setWalletData({
          balance: walletResponse.data.balance,
          carbonCreditBalance: walletResponse.data.carbonCreditBalance,
          netRemainingCredit: usageSummaryResponse.data.netRemainingCredits || 0
        });

        setTransactions(transactionsResponse.data);
      } catch (error) {
        console.error('Error fetching wallet data:', error);
      } finally {
        setLoading(false);
      }
    };

    if (user?.id) {
      fetchWalletData();
    }
  }, [user]);

  const handleDeposit = async (e) => {
    e.preventDefault();
    try {
      const token = localStorage.getItem('token');
      const response = await axios.post(
        `${API_BASE_URL}/wallet/deposit`,
        { amount: parseFloat(depositAmount) },
        { headers: { Authorization: `Bearer ${token}` } }
      );

      setWalletData((prev) => ({
        ...prev,
        balance: response.data.balance
      }));

      setShowDepositForm(false);
      setDepositAmount('');
      alert('Funds added successfully!');
    } catch (error) {
      console.error('Error adding funds:', error);
      alert('Failed to add funds. Please try again.');
    }
  };

  if (loading) {
    return (
      <GlassCard title="Loading wallet" subtitle="Fetching balances and activity.">
        <p className="text-white/70">Please wait…</p>
      </GlassCard>
    );
  }

  const columns = [
    {
      label: 'Date',
      accessor: 'timestamp',
      render: (value) => {
        const date = new Date(value);
        return date.toLocaleDateString('en-US', {
          year: 'numeric',
          month: 'short',
          day: 'numeric'
        });
      }
    },
    {
      label: 'Type',
      accessor: 'type',
      render: (value) => (
        <Tag variant={['CREDIT', 'DEPOSIT', 'SALE'].includes(value) ? 'success' : 'danger'}>
          {value}
        </Tag>
      )
    },
    { label: 'Description', accessor: 'description' },
    {
      label: 'Amount',
      accessor: 'amount',
      render: (value, row) => (
        <div>
          <span style={{ color: value > 0 ? 'var(--color-success)' : 'var(--color-error)' }}>
            {value > 0 ? '+' : ''}
            ₹{Number(value).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </span>
          {row.carbonCredits && (
            <p className="text-xs" style={{ color: 'var(--color-text-muted)' }}>
              {row.carbonCredits > 0 ? '+' : ''}
              {row.carbonCredits} credits
            </p>
          )}
        </div>
      )
    }
  ];

  return (
    <div className="space-y-8">
      <GlassCard
        title="Trading wallet"
        subtitle="Manage your account balance, carbon credit holdings, and portfolio value with a secure, real-time audit ledger."
        action={
          <Button onClick={() => setShowDepositForm(true)} size="sm">
            Add funds
          </Button>
        }
      >
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          <Metric label="Account balance" value={`₹${Number(walletData.balance).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`} helper="INR ready to deploy" />
          <Metric
            label="Carbon credits"
            value={walletData.carbonCreditBalance.toFixed(2)}
            helper="1 tonne CO₂ per credit"
          />
          <Metric
            label="Net remaining credit"
            value={walletData.netRemainingCredit.toFixed(3)}
            helper={walletData.netRemainingCredit < 0 ? "Deficit (usage exceeds credits)" : "Surplus (credits exceed usage)"}
            variant={walletData.netRemainingCredit < 0 ? 'danger' : 'success'}
          />
        </div>
      </GlassCard>

      <GlassCard title="Transaction history" subtitle="Complete ledger of trading activity, deposits, and credit movements — tracked in a secure audit ledger with real-time WebSocket updates.">
        <Table columns={columns} data={transactions} emptyState="No transactions found." />
      </GlassCard>

      <Modal
        open={showDepositForm}
        onClose={() => setShowDepositForm(false)}
        title="Add funds"
        subtitle="Secure deposit into your wallet balance"
        actions={[
          <Button key="cancel" variant="secondary" onClick={() => setShowDepositForm(false)}>
            Cancel
          </Button>,
          <Button key="add" onClick={handleDeposit}>
            Add funds
          </Button>
        ]}
      >
        <form onSubmit={handleDeposit} className="space-y-4">
          <InputField
            label="Amount (INR)"
            type="number"
            min="0.01"
            step="0.01"
            value={depositAmount}
            onChange={(e) => setDepositAmount(e.target.value)}
            placeholder="1000"
            required
          />
        </form>
      </Modal>
    </div>
  );
};

const Metric = ({ label, value, helper, variant }) => (
  <div className="rounded-3xl border border-white/10 bg-white/5 px-4 sm:px-5 py-5 sm:py-6">
    <p className="text-xs uppercase tracking-[0.3em] text-white/40">{label}</p>
    <p
      className="mt-3 text-3xl font-semibold"
      style={{
        color: variant === 'danger' ? 'var(--color-error)' : variant === 'success' ? 'var(--color-success)' : 'var(--color-text)'
      }}
    >
      {value}
    </p>
    <p className="text-sm text-white/60">{helper}</p>
  </div>
);

export default Wallet;
