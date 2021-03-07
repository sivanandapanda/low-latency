package com.example.core;

import com.example.model.DataElement;
import com.example.model.Element;
import com.example.util.logging.Logger;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class Producer {

    private final Logger logger;
    private final int frequency;
    private final Definition definition;
    private final BlockingQueue<String> queue;
    private final ScheduledExecutorService scheduler;

    private static final AtomicLong totalProducedCounter = new AtomicLong(0);
    private static final Map<Element, ExecutorService> publisherExecutorMap = new HashMap<>();
    private static final Map<Element, Map<String, AtomicLong>> elementCounterMap = new ConcurrentHashMap<>();

    public Producer(Logger logger, int frequency, Definition definition, BlockingQueue<String> queue) {
        this.logger = logger;
        this.frequency = frequency;
        this.definition = definition;
        this.queue = queue;

        scheduler = Executors.newScheduledThreadPool(1);
        definition.getElements().forEach(e -> {
            publisherExecutorMap.put(e, Executors.newSingleThreadExecutor());
            elementCounterMap.put(e, new HashMap<>());
        });
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::publish, frequency, frequency, TimeUnit.SECONDS);
    }

    private void publish() {
        definition.getElements().parallelStream()
                .forEach(e -> publisherExecutorMap.get(e).submit(() -> publishRandom(e)));
    }

    private void publishRandom(Element element) {
        Random random = new Random();
        int randomMultiplier = random.nextInt();

        int frequency = definition.getFrequency(element);

        logger.debug(LocalDateTime.now(), element + " with frequency " + frequency);

        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"));
        Map<String, AtomicLong> counterMap = elementCounterMap.get(element);

        AtomicLong counter = counterMap.computeIfAbsent(time, __ -> new AtomicLong(0));

        for (int i = 0; i < frequency; i++) {
            String asJson = new DataElement(element, random.nextDouble() * randomMultiplier).asJson();
            try {
                queue.put(asJson);
                totalProducedCounter.incrementAndGet();
                counter.incrementAndGet();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public Map<Element, Map<String, AtomicLong>> getElementCounterMap() {
        return Collections.unmodifiableMap(elementCounterMap);
    }

    public AtomicLong getTotalProducedCounter() {
        return totalProducedCounter;
    }

    public void stop() {
        scheduler.shutdownNow();
        publisherExecutorMap.values().forEach(ExecutorService::shutdownNow);
    }
}
