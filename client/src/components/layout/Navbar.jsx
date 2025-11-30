import { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { AUTH_LINKS, PUBLIC_LINKS } from '../../constants/navigation';
import Button from '../ui/Button';
import { cn } from '../../utils/cn';

const Navbar = ({ isAuthenticated, user, onLogout }) => {
  const [open, setOpen] = useState(false);
  const location = useLocation();
  const roles = Array.isArray(user?.roles) ? user.roles.map((role) => role.toUpperCase()) : [];
  const isAdmin = roles.includes('ROLE_ADMIN') || roles.includes('ADMIN');

  const navLinks = (isAuthenticated ? AUTH_LINKS : PUBLIC_LINKS).filter((link) => {
    // Hide Wallet, Retirement, and Usage for Admins
    if (isAdmin && (link.to === '/wallet' || link.to === '/retirement' || link.to === '/usage')) {
      return false;
    }

    if (!link.roles) return true;
    return link.roles.some((role) => roles.includes(role.toUpperCase()) || roles.includes(`ROLE_${role.toUpperCase()}`));
  });

  const isActive = (path) => location.pathname.startsWith(path);

  return (
    <header className="fixed left-0 right-0 top-0 z-40 px-4 sm:px-6 lg:px-10 xl:px-12 2xl:px-16 pt-4 pb-4">
      <div className="mx-auto flex max-w-[1800px] 2xl:max-w-[2000px] items-center justify-between rounded-3xl border border-white/15 bg-white/95 backdrop-blur-xl px-4 sm:px-5 py-3 sm:py-3.5 shadow-2xl">
        <Link to="/" className="text-base sm:text-lg font-semibold tracking-tight text-[#052e16]">
          Carbon Trading System
        </Link>

        <nav className="hidden items-center gap-1 md:flex">
          {navLinks.map((link) => (
            <Link
              key={link.to}
              to={link.to}
              className={cn(
                'rounded-full px-3 sm:px-4 py-1.5 sm:py-2 text-xs sm:text-sm transition-all',
                'text-[#052e16]/85 hover:text-[#052e16]',
                isActive(link.to) && 'bg-emerald-100/50 text-[#052e16] backdrop-blur-lg'
              )}
            >
              {link.label}
            </Link>
          ))}
        </nav>

        <div className="hidden items-center gap-2 sm:gap-3 md:flex">
          {isAuthenticated ? (
            <>
              <span className="text-xs sm:text-sm text-[#052e16]/85 truncate max-w-[150px] sm:max-w-none">{user?.email}</span>
              <Button variant="secondary" size="sm" onClick={onLogout}>
                Logout
              </Button>
            </>
          ) : (
            <>
              <Button as={Link} to="/login" variant="secondary" size="sm">
                Login
              </Button>
              <Button as={Link} to="/register" size="sm">
                Create account
              </Button>
            </>
          )}
        </div>

        <button
          className="md:hidden text-[#052e16]/80 hover:text-[#052e16] transition-colors"
          onClick={() => setOpen((prev) => !prev)}
          aria-label="Toggle navigation"
        >
          <span className="text-2xl">☰</span>
        </button>
      </div>

      {open && (
        <div className="glass-panel mt-3 rounded-2xl border border-white/15 p-4 text-sm shadow-xl backdrop-blur-xl md:hidden">
          <nav className="mb-4 flex flex-col gap-2">
            {navLinks.map((link) => (
              <Link
                key={link.to}
                to={link.to}
                onClick={() => setOpen(false)}
                className={cn(
                  'rounded-2xl px-4 py-2 transition-all',
                  'text-white/80 hover:text-white',
                  isActive(link.to) && 'bg-white/10 text-white'
                )}
              >
                {link.label}
              </Link>
            ))}
          </nav>
          <div className="flex flex-col gap-2">
            {isAuthenticated ? (
              <Button variant="secondary" onClick={onLogout}>
                Logout
              </Button>
            ) : (
              <>
                <Button as={Link} to="/login" variant="secondary" onClick={() => setOpen(false)}>
                  Login
                </Button>
                <Button as={Link} to="/register" onClick={() => setOpen(false)}>
                  Register
                </Button>
              </>
            )}
          </div>
        </div>
      )}
    </header>
  );
};

export default Navbar;