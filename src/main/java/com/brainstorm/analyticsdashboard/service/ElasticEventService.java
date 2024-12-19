//package com.brainstorm.analyticsdashboard.service;
//
//import com.brainstorm.analyticsdashboard.data.ElasticEvent;
//import com.brainstorm.analyticsdashboard.repository.ElasticEventRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.Optional;
//
//@Service
//public class ElasticEventService {
//    @Autowired
//    private ElasticEventRepository elasticEventRepository;
//
//    public ElasticEvent saveEvent(ElasticEvent event) {
//        return elasticEventRepository.save(event);
//    }
//
//    public Optional<ElasticEvent> getEventById(String id) {
//        return elasticEventRepository.findById(id);
//    }
//
//    public Iterable<ElasticEvent> getAllEvents() {
//        return elasticEventRepository.findAll();
//    }
//
//    public void deleteEventById(String id) {
//        elasticEventRepository.deleteById(id);
//    }
//}
