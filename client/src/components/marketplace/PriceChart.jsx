import { useState, useEffect } from 'react';
import { Line } from 'react-chartjs-2';
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
import 'chartjs-adapter-date-fns';

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

const UP_COLOR = '#34d399'; // Emerald 400
const DOWN_COLOR = '#f87171'; // Red 400

const PriceChart = () => {
    const [chartData, setChartData] = useState({
        datasets: []
    });
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchPriceHistory = async () => {
            try {
                const token = localStorage.getItem('token');
                const response = await axios.get(`${API_BASE_URL}/marketplace/price-history`, {
                    headers: { Authorization: `Bearer ${token}` }
                });

                const trades = response.data || [];
                // Sort trades by date just in case
                trades.sort((a, b) => new Date(a.executedAt) - new Date(b.executedAt));

                const dataPoints = trades.map(trade => ({
                    x: new Date(trade.executedAt),
                    y: trade.pricePerUnit
                }));

                const canvas = document.createElement('canvas');
                const ctx = canvas.getContext('2d');
                const gradient = ctx.createLinearGradient(0, 0, 0, 400);
                gradient.addColorStop(0, 'rgba(52, 211, 153, 0.2)'); // Start with low opacity emerald
                gradient.addColorStop(1, 'rgba(52, 211, 153, 0)');   // Fade to transparent

                setChartData({
                    datasets: [
                        {
                            label: 'Carbon Credit Price (INR per 1 tonne CO₂)',
                            data: dataPoints,
                            borderColor: UP_COLOR,
                            backgroundColor: gradient,
                            fill: true,
                            borderWidth: 2,
                            pointRadius: 0, // Hide points for a cleaner look
                            pointHoverRadius: 6,
                            pointHoverBackgroundColor: '#fff',
                            pointHoverBorderColor: UP_COLOR,
                            pointHoverBorderWidth: 2,
                            tension: 0.1 // Slight curve
                        }
                    ]
                });
            } catch (error) {
                console.error('Error fetching price history:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchPriceHistory();
        const interval = setInterval(fetchPriceHistory, 10000);
        return () => clearInterval(interval);
    }, []);

    const options = {
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
                borderColor: 'rgba(34, 197, 94, 0.4)',
                borderWidth: 1,
                padding: 12,
                displayColors: false,
                callbacks: {
                    title: (items) => {
                        const date = new Date(items[0].parsed.x);
                        return date.toLocaleString();
                    },
                    label: (context) => `Price: ₹${Number(context.parsed.y).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} per credit (1 tonne CO₂)`
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
                    display: false, // Remove vertical grid lines
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
                    text: 'Price (INR)',
                    color: 'rgba(5, 46, 22, 0.7)'
                },
                grid: {
                    color: 'rgba(5, 46, 22, 0.1)',
                    drawBorder: false
                },
                ticks: {
                    color: 'rgba(5, 46, 22, 0.7)',
                    callback: (value) => `₹${Number(value).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
                }
            }
        }
    };

    if (loading) {
        return <div className="flex h-64 items-center justify-center text-white/70 animate-pulse">Loading price chart...</div>;
    }

    if (!chartData.datasets[0]?.data.length) {
        return (
            <div className="flex h-64 items-center justify-center rounded-3xl border border-white/10 bg-white/5 text-white/70">
                No trading history available yet. Start trading to see price movements.
            </div>
        );
    }

            return (
                <div className="h-64 sm:h-80 w-full min-w-0">
                    <Line options={options} data={chartData} />
                </div>
            );
};

export default PriceChart;
