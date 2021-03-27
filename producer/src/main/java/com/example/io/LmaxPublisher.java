package com.example.io;

import com.example.model.lmax.ValueEvent;
import com.example.util.logging.Logger;
import com.lmax.disruptor.EventHandler;

import java.io.IOException;

public class LmaxPublisher extends Publisher {

    public LmaxPublisher(int port, String host, Logger logger) throws IOException {
        super(port, host, logger);
    }

    public EventHandler<ValueEvent<String>>[] getEventHandler() {
        EventHandler<ValueEvent<String>> eventHandler = (event, sequence, endOfBatch) -> publish(event.getValue());
        return new EventHandler[] { eventHandler };
    }

    @Override
    public void start() {}
}
