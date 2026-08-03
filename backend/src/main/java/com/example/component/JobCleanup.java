package com.example.component;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JobCleanup {

    private static final Logger log = LoggerFactory.getLogger(JobCleanup.class);

    private final JdbcTemplate jdbc;

    public JobCleanup(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Runs 30s after startup, then hourly. Ages jobs out by firstSeen: since
    // ingestion already rejects postings older than 3 days, this keeps the
    // dashboard a rolling window (worst case ~6 days after the posted date).
    @Scheduled(initialDelay = 30_000, fixedDelay = 3_600_000)
    public void deleteExpiredJobs() {

        int deleted = jdbc.update(
            "DELETE FROM dist_jobs_scheduler.found_jobs WHERE firstSeen < now() - INTERVAL '3 days'"
        );

        if (deleted > 0) {
            log.info("Cleanup: deleted {} job(s) older than 3 days", deleted);
        } else {
            log.debug("Cleanup: nothing to delete");
        }
    }
}
