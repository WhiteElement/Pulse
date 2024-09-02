package org.whiteelement;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Pulse {
    private HttpClient client;
    private HttpRequest request;
    public Pulse(String url) throws URISyntaxException {
        this.request = HttpRequest.newBuilder()
                .GET()
                .uri(new URI(url))
                .build();
        
        this.client = HttpClient.newHttpClient();
    }
    
    public int startPulsing () {
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode();
        } catch (IOException e) {
            //TODO logging
            return -1;
        } catch (InterruptedException e) {
            //TODO logging
            return -1;
        }
    }
    
}