package com.dehr;

public class Pulse {

    private String key;
    private int pulse;
    private long timestamp;

    public Pulse(String key, int pulse, long timestamp) {
        this.key = key;
        this.pulse = pulse;
        this.timestamp = timestamp;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getPulse() {
        return pulse;
    }

    public void setPulse(int pulse) {
        this.pulse = pulse;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Pulse{" +
                "key='" + key + '\'' +
                ", pulse=" + pulse +
                ", timestamp=" + timestamp +
                '}';
    }
}
