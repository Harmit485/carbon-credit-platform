import { cn } from '../../utils/cn';

const variantMap = {
  neutral: 'badge',
  success: 'badge badge-success',
  warning: 'badge badge-warning',
  danger: 'badge badge-error',
  accent: 'badge bg-[rgba(250,204,21,0.18)] border-[rgba(250,204,21,0.35)] text-[#052e16]'
};

const Tag = ({ variant = 'neutral', children, className }) => (
  <span className={cn(variantMap[variant], className)}>{children}</span>
);

export default Tag;


