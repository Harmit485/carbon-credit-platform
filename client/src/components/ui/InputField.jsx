import { cn } from '../../utils/cn';

const InputField = ({
  label,
  helper,
  error,
  className,
  inputClassName,
  required,
  children,
  ...rest
}) => (
  <label className={cn('flex flex-col gap-2 text-sm', className)}>
    {label && (
      <span className="font-medium text-white/90">
        {label}
        {required && <span className="ml-1 text-red-400">*</span>}
      </span>
    )}
    <input
      className={cn(
        'glass-input',
        error && 'border-red-400/70 focus:border-red-300 focus:ring-red-400/40',
        inputClassName
      )}
      required={required}
      {...rest}
    />
    {helper && !error && <small className="glass-helper">{helper}</small>}
    {error && <small className="text-red-300">{error}</small>}
    {children}
  </label>
);

export default InputField;


