import { useState, useEffect, useMemo } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Client } from '@stomp/stompjs';

import api from '../../api';
import { getToken, getUsername, clearAuth } from '../../auth';
import { ATS_LIST } from '../../ats';
import './Dashboard.css';

const subsKey = (username) => `subscriptions:${username}`;

const jobKey = (job) => job.url || `${job.company}|${job.title}`;

// `posted` comes straight from each ATS and the format varies:
// ISO strings (greenhouse/ashby), epoch millis/seconds (lever), free text, or empty.
const parsePostedMs = (posted) => {
  if (!posted) return NaN;
  if (/^\d{13,}$/.test(posted)) return Number(posted);
  if (/^\d{10}$/.test(posted)) return Number(posted) * 1000;
  return Date.parse(posted);
};

const formatPosted = (posted) => {
  if (!posted) return null;
  const ms = parsePostedMs(posted);
  if (Number.isNaN(ms)) return posted; // unparseable — show the raw value
  return new Date(ms).toLocaleDateString(undefined, {
    year: 'numeric', month: 'short', day: 'numeric',
  });
};

const sortKeys = {
  found: (job) => (job.firstSeen ? Date.parse(job.firstSeen) || 0 : 0),
  posted: (job) => {
    const ms = parsePostedMs(job.posted);
    return Number.isNaN(ms) ? 0 : ms;
  },
};

const formatFound = (iso) => {
  if (!iso) return null;
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return null;
  return d.toLocaleString(undefined, {
    year: 'numeric', month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit', second: '2-digit',
  });
};

const loadSavedSubs = (username) => {
  try {
    return JSON.parse(localStorage.getItem(subsKey(username))) ?? {};
  } catch {
    return {};
  }
};

