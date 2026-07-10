package com.example.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import com.example.model.HistoryEntryDTO;
import com.example.model.JobSummaryDTO;

@Service
public class JobQueryService {

    private static final String LIST_JOBS_SQL =
        "SELECT j.id, j.schedule, j.retriesCount, j.maxRetries, j.nextRun, j.jobActive, " +
        "       h.jobStatus AS latestStatus, h.jobFinished AS latestFinished " +
        "FROM dist_jobs_scheduler.jobs j " +
        "LEFT JOIN LATERAL ( " +
        "    SELECT jobStatus, jobFinished FROM dist_jobs_scheduler.history " +
        "    WHERE history.jobId = j.id ORDER BY jobStarted DESC LIMIT 1 " +
        ") h ON true " +
        "ORDER BY j.nextRun ASC";

    private static final String GET_JOB_SQL = LIST_JOBS_SQL.replace(
        "ORDER BY j.nextRun ASC", "WHERE j.id = ? ORDER BY j.nextRun ASC"
    );

    private static final String GET_HISTORY_SQL =
        "SELECT id, jobStatus, jobStarted, jobFinished FROM dist_jobs_scheduler.history " +
        "WHERE jobId = ? ORDER BY jobStarted DESC";

    private final JdbcTemplate jdbcTemplate;

    public JobQueryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<JobSummaryDTO> listJobsWithStatus() {
        return jdbcTemplate.query(LIST_JOBS_SQL, JOB_SUMMARY_ROW_MAPPER);
    }

    public JobSummaryDTO getJobWithStatus(UUID id) {
        List<JobSummaryDTO> rows = jdbcTemplate.query(GET_JOB_SQL, JOB_SUMMARY_ROW_MAPPER, id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public List<HistoryEntryDTO> getHistory(UUID jobId) {
        return jdbcTemplate.query(GET_HISTORY_SQL, HISTORY_ROW_MAPPER, jobId);
    }

    private static final RowMapper<JobSummaryDTO> JOB_SUMMARY_ROW_MAPPER = (rs, rowNum) -> {
        String jobActive = rs.getString("jobActive");
        String latestStatus = rs.getString("latestStatus");
        Timestamp latestFinished = rs.getTimestamp("latestFinished");

        return new JobSummaryDTO(
            UUID.fromString(rs.getString("id")),
            rs.getString("schedule"),
            rs.getInt("retriesCount"),
            rs.getInt("maxRetries"),
            toInstant(rs.getTimestamp("nextRun")),
            jobActive,
            deriveStatus(jobActive, latestStatus, latestFinished)
        );
    };

    private static final RowMapper<HistoryEntryDTO> HISTORY_ROW_MAPPER = (rs, rowNum) -> new HistoryEntryDTO(
        UUID.fromString(rs.getString("id")),
        rs.getString("jobStatus"),
        toInstant(rs.getTimestamp("jobStarted")),
        toInstant(rs.getTimestamp("jobFinished"))
    );

    private static java.time.Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    private static String deriveStatus(String jobActive, String latestStatus, Timestamp latestFinished) {
        if ("dead".equals(jobActive)) {
            return "dead";
        }
        if (latestStatus == null) {
            return "pending";
        }
        if ("processing".equals(latestStatus) && latestFinished == null) {
            return "processing";
        }
        return latestStatus;
    }
}
