package com.example.util.lmax;

import com.example.model.lmax.ValueEvent;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

public class LmaxUtils {

    public static Disruptor<ValueEvent<String>> createDisruptor() {
        DaemonThreadFactory threadFactory = DaemonThreadFactory.INSTANCE;
        BusySpinWaitStrategy waitStrategy = new BusySpinWaitStrategy();

        EventFactory<ValueEvent<String>> EVENT_FACTORY = ValueEvent::new;

        return new Disruptor<>(EVENT_FACTORY, 64, threadFactory, ProducerType.MULTI, waitStrategy);
    }
}
