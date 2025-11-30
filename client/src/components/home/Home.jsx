import { Link } from 'react-router-dom';
import GlassCard from '../ui/GlassCard';
import Button from '../ui/Button';
import Tag from '../ui/Tag';

const steps = [
  {
    title: 'Register & Set Emission Cap',
    copy: 'Create an account and establish your emission cap. Low emitters can sell excess credits; high emitters can buy to meet requirements.',
    icon: '01'
  },
  {
    title: 'Trade Carbon Credits',
    copy: 'Participate in real-time carbon credit trading. Each credit represents 1 ton of CO₂. Buy or sell credits through the order matching engine.',
    icon: '02'
  },
  {
    title: 'Monitor & Offset',
    copy: 'Track your emissions, portfolio performance, and carbon offsets with real-time analytics, WebSocket updates, and a secure audit ledger.',
    icon: '03'
  }
];

const stats = [
  { label: 'Credits traded', value: '10K+', detail: 'Year to date' },
  { label: 'Active participants', value: '512', detail: 'Low & high emitters' },
  { label: 'Avg. price per credit', value: '₹12.40', detail: '1 tonne CO₂ equivalent' },
  { label: 'Emissions offset', value: '1.2M tCO₂e', detail: 'Total impact' }
];

const Home = () => (
  <div className="space-y-8">
    <GlassCard
      variant="accent"
      className="relative overflow-hidden px-6 py-10 lg:px-12 lg:py-14"
      title="Carbon Trading System"
      subtitle="A market platform for carbon credit trading and emissions reduction. Manage emission caps, and participate in real-time order matching."
      action={
        <div className="flex flex-col gap-3 sm:flex-row">
          <Button as={Link} to="/register">
            Get started
          </Button>
          <Button as={Link} to="/marketplace" variant="secondary">
            View marketplace
          </Button>
        </div>
      }
    >
      <div className="mt-10 grid gap-10 lg:grid-cols-2">
        <div className="space-y-6">
          <Tag variant="accent">Market-based trading</Tag>
          <h1 className="text-3xl sm:text-4xl lg:text-5xl">
            Real-time carbon credit trading with order matching and ledger transparency.
          </h1>
          <p className="text-base sm:text-lg text-white/70">
            Harmit Khimani -202201231<br />
            Nishank Kansara -202201227<br />
            <br />
            Mentored by Prof. Tapas Kumar Maiti
          </p>
        </div>
        <div className="glass-panel border border-white/20 p-6 sm:p-8">
          <p className="text-sm uppercase tracking-[0.3em] text-white/50">Live trading preview</p>
          <p className="mt-4 text-2xl sm:text-3xl font-semibold">Portfolio · ₹1,28,420</p>
          <div className="mt-6 grid gap-4 sm:grid-cols-2">
            <MiniStat label="Wallet balance" value="₹42,800" delta="+8.2%" />
            <MiniStat label="Carbon credits" value="3,560" delta="+120" />
            <MiniStat label="Avg. price" value="₹11.90" delta="-0.4%" />
            <MiniStat label="CO₂ offset" value="3.56K tCO₂e" delta="+12%" />
          </div>
        </div>
      </div>
    </GlassCard>

    <section className="grid gap-6 lg:grid-cols-3">
      {steps.map((step) => (
        <GlassCard key={step.title}>
          <div className="mb-6 flex h-12 w-12 items-center justify-center rounded-2xl border border-white/20 bg-white/10 text-lg font-semibold">
            {step.icon}
          </div>
          <h3 className="text-2xl">{step.title}</h3>
          <p className="mt-3 text-white/70">{step.copy}</p>
        </GlassCard>
      ))}
    </section>

    <GlassCard variant="subtle" className="text-center">
      <p className="text-sm uppercase tracking-[0.4em] text-white/50">Trading metrics</p>
      <div className="mt-6 grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
        {stats.map((stat) => (
          <div key={stat.label} className="rounded-3xl border border-white/5 bg-white/5 px-6 py-5">
            <p className="text-xs uppercase tracking-[0.4em] text-white/50">{stat.label}</p>
            <p className="mt-3 text-3xl font-semibold">{stat.value}</p>
            <p className="text-sm text-white/60">{stat.detail}</p>
          </div>
        ))}
      </div>
    </GlassCard>
  </div>
);

const MiniStat = ({ label, value, delta }) => (
  <div className="rounded-2xl border border-white/5 bg-white/5 p-4">
    <p className="text-xs uppercase tracking-[0.3em] text-white/40">{label}</p>
    <p className="mt-2 text-2xl font-semibold">{value}</p>
    <p className={cnDelta(delta)}>{delta}</p>
  </div>
);

const cnDelta = (delta) => (delta?.startsWith('-') ? 'text-rose-300 text-sm mt-1' : 'text-emerald-300 text-sm mt-1');

export default Home;
