import { cn } from '../../utils/cn';

const variantClasses = {
  primary: 'btn-primary',
  secondary: 'btn-secondary',
  ghost: 'btn-ghost',
  danger: 'bg-red-500/80 border border-red-400/70 text-white hover:bg-red-500/95'
};

const sizeClasses = {
  sm: 'text-sm px-4 py-2',
  md: 'text-base px-5 py-3',
  lg: 'text-lg px-6 py-3.5'
};

const Button = ({
  as,
  href,
  type = 'button',
  variant = 'primary',
  size = 'md',
  icon,
  iconPosition = 'left',
  loading = false,
  disabled = false,
  className,
  children,
  ...rest
}) => {
  const Component = href ? 'a' : as || 'button';
  const isButton = Component === 'button';
  const isDisabled = disabled || loading;

  return (
    <Component
      {...(href ? { href } : {})}
      {...rest}
      {...(isButton ? { type, disabled: isDisabled } : {})}
      aria-disabled={!isButton ? isDisabled : undefined}
      className={cn(
        'btn',
        sizeClasses[size],
        variantClasses[variant],
        isDisabled && 'opacity-50 cursor-not-allowed pointer-events-none',
        className
      )}
    >
      {icon && iconPosition === 'left' && (
        <span className="inline-flex h-5 w-5 items-center justify-center">{icon}</span>
      )}
      <span>{loading ? 'Please wait...' : children}</span>
      {icon && iconPosition === 'right' && (
        <span className="inline-flex h-5 w-5 items-center justify-center">{icon}</span>
      )}
    </Component>
  );
};

export default Button;


