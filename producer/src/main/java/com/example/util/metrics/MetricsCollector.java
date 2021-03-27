package com.example.util.metrics;

import com.example.core.Definition;
import com.example.core.Producer;
import com.example.io.Publisher;
import com.example.util.logging.Logger;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class MetricsCollector {

    private final Logger logger;
    private final int frequency;
    private final List<Producer> producers;
    private final Definition definition;
    private final Publisher publisher;
    private final ScheduledExecutorService scheduler;

    Set<String> completedKeys = new HashSet<>();

    public MetricsCollector(Logger logger, int frequency, List<Producer> producers, Definition definition, Publisher publisher) {
        this.logger = logger;
        this.frequency = frequency;
        this.producers = producers;
        this.definition = definition;
        this.publisher = publisher;
        scheduler = Executors.newScheduledThreadPool(1);
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::collect, frequency, frequency, TimeUnit.SECONDS);
    }

    private void collect() {
        logger.info(LocalDateTime.now(), "Frequency => " + definition.getElementPublishFrequencyPerSec());
        logger.info(LocalDateTime.now(), "Total Produced => " + producers.stream().map(Producer::getTotalProducedCounter).mapToLong(AtomicLong::get).sum());
        logger.info(LocalDateTime.now(), "Total Published => " + publisher.getTotalPublishedCounter());
        logger.info(LocalDateTime.now(), "Published Stats at = " + LocalTime.now() + "=======================");
        String publisherMetrics = publisher.getCounterMap().entrySet().stream()
                .filter(e -> !completedKeys.contains(e.getKey()))
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", "));
        logger.info(LocalDateTime.now(), publisherMetrics);
        logger.info(LocalDateTime.now(), "Produced Stats at = " + LocalTime.now() + "=======================");
        producers.forEach(producer -> producer.getElementCounterMap().forEach((element, counterMap) -> {
            String producedMetrics = counterMap.entrySet().stream().filter(e -> !completedKeys.contains(e.getKey()))
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining(", "));

            logger.info(LocalDateTime.now(), element + " : " + producedMetrics);
        }));
        logger.info(LocalDateTime.now(), "======================================");

        publisher.getCounterMap().forEach((k, v) -> completedKeys.add(k));
        producers.forEach(producer -> producer.getElementCounterMap().forEach((element, counterMap) -> counterMap.forEach((k, v) -> completedKeys.add(k))));
    }

    public void stop() {
        scheduler.shutdownNow();
    }
}
