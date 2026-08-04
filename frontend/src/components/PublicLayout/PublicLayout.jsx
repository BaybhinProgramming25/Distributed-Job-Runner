import { Link } from 'react-router-dom';
import './PublicLayout.css';

const PublicLayout = ({ children }) => {
  return (
    <div className="public-layout">
      <header className="public-nav">
        <Link to="/" className="public-nav-logo">
          <span className="public-nav-logo-icon">SWE</span>
          <span className="public-nav-logo-text">SWE Job Feed</span>
        </Link>
        <nav className="public-nav-links">
          <Link to="/login">Log in</Link>
          <Link to="/signup" className="public-nav-signup">Sign up</Link>
        </nav>
      </header>
      <main className="public-main">
        {children}
      </main>
    </div>
  );
};

export default PublicLayout;
