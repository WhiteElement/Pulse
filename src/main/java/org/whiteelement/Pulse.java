package org.whiteelement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Pulse {
    private final HttpClient client;
    private final HttpRequest request;
    private static final ConcurrentLinkedQueue<Exception> failedRequests = new ConcurrentLinkedQueue<>();
    private final Logger LOG = LogManager.getRootLogger();
    public Pulse(String url) throws URISyntaxException {
        this.request = HttpRequest.newBuilder()
                .GET()
                .uri(new URI(url))
                .build();
        
        this.client = HttpClient.newHttpClient();
    }
    
    public void startPulsing () {
        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            LOG.error("Request not successful", e);
            failedRequests.add(e);
        }
    }
    
    public void printFailedRequests() {
        if (!failedRequests.isEmpty()) {
            LOG.error(STR."\{failedRequests.size()} Requests failed:");
            
            failedRequests.forEach(LOG::error);
        }
    }
}