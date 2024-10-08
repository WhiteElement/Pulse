package org.whiteelement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.stream.IntStream;


public class Main {
    private static final List<Sequence> sequences = new ArrayList<>();
    private static final Logger LOG = LogManager.getLogger();
    private static final String[] acceptedParams = {"sequence", "url"};
    private static final List<Thread> threads = new ArrayList<>(4000);
    
    public static void main(String[] args) throws Exception {
        var params = Arrays.stream(args).filter(param -> param.startsWith("--")).toList();
        LOG.info(STR."Parameters provided: \{String.join(", ", params)}");
        
        if (params.isEmpty())
            throw new IllegalArgumentException(STR."No Arguments provided. Need: \{String.join(" & ", acceptedParams)}");
        
        if (params.stream().noneMatch(x -> x.contains("sequence")) || params.stream().noneMatch(x -> x.contains("url")))
            throw new IllegalArgumentException(STR."Not all Arguments provided. Need: \{String.join(" & ", acceptedParams)}");
        
        var paramMap = mapParams(params);
        String url = paramMap.get("url");
        
        var sequenceFile = new File(paramMap.get("sequence"));
        try (BufferedReader br = new BufferedReader(new FileReader(sequenceFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#") || line.isEmpty())
                    continue;
                var sequenceParts = line.split(";");
                sequences.add(new Sequence(sequenceParts[0], sequenceParts[1], sequenceParts[2]));
            }
        } catch (IndexOutOfBoundsException e) {
            String error = "Sequence not provided in the right format format. Expected: semicolon separated (duration in seconds, number of clients, time " +
                    "between requests in ms)";
           LOG.fatal(error);
           throw new Exception(error);
        }
        
        LOG.info(STR."\{sequences.size()} Sequences found:");
        sequences.forEach(s -> LOG.info(STR."\{s.toString()}"));

        var pulse = new Pulse(url);
        for(var s : sequences) {
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
            LOG.info("Sequences finished.");
            LOG.info(STR."\{threads.size()} Requests sent via \{s.getNumOfClients()} clients over \{s.getDurationS()} seconds every \{s.getTimeBetweenRequestsMs()}ms");
        });
        
        pulse.printFailedRequests();
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
