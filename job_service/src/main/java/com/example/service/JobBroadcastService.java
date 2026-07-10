package com.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.example.model.JobSummaryDTO;

@Service
public class JobBroadcastService {

    private static final Logger log = LoggerFactory.getLogger(JobBroadcastService.class);
    private static final long EMITTER_TIMEOUT_MS = Duration.ofMinutes(30).toMillis();

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final JobQueryService jobQueryService;

    public JobBroadcastService(JobQueryService jobQueryService) {
        this.jobQueryService = jobQueryService;
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        sendSnapshot(emitter);

        return emitter;
    }

    @Scheduled(fixedDelay = 2000)
    public void broadcast() {
        if (emitters.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : emitters) {
            sendSnapshot(emitter);
        }
    }

    private void sendSnapshot(SseEmitter emitter) {
        try {
            List<JobSummaryDTO> jobs = jobQueryService.listJobsWithStatus();
            emitter.send(SseEmitter.event().name("jobs").data(jobs, MediaType.APPLICATION_JSON));
        } catch (IOException e) {
            log.debug("Removing stale SSE emitter", e);
            emitter.completeWithError(e);
            emitters.remove(emitter);
        }
    }
}
