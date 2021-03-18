package com.example.io;

import com.example.util.logging.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public abstract class Publisher {
    protected final Logger logger;
    protected static final AtomicLong totalPublishedCounter = new AtomicLong(0);
    protected static final Map<String, AtomicLong> counterMap = new ConcurrentHashMap<>();

    private final int port;
    private final String host;
    private final DatagramChannel channel;

    public Publisher(int port, String host, Logger logger) throws IOException {
        this.port = port;
        this.host = host;
        this.logger = logger;
        channel = DatagramChannel.open();
        channel.socket().bind(new InetSocketAddress(50000));
        channel.configureBlocking(false);
    }

    public abstract void start();

    public void publish(String data) {
        try {
            ByteBuffer buf = ByteBuffer.allocate(100);
            buf.clear();
            buf.put(data.getBytes());
            buf.flip();

            int bytesSent = channel.send(buf, new InetSocketAddress(host, port));
            logger.debug(LocalDateTime.now(), data + " published with byte " + bytesSent);
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"));
            AtomicLong counter = counterMap.computeIfAbsent(time, __ -> new AtomicLong(0));
            counter.incrementAndGet();
            counterMap.put(time, counter);

            totalPublishedCounter.incrementAndGet();

            TimeUnit.MILLISECONDS.sleep(10);

        } catch (Throwable e) {
            logger.error(LocalDateTime.now(), "Exception occurred while retrieving data from queue", e);
        }
    }

    public Map<String, AtomicLong> getCounterMap() {
        return Collections.unmodifiableMap(counterMap);
    }

    public AtomicLong getTotalPublishedCounter() {
        return totalPublishedCounter;
    }

    public void close() throws IOException {
        channel.close();
    }
}
