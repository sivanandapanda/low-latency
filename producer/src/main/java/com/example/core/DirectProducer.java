package com.example.core;

import com.example.io.Publisher;
import com.example.util.logging.Logger;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DirectProducer extends Producer {

    private final Publisher publisher;
    private final static Lock LOCK = new ReentrantLock();
    private static final Random RANDOM = new Random();

    public DirectProducer(Logger logger, int frequency, Definition definition, Publisher publisher) {
        super(logger, frequency, definition);
        this.publisher = publisher;
    }

    @Override
    public void publish(String json) throws InterruptedException {
        try {
            LOCK.lock();
            TimeUnit.MILLISECONDS.sleep(RANDOM.nextInt(10));
            publisher.publish(json);
        } finally {
            LOCK.unlock();
        }
    }
}
