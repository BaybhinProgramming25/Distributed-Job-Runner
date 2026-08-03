import { Link } from 'react-router-dom';
import './Home.css';

const Home = () => {
  return (
    <div className="home-page">
      <section className="home-hero">
        <h1 className="home-hero-title">Job Runner</h1>
        <p className="home-hero-sub">
          A distributed job-posting watcher. Subscribe to the companies you care about
          and see new openings land on your dashboard the moment our workers find them.
        </p>
        <div className="home-hero-ctas">
          <Link to="/signup" className="home-cta home-cta--primary">Get Started</Link>
          <Link to="/login" className="home-cta home-cta--secondary">Log in</Link>
        </div>
      </section>

      <section className="home-features">
        <div className="home-feature-card">
          <h2>Distributed Scraping</h2>
          <p>A scheduler fans polling work out to a fleet of workers over RabbitMQ, covering multiple job boards in parallel.</p>
        </div>
        <div className="home-feature-card">
          <h2>Live Dashboard</h2>
          <p>New postings are pushed to your browser over WebSockets the instant they are discovered — no refreshing.</p>
        </div>
        <div className="home-feature-card">
          <h2>Company Subscriptions</h2>
          <p>Pick exactly which companies you want to watch. Your feed only ever shows openings from your own list.</p>
        </div>
      </section>
    </div>
  );
};

export default Home;
