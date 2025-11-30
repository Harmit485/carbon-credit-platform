import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';
import { API_BASE_URL } from '../../config/api';
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend,
    TimeScale,
    Filler
} from 'chart.js';
import { Line } from 'react-chartjs-2';
import 'chartjs-adapter-date-fns';
import GlassCard from '../ui/GlassCard';
import Button from '../ui/Button';
import InputField from '../ui/InputField';
import Table from '../ui/Table';
import Tag from '../ui/Tag';

ChartJS.register(
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend,
    TimeScale,
    Filler
);

const Usage = () => {
    const [config, setConfig] = useState({
        broker: '',
        username: '',
        password: '',
        topic: ''
    });
    const [summary, setSummary] = useState({
        totalUsageKg: 0,
        walletCredits: 0,
        netRemainingCredits: 0,
        showWarning: false
    });
    const [recentUsage, setRecentUsage] = useState([]);
    const [history, setHistory] = useState([]);
    const [loading, setLoading] = useState(true);
    const [configLoading, setConfigLoading] = useState(false);
    const [message, setMessage] = useState({ text: '', type: '' });

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            const token = localStorage.getItem('token');
            const headers = { Authorization: `Bearer ${token}` };

            const [summaryRes, recentRes, historyRes, configRes] = await Promise.all([
                axios.get(`${API_BASE_URL}/usage/summary`, { headers }),
                axios.get(`${API_BASE_URL}/usage/recent`, { headers }),
                axios.get(`${API_BASE_URL}/usage/history`, { headers }),
                axios.get(`${API_BASE_URL}/usage/config`, { headers })
            ]);

            setSummary(summaryRes.data);
            setRecentUsage(recentRes.data);
            setHistory(historyRes.data);
            if (configRes.data) {
                setConfig({
                    broker: configRes.data.broker || '',
                    username: configRes.data.username || '',
                    password: configRes.data.password || '',
                    topic: configRes.data.topic || ''
                });
            }
            setLoading(false);
        } catch (error) {
            console.error('Error fetching usage data:', error);
            setLoading(false);
        }
    };

    const handleConfigSubmit = async (e) => {
        e.preventDefault();
        setConfigLoading(true);
        setMessage({ text: '', type: '' });
        try {
            const token = localStorage.getItem('token');
            await axios.post(`${API_BASE_URL}/usage/config`, config, {
                headers: { Authorization: `Bearer ${token}` }
            });
            setMessage({ text: 'Configuration updated successfully', type: 'success' });
        } catch (error) {
            console.error('Error updating config:', error);
            setMessage({ text: 'Failed to update configuration', type: 'error' });
        } finally {
            setConfigLoading(false);
        }
    };

    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    const gradient = ctx.createLinearGradient(0, 0, 0, 400);
    gradient.addColorStop(0, 'rgba(16, 185, 129, 0.2)'); // Emerald with low opacity
    gradient.addColorStop(1, 'rgba(16, 185, 129, 0)');   // Fade to transparent

    // Calculate cumulative usage for the chart
    // Reverse the array to go from oldest to newest for proper cumulative calculation
    let cumulativeUsage = 0;
    const cumulativeData = [...history].reverse().map(entry => {
        cumulativeUsage += entry.co2KgDelta;
        return {
            x: new Date(entry.timestamp),
            y: cumulativeUsage / 1000.0 // Convert to tons
        };
    });

    const chartData = {
        datasets: [
            {
                label: 'Total CO₂ Usage (tons)',
                data: cumulativeData,
                borderColor: 'rgb(16, 185, 129)',
                backgroundColor: gradient,
                fill: true,
                borderWidth: 2,
                pointRadius: 0,
                pointHoverRadius: 6,
                pointHoverBackgroundColor: '#fff',
                pointHoverBorderColor: 'rgb(16, 185, 129)',
                pointHoverBorderWidth: 2,
                tension: 0.1
            }
        ]
    };

    const chartOptions = {
        responsive: true,
        maintainAspectRatio: false,
        interaction: {
            mode: 'index',
            intersect: false,
        },
        plugins: {
            legend: {
                display: false
            },
            tooltip: {
                backgroundColor: 'rgba(5, 46, 22, 0.95)',
                titleColor: 'rgba(255, 255, 255, 0.9)',
                bodyColor: '#fff',
                borderColor: 'rgba(16, 185, 129, 0.4)',
                borderWidth: 1,
                padding: 12,
                displayColors: false,
                callbacks: {
                    title: (items) => {
                        const date = new Date(items[0].parsed.x);
                        return date.toLocaleString();
                    },
                    label: (context) => `Total Usage: ${context.parsed.y.toFixed(3)} tons CO₂`
                }
            }
        },
        scales: {
            x: {
                type: 'time',
                time: {
                    unit: 'day',
                    displayFormats: {
                        day: 'MMM d',
                        hour: 'HH:mm'
                    }
                },
                grid: {
                    display: false,
                    drawBorder: false
                },
                ticks: {
                    color: 'rgba(5, 46, 22, 0.7)',
                    maxRotation: 0,
                    autoSkip: true,
                    maxTicksLimit: 6
                }
            },
            y: {
                position: 'right',
                title: {
                    display: true,
                    text: 'Total Usage (tons CO₂)',
                    color: 'rgba(5, 46, 22, 0.7)'
                },
                grid: {
                    color: 'rgba(5, 46, 22, 0.1)',
                    drawBorder: false
                },
                ticks: {
                    color: 'rgba(5, 46, 22, 0.7)',
                    callback: (value) => `${value.toFixed(3)} tons`
                }
            }
        }
    };

    const usageTableColumns = [
        {
            label: 'Timestamp',
            accessor: 'timestamp',
            render: (value) => {
                const date = new Date(value);
                return date.toLocaleString('en-US', {
                    year: 'numeric',
                    month: '2-digit',
                    day: '2-digit',
                    hour: '2-digit',
                    minute: '2-digit',
                    second: '2-digit',
                    hour12: false
                });
            }
        },
        {
            label: 'Usage (kg)',
            accessor: 'co2KgDelta',
            render: (value) => `${Number(value || 0).toFixed(2)} kg`
        }
    ];

    if (loading) {
        return (
            <GlassCard title="Loading" subtitle="Fetching usage data and configuration.">
                <p className="text-white/70">Please stand by...</p>
            </GlassCard>
        );
    }

    const summaryCards = [
        {
            label: 'Total Usage',
            value: `${(summary.totalUsageTons || 0).toFixed(3)} tons`,
            helper: `${(summary.totalUsageKg || 0).toFixed(2)} kg CO₂`,
            variant: 'neutral'
        },
        {
            label: 'Wallet Credits',
            value: (summary.walletCredits || 0).toFixed(2),
            helper: 'Available carbon credits',
            variant: 'success'
        },
        {
            label: 'Net Remaining',
            value: (summary.netRemainingCredits || 0).toFixed(3),
            helper: (summary.netRemainingCredits || 0) < 0 ? 'Deficit' : 'Surplus',
            variant: (summary.netRemainingCredits || 0) < 0 ? 'danger' : 'accent'
        }
    ];

    return (
        <div className="space-y-8">

            {/* Warning Banner */}
            {summary.showWarning && (
                <GlassCard variant="highlight">
                    <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                        <div>
                            <strong className="font-semibold text-white/90">Warning:</strong>
                            <p className="mt-1 text-sm text-white/70">
                                Your carbon usage exceeds your available credits.
                            </p>
                        </div>
                        <Button as={Link} to="/marketplace" variant="danger">
                            Buy Credits
                        </Button>
                    </div>
                </GlassCard>
            )}

            {/* Summary Cards */}
            <GlassCard title="Usage overview" subtitle="Real-time carbon usage metrics and credit balance.">
                <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                    {summaryCards.map((card) => (
                        <div
                            key={card.label}
                            className="rounded-3xl border border-white/10 bg-white/[0.04] px-4 sm:px-5 py-5 sm:py-6"
                        >
                            <p className="text-xs uppercase tracking-[0.3em] text-white/40">{card.label}</p>
                            <p className="mt-3 text-3xl font-semibold">{card.value}</p>
                            <p className="text-sm text-white/60">{card.helper}</p>

                        </div>
                    ))}
                </div>
            </GlassCard>

            <div className="grid gap-6 lg:grid-cols-2">
                {/* MQTT Config Form */}
                <GlassCard
                    title="MQTT Configuration"
                    subtitle="Configure MQTT broker settings to stream real-time carbon usage data. The system will automatically track emissions from connected devices."
                >
                    <form onSubmit={handleConfigSubmit} className="space-y-5">
                        <InputField
                            label="Broker URL"
                            name="broker"
                            type="text"
                            value={config.broker}
                            onChange={(e) => setConfig({ ...config, broker: e.target.value })}
                            placeholder="tcp://broker.hivemq.com:1883"
                            required
                        />
                        <div className="grid gap-5 sm:grid-cols-2">
                            <InputField
                                label="Username"
                                name="username"
                                type="text"
                                value={config.username}
                                onChange={(e) => setConfig({ ...config, username: e.target.value })}
                            />
                            <InputField
                                label="Password"
                                name="password"
                                type="password"
                                value={config.password}
                                onChange={(e) => setConfig({ ...config, password: e.target.value })}
                            />
                        </div>
                        <InputField
                            label="Topic"
                            name="topic"
                            type="text"
                            value={config.topic}
                            onChange={(e) => setConfig({ ...config, topic: e.target.value })}
                            placeholder="carbon/usage/data"
                            required
                        />
                        <Button
                            type="submit"
                            disabled={configLoading}
                            loading={configLoading}
                            className="w-full"
                        >
                            Save Configuration
                        </Button>
                        {message.text && (
                            <div
                                className="text-sm"
                                style={{
                                    color: message.type === 'success' ? 'var(--color-success)' : 'var(--color-error)'
                                }}
                            >
                                {message.text}
                            </div>
                        )}
                    </form>
                </GlassCard>

                {/* Chart */}
                <GlassCard
                    title="Usage history"
                    subtitle="Historical CO₂ usage trends over time. Data is updated in real-time from MQTT streams."
                >
                    <div className="h-64 sm:h-80 w-full min-w-0">
                        <Line options={chartOptions} data={chartData} />
                    </div>
                </GlassCard>
            </div>

            {/* Usage History Table */}
            <GlassCard
                title="Usage activity"
                subtitle="Complete carbon usage history recorded from MQTT streams. All usage is automatically deducted from your credit balance."
            >
                <Table
                    columns={usageTableColumns}
                    data={history}
                    emptyState="No usage data found."
                />
            </GlassCard>
        </div>
    );
};

export default Usage;
