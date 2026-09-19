import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Main.java
 *
 * This is the Java backend for the "AI-Driven Closed-Loop Wireless
 * Resource Management" prototype.
 *
 * What it does:
 *   1. Trains a Q-Learning agent (offline, at startup) against a
 *      simulated wireless network environment.
 *   2. Starts a small built-in HTTP server (no external frameworks
 *      required) that serves the web/ dashboard files.
 *   3. Exposes one API endpoint, /api/run-loop, which performs a
 *      single, real closed-loop control cycle:
 *
 *         Telemetry -> State -> Q-Learning Controller -> Action
 *         -> Environment -> New Telemetry -> Reward -> Q-table update
 *
 *      and returns the full result as JSON so the browser dashboard
 *      can visualise every stage of the loop.
 *
 * This project is a simulation/prototype only. It does not connect
 * to, or control, any real Wi-Fi hardware, router or access point.
 */
public class Main {

    private static final int PORT = 8081;
    private static final String WEB_ROOT = "web";

    // Offline training configuration (run once, before the server starts)
    private static final int TRAINING_EPISODES = 5000;
    private static final int STEPS_PER_EPISODE = 20;

    private static NetworkEnvironment environment;
    private static QLearningAgent agent;

    public static void main(String[] args) throws IOException {

        environment = new NetworkEnvironment();
        agent = new QLearningAgent();

        System.out.println("Training Q-Learning controller offline...");
        trainAgent();
        System.out.println(
                "Training complete. Total Q-value updates so far: "
                + agent.getTotalUpdates()
        );

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // API: runs one closed-loop control cycle for a chosen scenario
        server.createContext("/api/run-loop", new RunLoopHandler());

        // Serves index.html, style.css, script.js from the web/ folder
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("=================================================");
        System.out.println(" AI-DRIVEN CLOSED-LOOP WIRELESS RESOURCE MANAGEMENT");
        System.out.println(" Server running at: http://localhost:" + PORT);
        System.out.println(" Open that address in your browser to view the dashboard.");
        System.out.println("=================================================");
    }

    /**
     * Offline training loop. This is standard Q-Learning: the agent
     * explores the simulated environment for many episodes, updating
     * its Q-table after every step, so that by the time the server
     * starts, the controller already has a learned policy to use for
     * the live demonstration.
     */
    private static void trainAgent() {

        for (int episode = 1; episode <= TRAINING_EPISODES; episode++) {

            environment.reset();

            for (int step = 0; step < STEPS_PER_EPISODE; step++) {

                NetworkState currentState = environment.getCurrentState();

                Action action = agent.chooseAction(currentState);

                NetworkState nextState = environment.applyAction(action);

                double reward = environment.calculateReward(nextState);

                agent.updateQValue(currentState, action, reward, nextState);
            }

            agent.reduceExploration();
        }
    }

    /**
     * Runs exactly one closed-loop control cycle for the given
     * scenario and returns the result as a JSON string. This is the
     * method the frontend is really asking for whenever it clicks
     * "RUN CLOSED-LOOP CONTROL". The steps below match the loop
     * described in the paper's closed-loop framework:
     *
     *   telemetry -> state -> AI controller -> action -> environment
     *   -> new telemetry -> reward -> controller update
     *
     * The method is synchronized because the Q-table and the
     * simulated environment are shared, mutable state; only one
     * demo run should touch them at a time.
     */
    private static synchronized String runClosedLoopCycle(
            String scenarioName,
            int trafficLevel
    ) {

        // The chosen scenario sets the simulated traffic condition
        environment.setScenario(trafficLevel);

        // 1. TELEMETRY + STATE (before the AI acts)
        NetworkState stateBefore = environment.getCurrentState();
        NetworkTelemetry telemetryBefore = environment.getTelemetry();

        // 2. AI CONTROL ENGINE: read the learned Q-values, pick the
        //    best known action for this state (this uses the policy
        //    learned during offline training, not a random guess)
        double[] qValues = agent.getQValues(stateBefore);
        Action action = agent.getBestAction(stateBefore);

        // 3. RESOURCE CONTROL ACTION applied to the simulated network
        NetworkState stateAfter = environment.applyAction(action);

        // 4. FEEDBACK: reward calculated from the resulting state
        double reward = environment.calculateReward(stateAfter);

        // 5. Q-TABLE UPDATE: the controller learns from this outcome,
        //    even during a live demo, so the loop is genuinely closed
        agent.updateQValue(stateBefore, action, reward, stateAfter);

        // 6. NEW TELEMETRY after the action was applied
        NetworkTelemetry telemetryAfter = environment.getTelemetry();

        return buildJsonResponse(
                scenarioName,
                telemetryBefore,
                stateBefore,
                qValues,
                action,
                reward,
                telemetryAfter,
                stateAfter
        );
    }

