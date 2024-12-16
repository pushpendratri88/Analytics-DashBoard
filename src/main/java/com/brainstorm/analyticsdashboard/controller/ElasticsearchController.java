package com.brainstorm.analyticsdashboard.controller;

import com.brainstorm.analyticsdashboard.data.ElasticEvent;
import com.brainstorm.analyticsdashboard.service.ElasticEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/elastic/events")
public class ElasticsearchController {
    @Autowired
    private ElasticEventService elasticEventService;

    @PostMapping(value = "/createEvent")
    public ElasticEvent createEvent(@RequestBody ElasticEvent event) {
        return elasticEventService.saveEvent(event);
    }

    @GetMapping("/getEventById/{id}")
    public Optional<ElasticEvent> getEventById(@PathVariable String id) {
        return elasticEventService.getEventById(id);
    }

    @GetMapping(value = "/getAllEvents")
    public Iterable<ElasticEvent> getAllEvents() {
        return elasticEventService.getAllEvents();
    }

    @DeleteMapping("/deleteEvent/{id}")
    public String deleteEvent(@PathVariable String id) {
        elasticEventService.deleteEventById(id);
        return "Event deleted with ID: " + id;
    }
}
