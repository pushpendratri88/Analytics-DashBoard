package com.brainstorm.analyticsdashboard.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "events")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ElasticEvent {
    @Id
    private String id;
    private String eventType;
    private String timestamp;
    private String userId;
}
