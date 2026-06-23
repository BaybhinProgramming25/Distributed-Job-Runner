package com.example.polling;

import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataAccessException;

@Component
public class JobPolling {

    private final JdbcTemplate jdbcTemplate; 

    public JobPolling(JdbcTemplate jdbctemplate) {
        this.jdbcTemplate = jdbctemplate;
    }

    public void pollJobs() {
    
        while (true) {

            try {
                
                List<Map<String, Object>> jobs = jdbcTemplate.queryForObject(
                    "SELECT * FROM dist_jobs_scheduler.jobs WHERE nextRun <= now()"
                );
                
                // Verify jobs are being pulled 
                for (Map<String, Object> job : jobs) {
                    System.out.println(job);
                }

                // Do some processing here afterwards with RabbitMQ

                // 30-second sleep 
                Thread.sleep(30000);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
