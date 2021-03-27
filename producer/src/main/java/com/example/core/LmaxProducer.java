package com.example.core;

import com.example.model.lmax.ValueEvent;
import com.example.util.logging.Logger;
import com.lmax.disruptor.RingBuffer;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class LmaxProducer extends Producer {

    private final RingBuffer<ValueEvent<String>> ringBuffer;
    private static final Random RANDOM = new Random();

    public LmaxProducer(Logger logger, int frequency, Definition definition, RingBuffer<ValueEvent<String>> ringBuffer) {
        super(logger, frequency, definition);
        this.ringBuffer = ringBuffer;
    }

    @Override
    public void publish(String json) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(RANDOM.nextInt(10));
        long sequenceId = ringBuffer.next();
        ValueEvent<String> valueEvent = ringBuffer.get(sequenceId);
        valueEvent.setValue(json);
        ringBuffer.publish(sequenceId);
    }
}
