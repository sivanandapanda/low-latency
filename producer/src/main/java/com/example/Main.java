package com.example;

import com.example.core.Definition;
import com.example.core.Producer;
import com.example.io.SocketPublisher;
import com.example.model.Element;
import com.example.util.logging.LogLevel;
import com.example.util.logging.Logger;
import com.example.util.metrics.MetricsCollector;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

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

    private Logger logger;
    private Producer producer;
    private SocketPublisher socketPublisher;
    private MetricsCollector metricsCollector;

    public Main(int port, String hostname, LogLevel logLevel, int frequencyInSec, Element[] elementsArr, int publishQueueCapacity) {
        this.port = port;
        this.hostname = hostname;
        this.logLevel = logLevel;
        this.frequencyInSec = frequencyInSec;
        this.elementsArr = elementsArr;
        this.publishQueueCapacity = publishQueueCapacity;
    }

    void start() throws IOException {
        logger = new Logger(logLevel);

        BlockingQueue<String> queue = new ArrayBlockingQueue<>(publishQueueCapacity);

        socketPublisher = new SocketPublisher(port, hostname, logger, queue);
        socketPublisher.start();

        Definition definition = new Definition(elementsArr);

        producer = new Producer(logger, frequencyInSec, definition, queue);
        producer.start();

        metricsCollector = new MetricsCollector(logger, frequencyInSec * 5, producer, definition, socketPublisher);
        metricsCollector.start();
    }

    void shutdown() {
        Objects.requireNonNull(producer).stop();

        try {
            Objects.requireNonNull(socketPublisher).close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Objects.requireNonNull(metricsCollector).stop();

        Objects.requireNonNull(logger).stop();
    }
}
