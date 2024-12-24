package com.brainstorm.analyticsdashboard.config;

import com.brainstorm.analyticsdashboard.service.EventProcessingService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Properties;

@Configuration
public class KafkaStreamsConfig {

    private final EventProcessingService eventProcessingService;

    public KafkaStreamsConfig(EventProcessingService eventProcessingService) {
        this.eventProcessingService = eventProcessingService;
    }
    @Bean
    public KStream<String, String> processStream() {

        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "Analytic-Dashboard-kafka-streams-app");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "13.203.157.163:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();

        // Define the source stream
        KStream<String, String> sourceStream = builder.stream("raw-events");

        // Process click events
        eventProcessingService.processEvents(sourceStream);

        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

        return sourceStream;
    }
}
