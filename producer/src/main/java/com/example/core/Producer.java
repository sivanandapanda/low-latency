package com.example.core;

import com.example.io.SocketPublisher;
import com.example.model.DataElement;
import com.example.model.Element;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class Producer {

    private final int frequency;
    private final Definition definition;
    private final SocketPublisher publisher;
    private final ScheduledExecutorService scheduler;

    private static final Map<Element, ExecutorService> publisherExecutorMap = new HashMap<>();
    private static final Map<Element, Map<String, AtomicLong>> elementCounterMap = new ConcurrentHashMap<>();

    public Producer(int frequency, Definition definition, SocketPublisher publisher) {
        this.frequency = frequency;
        this.definition = definition;
        this.publisher = publisher;

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

        //System.out.println(LocalTime.now() + " - " + Thread.currentThread().getName() + " : " + element + " with frequency " + frequency);

        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"));
        Map<String, AtomicLong> counterMap = elementCounterMap.get(element);

        AtomicLong counter = counterMap.computeIfAbsent(time, __ -> new AtomicLong(0));

        for (int i = 0; i < frequency; i++) {
            String asJson = new DataElement(element, random.nextDouble() * randomMultiplier).asJson();
            try {
                publisher.publish(asJson);
                counter.incrementAndGet();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        counterMap.put(time, counter);
    }

    public Map<Element, Map<String, AtomicLong>> getElementCounterMap() {
        return Collections.unmodifiableMap(elementCounterMap);
    }

    public void stop() {
        scheduler.shutdownNow();
        publisherExecutorMap.values().forEach(ExecutorService::shutdownNow);
    }
}
