package com.example;

import com.example.model.Element;
import com.example.model.RunMode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.OptionalDouble;
import java.util.concurrent.*;

import static com.example.model.Element.*;
import static com.example.model.RunMode.DIRECT;
import static com.example.model.RunMode.QUEUE;
import static com.example.util.logging.LogLevel.*;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void publish_test() throws IOException, InterruptedException {
        Main main = new Main(9979, "localhost", DEBUG, 2, new Element[]{ABC, XYZ, LMN}, 50);
        main.start();

        TimeUnit.SECONDS.sleep(15);

        main.shutdown();
    }

    @Test
    void listen_test_direct_publish() throws IOException, InterruptedException {
        int port = 4445;
        int timeoutInSeconds = 30;
        new Thread(() -> startApp(port, timeoutInSeconds, DIRECT)).start();

        startListenerAndMeasureLatency(port, timeoutInSeconds);
    }

    @Test
    void listen_test_queue_publish() throws IOException, InterruptedException {
        int port = 4445;
        int timeoutInSeconds = 30;
        new Thread(() -> startApp(port, timeoutInSeconds, QUEUE)).start();

        startListenerAndMeasureLatency(port, timeoutInSeconds);
    }

    private void startApp(int port, int timeoutInSeconds, RunMode runMode) {
        try {
            Main main = new Main(port, "localhost", ERROR, 2, Element.values(), 50, runMode);
            main.start();

            TimeUnit.SECONDS.sleep(timeoutInSeconds);

            main.shutdown();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startListenerAndMeasureLatency(int port, int timeoutInSeconds) throws IOException, InterruptedException {
        DatagramChannel channel = DatagramChannel.open();
        channel.socket().bind(new InetSocketAddress("localhost", port));

        List<Long> latencyList = new CopyOnWriteArrayList<>();
        ExecutorService asyncLogger = Executors.newCachedThreadPool();

        CountDownLatch countDownLatch = new CountDownLatch(1);

        new Thread(() -> {
            try {
                LocalTime nowPlus10Secs = LocalTime.now().plusSeconds(timeoutInSeconds);
                while (true) {
                    if(LocalTime.now().isAfter(nowPlus10Secs)) {
                        countDownLatch.countDown();
                    }

                    ByteBuffer buf = ByteBuffer.allocate(70);
                    buf.clear();
                    channel.receive(buf);
                    String s = new String(buf.array(), 0, buf.position());
                    long receivedTime = new Date().getTime();
                    asyncLogger.submit(() -> {
                        long publishedTIme = Long.parseLong(s.split("publishedTime:")[1].split("}")[0]);
                        long latency = receivedTime - publishedTIme;
                        latencyList.add(latency);
                        System.out.println(receivedTime + " :: " + s + " :: " + latency);
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        boolean await = countDownLatch.await(timeoutInSeconds + 1, TimeUnit.SECONDS);

        System.out.println("Closed " + (await ? "gracefully" : "forcefully"));

        asyncLogger.shutdownNow();

        OptionalDouble averageLatency = latencyList.stream().mapToLong(a -> a).average();
        System.out.println("===========================");
        System.out.println("Total received " + latencyList.size());
        System.out.println("Average latency " + averageLatency);
        System.out.println("===========================");

        assertTrue(averageLatency.isPresent());
        assertTrue(averageLatency.getAsDouble() < 1);
    }
}