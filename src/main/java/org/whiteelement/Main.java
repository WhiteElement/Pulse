package org.whiteelement;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Time;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.io.FileReader;

public class Main {
    private static final List<Sequence> sequences = new ArrayList<Sequence>();
    private static short port;
    private static final Logger LOG = Logger.getLogger(Main.class.getName());
    private static final String prefix = "[  CLIENT  ]";
    private static final String[] acceptedParams = {"sequence", "port"};
    
    
    public static void main(String[] args) throws IOException, InterruptedException {
        
        // args
        // port, sequence file, which request???
        //# duration in seconds, number of clients, time between one request in ms
		//TODO: logging into log file
		//TODO: sum requests
		//TODO: endpoint dynamic
        
        var params = Arrays.stream(args).filter(param -> param.startsWith("--")).toList();
        LOG.info(STR."\{prefix} Parameters provided: \{String.join(", ", params)}");
        
        if (params.isEmpty())
            throw new IllegalArgumentException(STR."No Arguments provided. Need: \{String.join(" & ", acceptedParams)}");
        
        if (params.stream().noneMatch(x -> x.contains("sequence")) || params.stream().noneMatch(x -> x.contains("port")))
            throw new IllegalArgumentException(STR."Not all Arguments provided. Need: \{String.join(" & ", acceptedParams)}");
        
        var paramMap = mapParams(params);
        port = Short.parseShort(paramMap.get("port"));
		// TODO: new String Templates
        LOG.info(String.format(STR."\{prefix} Port: \{port} => URL: http://localhost:\{port}"));
        
        var sequenceFile = new File(paramMap.get("sequence"));

        try(BufferedReader br = new BufferedReader(new FileReader(sequenceFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#") || line.isEmpty())
                    continue;
                var sequenceParts = line.split(";");
                sequences.add(new Sequence(sequenceParts[0], sequenceParts[1], sequenceParts[2]));
            }
        } 
        
		// TODO: new String Templates
        LOG.info(STR."\{prefix} \{sequences.size()} Sequences found:");
        sequences.forEach(s -> LOG.info(STR."\{prefix} \{s.toString()}"));
            
        long timeBound;
        for(var s : sequences) {
            timeBound = System.currentTimeMillis() + (s.getDurationS() * 1000);
            while (System.currentTimeMillis() <= timeBound) {
                for (int i = 0; i < s.getNumOfClients(); i++) {
                    //send request
                    Thread.ofVirtual().start(() -> {
                        var client = HttpClient.newHttpClient();
                        try {
                            var request = HttpRequest.newBuilder()
                                    .GET()
                                    .uri(new URI("http://localhost:" + port))
                                    .build();
							//TODO sendAsync
                            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
						// TODO: new String Templates
                            LOG.info(STR."\{prefix} Request send -> returned \{response.statusCode()}");
                        } catch (URISyntaxException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }


                    });
                }
                Thread.sleep(s.getTimeBetweenRequestsMs());
            }
        }
            
    }
    
    private static HashMap<String, String> mapParams(List<String> params) {
        var hashMap = new HashMap<String, String>(2);
        for (final var param : params) {
            if (!param.contains("="))
                throw new IllegalArgumentException(STR."Parameter \{param} is not provided with '='");
            
            var noHyphen = param.replace("--", "");
            var keyAndValue = noHyphen.split("=");
            hashMap.put(keyAndValue[0], keyAndValue[1]);
        }
        return hashMap;
    }
}
