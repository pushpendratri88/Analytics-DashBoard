package com.brainstorm.analyticsdashboard.repository;

import com.brainstorm.analyticsdashboard.data.ElasticEvent;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticEventRepository extends ElasticsearchRepository<ElasticEvent, String> {

}
