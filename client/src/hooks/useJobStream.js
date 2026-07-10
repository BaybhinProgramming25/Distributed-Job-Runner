import { useEffect, useRef, useState } from 'react'

export const useJobStream = () => {
  const [jobs, setJobs] = useState([])
  const [connected, setConnected] = useState(false)
  const sourceRef = useRef(null)

  useEffect(() => {
    const source = new EventSource('/job/stream')
    sourceRef.current = source

    source.addEventListener('jobs', (event) => {
      setJobs(JSON.parse(event.data))
      setConnected(true)
    })

    source.onerror = () => {
      setConnected(false)
    }

    return () => {
      source.close()
    }
  }, [])

  return { jobs, connected }
}
