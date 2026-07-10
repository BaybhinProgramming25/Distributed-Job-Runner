const STATUS_LABELS = {
  pending: 'Pending',
  processing: 'Processing',
  success: 'Success',
  failed: 'Failed',
  dead: 'Dead',
}

export const StatusBadge = ({ status }) => (
  <span className={`status-badge status-${status || 'pending'}`}>
    {STATUS_LABELS[status] || status}
  </span>
)
