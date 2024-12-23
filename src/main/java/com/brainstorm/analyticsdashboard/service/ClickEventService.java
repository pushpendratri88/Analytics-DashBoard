package com.brainstorm.analyticsdashboard.service;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class ClickEventService implements EventService{
    @Override
    public void processEvent(KStream<String, String> sourceStream) {
        // Filter for "click" events and transform
        KStream<String, String> transformedStream = sourceStream
                .filter((key, value) -> value.contains("click"))
                .mapValues(value -> value.toUpperCase());


        // Map userId as the key for aggregation
        KStream<String, String> userClicks = transformedStream
                .selectKey((key, value) -> extractUserId(value));

        // Aggregate the number of clicks per user
        KTable<Windowed<String>, Long> clickCounts = userClicks
                .groupByKey()
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofDays(1)))
                .count();

        // Send to output topic
        clickCounts.toStream()
                .map((key, value) -> KeyValue.pair(key.key(), String.format("{\"eventId\":\"%s\", \"clicks\":%d}", key.key(), value)))
                .to("processed-click-events");
    }

    private String extractUserId(String value) {
        try {
            return value.split("\"")[3]; // Adjust this to your JSON parsing logic
        } catch (Exception e) {
            return "unknown";
        }
    }
}
