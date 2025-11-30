# Carbon Credit Platform – Frontend

The frontend now ships with a cohesive glassmorphism (“frozen / liquid glass”) design system. All screens share the same gradient background, frosted surfaces, spacing, and typography to create a calm, modern trading experience.

## Theme tokens & global styling

Design tokens live in `src/index.css` under `:root`. Tweak these variables to adjust the entire look:

- `--color-bg`, `--color-surface`, `--color-primary`, etc. control palette
- `--radius-*`, `--blur-*`, and `--shadow-*` control curvature, blur, and depth
- `--space-*` drives the spacing scale

Legacy Tailwind utility overrides (toward the bottom of `index.css`) ensure older layouts inherit the same frosted look. Update those if you need to tweak the fallback styling for `bg-white`, `text-gray-600`, etc.

## Shared UI components

Reusable building blocks live under `src/components/ui/`:

| Component | Purpose |
| --- | --- |
| `Button` | Primary/secondary/ghost buttons with pill radius + glow states |
| `GlassCard` | Base panel with blur, gradient sheen, optional header/action slots |
| `InputField`, `SelectField` | Labeled glass inputs with helper/error text |
| `Modal` | Frosted dialog with overlay and action slots |
| `Table` | Glass table shell w/ flexible columns renderers |
| `Tag` | Status chips for states (success, warning, accent, danger) |

Layout pieces (`components/layout/`) combine `Navbar`, `Sidebar`, and `Footer` inside `AppLayout` so every page automatically inherits the gradient background and frosted chrome.

## Customizing the look

1. **Colors / blur / typography** – edit the variables at the top of `src/index.css`. The current values are tuned for a green-energy palette (`--color-primary`, `--color-accent`, `--color-bg`). Swapping those hex codes instantly re-themes the glass surfaces.
2. **Component defaults** – adjust styles inside each UI component (e.g., `Button.jsx` for gradient direction, `GlassCard.jsx` for canopy variants, `Tag.jsx` for status chips).
3. **Chart styling** – update `UP_COLOR` / `DOWN_COLOR` and the gradient fill in `src/components/marketplace/PriceChart.jsx` to change the trading-style sparkline colors.
4. **Global overrides** – legacy Tailwind classes are remapped near the bottom of `src/index.css` so older layouts inherit the frosted surface automatically. Tweak those overrides if you need a different fallback treatment.

## Development

```bash
cd client
npm install
npm run dev
```

The UI uses Vite + React 19 with Tailwind 3 for utility classes, and Chart.js for price history.
