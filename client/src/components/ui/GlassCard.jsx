import { cn } from '../../utils/cn';

const variantMap = {
  default: '',
  accent: 'bg-gradient-to-br from-emerald-400/20 via-emerald-400/5 to-transparent border-emerald-200/30',
  subtle: 'bg-white/5 border-white/10',
  highlight: 'bg-gradient-to-br from-amber-300/15 via-transparent to-transparent border-amber-200/30',
  canopy: 'bg-gradient-to-r from-emerald-500/15 via-emerald-500/5 to-transparent border-emerald-300/25'
};

const GlassCard = ({
  title,
  subtitle,
  action,
  footer,
  variant = 'default',
  className,
  children,
  ...rest
}) => (
  <section
    className={cn(
      'glass-card',
      'overflow-hidden',
      'relative',
      'before:pointer-events-none before:absolute before:inset-0 before:bg-gradient-to-br before:from-white/10 before:via-transparent before:to-transparent before:opacity-0 before:transition-opacity hover:before:opacity-100',
      variantMap[variant],
      className
    )}
    {...rest}
  >
    {(title || subtitle || action) && (
      <div className="mb-6 flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
        <div>
          {title && <h3 className="text-xl font-semibold">{title}</h3>}
          {subtitle && <p className="mt-1 text-sm text-white/70">{subtitle}</p>}
        </div>
        {action && <div className="shrink-0">{action}</div>}
      </div>
    )}
    {children}
    {footer && <div className="mt-6 border-t border-white/10 pt-4 text-sm text-white/70">{footer}</div>}
  </section>
);

export default GlassCard;


