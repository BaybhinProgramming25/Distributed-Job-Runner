import axios from 'axios'

export const api = axios.create({ withCredentials: true })

export const createJob = (job) => api.post('/job', job).then((res) => res.data)

export const fetchJobHistory = (id) => api.get(`/job/${id}/history`).then((res) => res.data)
