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

//        sourceStream
//                .foreach((key, value) -> {
//                    if(value.contains("click")){
//                        clickEventService.processEvent(sourceStream);
//                    }
//                    else if(value.contains("view")){
//                        errorEventService.processEvent(sourceStream);
//                    }
//                    else if(value.contains("search")){
//                        errorEventService.processEvent(sourceStream);
//                    }
//                    else if(value.contains("purchase")){
//                        errorEventService.processEvent(sourceStream);
//                    }
//                    else if(value.contains("error")){
//                        errorEventService.processEvent(sourceStream);
//                    }
//                    else if(value.contains("logout")){
//                        errorEventService.processEvent(sourceStream);
//                    }
//                    else {
//                        System.err.println("Unhandled event type: " + value);
//                    }
//                });

        KStream<String, String>[] branches = sourceStream.branch(
                (key, value) -> value != null && value.contains("click"),   // Click events
                (key, value) -> value != null && value.contains("view"),    // View events
                (key, value) -> value != null && value.contains("search"),  // Search events
                (key, value) -> value != null && value.contains("purchase"),// Purchase events
                (key, value) -> value != null && value.contains("error"),   // Error events
                (key, value) -> value != null && value.contains("logout"),  // Logout events
                (key, value) -> true                                        // Default branch for unhandled events
        );

        // Process each branch with the corresponding service
        branches[0].peek((key, value) -> clickEventService.processEvent(branches[0]));
        branches[1].peek((key, value) -> viewEventService.processEvent(branches[1]));
        branches[2].peek((key, value) -> searchEventService.processEvent(branches[2]));
        branches[3].peek((key, value) -> purchaseEventService.processEvent(branches[3]));
        branches[4].peek((key, value) -> errorEventService.processEvent(branches[4]));
        branches[5].peek((key, value) -> logoutEventService.processEvent(branches[5]));
    }
}
