CREATE DATABASE IF NOT EXISTS dist_jobs_scheduler;

CREATE TABLE IF NOT EXISTS dist_jobs_scheduler.jobs(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    schedule STRING NOT NULL, 
    retriesCount INTEGER NOT NULL DEFAULT 0,
    maxRetries INTEGER NOT NULL DEFAULT 10,
    createdAt TIMESTAMPTZ NOT NULL,
    nextRun TIMESTAMPTZ NOT NULL
)


CREATE TABLE IF NOT EXISTS dist_jobs_scheduler.execution_history(
    id PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    execution_time INTEGER NOT NULL,
    isCompleted BOOLEAN NOT NULL,
    last_update_time TIMESTAMPTZ NOT NULL
)