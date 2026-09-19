import java.util.Random;

public class QLearningAgent {

    // 3 traffic levels × 3 latency levels × 3 resource levels
    private static final int NUMBER_OF_STATES = 27;

    private final double[][] qTable;
    private final Random random;

    // Q-learning parameters
    private final double learningRate = 0.1;
    private final double discountFactor = 0.9;

    // Exploration vs exploitation
    private double explorationRate = 0.2;

    // Training statistics
    private int totalUpdates = 0;

    public QLearningAgent() {

        qTable =
                new double[
                        NUMBER_OF_STATES
                ][Action.values().length];

        random = new Random();
    }

    /*
     * Convert the three-part network state
     * into one Q-table index.
     *
     * Example:
     * Traffic = 2
     * Latency = 1
     * Resources = 2
     *
     * index = 2*9 + 1*3 + 2 = 23
     */
    private int stateToIndex(NetworkState state) {

        return state.getTrafficLevel() * 9
                + state.getLatencyLevel() * 3
                + state.getResourceLevel();
    }

    /*
     * Choose an action using the learned policy.
     *
     * During training:
     * - Sometimes explore a random action.
     * - Otherwise use the best learned action.
     */
    public Action chooseAction(NetworkState state) {

        int stateIndex =
                stateToIndex(state);

        // Exploration
        if (random.nextDouble()
                < explorationRate) {

            Action[] actions =
                    Action.values();

            return actions[
                    random.nextInt(
                            actions.length
                    )
            ];
        }

        // Exploitation
        return getBestAction(state);
    }

    /*
     * Select the action having the
     * highest learned Q-value.
     */
    public Action getBestAction(NetworkState state) {

        int stateIndex =
                stateToIndex(state);

        double highestValue =
                qTable[stateIndex][0];

        int bestAction = 0;

        for (
                int i = 1;
                i < Action.values().length;
                i++
        ) {

            if (
                    qTable[stateIndex][i]
                    > highestValue
            ) {

                highestValue =
                        qTable[stateIndex][i];

                bestAction = i;
            }
        }

        return Action.values()[bestAction];
    }

    /*
     * Q-learning update:
     *
     * Q(s,a) = Q(s,a) +
     * α [ r + γ max Q(s',a') - Q(s,a) ]
     */
    public void updateQValue(
            NetworkState currentState,
            Action action,
            double reward,
            NetworkState nextState) {

        int currentIndex =
                stateToIndex(currentState);

        int nextIndex =
                stateToIndex(nextState);

        int actionIndex =
                action.ordinal();

        double currentQ =
                qTable[
                        currentIndex
                ][actionIndex];

        // Find maximum Q-value of next state
        double maxNextQ =
                qTable[nextIndex][0];

        for (
                int i = 1;
                i < Action.values().length;
                i++
        ) {

            if (
                    qTable[nextIndex][i]
                    > maxNextQ
            ) {

                maxNextQ =
                        qTable[nextIndex][i];
            }
        }

        // Q-learning formula
        double newQ =
                currentQ
                + learningRate
                * (
                    reward
                    + discountFactor * maxNextQ
                    - currentQ
                );

        qTable[
                currentIndex
        ][actionIndex] = newQ;

        // Record learning update
        totalUpdates++;
    }

    /*
     * Return all Q-values for the current state.
     *
     * Order:
     * 0 = DECREASE
     * 1 = MAINTAIN
     * 2 = INCREASE
     */
    public double[] getQValues(
            NetworkState state) {

        int stateIndex =
                stateToIndex(state);

        return qTable[stateIndex];
    }

    /*
     * Return the Q-value of the
     * selected best action.
     */
    public double getBestActionValue(
            NetworkState state) {

        int stateIndex =
                stateToIndex(state);

        double highestValue =
                qTable[stateIndex][0];

        for (
                int i = 1;
                i < Action.values().length;
                i++
        ) {

            if (
                    qTable[stateIndex][i]
                    > highestValue
            ) {

                highestValue =
                        qTable[stateIndex][i];
            }
        }

        return highestValue;
    }

    /*
     * Gradually reduce exploration
     * as training progresses.
     */
    public void reduceExploration() {

        explorationRate *= 0.995;

        if (explorationRate < 0.01) {

            explorationRate = 0.01;
        }
    }

    /*
     * Return current exploration rate.
     */
    public double getExplorationRate() {

        return explorationRate;
    }

    /*
     * Return total number of
     * Q-learning updates performed.
     */
    public int getTotalUpdates() {

        return totalUpdates;
    }
}