package com.example.util;

import com.example.core.Definition;
import com.example.core.Producer;
import com.example.io.SocketPublisher;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MetricsCollector {

    private final int frequency;
    private final Producer producer;
    private final Definition definition;
    private final SocketPublisher socketPublisher;
    private final ScheduledExecutorService scheduler;

    Set<String> completedKeys = new HashSet<>();

    public MetricsCollector(int frequency, Producer producer, Definition definition, SocketPublisher socketPublisher) {
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
        System.out.println("Frequency => " + definition.getElementPublishFrequencyPerSec());
        System.out.println("Published Stats at = "+ LocalTime.now() +"=======================");
        socketPublisher.getCounterMap().forEach((k, v) -> {
            if(!completedKeys.contains(k)) {
                System.out.println(k + "=" + v);
            }
        });
        System.out.println("Produced Stats at = "+ LocalTime.now() +"=======================");
        producer.getElementCounterMap().forEach((element, counterMap) -> counterMap.forEach((k, v) -> {
            if(!completedKeys.contains(k)) {
                System.out.println(element + " : " + k + "=" + v);
            }
        }));
        System.out.println("======================================");

        socketPublisher.getCounterMap().forEach((k, v) -> completedKeys.add(k));
        producer.getElementCounterMap().forEach((element, counterMap) -> counterMap.forEach((k, v) -> completedKeys.add(k)));
    }

    public void stop() {
        scheduler.shutdownNow();
    }
}
