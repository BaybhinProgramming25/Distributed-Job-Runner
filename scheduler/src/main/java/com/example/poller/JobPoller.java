package com.example.poller;

import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.example.config.PollingTargets;
import com.example.helpers.AtsFetchers;
import com.example.helpers.CsJobFilter;
import com.example.model.JobBatch;
import com.example.model.JobFound;

@Component
public class JobPoller implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(JobPoller.class);
    private static final Duration BETWEEN_BOARDS = Duration.ofMillis(800);

    private final PollingTargets targets;
    private final AtsFetchers fetchers;
    private final RabbitTemplate rabbitTemplate;

    public JobPoller(PollingTargets targets, AtsFetchers fetchers, RabbitTemplate rabbitTemplate) {
        this.targets = targets;
        this.fetchers = fetchers;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void run(String...args) {

        while (true) {

            Instant startedAt = Instant.now();
            log.info("Poll cycle starting - {} board(s)", targets.getTargets().size());

            List<JobFound> jobHits = new ArrayList<>();
            List<String> jobFails = new ArrayList<>();

            for (PollingTargets.Target t : targets.getTargets()) {
                try {
                    List<JobFound> board = fetchers.fetch(t.getAts(), t.getSlug());
                    List<JobFound> hits = board.stream().filter(CsJobFilter::isCsJob).toList();
                    jobHits.addAll(hits);
                    log.info("   {}: {}/{} match", t, hits.size(), board.size());
                } catch (Exception e) {
                    
                    String message = t + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage();
                    log.warn(" {}", message);
                    jobFails.add(message);

                }

                try {
                    // Then we sleep between each job board
                    Thread.sleep(BETWEEN_BOARDS.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.warn("Poll cycle has been interrupted...publishing what we have");
                    break;
                }
            }

            JobBatch batch = new JobBatch(startedAt, jobHits, jobFails);

            try {
                rabbitTemplate.convertAndSend("job.queue", batch);
                log.info("Poll cycle done in {}s - published {} job(s), {} failure(s)", Duration.between(startedAt, Instant.now()).toSeconds(), jobHits.size(), jobFails.size());
            } catch (Exception e) {
                // Something went wrong with RabbitMQ
                log.error("Failed to publish batch to RabbitMQ", e);
            }

            // 30 minutes sleep
            try {
                Thread.sleep(30*60*1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.info("Scheduler is down...");
                break;
            }
        }
    }
}