const Dashboard = () => {
  const navigate = useNavigate();
  const username = getUsername();

  const [jobs, setJobs] = useState([]);
  const [connected, setConnected] = useState(false);
  const [subs, setSubs] = useState(() => loadSavedSubs(username));
  const [filterAts, setFilterAts] = useState(null); // null = show all
  const [sortBy, setSortBy] = useState('found');    // 'found' | 'posted', newest first
  const [saving, setSaving] = useState(false);
  const [saveMsg, setSaveMsg] = useState(null);
  const [error, setError] = useState(null);

  // Initial snapshot over REST
  useEffect(() => {
    api.get('/api/dashboard')
      .then((res) => setJobs(res.data))
      .catch(() => setError('Failed to load your dashboard'));
  }, []);

  // Live updates over STOMP/WebSocket
  useEffect(() => {
    const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
    const client = new Client({
      brokerURL: `${protocol}://${window.location.host}/ws`,
      connectHeaders: { Authorization: `Bearer ${getToken()}` },
      reconnectDelay: 5000,
      onConnect: () => {
        setConnected(true);
        client.subscribe('/user/queue/jobs', (msg) => {
          // Live events carry no firstSeen — stamp the moment it reached the dashboard
          const job = { ...JSON.parse(msg.body), live: true, firstSeen: new Date().toISOString() };
          setJobs((prev) => [job, ...prev.filter((j) => jobKey(j) !== jobKey(job))]);
        });
      },
      onWebSocketClose: () => setConnected(false),
    });

    client.activate();
    return () => { client.deactivate(); };
  }, []);

  // Static checklist: every ATS the scheduler can poll, unticked unless saved
  const atsList = useMemo(
    () => ATS_LIST.map((name) => [name, subs[name] === true]),
    [subs]
  );

  const subscribedAts = useMemo(
    () => atsList.filter(([, checked]) => checked).map(([name]) => name),
    [atsList]
  );

  const visibleJobs = useMemo(() => {
    const filtered = filterAts ? jobs.filter((j) => j.ats === filterAts) : jobs;
    const key = sortKeys[sortBy];
    return [...filtered].sort((a, b) => key(b) - key(a));
  }, [jobs, filterAts, sortBy]);

  const toggleAts = (name, checked) => {
    setSaveMsg(null);
    setSubs((prev) => ({ ...prev, [name]: checked }));
  };

  const handleSave = async () => {
    setSaving(true);
    setSaveMsg(null);
    setError(null);

    try {
      await api.post('/api/subscribe', { companies: subscribedAts });
      localStorage.setItem(subsKey(username), JSON.stringify(subs));
      setSaveMsg('Subscriptions saved');
      if (filterAts && !subscribedAts.includes(filterAts)) setFilterAts(null);
    } catch {
      setError('Failed to save subscriptions');
    } finally {
      setSaving(false);
    }
  };

  const handleLogout = () => {
    clearAuth();
    navigate('/login');
  };

  return (
    <div className='dashboard'>
      <aside className='dashboard-sidebar'>
        <div className='dashboard-sidebar-top'>
          <Link to='/' className='sidebar-logo'>
            <span className='sidebar-logo-icon'>JR</span>
            <span className='sidebar-logo-text'>Job Runner</span>
          </Link>

          <div className='company-panel'>
            <p className='company-panel-label'>Job boards</p>

            <ul className='company-list'>
              {atsList.map(([name, checked]) => (
                <li key={name} className='company-item'>
                  <label>
                    <input
                      type='checkbox'
                      checked={checked}
                      onChange={(e) => toggleAts(name, e.target.checked)}
                    />
                    <span>{name}</span>
                  </label>
                </li>
              ))}
            </ul>

            {error && <p className='company-error'>{error}</p>}
            {saveMsg && <p className='company-saved'>{saveMsg}</p>}

            <button className='company-save' onClick={handleSave} disabled={saving}>
              {saving ? 'Saving...' : 'Save subscriptions'}
            </button>
          </div>
        </div>

        <div className='dashboard-sidebar-bottom'>
          <div className='sidebar-user'>
            <span className='sidebar-avatar'>{(username || '?').charAt(0).toUpperCase()}</span>
            <span className='sidebar-username'>{username}</span>
          </div>
          <button className='sidebar-logout' onClick={handleLogout}>Log out</button>
        </div>
      </aside>

      <div className='dashboard-feed'>
        <header className='feed-header'>
          <h1 className='feed-title'>Job Feed</h1>
          <span className={connected ? 'feed-status feed-status--live' : 'feed-status'}>
            <span className='feed-status-dot' />
            {connected ? 'Live' : 'Connecting...'}
          </span>
        </header>

        <div className='feed-toolbar'>
          {subscribedAts.length > 0 && (
            <div className='feed-filters'>
              <button
                className={filterAts === null ? 'filter-chip filter-chip--active' : 'filter-chip'}
                onClick={() => setFilterAts(null)}
              >
                All
              </button>
              {subscribedAts.map((name) => (
                <button
                  key={name}
                  className={filterAts === name ? 'filter-chip filter-chip--active' : 'filter-chip'}
                  onClick={() => setFilterAts(filterAts === name ? null : name)}
                >
                  {name}
                </button>
              ))}
            </div>
          )}
          <div className='feed-sort'>
            <span className='feed-sort-label'>Sort by</span>
            <button
              className={sortBy === 'found' ? 'filter-chip filter-chip--active' : 'filter-chip'}
              onClick={() => setSortBy('found')}
            >
              Newest found
            </button>
            <button
              className={sortBy === 'posted' ? 'filter-chip filter-chip--active' : 'filter-chip'}
              onClick={() => setSortBy('posted')}
            >
              Newest posted
            </button>
          </div>
        </div>

        {visibleJobs.length === 0 ? (
          <div className='feed-empty'>
            <p>No jobs yet.</p>
            <p>Tick some job boards and save — new CS openings will appear here the moment a worker finds them.</p>
          </div>
        ) : (
          <ul className='feed-list'>
            {visibleJobs.map((job) => (
              <li key={jobKey(job)} className={job.live ? 'job-card job-card--live' : 'job-card'}>
                <div className='job-card-main'>
                  <a className='job-title' href={job.url} target='_blank' rel='noreferrer'>
                    {job.title || 'Untitled role'}
                  </a>
                  <p className='job-meta'>
                    <span className='job-company'>{job.company}</span>
                    {job.location && <span> · {job.location}</span>}
                    {job.department && <span> · {job.department}</span>}
                  </p>
                </div>
                <div className='job-card-side'>
                  <div className='job-badges'>
                    {job.live && <span className='job-new-badge'>NEW</span>}
                    {job.ats && <span className='job-ats-badge'>{job.ats}</span>}
                  </div>
                  <div className='job-dates'>
                    {formatPosted(job.posted) && (
                      <span className='job-time'>Posted: {formatPosted(job.posted)}</span>
                    )}
                    {formatFound(job.firstSeen) && (
                      <span className='job-time'>Found: {formatFound(job.firstSeen)}</span>
                    )}
                  </div>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
};

export default Dashboard;
