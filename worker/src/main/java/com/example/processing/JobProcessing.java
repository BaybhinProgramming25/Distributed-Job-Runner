package com.example.processing;

import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
// import java.util.HashMap;
import java.util.List;

import com.example.model.JobBatch;
import com.example.model.JobFound;

@Component
public class JobProcessing {

    private static final Logger log = LoggerFactory.getLogger(JobProcessing.class);
    // private static final int SMS_MAX_JOBS_PER_ALERT = 25;

    private final JdbcTemplate jdbcTemplate;
    private final RabbitTemplate jsonRabbitTemplate;

    public JobProcessing(JdbcTemplate jdbctemplate, RabbitTemplate jsonRabbitTemplate) {
        this.jdbcTemplate = jdbctemplate;
        this.jsonRabbitTemplate = jsonRabbitTemplate;
    }

    @RabbitListener(queues = "job.queue")
    public void processJob(JobBatch batch) {

        log.info("Batch received: {} job(s), {} board failure(s), polled at {}", batch.foundJobs().size(), batch.failures().size(), batch.polledAt());

        for (String f : batch.failures()) {
            log.warn("Board failed during poll: {}", f);
        }

        List<JobFound> freshJobs = insertAll(batch.foundJobs());

        if (freshJobs.isEmpty()) {
            log.info("No new jobs found this cycle...");
        } else {
            log.info("{} new job(s)", freshJobs.size());

            for (JobFound job : freshJobs) {
                jsonRabbitTemplate.convertAndSend("new-jobs", job);
            }

            // Leave this unmarked for now 
            // notifyBatch(freshJobs);
        }
    }

    private List<JobFound> insertAll(List<JobFound> newJobs) {

        List<JobFound> freshJobs = new ArrayList<>();
        for (JobFound j : newJobs) {
            
            int processedJob = jdbcTemplate.update(
                "UPDATE dist_jobs_scheduler.found_jobs SET lastSeen = now() WHERE jobId = ?", j.jobId()
            );
            
            if (processedJob == 0) {
                jdbcTemplate.update("""
                    INSERT INTO dist_jobs_scheduler.found_jobs
                    (company, ats, jobId, title, location, department, url, posted, firstSeen, lastSeen)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, now(), now())
                    ON CONFLICT (jobId) DO UPDATE SET lastSeen = now()""", j.company(), j.ats(), j.jobId(), j.title(), j.location(), j.department(), j.url(), j.posted());
                freshJobs.add(j);
            }
        }
        return freshJobs;
    }

    /* 
    private void notifyBatch(List<JobFound> newJobs) {

        if (newJobs.isEmpty()) {
            log.info("Nobody to notify since there are no new jobs...");
            return;
        }

        HashMap<String, List<JobFound>> companyJobMap = new HashMap<>();
        for (JobFound job : newJobs) {

            String company = job.company();
            if (companyJobMap.containsKey(company)) {
                companyJobMap.get(company).add(job);
            } else {
                List<JobFound> companyJobList = new ArrayList<>();
                companyJobList.add(job);
                companyJobMap.put(company, companyJobList);
            }

        }

        HashMap<Long, List<JobFound>> userJobsMap = new HashMap<>();
        for (String company : companyJobMap.keySet()) {
            
            List<Long> userIds = jdbcTemplate.queryForList("SELECT user_id FROM dist_jobs_scheduler.watched_jobs WHERE company_id = ?", Long.class, company);

            for (Long userId : userIds) {

                List<JobFound> jobs = companyJobMap.get(company);
                if (userJobsMap.containsKey(userId)) {
                    userJobsMap.get(userId).addAll(jobs);
                } else {
                    userJobsMap.put(userId, jobs);
                }
            }
        }

        // We iterate and get the phone number of each of the persons 
        // For now we just print the ID
        for (Long userId : userJobsMap.keySet()) {
            System.out.println(userId);
            // Do more stuff later...
        }
    }
    */
}
