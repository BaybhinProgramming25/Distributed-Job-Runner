import { useJobStream } from '../hooks/useJobStream'
import { StatusBadge } from './StatusBadge'
import { JobDetail } from './JobDetail'

const shortId = (id) => id.slice(0, 8)

const formatDate = (value) => (value ? new Date(value).toLocaleString() : '—')

export const Dashboard = () => {
  const { jobs, connected } = useJobStream()

  return (
    <section className="panel dashboard-panel">
      <div className="dashboard-header">
        <h2>Jobs</h2>
        <span className={`connection-dot ${connected ? 'connection-live' : 'connection-down'}`}>
          {connected ? 'Live' : 'Connecting…'}
        </span>
      </div>

      {jobs.length === 0 ? (
        <p className="empty-state">No jobs yet. Send a quick job to see it appear here.</p>
      ) : (
        <div className="table-wrap">
          <table className="job-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Schedule</th>
                <th>Status</th>
                <th>Retries</th>
                <th>Next Run</th>
                <th>Active</th>
              </tr>
            </thead>
            <tbody>
              {jobs.map((job) => (
                <tr key={job.id}>
                  <td className="mono" title={job.id}>{shortId(job.id)}</td>
                  <td className="mono">{job.schedule}</td>
                  <td><StatusBadge status={job.status} /></td>
                  <td>{job.retriesCount} / {job.maxRetries}</td>
                  <td>{formatDate(job.nextRun)}</td>
                  <td>{job.jobActive}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <JobDetail jobs={jobs} />
    </section>
  )
}
