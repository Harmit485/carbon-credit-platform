import { useMemo } from 'react';
import { Line } from 'react-chartjs-2';
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend,
    Filler
} from 'chart.js';

ChartJS.register(
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend,
    Filler
);

const MarketDepthChart = ({ buyOrders = [], sellOrders = [] }) => {
    const chartData = useMemo(() => {
        // Process Buy Orders (Bids)
        // Group by price and sum quantity
        const bidsMap = new Map();
        buyOrders.forEach(order => {
            const price = order.pricePerUnit;
            const qty = order.quantity;
            bidsMap.set(price, (bidsMap.get(price) || 0) + qty);
        });

        // Sort bids by price descending (highest bid first)
        const sortedBids = Array.from(bidsMap.entries()).sort((a, b) => b[0] - a[0]);

        // Calculate cumulative quantity for bids
        // For depth chart, we usually want to show accumulation from the mid-market outwards.
        // But standard depth charts show accumulation from highest bid down to lowest, and lowest ask up to highest.
        // Let's stick to standard:
        // Bids: Accumulate from highest price (closest to market) down to lowest.
        // Asks: Accumulate from lowest price (closest to market) up to highest.

        const bidPoints = [];
        let bidAcc = 0;
        for (const [price, qty] of sortedBids) {
            bidAcc += qty;
            bidPoints.push({ x: price, y: bidAcc });
        }
        // Reverse bid points so they plot correctly on x-axis (low to high price)
        // Actually, for a continuous line, we just need x,y points. Chart.js will handle the axis.
        // But we want the line to go "up" as we move away from the center price.
        // Wait, standard depth chart:
        // X-axis: Price
        // Y-axis: Cumulative Volume
        // Bids are on the left (lower prices usually? No, Bids are lower than Asks).
        // So Bids are on the left side of the chart (lower prices), Asks on the right (higher prices).
        // Bids curve: Starts high volume at low price? No.
        // At highest bid (closest to market), volume is just that bid.
        // As we go lower in price, we include all bids above that price.
        // So volume increases as price decreases.
        // Asks curve: Starts low volume at lowest ask (closest to market).
        // As we go higher in price, volume increases.

        // Let's re-verify standard depth chart visualization.
        // X-axis: Price.
        // Y-axis: Cumulative Volume.
        // Bids (Green): On the left. Price 0 to Best Bid.
        // Asks (Red): On the right. Best Ask to Max Price.
        // Shape:
        // Bids: Volume is cumulative sum of all orders with price >= P.
        // Asks: Volume is cumulative sum of all orders with price <= P.

        // Let's implement this.

        const processedBids = [];
        let currentBidVol = 0;
        // Sort bids descending to accumulate from best bid downwards
        sortedBids.forEach(([price, qty]) => {
            currentBidVol += qty;
            processedBids.push({ x: price, y: currentBidVol });
        });
        // For the chart, we need points sorted by X (Price) ascending
        processedBids.sort((a, b) => a.x - b.x);


        // Process Sell Orders (Asks)
        const asksMap = new Map();
        sellOrders.forEach(order => {
            const price = order.pricePerUnit;
            const qty = order.quantity;
            asksMap.set(price, (asksMap.get(price) || 0) + qty);
        });

        // Sort asks by price ascending (lowest ask first)
        const sortedAsks = Array.from(asksMap.entries()).sort((a, b) => a[0] - b[0]);

        const processedAsks = [];
        let currentAskVol = 0;
        sortedAsks.forEach(([price, qty]) => {
            currentAskVol += qty;
            processedAsks.push({ x: price, y: currentAskVol });
        });
        // Already sorted by price ascending

        return {
            datasets: [
                {
                    label: 'Bids',
                    data: processedBids,
                    borderColor: '#34d399', // Green
                    backgroundColor: 'rgba(52, 211, 153, 0.2)',
                    fill: true,
                    stepped: 'before', // Step chart look
                    tension: 0,
                    pointRadius: 0,
                    pointHoverRadius: 4
                },
                {
                    label: 'Asks',
                    data: processedAsks,
                    borderColor: '#f87171', // Red
                    backgroundColor: 'rgba(248, 113, 113, 0.2)',
                    fill: true,
                    stepped: 'after',
                    tension: 0,
                    pointRadius: 0,
                    pointHoverRadius: 4
                }
            ]
        };
    }, [buyOrders, sellOrders]);

    const options = useMemo(() => {
        // Calculate average price across all orders
        const allOrders = [...buyOrders, ...sellOrders];
        const avgPrice = allOrders.length > 0
            ? allOrders.reduce((sum, order) => sum + (order.pricePerUnit * order.quantity), 0) /
            allOrders.reduce((sum, order) => sum + order.quantity, 0)
            : 0;

        return {
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
                    callbacks: {
                        title: (items) => `Price: ₹${Number(items[0].parsed.x).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} per credit (1 tonne CO₂)`,
                        label: (item) => `${item.dataset.label}: ${item.parsed.y.toLocaleString()} credits`
                    }
                }
            },
            scales: {
                x: {
                    type: 'linear',
                    title: {
                        display: true,
                        text: `Price (INR per credit) · Avg: ₹${Number(avgPrice).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`,
                        color: 'rgba(5, 46, 22, 0.7)'
                    },
                    grid: {
                        color: 'rgba(5, 46, 22, 0.1)'
                    },
                    ticks: {
                        color: 'rgba(5, 46, 22, 0.7)',
                        callback: function (value) {
                            const diff = value - avgPrice;
                            const sign = diff >= 0 ? '+' : '';
                            return `₹${Number(value).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} (${sign}${Number(diff).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })})`;
                        }
                    }
                },
                y: {
                    title: {
                        display: true,
                        text: 'Cumulative Volume (credits)',
                        color: 'rgba(5, 46, 22, 0.7)'
                    },
                    grid: {
                        color: 'rgba(5, 46, 22, 0.1)'
                    },
                    ticks: {
                        color: 'rgba(5, 46, 22, 0.7)'
                    },
                    beginAtZero: true
                }
            }
        };
    }, [buyOrders, sellOrders]);

    if (buyOrders.length === 0 && sellOrders.length === 0) {
        return (
            <div className="flex h-full items-center justify-center text-white/70">
                No market depth data available. Place orders to see buy and sell liquidity.
            </div>
        );
    }

    return (
        <div className="h-80 w-full">
            <Line data={chartData} options={options} />
        </div>
    );
};

export default MarketDepthChart;
