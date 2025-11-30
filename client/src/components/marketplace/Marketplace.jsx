import { useState, useEffect, useMemo } from 'react';
import axios from 'axios';
import { API_BASE_URL } from '../../config/api';
import PriceChart from './PriceChart';
import GlassCard from '../ui/GlassCard';
import Button from '../ui/Button';
import Tag from '../ui/Tag';
import Table from '../ui/Table';

const FILTERS = [
  { id: 'all', label: 'All orders' },
  { id: 'buy', label: 'Buy only' },
  { id: 'sell', label: 'Sell only' },
  { id: 'my_orders', label: 'My orders' }
];

const Marketplace = ({ user }) => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('all');

  const fetchOrders = async () => {
    const token = localStorage.getItem('token');
    const ordersRes = await axios.get(`${API_BASE_URL}/marketplace/orders`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    setOrders(ordersRes.data);
  };

  useEffect(() => {
    const loadData = async () => {
      try {
        await fetchOrders();
      } catch (error) {
        console.error('Error fetching marketplace data:', error);
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, []);

  const handleCancelOrder = async (orderId) => {
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        alert('Authentication token not found. Please log in again.');
        return;
      }

      console.log('Canceling order:', orderId);
      // Log token for debugging (masked)
      console.log('Token present:', !!token);

      await axios({
        method: 'put',
        url: `${API_BASE_URL}/marketplace/orders/${orderId}/cancel`,
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      await fetchOrders();
      alert('Order canceled successfully');
    } catch (error) {
      console.error('Error canceling order:', error);
      if (error.response) {
        console.error('Response status:', error.response.status);
        console.error('Response data:', error.response.data);
      }
      alert(`Failed to cancel order: ${error.response?.data?.message || error.message || 'Request failed'}`);
    }
  };

  const handleTrade = async (order, action) => {
    try {
      const token = localStorage.getItem('token');
      const payload = {
        quantity: order.quantity,
        pricePerUnit: order.pricePerUnit,
        type: action
      };
      await axios.post(`${API_BASE_URL}/marketplace/orders`, payload, {
        headers: { Authorization: `Bearer ${token}` }
      });
      await fetchOrders();
      alert(`${action} order placed successfully! It will be matched shortly.`);
    } catch (error) {
      console.error(`Error placing ${action} order:`, error);
      alert(`Failed to place ${action} order. Please try again.`);
    }
  };

  const normalizedOrders = useMemo(
    () =>
      orders.map((order) => ({
        ...order,
        total: order.quantity * order.pricePerUnit
      })),
    [orders]
  );

  const filteredOrders = useMemo(() => {
    if (filter === 'all') return normalizedOrders;
    if (filter === 'my_orders') {
      return normalizedOrders.filter((order) => order.userId === user?.id);
    }
    return normalizedOrders.filter(
      (order) =>
        order.type === filter.toUpperCase() &&
        (order.status === 'PENDING' || order.status === 'PARTIAL')
    );
  }, [filter, normalizedOrders, user]);

  const buyOrders = filteredOrders.filter((order) => order.type === 'BUY');
  const sellOrders = filteredOrders.filter((order) => order.type === 'SELL');

  if (loading) {
    return (
      <div className="glass-card">
        <p>Loading marketplace data...</p>
      </div>
    );
  }

  const orderColumns = [
    {
      label: 'Created',
      accessor: 'createdAt',
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
      render: (value) => <Tag variant={value === 'BUY' ? 'success' : 'accent'}>{value}</Tag>
    },
    { label: 'Quantity', accessor: 'quantity' },
    { label: 'Price', accessor: 'pricePerUnit', type: 'currency' },
    {
      label: 'Total',
      accessor: 'total',
      render: (value) => `₹${Number(value).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
    },
    {
      label: 'Status',
      accessor: 'status',
      render: (value) => (
        <Tag
          variant={
            value === 'EXECUTED' ? 'success' : value === 'CANCELLED' ? 'danger' : 'warning'
          }
        >
          {value}
        </Tag>
      )
    },
    {
      label: 'Action',
      accessor: 'id',
      render: (_, row) =>
        row.userId === user?.id && (row.status === 'PENDING' || row.status === 'PARTIAL') ? (
          <Button size="sm" variant="secondary" onClick={() => handleCancelOrder(row.id)}>
            Cancel
          </Button>
        ) : (
          '—'
        )
    }
  ];

  const liquidityStats = [
    {
      label: 'Lowest ask',
      value:
        sellOrders.length > 0
          ? `₹${Math.min(...sellOrders.map((o) => o.pricePerUnit)).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
          : '₹0.00',
      detail: 'Best sell offer'
    },
    {
      label: 'Highest bid',
      value:
        buyOrders.length > 0
          ? `₹${Math.max(...buyOrders.map((o) => o.pricePerUnit)).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
          : '₹0.00',
      detail: 'Top buy offer'
    },
    {
      label: 'Open orders',
      value: orders.length.toString(),
      detail: 'Across all books'
    },
    {
      label: 'Avg. blended price',
      value: `₹${(
        orders.reduce((sum, o) => sum + o.quantity * o.pricePerUnit, 0) / (orders.length || 1)
      ).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`,
      detail: 'Weight by size'
    }
  ];

  const isAdmin = user?.roles?.some(role => role === 'ADMIN' || role === 'ROLE_ADMIN');

  return (
    <div className="space-y-8">
      <GlassCard
        title="Carbon credit marketplace"
        subtitle="Real-time trading platform for carbon credits. Each credit represents 1 tonne CO₂. Orders are matched by the trading engine."
        action={
          <div className="flex flex-wrap gap-2">
            {FILTERS.map((item) => (
              <Button
                key={item.id}
                size="sm"
                variant={filter === item.id ? 'primary' : 'secondary'}
                onClick={() => setFilter(item.id)}
              >
                {item.label}
              </Button>
            ))}
          </div>
        }
      >
        <div className="mt-4">
          <PriceChart />
        </div>
      </GlassCard>

      {filter === 'my_orders' ? (
        <GlassCard title="My orders" subtitle="Track your active and executed orders. Transactions are recorded in a secure audit ledger with real-time analytics.">
          <Table columns={orderColumns} data={filteredOrders} emptyState="No orders yet." />
        </GlassCard>
      ) : (
        <div className="grid gap-6 lg:grid-cols-2 min-w-0">
          <BookCard
            title="Buy orders (High emitters)"
            highlight="success"
            orders={buyOrders}
            emptyLabel="No buy orders available"
            onCancel={handleCancelOrder}
            onAction={(order) => handleTrade(order, 'SELL')}
            userId={user?.id}
            actionLabel="Sell into bid"
            isAdmin={isAdmin}
          />
          <BookCard
            title="Sell orders (Low emitters)"
            highlight="accent"
            orders={sellOrders}
            emptyLabel="No sell orders available"
            onCancel={handleCancelOrder}
            onAction={(order) => handleTrade(order, 'BUY')}
            userId={user?.id}
            actionLabel="Buy from ask"
            isAdmin={isAdmin}
          />
        </div>
      )}

      <GlassCard title="Market statistics" subtitle="Real-time market metrics. Updates via WebSocket as orders are matched by the trading engine.">
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {liquidityStats.map((stat) => (
            <div
              key={stat.label}
              className="rounded-3xl border border-white/5 bg-white/5 px-6 py-5 text-left"
            >
              <p className="text-xs uppercase tracking-[0.3em] text-white/40">{stat.label}</p>
              <p className="mt-2 text-3xl font-semibold">{stat.value}</p>
              <p className="text-sm text-white/60">{stat.detail}</p>
            </div>
          ))}
        </div>
      </GlassCard>
    </div>
  );
};

const BookCard = ({
  title,
  highlight,
  orders,
  emptyLabel,
  onCancel,
  onAction,
  actionLabel,
  userId,
  isAdmin
}) => (
  <GlassCard title={title} subtitle="All price levels · Real-time order matching">
    {orders.length === 0 ? (
      <div className="py-16 text-center text-white/50">{emptyLabel}</div>
    ) : (
      <div className="glass-panel overflow-hidden">
        <div className="space-y-4 max-h-96 overflow-y-auto px-4 sm:px-0">
          {orders.map((order) => (
            <div
              key={order.id}
              className="flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-white/5 bg-white/5 px-5 py-4"
            >
              <div>
                <p className="text-lg font-semibold">₹{Number(order.pricePerUnit).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</p>
                <p className="text-sm text-white/60">{order.quantity} credits</p>
              </div>
              <div className="flex items-center gap-3">
                <Tag variant={highlight}>{order.status}</Tag>
                {(order.status === 'PENDING' || order.status === 'PARTIAL') ? (
                  <Button
                    size="sm"
                    variant="secondary"
                    onClick={() => order.userId === userId ? onCancel(order.id) : null}
                    disabled={order.userId !== userId || isAdmin}
                    className={order.userId !== userId || isAdmin ? "opacity-30 cursor-not-allowed" : ""}
                    title={
                      isAdmin
                        ? "Admins cannot cancel orders"
                        : order.userId !== userId
                          ? "You can only cancel your own orders"
                          : "Cancel this order"
                    }
                  >
                    Cancel
                  </Button>
                ) : (
                  <span className="text-white/40">—</span>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>
    )}
  </GlassCard>
);

export default Marketplace;
