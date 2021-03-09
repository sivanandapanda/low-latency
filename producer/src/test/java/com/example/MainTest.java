package com.example;

import com.example.model.Element;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.example.model.Element.*;
import static com.example.util.logging.LogLevel.DEBUG;
import static com.example.util.logging.LogLevel.INFO;

//import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void publish_test() throws IOException, InterruptedException {
        Main main = new Main(9979, "localhost", DEBUG, 2, new Element[]{ABC, XYZ, LMN}, 50);
        main.start();

        TimeUnit.SECONDS.sleep(15);

        main.shutdown();
    }

    @Test
    void listen_test() throws IOException {
        int port = 4445;
        new Thread(() -> {
            try {
                Main main = new Main(port, "localhost", INFO, 2, Element.values(), 50);
                main.start();

                TimeUnit.SECONDS.sleep(10);

                main.shutdown();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        DatagramChannel channel = DatagramChannel.open();
        channel.socket().bind(new InetSocketAddress("localhost", port));

        ExecutorService asyncLogger = Executors.newCachedThreadPool();

        while (true) {
            ByteBuffer buf = ByteBuffer.allocate(70);
            buf.clear();
            channel.receive(buf);
            String s = new String(buf.array(), 0, buf.position());
            asyncLogger.submit(() -> System.out.println(new Date().getTime() + " :: " + s));
        }
    }
}