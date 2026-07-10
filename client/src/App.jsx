import { useEffect, useState } from 'react'
import { generateRandomMinuteCron } from './jobTemplates'
import { createJob } from './api'
import { Dashboard } from './components/Dashboard'
import './App.css'

const App = () => {
  const [tab, setTab] = useState('dashboard')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [toast, setToast] = useState('')

  useEffect(() => {
    if (!toast) return
    const timeout = setTimeout(() => setToast(''), 3000)
    return () => clearTimeout(timeout)
  }, [toast])

  const handleSendQuickJob = async () => {
    setSubmitting(true)
    try {
      const schedule = generateRandomMinuteCron()
      await createJob({
        Name: 'Quick Job',
        Type: 'QUICK_JOB',
        isRecurring: true,
        Schedule: schedule,
      })
      setToast(`Quick job scheduled (${schedule})`)
      setError('')
    } catch {
      setError('Failed to schedule job. Is the job service running on localhost:7000?')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="app">
      <header className="topbar">
        <div className="brand">
          <span className="brand-icon">⚡</span>
          <div>
            <h1>Distributed Job Runner</h1>
            <p className="subtitle">Send a quick job and watch it run across the cluster.</p>
          </div>
        </div>
      </header>

      {error && <div className="banner banner-error">{error}</div>}

      <nav className="tabs">
        <button type="button" className={`tab ${tab === 'dashboard' ? 'tab-active' : ''}`} onClick={() => setTab('dashboard')}>
          Dashboard
        </button>
        <button type="button" className={`tab ${tab === 'quicksend' ? 'tab-active' : ''}`} onClick={() => setTab('quicksend')}>
          Quick Send
        </button>
      </nav>

      <main>
        {tab === 'dashboard' && <Dashboard />}

        {tab === 'quicksend' && (
          <section className="panel quick-send-panel">
            <h2>Send a Quick Job</h2>
            <p>Click the button to schedule a quick job with a randomly generated interval (every 1-3 minutes).</p>
            <button type="button" className="btn btn-primary" onClick={handleSendQuickJob} disabled={submitting}>
              {submitting ? 'Sending…' : 'Send Quick Job'}
            </button>
          </section>
        )}
      </main>

      {toast && <div className="toast">{toast}</div>}
    </div>
  )
}

export default App
