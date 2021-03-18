package com.example.io;

import com.example.util.logging.Logger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;

public class BlockingQueuePublisher extends Publisher {

    private final BlockingQueue<String> queue;

    public BlockingQueuePublisher(int port, String host, Logger logger, BlockingQueue<String> queue) throws IOException {
        super(port, host, logger);
        this.queue = queue;
    }

    @Override
    public void start() {
        new Thread(() -> {
            while (true) {
                try {
                    publish(queue.take());
                } catch (Throwable e) {
                    logger.error(LocalDateTime.now(), "Exception occurred while retrieving data from queue", e);
                }
            }
        }).start();
    }
}