    /**
     * Builds the JSON payload sent back to the browser. A tiny
     * hand-written builder is enough here because every field is a
     * known number, label or enum name -- there is no need to pull
     * in an external JSON library for a prototype this size.
     */
    private static String buildJsonResponse(
            String scenarioName,
            NetworkTelemetry before,
            NetworkState stateBefore,
            double[] qValues,
            Action action,
            double reward,
            NetworkTelemetry after,
            NetworkState stateAfter
    ) {

        StringBuilder json = new StringBuilder();

        json.append("{");

        json.append("\"scenario\":\"").append(scenarioName).append("\",");

        json.append("\"telemetryBefore\":{")
                .append("\"trafficLoad\":").append(round(before.getTrafficLoad())).append(",")
                .append("\"latency\":").append(round(before.getLatency())).append(",")
                .append("\"throughput\":").append(round(before.getThroughput())).append(",")
                .append("\"resourceUtilization\":").append(round(before.getResourceUsage()))
                .append("},");

        json.append("\"stateBefore\":{")
                .append("\"traffic\":\"").append(stateBefore.getTrafficLabel()).append("\",")
                .append("\"latency\":\"").append(stateBefore.getLatencyLabel()).append("\",")
                .append("\"resource\":\"").append(stateBefore.getResourceLabel()).append("\"")
                .append("},");

        json.append("\"controller\":{")
                .append("\"name\":\"Q-Learning\",")
                .append("\"qDecrease\":").append(round(qValues[0])).append(",")
                .append("\"qMaintain\":").append(round(qValues[1])).append(",")
                .append("\"qIncrease\":").append(round(qValues[2])).append(",")
                .append("\"explorationRate\":").append(round(agent.getExplorationRate())).append(",")
                .append("\"trainingEpisodes\":").append(TRAINING_EPISODES).append(",")
                .append("\"totalUpdates\":").append(agent.getTotalUpdates())
                .append("},");

        json.append("\"action\":\"").append(action.name()).append("\",");
        json.append("\"actionExplanation\":\"")
                .append(explanationFor(action)).append("\",");

        json.append("\"telemetryAfter\":{")
                .append("\"trafficLoad\":").append(round(after.getTrafficLoad())).append(",")
                .append("\"latency\":").append(round(after.getLatency())).append(",")
                .append("\"throughput\":").append(round(after.getThroughput())).append(",")
                .append("\"resourceUtilization\":").append(round(after.getResourceUsage()))
                .append("},");

        json.append("\"stateAfter\":{")
                .append("\"traffic\":\"").append(stateAfter.getTrafficLabel()).append("\",")
                .append("\"latency\":\"").append(stateAfter.getLatencyLabel()).append("\",")
                .append("\"resource\":\"").append(stateAfter.getResourceLabel()).append("\"")
                .append("},");

        json.append("\"reward\":").append(round(reward)).append(",");
        json.append("\"feedbackMessage\":\"Feedback received. Controller Q-table updated.\"");

        json.append("}");

        return json.toString();
    }

