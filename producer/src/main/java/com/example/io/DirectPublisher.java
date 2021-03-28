package com.example.io;

import com.example.util.logging.Logger;

import java.io.IOException;

public class DirectPublisher extends Publisher {

    public DirectPublisher(int port, String host, Logger logger) throws IOException {
        super(port, host, logger);
    }

    @Override
    public void start() {}

    @Override
    public synchronized void publish(String data) {
        super.publish(data);
    }
}
