package com.brainstorm.analyticsdashboard.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.time.Duration;
import java.util.Properties;

@Configuration
public class KafkaStreamsConfig {
    @Bean
    public KStream<String, String> processStream() {

        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "Analytic-Dashboard-kafka-streams-app");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "13.203.157.163:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();

        // Define the source stream and Filter for "click" events
        KStream<String, String> sourceStream = builder.stream("raw-events");
        // Split the stream into multiple substreams based on event types
        KStream<String, String>[] branches = sourceStream.branch(
                (key, value) -> value.contains("click"),    // Branch 1: Click events
                (key, value) -> value.contains("view"),
                (key, value) -> value.contains("search"),// Branch 2: View events
                (key, value) -> value.contains("purchase"),
                (key, value) -> value.contains("error"),// Branch 3: Purchase events
                (key, value) -> true                        // Default: All other events
        );

        // Process "click" events
        processClickEvents(branches[0]);
        // Process "view" events
        processViewEvents(branches[1]);
        // Process "search" events
        processSearchEvents(branches[2]);
        // Process "purchase" events
        processPurchaseEvents(branches[3]);
        // Process "error" events
        processErrorEvents(branches[4]);
        // Log or handle all other events
        branches[5].foreach((key, value) -> System.err.println("Unhandled event: " + value));


        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        return sourceStream;
    }

    private void processErrorEvents(KStream<String, String> errorStream) {
        errorStream
                .mapValues(value -> value.toUpperCase())
                .selectKey((key, value) -> extractUserId(value))
                .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(1)))
                .count()
                .toStream()
                .map((key, value) -> KeyValue.pair(key.key(), String.format("{\"userId\":\"%s\", \"errors\":%d}", key.key(), value)))
                .to("error-processed-events", Produced.with(Serdes.String(), Serdes.String()));
    }

    private void processSearchEvents(KStream<String, String> searchStream) {
        searchStream
                .mapValues(value -> value.toUpperCase())
                .selectKey((key, value) -> extractUserId(value))
                .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(1)))
                .count()
                .toStream()
                .map((key, value) -> KeyValue.pair(key.key(), String.format("{\"userId\":\"%s\", \"search\":%d}", key.key(), value)))
                .to("search-processed-events", Produced.with(Serdes.String(), Serdes.String()));
    }

    private void processClickEvents(KStream<String, String> clickStream) {
        clickStream
                .mapValues(value -> value.toUpperCase())
                .selectKey((key, value) -> extractUserId(value))
                .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(1)))
                .count()
                .toStream()
                .map((key, value) -> KeyValue.pair(key.key(), String.format("{\"userId\":\"%s\", \"clicks\":%d}", key.key(), value)))
                .to("click-processed-events", Produced.with(Serdes.String(), Serdes.String()));
    }

    private void processViewEvents(KStream<String, String> viewStream) {
        viewStream
                .mapValues(value ->
                        value.toUpperCase())
                .selectKey((key, value) -> extractUserId(value))
                .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(1)))
                .count()
                .toStream()
                .map((key, value) -> KeyValue.pair(key.key(), String.format("{\"userId\":\"%s\", \"views\":%d}", key.key(), value)))
                .to("view-processed-events", Produced.with(Serdes.String(), Serdes.String()));
    }

    private void processPurchaseEvents(KStream<String, String> purchaseStream) {
        purchaseStream
                .mapValues(value -> value.toUpperCase())
                .selectKey((key, value) -> extractUserId(value))
                .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(1)))
                .count()
                .toStream()
                .map((key, value) -> KeyValue.pair(key.key(), String.format("{\"userId\":\"%s\", \"purchases\":%d}", key.key(), value)))
                .to("purchase-processed-events", Produced.with(Serdes.String(), Serdes.String()));
    }

    private String extractUserId(String value) {
        // Simple parsing logic to extract userId from JSON string (update as needed)
        // Assuming the value is in JSON format: {"eventId":"1","eventType":"click","timestamp":"2024-12-12T10:00:00Z","userId":"user1"}
        try {
            return value.split("\"USERID\"")[1].split("\"")[1];
        } catch (Exception e) {
            return "unknown";
        }
    }
}
