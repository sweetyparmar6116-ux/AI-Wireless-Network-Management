public class NetworkState {

    private int trafficLevel;
    private int latencyLevel;
    private int resourceLevel;

    public NetworkState(int trafficLevel, int latencyLevel, int resourceLevel) {
        this.trafficLevel = trafficLevel;
        this.latencyLevel = latencyLevel;
        this.resourceLevel = resourceLevel;
    }

    public int getTrafficLevel() {
        return trafficLevel;
    }

    public int getLatencyLevel() {
        return latencyLevel;
    }

    public int getResourceLevel() {
        return resourceLevel;
    }

    /*
     * The Q-learning agent only understands discrete numbers
     * (0, 1, 2). These helper methods convert those numbers into
     * human-readable labels (LOW / MEDIUM / HIGH) so the web
     * dashboard can show the discretized state to the user.
     */
    public String getTrafficLabel() {
        return levelToLabel(trafficLevel);
    }

    public String getLatencyLabel() {
        return levelToLabel(latencyLevel);
    }

    public String getResourceLabel() {
        return levelToLabel(resourceLevel);
    }

    private String levelToLabel(int level) {
        if (level == 0) return "LOW";
        if (level == 1) return "MEDIUM";
        return "HIGH";
    }

    @Override
    public String toString() {
        return "Traffic=" + trafficLevel
                + ", Latency=" + latencyLevel
                + ", Resources=" + resourceLevel;
    }
}