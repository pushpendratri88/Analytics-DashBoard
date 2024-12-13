package com.brainstorm.analyticsdashboard.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
        KStream<String, String> sourceStream = builder.stream("raw-events");
        KStream<String, String> transformedStream = sourceStream
                .filter((key, value) -> value.contains("click"))
                .mapValues(value -> value.toUpperCase());
        transformedStream.to("processed-events");

        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.start();
        //printing topology
        System.out.println(streams.toString());

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

        KStream<String, String> stream = builder.stream("raw-events");
        KStream<String, String> processedStream = stream
                .filter((key, value) -> value.contains("click"))
                .mapValues(value -> value.toUpperCase());

        processedStream.to("processed-events", Produced.with(Serdes.String(), Serdes.String()));
        return stream;
    }
}
