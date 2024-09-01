package org.whiteelement;

public class Sequence {
    private int durationS;
    private int numOfClients;
    private int timeBetweenRequestsMs;
    public Sequence(String durationS, String numOfClients, String timeBetweenRequests) {
       this.durationS = Integer.parseInt(durationS);
       this.numOfClients = Integer.parseInt(numOfClients);
       this.timeBetweenRequestsMs = Integer.parseInt(timeBetweenRequests); 
    }

    public int getDurationS() {
        return durationS;
    }

    public int getNumOfClients() {
        return numOfClients;
    }

    public int getTimeBetweenRequestsMs() {
        return timeBetweenRequestsMs;
    }

    @Override
    public String toString() {
        return "Sequence{" +
                "durationS=" + durationS +
                ", numOfClients=" + numOfClients +
                ", timeBetweenRequests=" + timeBetweenRequestsMs +
                '}';
    }
}
