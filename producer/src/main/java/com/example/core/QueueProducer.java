package com.example.core;

import com.example.util.logging.Logger;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class QueueProducer extends Producer {

    private final BlockingQueue<String> queue;
    private static final Random RANDOM = new Random();

    public QueueProducer(Logger logger, int frequency, Definition definition, BlockingQueue<String> queue) {
        super(logger, frequency, definition);
        this.queue = queue;
    }

    @Override
    public void publish(String json) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(RANDOM.nextInt(10));
        queue.put(json);
    }
}
