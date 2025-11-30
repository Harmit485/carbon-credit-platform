import { cn } from '../../utils/cn';

const SelectField = ({
  label,
  helper,
  error,
  className,
  selectClassName,
  children,
  required,
  ...rest
}) => (
  <label className={cn('flex flex-col gap-2 text-sm', className)}>
    {label && (
      <span className="font-medium text-white/90">
        {label}
        {required && <span className="ml-1 text-red-400">*</span>}
      </span>
    )}
    <select
      className={cn(
        'glass-input appearance-none',
        'pr-10',
        error && 'border-red-400/70 focus:border-red-300 focus:ring-red-400/40',
        selectClassName
      )}
      required={required}
      {...rest}
    >
      {children}
    </select>
    {helper && !error && <small className="glass-helper">{helper}</small>}
    {error && <small className="text-red-300">{error}</small>}
  </label>
);

export default SelectField;


