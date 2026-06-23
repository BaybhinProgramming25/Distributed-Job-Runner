import { useEffect, useState } from 'react'
import { generateRandomMinuteCron } from './jobTemplates'
import { createJob } from './api'
import './App.css'

const App = () => {
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
            <h1>Distributed Job Scheduler</h1>
            <p className="subtitle">Send a quick job and watch it run across the cluster.</p>
          </div>
        </div>
      </header>

      {error && <div className="banner banner-error">{error}</div>}

      <main>
        <section className="panel quick-send-panel">
          <h2>Send a Quick Job</h2>
          <p>Click the button to schedule a quick job with a randomly generated interval (every 1-30 minutes).</p>
          <button type="button" className="btn btn-primary" onClick={handleSendQuickJob} disabled={submitting}>
            {submitting ? 'Sending…' : 'Send Quick Job'}
          </button>
        </section>
      </main>

      {toast && <div className="toast">{toast}</div>}
    </div>
  )
}

export default App
