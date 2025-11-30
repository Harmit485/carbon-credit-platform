import Navbar from './Navbar';
import Footer from './Footer';
import { cn } from '../../utils/cn';

const AppLayout = ({ children, isAuthenticated, user, onLogout }) => {
  return (
    <div className="relative min-h-screen overflow-hidden text-white">
      <div className="pointer-events-none absolute inset-0 -z-10">
        <div className="floating-gradient" />
      </div>

      <Navbar
        isAuthenticated={isAuthenticated}
        user={user}
        onLogout={onLogout}
      />

      <div className="pt-24 pb-12 sm:pb-16 sm:pt-28">
        <div className="px-4 sm:px-6 lg:px-10 xl:px-12 2xl:px-16">
          <div className="mx-auto flex w-full max-w-[1800px] 2xl:max-w-[2000px] gap-6 lg:gap-8">
            <main
              className="flex-1 min-w-0 space-y-6 sm:space-y-8 w-full ml-0"
            >
              {children}
            </main>
          </div>
        </div>
      </div>

      <Footer />
    </div>
  );
};

export default AppLayout;


