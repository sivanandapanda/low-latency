package com.example.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class SocketPublisher {

    private final int port;
    private final String host;
    private final DatagramChannel channel;

    private static final Map<String, AtomicLong> counterMap = new ConcurrentHashMap<>();

    public SocketPublisher(int port, String host) throws IOException {
        this.port = port;
        this.host = host;
        channel = DatagramChannel.open();
        channel.socket().bind(new InetSocketAddress(port));
        channel.configureBlocking(false);
    }

    public void publish(String data) throws IOException {
        try {
            ByteBuffer buf = ByteBuffer.allocate(70);
            buf.clear();
            buf.put(data.getBytes());
            buf.flip();

            /*int bytesSent =*/
            channel.send(buf, new InetSocketAddress(host, port));
            //System.out.println(LocalTime.now() + " - " + Thread.currentThread().getName() + " : " +data + " published with byte " + bytesSent);
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"));
            AtomicLong counter = counterMap.computeIfAbsent(time, __ -> new AtomicLong(0));
            counter.incrementAndGet();
            counterMap.put(time, counter);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public Map<String, AtomicLong> getCounterMap() {
        return Collections.unmodifiableMap(counterMap);
    }

    public void close() throws IOException {
        channel.close();
    }
}
