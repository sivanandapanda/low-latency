package com.example;

import com.example.core.Definition;
import com.example.core.Producer;
import com.example.io.SocketPublisher;
import com.example.model.Element;
import com.example.util.MetricsCollector;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        int frequencyInSec = 1;

        SocketPublisher socketPublisher = new SocketPublisher(9999, "localhost");

        Definition definition = new Definition(Element.values());

        Producer producer = new Producer(frequencyInSec, definition, socketPublisher);

        producer.start();

        MetricsCollector metricsCollector = new MetricsCollector(frequencyInSec * 5, producer, definition, socketPublisher);
        metricsCollector.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            producer.stop();

            try {
                socketPublisher.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            metricsCollector.stop();
        }));
    }
}
