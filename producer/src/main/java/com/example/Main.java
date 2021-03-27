package com.example;

import com.example.core.*;
import com.example.io.DirectPublisher;
import com.example.io.LmaxPublisher;
import com.example.io.Publisher;
import com.example.io.QueuePublisher;
import com.example.model.Element;
import com.example.model.RunMode;
import com.example.model.lmax.ValueEvent;
import com.example.util.lmax.LmaxUtils;
import com.example.util.logging.LogLevel;
import com.example.util.logging.Logger;
import com.example.util.metrics.MetricsCollector;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static com.example.model.RunMode.DIRECT;

public class Main {

    public static void main(String[] args) throws IOException {
        Main main = new Main(9999, "localhost", LogLevel.INFO, 1, Element.values(), 100);
        main.start();

        Runtime.getRuntime().addShutdownHook(new Thread(main::shutdown));
    }

    private final int port;
    private final String hostname;
    private final LogLevel logLevel;
    private final int frequencyInSec;
    private final Element[] elementsArr;
    private final int publishQueueCapacity;
    private final RunMode runMode;

    private Logger logger;
    private List<Producer> producers;
    private Publisher publisher;
    private MetricsCollector metricsCollector;

    public Main(int port, String hostname, LogLevel logLevel, int frequencyInSec, Element[] elementsArr, int publishQueueCapacity) {
        this(port, hostname, logLevel, frequencyInSec, elementsArr, publishQueueCapacity, DIRECT);
    }

    public Main(int port, String hostname, LogLevel logLevel, int frequencyInSec, Element[] elementsArr, int publishQueueCapacity, RunMode runMode) {
        this.port = port;
        this.hostname = hostname;
        this.logLevel = logLevel;
        this.frequencyInSec = frequencyInSec;
        this.elementsArr = elementsArr;
        this.publishQueueCapacity = publishQueueCapacity;
        this.runMode = runMode;
    }

    void start() throws IOException {
        logger = new Logger(logLevel);

        Definition definition = new Definition(elementsArr);

        switch (runMode) {
            case DIRECT:
                publisher = new DirectPublisher(port, hostname, logger);
                Producer producer1 = new DirectProducer(logger, frequencyInSec, definition, publisher);
                Producer producer2 = new DirectProducer(logger, frequencyInSec, definition, publisher);
                Producer producer3 = new DirectProducer(logger, frequencyInSec, definition, publisher);
                producers = Arrays.asList(producer1, producer2, producer3);
                break;
            case QUEUE:
                BlockingQueue<String> queue = new ArrayBlockingQueue<>(publishQueueCapacity);
                publisher = new QueuePublisher(port, hostname, logger, queue);
                producer1 = new QueueProducer(logger, frequencyInSec, definition, queue);
                producer2 = new QueueProducer(logger, frequencyInSec, definition, queue);
                producer3 = new QueueProducer(logger, frequencyInSec, definition, queue);
                producers = Arrays.asList(producer1, producer2, producer3);
                break;
            case LMAX:
                Disruptor<ValueEvent<String>> disruptor = LmaxUtils.createDisruptor();
                publisher = new LmaxPublisher(port, hostname, logger);
                disruptor.handleEventsWith(((LmaxPublisher)publisher).getEventHandler());
                RingBuffer<ValueEvent<String>> ringBuffer = disruptor.start();
                producer1 = new LmaxProducer(logger, frequencyInSec, definition, ringBuffer);
                producer2 = new LmaxProducer(logger, frequencyInSec, definition, ringBuffer);
                producer3 = new LmaxProducer(logger, frequencyInSec, definition, ringBuffer);
                producers = Arrays.asList(producer1, producer2, producer3);
                break;
        }

        publisher.start();
        producers.forEach(Producer::start);

        metricsCollector = new MetricsCollector(logger, frequencyInSec * 5, producers, definition, publisher);
        metricsCollector.start();
    }

    void shutdown() {
        Objects.requireNonNull(producers).forEach(Producer::stop);

        try {
            Objects.requireNonNull(publisher).close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Objects.requireNonNull(metricsCollector).stop();

        Objects.requireNonNull(logger).stop();
    }
}
