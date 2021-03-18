package com.example;

import com.example.core.BlockingQueueProducer;
import com.example.core.Definition;
import com.example.core.DirectProducer;
import com.example.core.Producer;
import com.example.io.BlockingQueuePublisher;
import com.example.io.DirectPublisher;
import com.example.io.Publisher;
import com.example.model.Element;
import com.example.model.RunMode;
import com.example.util.logging.LogLevel;
import com.example.util.logging.Logger;
import com.example.util.metrics.MetricsCollector;

import java.io.IOException;
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
    private Producer producer;
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
                producer = new DirectProducer(logger, frequencyInSec, definition, publisher);
                break;
            case QUEUE:
                BlockingQueue<String> queue = new ArrayBlockingQueue<>(publishQueueCapacity);
                publisher = new BlockingQueuePublisher(port, hostname, logger, queue);
                producer = new BlockingQueueProducer(logger, frequencyInSec, definition, queue);
                break;
        }

        publisher.start();
        producer.start();

        metricsCollector = new MetricsCollector(logger, frequencyInSec * 5, producer, definition, publisher);
        metricsCollector.start();
    }

    void shutdown() {
        Objects.requireNonNull(producer).stop();

        try {
            Objects.requireNonNull(publisher).close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Objects.requireNonNull(metricsCollector).stop();

        Objects.requireNonNull(logger).stop();
    }
}
