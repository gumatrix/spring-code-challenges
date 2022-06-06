package com.cecilireid.springchallenges;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CateringJobsEndpointUTest {

    @Mock private CateringJobRepository cateringJobRepository;
    @Mock private CateringJob cateringJob;
    
    @InjectMocks
    private CateringJobsEndpoint cateringJobsEndpoint;

    @Test
    void getCateringJobsMetrics_noJobs_metricsIsEmpty() {
        // Arrange
        when(cateringJobRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        final Map<Status, Long> metrics = cateringJobsEndpoint.getCateringJobsMetrics();

        // Assert
        assertTrue(metrics.isEmpty());
    }

    @Test
    void getCateringJobsMetrics_oneInProgressJob_metricsContainsOneInProgressJob() {
        // Arrange
        when(cateringJob.getStatus()).thenReturn(Status.IN_PROGRESS);
        when(cateringJobRepository.findAll()).thenReturn(Collections.singletonList(cateringJob));

        // Act
        final Map<Status, Long> metrics = cateringJobsEndpoint.getCateringJobsMetrics();

        // Assert
        assertEquals(1, metrics.get(Status.IN_PROGRESS));
    }
}