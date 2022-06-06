package com.cecilireid.springchallenges;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScheduledTasks {
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    private CateringJobRepository cateringJobRepository;

    @Autowired
    public ScheduledTasks(CateringJobRepository cateringJobRepository) {
        this.cateringJobRepository = cateringJobRepository;
    }

    @Scheduled(cron = "*/10 * * * * *")
    public void reportOrderStats() {
        final List<CateringJob> jobs = cateringJobRepository.findAll();

        logger.info("Number of jobs: {}", jobs.size());
    }
}
