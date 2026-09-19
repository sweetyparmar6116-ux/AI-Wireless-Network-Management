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

**What is explicitly *not* implemented**, and is not claimed to be: the paper's
multi-horizon (fast/medium/slow) control hierarchy, PID/MPC hybrid safety
filtering, transmit power / channel assignment / scheduling / handover-threshold
actions, federated/distributed learning, telemetry pipelines built on
Prometheus/Kafka, sandboxed validation and rollback tooling, or deep-RL methods
(DQN/DDPG/PPO). This prototype is a small, single-loop illustration of the
concept the paper proposes at a much larger production scale.

---

## 7. Rubric Alignment

### Rubric 1 — Implementation of a small part of the system using an AI tool (5M)

- Studied the research paper's closed-loop AI/ML wireless network management
  framework.
- Selected one clearly bounded component to implement: the telemetry → AI
  controller → resource action → response → feedback loop.
- Implemented a genuine Q-Learning controller (Q-table, ε-greedy exploration,
  the standard Q-learning update rule, and exploration decay).
- Simulated wireless network telemetry (traffic, latency, throughput, resource
  utilization) with randomised variation, not fixed numbers.
- Implemented three resource-control actions applied to a simulated environment.
- Implemented a reward function that scores latency and resource-allocation
  efficiency.
- Wired all of the above into one end-to-end closed-loop prototype, developed
  with AI-assisted development, exposed through a Java backend API and a
  browser dashboard.

### Rubric 2 — Demonstration (5M)

The dashboard is built specifically to walk through this sequence live:

1. Select a network scenario (Low / Medium / High demand).
2. Show the simulated telemetry for that scenario.
3. Show the discretized network state used by the controller.
4. Execute the Q-Learning controller and show its Q-values.
5. Show the selected resource-control action and why it was chosen.
6. Show the before/after network metrics.
7. Show the calculated reward.
8. Show the feedback message.
9. Explain, out loud, that the reward was just used to update the Q-table —
   i.e. the loop closed and the controller is (slightly) different than it was
   before the click.

---

## 8. Explanation for the demo

**Problem statement.** Wireless networks need to keep adjusting resource
allocation as demand changes, but manual/rule-based tuning doesn't adapt well.
The paper proposes closing the loop with AI/ML: monitor the network, let a
learned controller decide, apply the action, and learn from what happens.

**What the paper proposes.** A layered, closed-loop architecture where telemetry
from network devices feeds AI/ML decision models, which choose control actions
(power, channel, scheduling, handover, etc.); those actions are applied by
actuators; and the resulting outcomes feed back as rewards/labels that keep the
models updated — all wrapped in safety filters and staged rollout for real
deployments.

**The small part I implemented.** Just the core loop, with one control decision
— resource allocation — instead of the paper's full multi-tier, multi-action,
production-grade system.

**Why Q-Learning.** It's a simple, transparent reinforcement-learning method:
a Q-table you can inspect directly, no black-box neural network, and it fits
naturally into a closed-loop control setting where the agent needs to keep
improving from feedback. The paper itself lists Q-learning-style reinforcement
learning among the candidate decision models for this kind of problem.

**How telemetry is represented.** Raw values (percentages, milliseconds, Mbps)
are simulated, then discretized into LOW / MEDIUM / HIGH buckets per metric —
that's the state the Q-Learning agent actually reasons over.

**How the AI selects an action.** The agent looks up its learned Q-values for
the current state and picks the action with the highest value (the policy
learned from 5,000 offline training episodes, refined further on every live
run).

**How the simulated network responds.** Increasing resources generally lowers
latency and raises throughput; decreasing resources does the opposite, more so
under high traffic; the environment adds enough randomness that it behaves like
a simulation rather than a fixed lookup table.

**How reward/feedback closes the loop.** The reward scores the resulting
latency and how efficiently resources were used for that traffic level. That
reward updates the Q-table via the standard Q-learning update rule, so the next
decision in that state is informed by this one.

**What is actually implemented:** the full loop above, for one action space
(resource allocation), using genuine Q-Learning.

**What is outside scope:** everything in section 6 above that is explicitly
listed as not implemented.

---

## 9. Likely viva questions

**Q1. Why did you choose Q-Learning instead of a deep neural network?**
Q-Learning is simple enough to fully explain and inspect (a small table of
numbers), which fits both the loop I'm demonstrating and the level of this
project. The paper lists RL broadly, including simpler tabular methods, as a
candidate decision model.

**Q2. What exactly is the "state" the agent sees?**
Three discretized values — traffic load, latency, and current resource level —
each bucketed into LOW / MEDIUM / HIGH, giving 27 possible states.

**Q3. How does the agent decide an action?**
It looks up the Q-values for the current state in its Q-table and picks the
action with the highest value (during the live demo). During offline training,
it also sometimes picks a random action (ε-greedy) to keep exploring.

**Q4. What is the reward based on?**
Lower latency and appropriately-sized resource usage for the current traffic
level. Wasting resources when traffic is low is penalized; under-provisioning
when traffic is high is penalized more heavily, because that directly hurts
latency.

**Q5. Is this controlling a real Wi-Fi network?**
No. All telemetry and network behaviour is simulated in Java. Nothing here
talks to real hardware.

**Q6. Why does clicking "Run" sometimes give a different action for the same
scenario?**
It usually won't, once training has converged, because the dashboard always
uses the best learned action (`getBestAction`) rather than exploring randomly.
But the agent does keep learning from every click, so the underlying Q-values
can shift slightly over a long demo session.

**Q7. How does this relate to the research paper?**
The paper describes a much larger, production-scale closed-loop architecture
(multi-tier, multiple control actions, hybrid classical/ML control, safety
filters, federated learning). This project implements only its core telemetry
→ AI controller → action → response → feedback loop, for a single action space
(resource allocation), as a teaching-scale prototype — see section 6 for the
exact mapping.

**Q8. Why a web dashboard instead of a desktop GUI?**
It keeps the frontend and backend cleanly separated: Java owns all of the AI
and simulation logic and exposes it over a small HTTP API, while HTML/CSS/JS
is only responsible for displaying what the backend computed — which also
makes it easy to show the API response (JSON) directly if asked.

**Q9. What would need to change to make this closer to the paper's full
system?**
Multiple control actions (power, channel, scheduling), a richer state space,
a neural or deep-RL policy for scale, real telemetry pipelines, safety filters
before actuation, and rollback/monitoring — all called out in the paper's
"Implementation & Prototype" and "Limitations" sections.

**Q10. How do you know the Q-Learning is real and not hard-coded output?**
The Q-values themselves are printed on the dashboard and are visibly different
per scenario and per state; they are computed by `QLearningAgent.java` from the
actual Q-table, not hand-picked strings, and the "Total Q-value updates"
counter increases with every run, showing the table is genuinely being
updated live.
