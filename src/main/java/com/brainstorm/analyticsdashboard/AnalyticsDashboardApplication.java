package com.brainstorm.analyticsdashboard;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import java.util.Properties;

@SpringBootApplication
@EnableKafkaStreams
public class AnalyticsDashboardApplication {

	public static void main(String[] args) {


		SpringApplication.run(AnalyticsDashboardApplication.class, args);
	}

}
