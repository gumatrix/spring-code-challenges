package com.cecilireid.springchallenges;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduledTasksUTest {

    private static final String LOGGER_NAME = "com.cecilireid.springchallenges";

    @Mock private CateringJobRepository cateringJobRepository;
    @Mock private CateringJob cateringJob;

    @InjectMocks
    private ScheduledTasks scheduledTasks;

    private MemoryAppender memoryAppender;

    @BeforeEach
    void setUp() {
        Logger logger = (Logger) LoggerFactory.getLogger(LOGGER_NAME);
        memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.setLevel(Level.DEBUG);
        logger.addAppender(memoryAppender);
        memoryAppender.start();
    }

    @Test
    void reportOrderStats_noOrders_logNumberOfOrdersZero() {
        // Arrange
        when(cateringJobRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        scheduledTasks.reportOrderStats();

        // Assert
        assertEquals(1, memoryAppender.search("Number of jobs: 0", Level.INFO).size());
    }

    @Test
    void reportOrderStats_oneOrder_logNumberOfOrdersOne() {
        // Arrange
        when(cateringJobRepository.findAll()).thenReturn(Collections.singletonList(cateringJob));

        // Act
        scheduledTasks.reportOrderStats();

        // Assert
        assertEquals(1, memoryAppender.search("Number of jobs: 1", Level.INFO).size());
    }
}