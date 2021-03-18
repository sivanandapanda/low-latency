package com.example.core;

import com.example.util.logging.Logger;

import java.util.concurrent.BlockingQueue;

public class BlockingQueueProducer extends Producer {

    private final BlockingQueue<String> queue;

    public BlockingQueueProducer(Logger logger, int frequency, Definition definition, BlockingQueue<String> queue) {
        super(logger, frequency, definition);
        this.queue = queue;
    }

    @Override
    void publish(String json) throws InterruptedException {
        queue.put(json);
    }
}
