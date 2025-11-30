import { useState, useRef, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { AUTH_LINKS } from '../../constants/navigation';
import { cn } from '../../utils/cn';
import Tag from '../ui/Tag';

const Sidebar = ({ user, navbarVisible = true }) => {
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef(null);
  const location = useLocation();
  const roles = Array.isArray(user?.roles) ? user.roles.map((role) => role.toUpperCase()) : [];

  const canViewLink = (link) => {
    if (!link.roles) return true;
    return link.roles.some((role) => roles.includes(role.toUpperCase()) || roles.includes(`ROLE_${role.toUpperCase()}`));
  };

  const filteredLinks = AUTH_LINKS.filter(canViewLink);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    };

    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
      document.addEventListener('touchstart', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      document.removeEventListener('touchstart', handleClickOutside);
    };
  }, [isOpen]);

  const isActive = (path) => location.pathname.startsWith(path);

  // Only show menu button when navbar is hidden
  if (navbarVisible) {
    return null;
  }

  return (
    <div 
      className="fixed left-4 top-4 z-50 sm:left-6 sm:top-6 transition-all duration-300 ease-out" 
      ref={dropdownRef}
    >
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="glass-panel flex items-center gap-2 rounded-2xl border border-white/15 px-3 py-2 sm:px-4 sm:py-2.5 transition-all hover:bg-white/10 shadow-lg"
        aria-label="Navigation menu"
      >
        <div className="flex flex-col gap-1">
          <div className="h-1 w-1 rounded-full bg-current"></div>
          <div className="h-1 w-1 rounded-full bg-current"></div>
          <div className="h-1 w-1 rounded-full bg-current"></div>
        </div>
        <span className="text-sm font-medium">Menu</span>
      </button>

      {isOpen && (
        <div className="glass-panel absolute left-0 top-full z-50 mt-2 w-64 rounded-2xl border border-white/15 p-4 shadow-2xl backdrop-blur-xl">
          <div className="mb-4 border-b border-white/10 pb-4">
            <p className="text-xs text-white/60">Signed in as</p>
            <p className="mt-1 text-base font-semibold">{user?.email || 'Guest'}</p>
            {roles.length > 0 && (
              <div className="mt-2 flex flex-wrap gap-2">
                {roles.slice(0, 2).map((role) => (
                  <Tag key={role} variant={role.includes('ADMIN') ? 'danger' : 'accent'} className="text-[0.7rem]">
                    {role.replace('ROLE_', '')}
                  </Tag>
                ))}
              </div>
            )}
          </div>

          <nav className="space-y-1">
            {filteredLinks.map((link) => {
              const active = isActive(link.to);
              return (
                <Link
                  key={link.to}
                  to={link.to}
                  onClick={() => setIsOpen(false)}
                  className={cn(
                    'flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-all',
                    'text-white/80 hover:text-white hover:bg-white/10',
                    active && 'bg-white/10 text-white'
                  )}
                >
                  <span className="text-lg">{link.icon}</span>
                  <span>{link.label}</span>
                </Link>
              );
            })}
          </nav>
        </div>
      )}
    </div>
  );
};

export default Sidebar;
