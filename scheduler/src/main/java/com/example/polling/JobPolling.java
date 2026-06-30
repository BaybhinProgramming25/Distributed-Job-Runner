package com.example.polling;

import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jdbc.core.RowMapper; 

import com.cronutils.parser.CronParser;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.CronType;
import com.cronutils.model.Cron;
import com.cronutils.model.time.ExecutionTime;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.example.model.JobHistory;

@Component
public class JobPolling implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate; 
    private final RabbitTemplate rabbitTemplate;

    private final RowMapper<JobHistory> jobRowMapper = (rs, rowNum) -> new JobHistory(
        UUID.fromString(rs.getString("jobId")),
        UUID.fromString(rs.getString("historyId")),
        rs.getString("schedule"),
        rs.getInt("retriesCount"),
        rs.getInt("maxRetries"),
        rs.getTimestamp("nextRun")
    );

    public JobPolling(JdbcTemplate jdbctemplate, RabbitTemplate rabbittemplate) {
        this.jdbcTemplate = jdbctemplate;
        this.rabbitTemplate = rabbittemplate;
    }

    public static Timestamp getNextRunTime(Timestamp currentTime, String cronString) {

        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        Cron cron = parser.parse(cronString); 

        ZonedDateTime nowUTC = currentTime.toInstant().atZone(ZoneOffset.UTC);
        ZonedDateTime nextUTC = ExecutionTime.forCron(cron).nextExecution(nowUTC).orElse(null);

        if (nextUTC == null) { 
            return null;
        }

        return Timestamp.from(nextUTC.toInstant());
    }

    @Override
    public void run(String...args) {

        while (true) {

            try {
                
                List<JobHistory> jobs = jdbcTemplate.query(
                    "SELECT jobs.id AS jobId, jobs.schedule AS schedule, jobs.maxRetries AS maxRetries, jobs.nextRun AS nextRun, history.id AS historyId, history.retriesCount AS retriesCount FROM jobs JOIN history ON jobs.id = history.jobId WHERE nextRun <= now()", jobRowMapper
                );
                
                for(JobHistory job : jobs) {

                    if (job.retriesCount() >= job.maxRetries()) {

                        try {

                            int rowsChanged = jdbcTemplate.update(
                                "UPDATE history SET jobStatus = ? WHERE id = ?", "failed", job.HistoryId()
                            );

                            if (rowsChanged == 0) {
                                // Log that something didnt change in the database (not necessarily an error)
                                // Add the logging and error stuff afterwards

                            }

                        } catch (DataAccessException e) {

                            // Do something here 
                        }
                    }
                    else {
                        
                        // Add the job into the queue
                        rabbitTemplate.convertAndSend("job.queue", job);

                        // Calculate the next run 
                        Timestamp nextUTC = getNextRunTime(Timestamp.from(Instant.now()), job.Schedule());

                        try {
                            jdbcTemplate.update(
                                "UPDATE jobs SET nextRun = ? WHERE id = ?", nextUTC, job.JobId()
                            );

                        } catch (DataAccessException e) {
                            
                            // Do something else here instead of returning
                        }
                    }
                }

                // 30-second sleep 
                Thread.sleep(30000);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
