package com.brainstorm.analyticsdashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@SpringBootApplication
@EnableKafkaStreams
public class AnalyticsDashboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnalyticsDashboardApplication.class, args);
	}

}
