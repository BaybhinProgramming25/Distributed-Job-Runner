# CS Job Runner

A distributed job-feed aggregator that polls **11 applicant-tracking systems** (Greenhouse, Ashby, Lever, SmartRecruiters, Recruitee, Workable, Teamtailor, Workday, Remotive, RemoteOK, Arbeitnow) for fresh software-engineering openings and publishes them on a dashboard.

## Architecture

```
   11 ATS platforms (Greenhouse, Workday, Lever, ...)
                 │  polls every POLL_MINUTES (default 30)
                 ▼
          ┌─────────────┐   job.queue    ┌─────────────┐
          │  scheduler  │───────────────►│  RabbitMQ   │
          └─────────────┘                └──────┬──────┘
                                                │ competing consumers
                                                ▼
                                         ┌─────────────┐
                    idempotent upserts   │   worker    │
              ┌──────────────────────────│ (3 replicas)│
              ▼                          └──────┬──────┘
       ┌─────────────┐                          │ fresh jobs only → new-jobs
       │ CockroachDB │                          ▼
       └──────┬──────┘                   ┌─────────────┐
              │ reads                    │   backend   │──► STOMP WebSocket
              └─────────────────────────►│ (REST + WS) │    /user/queue/jobs
                                         └──────┬──────┘
                                                ▲
                 HTTPS (automatic TLS)          │ /api/*, /ws
   Browser ────────────────────────────► ┌─────────────┐
              React dashboard  ◄──────── │    Caddy    │
                                         └─────────────┘
```

## Tech stack

| Layer | Technology |
|---|---|
| Services | Java 21, Spring Boot (`backend`, `scheduler`, `worker`) |
| Messaging | RabbitMQ 3 (competing consumers via Spring AMQP) |
| Database | CockroachDB (Postgres wire-compatible), Spring JDBC |
| Auth | JWT (stateless), bcrypt password hashing |
| Live updates | STOMP over WebSocket (per-user queues via `SimpMessagingTemplate`) |
| Frontend | React 19, Vite |


## API

| Endpoint | Purpose |
|---|---|
| `POST /api/users/signup` | Create an account (returns a JWT — user lands on the dashboard signed in) |
| `POST /api/users/login` | Authenticate, returns a JWT |
| `GET /api/dashboard` | Recent jobs for the authenticated user's subscriptions |
| `POST /api/subscribe` | Update which ATS platforms the user watches |
| `WS /ws` (STOMP) | Live feed — new jobs pushed to `/user/queue/jobs` 


## Visit

You can signup at [CS-Job-Runner](https://csjobrunner.org) and see the various postings!