    /**
     * Short, human-readable reason shown next to the resource action
     * so the evaluator immediately understands why the AI chose it.
     */
    private static String explanationFor(Action action) {

        switch (action) {

            case INCREASE_RESOURCES:
                return "High network demand detected.";

            case DECREASE_RESOURCES:
                return "Resource allocation can be reduced.";

            default:
                return "Network conditions are stable.";
        }
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    // =====================================================================
    // HTTP HANDLERS
    // =====================================================================

    /**
     * Handles GET /api/run-loop?scenario=LOW|MEDIUM|HIGH
     * Runs one closed-loop cycle and returns it as JSON.
     */
    static class RunLoopHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            exchange.getResponseHeaders().set("Content-Type", "application/json");

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Only GET is supported.\"}");
                return;
            }

            try {

                Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
                String scenarioParam = params.getOrDefault("scenario", "MEDIUM").toUpperCase();

                int trafficLevel;

                switch (scenarioParam) {
                    case "LOW":
                        trafficLevel = 0;
                        break;
                    case "HIGH":
                        trafficLevel = 2;
                        break;
                    default:
                        scenarioParam = "MEDIUM";
                        trafficLevel = 1;
                        break;
                }

                String json = runClosedLoopCycle(scenarioParam, trafficLevel);

                sendJson(exchange, 200, json);

            } catch (Exception e) {

                // Keep the server alive even if something unexpected happens
                sendJson(
                        exchange,
                        500,
                        "{\"error\":\"Backend error while running the closed-loop cycle.\"}"
                );
            }
        }

        private void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {

            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, bytes.length);

            OutputStream responseBody = exchange.getResponseBody();
            responseBody.write(bytes);
            responseBody.close();
        }
    }

    /**
     * Serves the static dashboard files (index.html, style.css,
     * script.js) from the web/ folder next to this program.
     */
    static class StaticFileHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            String requestedPath = exchange.getRequestURI().getPath();

            if (requestedPath.equals("/")) {
                requestedPath = "/index.html";
            }

            File file = new File(WEB_ROOT, requestedPath);

            if (!file.exists() || file.isDirectory()) {

                String notFound = "404 - File not found: " + requestedPath;
                byte[] bytes = notFound.getBytes(StandardCharsets.UTF_8);

                exchange.sendResponseHeaders(404, bytes.length);

                OutputStream responseBody = exchange.getResponseBody();
                responseBody.write(bytes);
                responseBody.close();

                return;
            }

            exchange.getResponseHeaders().set("Content-Type", contentTypeFor(file.getName()));

            byte[] bytes = Files.readAllBytes(file.toPath());
            exchange.sendResponseHeaders(200, bytes.length);

            OutputStream responseBody = exchange.getResponseBody();
            responseBody.write(bytes);
            responseBody.close();
        }

        private String contentTypeFor(String fileName) {

            if (fileName.endsWith(".html")) return "text/html";
            if (fileName.endsWith(".css")) return "text/css";
            if (fileName.endsWith(".js")) return "application/javascript";

            return "text/plain";
        }
    }

    /**
     * Very small query-string parser (?scenario=HIGH -> {scenario: HIGH}).
     * Written by hand since the project intentionally avoids extra
     * libraries/frameworks for what is a one-line lookup.
     */
    private static Map<String, String> parseQuery(String query) {

        Map<String, String> result = new HashMap<>();

        if (query == null || query.isEmpty()) {
            return result;
        }

        for (String pair : query.split("&")) {

            String[] keyValue = pair.split("=", 2);

            if (keyValue.length == 2) {
                try {
                    String key = URLDecoder.decode(keyValue[0], "UTF-8");
                    String value = URLDecoder.decode(keyValue[1], "UTF-8");
                    result.put(key, value);
                } catch (Exception ignored) {
                    // Malformed query parameter - ignore it and use defaults
                }
            }
        }

        return result;
    }
}
