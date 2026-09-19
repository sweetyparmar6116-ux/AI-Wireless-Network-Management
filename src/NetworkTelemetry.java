public class NetworkTelemetry {

    private double trafficLoad;
    private double latency;
    private double throughput;
    private double resourceUsage;

    public NetworkTelemetry(
            double trafficLoad,
            double latency,
            double throughput,
            double resourceUsage) {

        this.trafficLoad = trafficLoad;
        this.latency = latency;
        this.throughput = throughput;
        this.resourceUsage = resourceUsage;
    }

    public double getTrafficLoad() {
        return trafficLoad;
    }

    public double getLatency() {
        return latency;
    }

    public double getThroughput() {
        return throughput;
    }

    public double getResourceUsage() {
        return resourceUsage;
    }

    @Override
    public String toString() {

        return String.format(
                "Traffic Load: %.1f%%\n" +
                "Latency: %.1f ms\n" +
                "Throughput: %.1f Mbps\n" +
                "Resource Usage: %.1f%%",
                trafficLoad,
                latency,
                throughput,
                resourceUsage
        );
    }
}
