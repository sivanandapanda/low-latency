package com.example.util.metrics;

import com.example.core.Definition;
import com.example.core.Producer;
import com.example.io.SocketPublisher;
import com.example.util.logging.Logger;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MetricsCollector {

    private final Logger logger;
    private final int frequency;
    private final Producer producer;
    private final Definition definition;
    private final SocketPublisher socketPublisher;
    private final ScheduledExecutorService scheduler;

    Set<String> completedKeys = new HashSet<>();

    public MetricsCollector(Logger logger, int frequency, Producer producer, Definition definition, SocketPublisher socketPublisher) {
        this.logger = logger;
        this.frequency = frequency;
        this.producer = producer;
        this.definition = definition;
        this.socketPublisher = socketPublisher;
        scheduler = Executors.newScheduledThreadPool(1);
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::collect, frequency, frequency, TimeUnit.SECONDS);
    }

    private void collect() {
        logger.info(LocalDateTime.now(), "Frequency => " + definition.getElementPublishFrequencyPerSec());
        logger.info(LocalDateTime.now(), "Total Produced => " + producer.getTotalProducedCounter());
        logger.info(LocalDateTime.now(), "Total Published => " + socketPublisher.getTotalPublishedCounter());
        logger.info(LocalDateTime.now(), "Published Stats at = " + LocalTime.now() + "=======================");
        String publisherMetrics = socketPublisher.getCounterMap().entrySet().stream()
                .filter(e -> !completedKeys.contains(e.getKey()))
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", "));
        logger.info(LocalDateTime.now(), publisherMetrics);
        logger.info(LocalDateTime.now(), "Produced Stats at = " + LocalTime.now() + "=======================");
        producer.getElementCounterMap().forEach((element, counterMap) -> {
            String producedMetrics = counterMap.entrySet().stream().filter(e -> !completedKeys.contains(e.getKey()))
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining(", "));

            logger.info(LocalDateTime.now(), element + " : " + producedMetrics);
        });
        logger.info(LocalDateTime.now(), "======================================");

        socketPublisher.getCounterMap().forEach((k, v) -> completedKeys.add(k));
        producer.getElementCounterMap().forEach((element, counterMap) -> counterMap.forEach((k, v) -> completedKeys.add(k)));
    }

    public void stop() {
        scheduler.shutdownNow();
    }
}
