import { useState } from 'react';
import { Link, NavLink, Outlet } from 'react-router-dom';
import Icon from './Icons.jsx';

function Brand() {
  return <Link className="brand" to="/" aria-label="Serein home"><span className="brand-symbol">s</span><span>serein<span className="brand-dot">.</span></span></Link>;
}

export default function SiteLayout() {
  const [menuOpen, setMenuOpen] = useState(false);
  const close = () => setMenuOpen(false);

  return (
    <>
      <header className="site-header">
        <div className="nav-inner page-wrap">
          <Brand />
          <button className="mobile-menu-toggle" onClick={() => setMenuOpen(!menuOpen)} aria-label={menuOpen ? 'Close navigation' : 'Open navigation'}><Icon name={menuOpen ? 'close' : 'menu'} /></button>
          <nav className={`main-nav ${menuOpen ? 'is-open' : ''}`} aria-label="Main navigation">
            <NavLink to="/hotels" onClick={close}>Find a room</NavLink>
            <a href="/#destinations" onClick={close}>Inspiration</a>
            <NavLink to="/reservations" onClick={close}>My reservations</NavLink>
          </nav>
          <Link className="nav-cta" to="/hotels">Explore stays <Icon name="arrow" size={16} /></Link>
        </div>
      </header>
      <main><Outlet /></main>
      <footer className="site-footer">
        <div className="page-wrap footer-main">
          <div className="footer-brand-col"><Brand /><p>Thoughtful stays, made a little simpler.</p></div>
          <div className="footer-links"><span className="footer-heading">EXPLORE</span><Link to="/hotels">Find a room</Link><Link to="/reservations">My reservations</Link><a href="/#destinations">Travel inspiration</a></div>
          <div className="footer-note"><span className="footer-heading">A NOTE</span><p>Room availability and booking details are provided by the reservation service.</p></div>
        </div>
        <div className="page-wrap footer-bottom"><span>© {new Date().getFullYear()} Serein Stays</span><span>Made for slower mornings.</span></div>
      </footer>
    </>
  );
}
