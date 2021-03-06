package com.example;

import com.example.core.Definition;
import com.example.core.Producer;
import com.example.io.SocketPublisher;
import com.example.model.Element;
import com.example.util.MetricsCollector;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

//import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void publish_test() throws IOException, InterruptedException {
        int frequencyInSec = 2;

        SocketPublisher socketPublisher = new SocketPublisher(9989, "localhost");

        Definition definition = new Definition(Element.values());

        Producer producer = new Producer(frequencyInSec, definition, socketPublisher);

        producer.start();

        MetricsCollector metricsCollector = new MetricsCollector(frequencyInSec * 5, producer, definition, socketPublisher);
        metricsCollector.start();

        TimeUnit.SECONDS.sleep(10);

        producer.stop();
        socketPublisher.close();
        metricsCollector.stop();
    }

    @Test
    void listen_test() throws IOException {
        int port = 4445;
        new Thread(() -> {
            try {
                int frequencyInSec = 2;

                SocketPublisher socketPublisher = new SocketPublisher(port, "localhost");

                Definition definition = new Definition(Element.values());

                Producer producer = new Producer(frequencyInSec, definition, socketPublisher);

                producer.start();

                TimeUnit.SECONDS.sleep(10);

                producer.stop();
                socketPublisher.close();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        DatagramChannel channel = DatagramChannel.open();
        channel.socket().connect(new InetSocketAddress("localhost", port));
        channel.configureBlocking(false);

        ExecutorService asyncLogger = Executors.newCachedThreadPool();

        while (true) {
            ByteBuffer buf = ByteBuffer.allocate(70);
            buf.clear();
            channel.receive(buf);
            String s = StandardCharsets.UTF_8.decode(buf).toString();
            asyncLogger.submit(() -> System.out.println(LocalTime.now() + " : " + s));
        }
    }



}