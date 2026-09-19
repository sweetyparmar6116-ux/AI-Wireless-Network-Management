/*
 * script.js
 *
 * This file does NOT make any AI decisions itself. Its only job is to:
 *   1. Ask the Java backend to run one closed-loop control cycle
 *      (GET /api/run-loop?scenario=...)
 *   2. Paint the result onto the dashboard.
 *
 * The real telemetry -> state -> Q-learning -> action -> environment
 * -> reward -> Q-table update sequence happens in Main.java on the
 * server, so the browser only ever displays what the backend
 * controller actually computed.
 */

const runButton       = document.getElementById("runButton");
const scenarioSelect  = document.getElementById("scenarioSelect");
const statusBanner    = document.getElementById("statusBanner");
const connectionDot   = document.getElementById("connectionDot");
const connectionLabel = document.getElementById("connectionLabel");

runButton.addEventListener("click", () => {
  runAiControl(scenarioSelect.value);
});

// Run once on load so the dashboard is never empty.
window.addEventListener("DOMContentLoaded", () => {
  runAiControl(scenarioSelect.value);
});

/* -------------------------------------------------------------------- */
/* ONE CONTROL CYCLE                                                     */
/* -------------------------------------------------------------------- */

async function runAiControl(scenario) {

  setBusy(true);
  hideStatusBanner();

  try {

    const response = await fetch(
      `/api/run-loop?scenario=${encodeURIComponent(scenario)}`
    );

    if (!response.ok) {
      throw new Error("The backend returned an error (status " + response.status + ").");
    }

    const result = await response.json();

    render(result);
    setConnectionState(true);

  } catch (error) {

    setConnectionState(false);
    showStatusBanner(
      "Could not reach the Java backend. Make sure the server started " +
      "successfully (run.bat) and that this page was opened at " +
      "http://localhost:8080."
    );
    console.error(error);

  } finally {
    setBusy(false);
  }
}

/* -------------------------------------------------------------------- */
/* RENDERING                                                             */
/* -------------------------------------------------------------------- */

function render(result) {

  const before = result.telemetryBefore;
  const after  = result.telemetryAfter;
  const state  = result.stateBefore;

  // --- Network telemetry -------------------------------------------
  setValue("telTraffic", before.trafficLoad.toFixed(0), "%");
  setValue("telLatency", before.latency.toFixed(1), "ms");
  setValue("telThroughput", before.throughput.toFixed(0), "Mbps");
  setValue("telResource", before.resourceUtilization.toFixed(0), "%");

  setText("networkCondition", conditionLabel(state.traffic));

  // --- AI control ---------------------------------------------------
  setText("stateTraffic", state.traffic + " TRAFFIC");
  setText("stateLatency", state.latency + " LATENCY");
  setText("stateResource", state.resource + " RESOURCE USAGE");

  setText("actionName", actionTitle(result.action));
  setText("actionExplanation", actionSentence(result.action, result.actionExplanation));
  flash("actionName");

  // --- Network response ---------------------------------------------
  setText("beforeLatency", before.latency.toFixed(1) + " ms");
  setText("beforeThroughput", before.throughput.toFixed(0) + " Mbps");
  setText("beforeResource", before.resourceUtilization.toFixed(0) + "%");

  setAfter("afterLatency", after.latency.toFixed(1) + " ms",
           after.latency < before.latency);
  setAfter("afterThroughput", after.throughput.toFixed(0) + " Mbps",
           after.throughput > before.throughput);
  setAfter("afterResource", after.resourceUtilization.toFixed(0) + "%", false);

  // --- Feedback ------------------------------------------------------
  const rewardEl = document.getElementById("rewardValue");
  rewardEl.textContent = (result.reward >= 0 ? "+" : "") + result.reward.toFixed(1);
  rewardEl.className = "feedback-value " + (result.reward >= 0 ? "positive" : "negative");
  flash("rewardValue");

  setText("controllerStatus", "UPDATED");
  setText(
    "feedbackMessage",
    "The network response was evaluated and the resulting reward was returned " +
    "to the controller, which updated its learned values."
  );

  // --- Small model details line ---------------------------------------
  setText(
    "modelLine",
    "Q-Learning \u2022 " +
    Number(result.controller.trainingEpisodes).toLocaleString() +
    " training episodes \u2022 3 actions"
  );
}

/* -------------------------------------------------------------------- */
/* LABELS                                                                */
/* -------------------------------------------------------------------- */

function conditionLabel(trafficLabel) {

  switch (trafficLabel) {
    case "HIGH":   return "HIGH DEMAND";
    case "MEDIUM": return "MODERATE DEMAND";
    default:       return "LOW DEMAND";
  }
}

function actionTitle(actionEnumValue) {

  switch (actionEnumValue) {
    case "INCREASE_RESOURCES": return "INCREASE RESOURCES";
    case "DECREASE_RESOURCES": return "DECREASE RESOURCES";
    default: return "MAINTAIN RESOURCES";
  }
}

/* The backend already sends a short reason. A second clause is added
 * here so the sentence reads naturally on screen; the decision itself
 * still comes entirely from the Java controller. */
function actionSentence(actionEnumValue, backendExplanation) {

  switch (actionEnumValue) {

    case "INCREASE_RESOURCES":
      return backendExplanation +
        " The controller increases resource allocation to improve network performance.";

    case "DECREASE_RESOURCES":
      return backendExplanation +
        " The controller reduces allocation to avoid wasting capacity.";

    default:
      return backendExplanation +
        " The controller keeps the current allocation unchanged.";
  }
}

/* -------------------------------------------------------------------- */
/* SMALL UI HELPERS                                                      */
/* -------------------------------------------------------------------- */

function setValue(id, number, unit) {
  const el = document.getElementById(id);
  if (!el) return;
  el.textContent = number;          // replaces any previous content
  const unitEl = document.createElement("span");
  unitEl.className = "unit";
  unitEl.textContent = unit;
  el.appendChild(unitEl);
  flash(id);
}

function setAfter(id, text, improved) {
  const el = document.getElementById(id);
  if (!el) return;
  el.textContent = text;
  el.className = "response-after" + (improved ? " improved" : "");
  flash(id);
}

function setText(id, value) {
  const el = document.getElementById(id);
  if (el) el.textContent = value;
}

/* A brief fade-in on updated values. Nothing moves on the page. */
function flash(id) {
  const el = document.getElementById(id);
  if (!el) return;
  el.classList.remove("refresh");
  void el.offsetWidth;
  el.classList.add("refresh");
}

function setBusy(busy) {
  runButton.disabled = busy;
  scenarioSelect.disabled = busy;
  runButton.textContent = busy ? "Running\u2026" : "Run AI Control";
  document.querySelectorAll(".card").forEach(card => {
    card.classList.toggle("pending", busy);
  });
}

function setConnectionState(isOnline) {
  connectionDot.className = "conn-dot " + (isOnline ? "online" : "offline");
  connectionLabel.textContent = isOnline ? "backend connected" : "backend unreachable";
}

function showStatusBanner(message) {
  statusBanner.textContent = message;
  statusBanner.classList.remove("hidden");
}

function hideStatusBanner() {
  statusBanner.classList.add("hidden");
}
