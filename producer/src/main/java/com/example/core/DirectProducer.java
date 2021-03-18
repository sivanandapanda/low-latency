package com.example.core;

import com.example.io.Publisher;
import com.example.util.logging.Logger;

public class DirectProducer extends Producer {

    private final Publisher publisher;

    public DirectProducer(Logger logger, int frequency, Definition definition, Publisher publisher) {
        super(logger, frequency, definition);
        this.publisher = publisher;
    }

    @Override
    void publish(String json) {
        publisher.publish(json);
    }
}
