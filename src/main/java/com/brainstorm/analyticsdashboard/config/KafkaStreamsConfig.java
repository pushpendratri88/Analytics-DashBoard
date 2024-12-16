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
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "my-kafka-streams-app");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();

        // Define the source stream and Filter for "click" events
        KStream<String, String> sourceStream = builder.stream("raw-events");
        KStream<String, String> transformedStream = sourceStream
                .filter((key, value) -> value.contains("click"))
                .mapValues(value -> value.toUpperCase());

        // Map userId as the key for aggregation
        KStream<String, String> userClicks = transformedStream
                .selectKey((key, value) -> extractUserId(value));

        // Aggregate the number of clicks per user
        KTable<Windowed<String>, Long> clickCounts = userClicks
                .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(1)))
                .count();

        clickCounts.toStream()
                .map((key, value) -> KeyValue.pair(key.key(), "EventId : " + key.key() + ", Clicks: " + value))
                .to("processed-events", Produced.with(Serdes.String(), Serdes.String()));
      //  transformedStream.to("processed-events");

        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.start();
        //printing topology
        System.out.println(streams.toString());
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        return sourceStream;
    }

    private String extractUserId(String value) {
        // Simple parsing logic to extract userId from JSON string (update as needed)
        // Assuming the value is in JSON format: {"eventId":"1","eventType":"click","timestamp":"2024-12-12T10:00:00Z","userId":"user1"}
        try {
            return value.split("\"")[3];
        } catch (Exception e) {
            return "unknown";
        }
    }
}
