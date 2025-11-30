const Footer = () => (
  <footer className="px-4 pb-10 pt-6 sm:px-6 lg:px-10 xl:px-12 2xl:px-16">
    <div className="mx-auto max-w-[1800px] 2xl:max-w-[2000px] rounded-3xl border border-white/10 bg-white/[0.03] px-6 sm:px-8 py-8 sm:py-10 text-white/80 backdrop-blur-2xl">
      <div className="flex flex-col gap-8 lg:flex-row lg:justify-between lg:gap-10">
        <div className="max-w-sm">
          <p className="text-2xl font-semibold">Carbon Trading System</p>
          <p className="mt-3 text-sm text-white/70">
            A market-based platform for carbon credit trading and emissions reduction. Trade carbon credits representing 1 tonne CO₂, manage emission caps, and participate in real-time order matching.
          </p>
        </div>

        {['Platform', 'Resources', 'Legal'].map((section) => (
          <div key={section}>
            <p className="text-sm font-semibold uppercase tracking-[0.2em] text-white/60">{section}</p>
            <ul className="mt-4 space-y-2 text-sm">
              {LINKS[section].map((item) => (
                <li key={item}>
                  <a href="/#" className="text-white/70 transition hover:text-white">
                    {item}
                  </a>
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>

      <div className="mt-10 flex flex-col gap-4 border-t border-white/10 pt-6 text-sm text-white/60 sm:flex-row sm:items-center sm:justify-between">
        <p>&copy; {new Date().getFullYear()} Carbon Trading System. All rights reserved.</p>
        <p>Built by Harmit Khimani & Nishank Kansara.</p>
      </div>
    </div>
  </footer>
);

const LINKS = {
  Platform: ['Trading System', 'Order Matching', 'Marketplace', 'Emission Caps'],
  Resources: ['API Documentation', 'User Manual', 'Trading Guide', 'FAQs'],
  Legal: ['Privacy Policy', 'Terms of Service', 'Compliance', 'Data Security']
};

export default Footer;