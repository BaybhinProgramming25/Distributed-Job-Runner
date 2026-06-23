import axios from 'axios'

export const api = axios.create({ withCredentials: true })

export const createJob = (job) => api.post('/job', job).then((res) => res.data)
