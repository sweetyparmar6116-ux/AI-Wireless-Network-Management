# AI-Driven Closed-Loop Wireless Resource Management

A small academic prototype for LO6.1 / LO6.2: *"Design and develop AI applications in
real world scenarios using AI tools and effectively present the solution."*

It implements **one focused, defensible part** of the closed-loop AI/ML wireless
network management concept described in the research paper *"Intelligent Wireless
Network Management Through AIML-Driven Closed-Loop Control and Feedback Mechanisms"*
(IC-EETA 2025) — the telemetry → AI controller → resource action → network response →
feedback loop — implemented with a genuine Q-Learning agent.

**This is a simulation/prototype.** It does not connect to, or control, any real
Wi-Fi router, access point, or radio hardware.

---

## 1. What this project actually does

```
NETWORK MONITORING → AI CONTROL ENGINE → RESOURCE ACTION → NETWORK RESPONSE → FEEDBACK
        ↑______________________________________________________________________|
```

1. A simulated wireless environment produces telemetry (traffic load, latency,
   throughput, resource utilization).
2. That telemetry is discretized into a state (LOW / MEDIUM / HIGH per metric).
3. A **Q-Learning agent** (trained offline for 5,000 episodes, and updated live on
   every demo run) chooses one of three resource actions.
4. The simulated environment applies the action and produces new telemetry.
5. A reward is calculated from the outcome and fed back into the agent's Q-table,
   closing the loop before the next decision.

## 2. Technology

| Layer     | Technology                                                        |
|-----------|--------------------------------------------------------------------|
| Frontend  | HTML, CSS, vanilla JavaScript                                       |
| Backend   | Java (`com.sun.net.httpserver.HttpServer`, part of the JDK — no frameworks) |
| AI        | Q-Learning, implemented from scratch in `QLearningAgent.java`       |

No Python, Node.js, Spring Boot, Maven, or database is used, so the whole project
compiles and runs with just a JDK.

## 3. Project structure

```
AI-Wireless-Network-Management/
├── src/
│   ├── Main.java              Backend HTTP server + closed-loop orchestration
│   ├── Action.java            The 3 possible resource-control actions
│   ├── NetworkEnvironment.java Simulated wireless environment + reward logic
│   ├── NetworkState.java      Discretized state (traffic / latency / resources)
│   ├── NetworkTelemetry.java  Simulated raw telemetry values
│   └── QLearningAgent.java    Q-table, ε-greedy policy, Q-value update
├── web/
│   ├── index.html
│   ├── style.css
│   └── script.js
├── run.bat
└── README.md
```

## 4. How to run it

**Windows:** double-click `run.bat`. It compiles the Java backend, starts the
server (which trains the Q-Learning agent first — this takes a couple of seconds),
and opens `http://localhost:8080` in your browser.

**Manually (any OS with a JDK installed):**

```
cd AI-Wireless-Network-Management
javac -d out src\*.java      (or src/*.java on macOS/Linux)
java -cp out Main
```

Then open `http://localhost:8080` in a browser.

If the dashboard shows "backend unreachable", the Java server did not start —
check the console window for a compilation or port-binding error before retrying.

## 5. Using the dashboard

1. Choose a scenario: **Low**, **Medium**, or **High** network demand.
2. Click **Run AI Control**.
3. The dashboard updates in place:

   | Section | What it shows |
   |---------|----------------|
   | Network Telemetry | Simulated traffic load, latency, throughput and resource utilization, plus the current network condition |
   | AI Control | The discretized state the Q-Learning controller received, the action it selected, and a one-line reason |
   | Network Response | Before / after latency, throughput and resource utilization once the action was applied |
   | Feedback | The reward calculated from that outcome, and confirmation that the controller was updated |

   The page is a normal application layout, not a diagram: telemetry, decision,
   response and feedback are simply read top to bottom. Q-Learning configuration
   is reduced to a single line under *About this prototype*.

Every run also performs a real Q-table update on the server, so the controller is
slightly different after each click — the loop is genuinely closed, not replayed
from a script. (The raw update counter is available in the API response from
`/api/run-loop` if an evaluator asks to see it.)

---

## 6. Mapping to Research Paper

This project implements a focused prototype of the paper's closed-loop AI/ML
wireless network management concept. Instead of reproducing the complete
multi-tier wireless architecture described in the paper (edge/regional/cloud
tiers, Prometheus/Kafka telemetry pipelines, federated learning, hybrid PID/MPC
safety filters, etc.), the prototype concentrates on the **core control loop**:
network telemetry, AI-based control, resource allocation, simulated network
response, and feedback.

| Paper concept                                   | Project implementation                                                     |
|--------------------------------------------------|------------------------------------------------------------------------------|
| Network telemetry / KPIs                          | Simulated traffic load, latency, throughput, and resource utilization        |
| AI/ML controller (paper discusses DQN, DDPG, PPO, bandits, etc.) | A Q-Learning agent (`QLearningAgent.java`)                       |
| Control action (paper's action space includes power, channel, scheduling, handover) | Increase / Maintain / Decrease resource allocation only |
| Network response / actuator                       | `NetworkEnvironment.java` (a simulated environment, not real hardware)       |
| Feedback loop / reward                            | A reward calculated from latency and resource efficiency, fed back to update the Q-table |
| Closed-loop operation                             | Telemetry → Controller → Action → Response → Feedback, executed end-to-end on every dashboard run |
