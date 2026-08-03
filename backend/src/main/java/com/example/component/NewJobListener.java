package com.example.component;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.example.dto.JobFound;
import com.example.repository.SubscribeRepository;
import java.util.List;

@Component
public class NewJobListener {

    private final SimpMessagingTemplate messaging;
    private final SubscribeRepository subscribeRepository;

    public NewJobListener(SimpMessagingTemplate messaging, SubscribeRepository subscribeRepository) {
        this.messaging = messaging;
        this.subscribeRepository = subscribeRepository;
    }

    @RabbitListener(queues = "new-jobs")
    public void onNewJob(JobFound event) {

        List<String> usernames = subscribeRepository.findUsernamesWatching(event.ats());
        for (String username: usernames) {
            messaging.convertAndSendToUser(username, "/queue/jobs", event);
        }
    }
    
}
