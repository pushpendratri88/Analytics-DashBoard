package com.brainstorm.analyticsdashboard.service;

import org.apache.kafka.streams.kstream.KStream;

public interface EventService {
    void processEvent(KStream<String, String> sourceStream);
}
