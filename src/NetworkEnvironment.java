import java.util.Random;

public class NetworkEnvironment {

    private int trafficLevel;
    private int latencyLevel;
    private int resourceLevel;

    private final Random random;

    public NetworkEnvironment() {
        random = new Random();
        reset();
    }

    // Reset the network to a new random traffic condition
    public void reset() {

        trafficLevel = random.nextInt(3);

        // Start with medium resource allocation
        resourceLevel = 1;

        updateLatency();
    }

    // Return the current state of the network
    public NetworkState getCurrentState() {

        return new NetworkState(
                trafficLevel,
                latencyLevel,
                resourceLevel
        );
    }

    // Apply the action selected by the AI
    public NetworkState applyAction(Action action) {

        switch (action) {

            case INCREASE_RESOURCES:

                if (resourceLevel < 2) {
                    resourceLevel++;
                }

                break;

            case DECREASE_RESOURCES:

                if (resourceLevel > 0) {
                    resourceLevel--;
                }

                break;

            case MAINTAIN_RESOURCES:

                // Keep the current resource level
                break;
        }

        updateLatency();

        return getCurrentState();
    }

    // Calculate latency based on traffic and available resources
    private void updateLatency() {

        /*
         * Traffic creates network pressure.
         *
         * LOW traffic:
         *     Low latency even with fewer resources.
         *
         * MEDIUM traffic:
         *     Medium resources are normally sufficient.
         *
         * HIGH traffic:
         *     More resources are required to keep latency low.
         */

        if (trafficLevel == 0) {

            // LOW traffic
            if (resourceLevel == 0) {
                latencyLevel = 1;
            }
            else {
                latencyLevel = 0;
            }

        }
        else if (trafficLevel == 1) {

            // MEDIUM traffic
            if (resourceLevel == 0) {
                latencyLevel = 2;
            }
            else if (resourceLevel == 1) {
                latencyLevel = 1;
            }
            else {
                latencyLevel = 0;
            }

        }
        else {

            // HIGH traffic
            if (resourceLevel == 0) {
                latencyLevel = 2;
            }
            else if (resourceLevel == 1) {
                latencyLevel = 2;
            }
            else {
                latencyLevel = 0;
            }
        }
    }

    // Calculate reward for the AI
    public double calculateReward(NetworkState state) {

        double reward = 0;

        int traffic = state.getTrafficLevel();
        int latency = state.getLatencyLevel();
        int resources = state.getResourceLevel();

        /*
         * -------------------------
         * LATENCY REWARD
         * -------------------------
         */

        if (latency == 0) {

            // LOW latency
            reward += 6;

        }
        else if (latency == 1) {

            // MEDIUM latency
            reward += 2;

        }
        else {

            // HIGH latency
            reward -= 6;
        }


        /*
         * -------------------------
         * RESOURCE EFFICIENCY
         * -------------------------
         */

        if (traffic == 2) {

            // HIGH traffic

            if (resources == 2) {

                // High resources are justified
                reward += 6;

            }
            else if (resources == 1) {

                reward += 1;

            }
            else {

                // Too few resources
                reward -= 6;
            }

        }
        else if (traffic == 1) {

            // MEDIUM traffic

            if (resources == 1) {

                // Balanced resource allocation
                reward += 4;

            }
            else if (resources == 2) {

                // Works, but uses more resources than necessary
                reward += 1;

            }
            else {

                // Too few resources
                reward -= 2;
            }

        }
        else {

            // LOW traffic

            if (resources == 0) {

                // Efficient resource usage
                reward += 3;

            }
            else if (resources == 1) {

                reward += 2;

            }
            else {

                // Unnecessary resource usage
                reward -= 3;
            }
        }

        return reward;
    }

    // Generate network telemetry for the GUI
    public NetworkTelemetry getTelemetry() {

        double trafficLoad;
        double latency;
        double throughput;
        double resourceUsage;


        /*
         * -------------------------
         * TRAFFIC LOAD
         * -------------------------
         */

        if (trafficLevel == 0) {

            // LOW traffic
            trafficLoad =
                    20 + random.nextDouble() * 10;

        }
        else if (trafficLevel == 1) {

            // MEDIUM traffic
            trafficLoad =
                    45 + random.nextDouble() * 10;

        }
        else {

            // HIGH traffic
            trafficLoad =
                    75 + random.nextDouble() * 10;
        }


        /*
         * -------------------------
         * LATENCY
         * -------------------------
         */

        if (latencyLevel == 0) {

            // LOW latency
            latency =
                    10 + random.nextDouble() * 5;

        }
        else if (latencyLevel == 1) {

            // MEDIUM latency
            latency =
                    30 + random.nextDouble() * 10;

        }
        else {

            // HIGH latency
            latency =
                    70 + random.nextDouble() * 10;
        }


        /*
         * -------------------------
         * THROUGHPUT
         * -------------------------
         *
         * More resources generally allow
         * higher throughput.
         *
         * High latency reduces throughput.
         */

        throughput =
                35
                + (resourceLevel * 20)
                + (trafficLevel * 5)
                - (latencyLevel * 5);


        /*
         * -------------------------
         * RESOURCE USAGE
         * -------------------------
         */

        if (resourceLevel == 0) {

            resourceUsage = 30;

        }
        else if (resourceLevel == 1) {

            resourceUsage = 60;

        }
        else {

            resourceUsage = 90;
        }


        return new NetworkTelemetry(
                trafficLoad,
                latency,
                throughput,
                resourceUsage
        );
    }

    // Set the scenario selected in the GUI
    public void setScenario(int traffic) {

        trafficLevel = traffic;

        // Start each scenario with medium resources
        resourceLevel = 1;

        updateLatency();
    }
}