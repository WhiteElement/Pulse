package org.whiteelement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.IntStream;


public class Main {
    private static final List<Sequence> sequences = new ArrayList<Sequence>();
    private static short port;
    private static final Logger LOG = Logger.getLogger(Main.class.getName());
    private static final String prefix = "[ CLIENT ]";
    private static final String[] acceptedParams = {"sequence", "port"};
    private static final List<Thread> threads = new ArrayList<Thread>(4000);
    
    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        
        // args
        // port, sequence file, which request???
        //# duration in seconds, number of clients, time between one request in ms
		//TODO: logging into log file
		//TODO: endpoint dynamic
        
        var params = Arrays.stream(args).filter(param -> param.startsWith("--")).toList();
        LOG.info(STR."\{prefix} Parameters provided: \{String.join(", ", params)}");
        
        if (params.isEmpty())
            throw new IllegalArgumentException(STR."No Arguments provided. Need: \{String.join(" & ", acceptedParams)}");
        
        if (params.stream().noneMatch(x -> x.contains("sequence")) || params.stream().noneMatch(x -> x.contains("port")))
            throw new IllegalArgumentException(STR."Not all Arguments provided. Need: \{String.join(" & ", acceptedParams)}");
        
        var paramMap = mapParams(params);
        port = Short.parseShort(paramMap.get("port"));
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
        
        LOG.info(STR."\{prefix} \{sequences.size()} Sequences found:");
        sequences.forEach(s -> LOG.info(STR."\{prefix} \{s.toString()}"));
            
        for(var s : sequences) {
            var pulse = new Pulse("http://localhost:" + port);
            var repeatTimes = s.getDurationS() * 1_000 / s.getTimeBetweenRequestsMs();
            
            for (final var _ : IntStream.range(0, repeatTimes).toArray()) {
                for (final var _ : IntStream.range(0, s.getNumOfClients()).toArray()) {
                    var t = Thread.ofVirtual().start(pulse::startPulsing);
                    threads.add(t);
                }
                Thread.sleep(s.getTimeBetweenRequestsMs());
            }
        }

        threads.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        
        sequences.forEach(s -> {
            LOG.info(STR."\{prefix} Sequences finished.");
            LOG.info(STR."\{prefix} \{threads.size()} Requests sent via \{s.getNumOfClients()} clients over \{s.getDurationS()} seconds every \{s.getTimeBetweenRequestsMs()}ms");
        });
    }
        
    private static HashMap<String, String> mapParams(List<String> params) {
        var hashMap = new HashMap<String, String>(2);
        for (final var param : params) {
            if (!param.contains("="))
                throw new IllegalArgumentException(STR."Parameter \{param} is not provided with '='");
            
            // is this .map?
            var noHyphen = param.replace("--", "");
            var keyAndValue = noHyphen.split("=");
            hashMap.put(keyAndValue[0], keyAndValue[1]);
        }
        return hashMap;
    }
}
