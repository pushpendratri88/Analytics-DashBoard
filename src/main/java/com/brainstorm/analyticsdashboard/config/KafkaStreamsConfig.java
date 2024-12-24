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
                (key, value) -> value.contains("view"),     // Branch 2: View events
                (key, value) -> value.contains("purchase"), // Branch 3: Purchase events
                (key, value) -> true                        // Default: All other events
        );

        // Process "click" events
        processClickEvents(branches[0]);
        // Process "view" events
        processViewEvents(branches[1]);
        // Process "purchase" events
        processPurchaseEvents(branches[2]);
        // Log or handle all other events
        branches[3].foreach((key, value) -> System.err.println("Unhandled event: " + value));


        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        return sourceStream;
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
                .to("processed-events", Produced.with(Serdes.String(), Serdes.String()));
    }

    private void processViewEvents(KStream<String, String> viewStream) {
        viewStream
                .mapValues(value -> value.toUpperCase())
                .selectKey((key, value) -> extractUserId(value))
                .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(1)))
                .count()
                .toStream()
                .map((key, value) -> KeyValue.pair(key.key(), String.format("{\"userId\":\"%s\", \"views\":%d}", key.key(), value)))
                .to("processed-events", Produced.with(Serdes.String(), Serdes.String()));
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
                .to("processed-events", Produced.with(Serdes.String(), Serdes.String()));
    }

    private String extractUserId(String value) {
        // Simple parsing logic to extract userId from JSON string (update as needed)
        // Assuming the value is in JSON format: {"eventId":"1","eventType":"click","timestamp":"2024-12-12T10:00:00Z","userId":"user1"}
        try {
            return value.split("\"USERID\":\"")[1].split("\"")[0];
        } catch (Exception e) {
            return "unknown";
        }
    }
}
