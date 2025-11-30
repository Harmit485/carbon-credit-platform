import { cn } from '../../utils/cn';
import Button from './Button';

const Modal = ({ open, onClose, title, subtitle, actions, children, className }) => {
  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center px-4 py-10">
      <div className="absolute inset-0 bg-black/40 backdrop-blur-md" onClick={onClose} />
      <div
        className={cn(
          'relative z-10 w-full max-w-lg glass-card p-6 sm:p-8',
          'animate-[fadeIn_0.3s_ease] border border-white/20 shadow-2xl',
          className
        )}
      >
        <div className="mb-6 flex items-start justify-between gap-4">
          <div>
            {title && <h3 className="text-2xl font-semibold">{title}</h3>}
            {subtitle && <p className="mt-2 text-sm text-white/70">{subtitle}</p>}
          </div>
          <Button variant="ghost" size="sm" onClick={onClose}>
            ✕
          </Button>
        </div>

        <div className="space-y-4">{children}</div>

        {actions && <div className="mt-8 flex flex-wrap justify-end gap-3">{actions}</div>}
      </div>
    </div>
  );
};

export default Modal;


