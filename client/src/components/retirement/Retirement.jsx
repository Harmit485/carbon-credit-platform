import { useState, useEffect } from 'react';
import axios from 'axios';
import { API_BASE_URL } from '../../config/api';
import GlassCard from '../ui/GlassCard';
import Button from '../ui/Button';
import InputField from '../ui/InputField';
import Modal from '../ui/Modal';
import Table from '../ui/Table';
import Tag from '../ui/Tag';

const Retirement = ({ user }) => {
  const [wallet, setWallet] = useState(null);
  const [retirements, setRetirements] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [retireData, setRetireData] = useState({
    quantity: 1,
    reason: ''
  });

  useEffect(() => {
    const fetchRetirementData = async () => {
      try {
        const token = localStorage.getItem('token');

        const [walletResponse, retirementsResponse] = await Promise.all([
          axios.get(`${API_BASE_URL}/wallet`, {
            headers: { Authorization: `Bearer ${token}` }
          }),
          axios.get(`${API_BASE_URL}/retirements`, {
            headers: { Authorization: `Bearer ${token}` }
          })
        ]);

        setWallet(walletResponse.data);
        setRetirements(retirementsResponse.data);
      } catch (error) {
        console.error('Error fetching retirement data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchRetirementData();
  }, []);

  const handleRetireChange = (e) => {
    const { name, value } = e.target;
    setRetireData({
      ...retireData,
      [name]: name === 'quantity' ? Number(value) : value
    });
  };

  const handleRetireSubmit = async (e) => {
    e.preventDefault();
    try {
      const token = localStorage.getItem('token');
      await axios.post(`${API_BASE_URL}/retirements`, retireData, {
        headers: { Authorization: `Bearer ${token}` }
      });

      setRetireData({
        quantity: 1,
        reason: ''
      });
      setShowForm(false);

      const [retirementsResponse, walletResponse] = await Promise.all([
        axios.get(`${API_BASE_URL}/retirements`, {
          headers: { Authorization: `Bearer ${token}` }
        }),
        axios.get(`${API_BASE_URL}/wallet`, {
          headers: { Authorization: `Bearer ${token}` }
        })
      ]);

      setRetirements(retirementsResponse.data);
      setWallet(walletResponse.data);

      alert('Credits retired successfully!');
    } catch (error) {
      console.error('Error retiring credits:', error);
      alert('Failed to retire credits. Please try again.');
    }
  };

  if (loading) {
    return (
      <GlassCard title="Loading retirement data" subtitle="Fetching your impact history and available credits.">
        <p className="text-white/70">Please wait…</p>
      </GlassCard>
    );
  }

  const totalRetired = retirements.reduce((sum, r) => sum + r.quantity, 0);
  const totalOffset = totalRetired * 1000;
  const avgRetirement = retirements.length > 0 ? Math.round(totalRetired / retirements.length) : 0;

  const impactStats = [
    {
      label: 'Credits retired',
      value: totalRetired.toLocaleString(),
      detail: 'Total lifetime',
      variant: 'success'
    },
    {
      label: 'CO₂ offset',
      value: `${totalOffset.toLocaleString()} kg`,
      detail: 'Environmental impact',
      variant: 'accent'
    },
    {
      label: 'Certificates',
      value: retirements.length.toString(),
      detail: 'Retirement records',
      variant: 'neutral'
    },
    {
      label: 'Avg. size',
      value: avgRetirement.toString(),
      detail: 'Per retirement',
      variant: 'warning'
    }
  ];

  const retirementColumns = [
    {
      label: 'Date',
      accessor: 'timestamp',
      render: (value, row) => {
        const date = new Date(value || row.retiredAt);
        return date.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
      }
    },
    {
      label: 'Quantity',
      accessor: 'quantity',
      render: (value) => `${value.toLocaleString()} credits`
    },
    {
      label: 'Reason',
      accessor: 'reason',
      render: (value) => value || 'N/A'
    },
    {
      label: 'Certificate',
      accessor: 'id',
      render: (_, row) => (
        <Button size="sm" variant="secondary" as="a" href={`#cert-${row.id}`}>
          Download
        </Button>
      )
    }
  ];

  return (
    <div className="space-y-8">
      <GlassCard
        title="Carbon credit retirement"
        subtitle="Permanently retire carbon credits to offset emissions. Each retired credit represents 1 tonne CO₂ permanently removed from circulation."
        action={
          <Button onClick={() => setShowForm(true)} size="sm">
            Retire credits
          </Button>
        }
      >
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {impactStats.map((stat) => (
            <div
              key={stat.label}
              className="rounded-3xl border border-white/10 bg-white/5 px-5 py-6 text-center"
            >
              <p className="text-xs uppercase tracking-[0.3em] text-white/40">{stat.label}</p>
              <p className="mt-3 text-3xl font-semibold">{stat.value}</p>
              <p className="text-sm text-white/60">{stat.detail}</p>
            </div>
          ))}
        </div>
      </GlassCard>

      <GlassCard title="Retirement history" subtitle="Complete record of all retired credits and offset certificates — permanently recorded in a tamper‑evident audit ledger.">
        <Table
          columns={retirementColumns}
          data={retirements}
          emptyState={
            <div className="py-12 text-center">
              <p className="mb-4 text-white/70">You haven't retired any credits yet</p>
              <Button onClick={() => setShowForm(true)} variant="secondary" size="sm">
                Retire your first credits
              </Button>
            </div>
          }
        />
      </GlassCard>

      <Modal
        open={showForm}
        onClose={() => setShowForm(false)}
        title="Retire carbon credits"
        subtitle="Permanently retire carbon credits to offset emissions. Each credit represents 1 tonne CO₂. Retirement is recorded in the ledger and credits cannot be traded after retirement."
        actions={[
          <Button key="cancel" variant="secondary" onClick={() => setShowForm(false)}>
            Cancel
          </Button>,
          <Button key="retire" type="submit" form="retire-form" variant="danger">
            Retire credits
          </Button>
        ]}
      >
        <form id="retire-form" onSubmit={handleRetireSubmit} className="space-y-5">
          <div className="rounded-3xl border border-white/10 bg-white/5 p-4">
            <p className="text-sm font-medium mb-1">Available credits</p>
            <p className="text-2xl font-semibold">{wallet?.carbonCreditBalance?.toFixed(2) || '0.00'}</p>
          </div>

          <InputField
            label="Quantity"
            name="quantity"
            type="number"
            min="1"
            max={wallet?.carbonCreditBalance || undefined}
            value={retireData.quantity}
            onChange={handleRetireChange}
            placeholder="Enter quantity to retire"
            required
          />

          <div>
            <label className="mb-2 block text-sm font-medium">Reason for retirement</label>
            <textarea
              name="reason"
              value={retireData.reason}
              onChange={handleRetireChange}
              rows="4"
              className="glass-input"
              placeholder="Describe the purpose of retirement (e.g., meeting emission cap compliance, offsetting company emissions, environmental commitment, etc.)"
            />
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default Retirement;
