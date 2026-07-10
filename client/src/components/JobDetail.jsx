import { useEffect, useState } from 'react'
import { fetchJobHistory } from '../api'
import { StatusBadge } from './StatusBadge'

const shortId = (id) => id.slice(0, 8)

const formatDate = (value) => (value ? new Date(value).toLocaleString() : '—')

export const JobDetail = ({ jobs }) => {
  const [selectedId, setSelectedId] = useState('')
  const [result, setResult] = useState({ forSignature: null, entries: [], error: '' })

  const selectedJob = jobs.find((job) => job.id === selectedId)
  const changeSignature = selectedJob ? `${selectedId}-${selectedJob.status}-${selectedJob.retriesCount}` : ''

  useEffect(() => {
    if (!selectedId) {
      return
    }
    let cancelled = false
    fetchJobHistory(selectedId)
      .then((data) => {
        if (!cancelled) setResult({ forSignature: changeSignature, entries: data, error: '' })
      })
      .catch(() => {
        if (!cancelled) setResult({ forSignature: changeSignature, entries: [], error: 'Failed to load job history.' })
      })
    return () => {
      cancelled = true
    }
  }, [selectedId, changeSignature])

  const loading = Boolean(selectedId) && result.forSignature !== changeSignature
  const { entries: history, error } = result

  return (
    <div className="job-detail">
      <div className="job-detail-header">
        <label htmlFor="job-select">Inspect a job</label>
        <select id="job-select" value={selectedId} onChange={(e) => setSelectedId(e.target.value)}>
          <option value="">Select a job ID…</option>
          {jobs.map((job) => (
            <option key={job.id} value={job.id}>
              {shortId(job.id)} — {job.schedule}
            </option>
          ))}
        </select>
      </div>

      {selectedJob && (
        <div className="job-detail-body">
          <div className="job-detail-summary">
            <span><strong>Status:</strong> <StatusBadge status={selectedJob.status} /></span>
            <span><strong>Retries:</strong> {selectedJob.retriesCount} / {selectedJob.maxRetries}</span>
            <span><strong>Next Run:</strong> {formatDate(selectedJob.nextRun)}</span>
          </div>

          {!loading && error && <p className="empty-state">{error}</p>}
          {loading && <p className="empty-state">Loading history…</p>}

          {!loading && !error && history.length === 0 && (
            <p className="empty-state">No execution history yet for this job.</p>
          )}

          {!loading && history.length > 0 && (
            <ul className="history-timeline">
              {history.map((entry) => (
                <li key={entry.id} className={`history-entry history-${entry.jobStatus}`}>
                  <StatusBadge status={entry.jobStatus} />
                  <span className="history-time">Started {formatDate(entry.jobStarted)}</span>
                  <span className="history-time">Finished {formatDate(entry.jobFinished)}</span>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  )
}
