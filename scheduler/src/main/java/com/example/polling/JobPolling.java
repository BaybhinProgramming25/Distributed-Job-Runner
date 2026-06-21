package com.example.polling;

import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

@Component
public class JobPolling {

    @Scheduled
    public void pollJobs() {
        /*
        1) Get the database connection
        2) Get the Orchestrator 
        3) Perform infinite while loop that pols every 5 minutes 
        
         */
    }
}
