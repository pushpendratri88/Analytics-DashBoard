package com.brainstorm.analyticsdashboard.service;

import org.apache.kafka.streams.kstream.KStream;
import org.springframework.stereotype.Service;


@Service
public class EventProcessingService {

    EventService clickEventService;
    EventService viewEventService;
    EventService searchEventService;
    EventService purchaseEventService;
    EventService errorEventService;
    EventService logoutEventService;


    public EventProcessingService(EventService clickEventService, EventService viewEventService, EventService searchEventService, EventService purchaseEventService, EventService errorEventService, EventService logoutEventService) {
        this.clickEventService = clickEventService;
        this.viewEventService = viewEventService;
        this.searchEventService = searchEventService;
        this.purchaseEventService = purchaseEventService;
        this.errorEventService = errorEventService;
        this.logoutEventService = logoutEventService;
    }

    public void processEvents(KStream<String, String> sourceStream) {

        sourceStream
                .map((key, value) -> {
                    if(value.contains("click")){
                        clickEventService.processEvent(sourceStream);
                    }
                    else if(value.contains("view")){
                        errorEventService.processEvent(sourceStream);
                    }
                    else if(value.contains("search")){
                        errorEventService.processEvent(sourceStream);
                    }
                    else if(value.contains("purchase")){
                        errorEventService.processEvent(sourceStream);
                    }
                    else if(value.contains("error")){
                        errorEventService.processEvent(sourceStream);
                    }
                    else if(value.contains("logout")){
                        errorEventService.processEvent(sourceStream);
                    }
                    return null;
                });
    }
}