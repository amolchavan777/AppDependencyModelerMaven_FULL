# AppDependencyModeler

A Maven-based Java tool for discovering, normalizing, and resolving application dependencies using multi-source evidence and truth discovery.

---

## 🔍 Overview

This system gathers dependency data from a wide variety of realistic sources (logs, config, traffic, CI/CD, APIs, etc.), processes them through dedicated **adapters**, and uses a **Latent Truth Model (LTM)** to resolve conflicts and identify the most likely truth about which applications depend on each other.

---

## 📦 Features

- Multi-source dependency discovery
- Truth discovery using Latent Truth Model (LTM)
- ArchiMate XML and GraphML exports for visualization
- JSON graph export for web visualizers
- CSV summaries of dependencies and edges
- Multi-sheet Excel audit workbook export (Process Flow + 8 pipeline tabs)
- Multi-sheet Excel audit workbook export (9-stage pipeline with a "Process Flow" sheet)
- Conflict detection of contradictory claims
- Interactive web dashboard
- Console histograms summarize dependency fan-in and fan-out
- Dependency metrics and analytics
- Multiplicity classifier distinguishes 1:1 vs 1:N relationship types
- Claim identity resolution with canonical IDs
- Pluggable adapter system
- Rich sample dataset included
- Maven project structure

## 📊 Excel Workbook Tabs

The generated `application_dependency_audit.xlsx` contains several tabs that trace how raw claims become finalized dependencies. New in this release is the **Process Flow** sheet summarizing the entire pipeline. Tabs appear in this order:

| # | Sheet | Purpose |
|---|-------|---------|
| 1 | Process Flow | High-level diagram of the modeling stages |
| 2 | Raw Claims | All collected dependency claims with confidence values |
| 3 | Normalization Mapping | Alias to canonical name mappings |
| 4 | Alias Groups | Canonical names grouped with their aliases |
| 5 | Normalized Claims | Claims after aliases have been resolved |
| 6 | Claim Identities | Canonical IDs assigned to each dependency |
| 7 | Negative Claims | Generated absence assertions |
| 8 | Initial Aggregation | Preliminary scores before EM |
| 9 | LTM Iterations | Trust scores for each source across EM steps |
|10 | Final Dependencies | Resolved dependency graph |
|11 | Data Coverage | Number of evidence sources per application |

## 📜 Changelog (last 48 hours)

The following log summarizes recent commits and the thinking behind them. Times
are taken from the Git history and use the `+03:00` timezone recorded there.

- **2025-07-01 01:43** – *Excel audit export*
  - Introduced `ExcelExporter.java` and supporting Maven dependencies to generate
    `application_dependency_audit.xlsx` from EM results.
  - Allows auditors to review dependency evidence in a structured workbook.
- **2025-07-01 18:50** – *Process flow sheet*
  - Excel workbook now includes a "Process Flow" sheet summarizing pipeline steps.
  - Ten sheets are produced in total.
- **2025-07-01 13:03** – *Excel workbook pipeline*
  - Excel export now produces eight sheets showing each processing stage.

- **2025-07-01 01:15** – *Interactive web dashboard*
  - Added `DashboardExporter.java` with a bundled `index.html` for local graph
    exploration.
  - Lets users inspect dependencies without external visualization tools.

- **2025-07-01 00:51** – *JSON graph export*
  - Implemented `JsonExporter.java` and wired it into `Main`.
  - Provides machine-readable output for JavaScript-based visualizers.

- **2025-07-01 00:37** – *CSV summary exporter*
  - Created `CsvExporter.java` plus basic tests and documentation updates.
  - Enables spreadsheet-style analysis of dependencies and edges.

- **2025-07-01 00:35** – *Dependency metrics analytics*
  - Added `DependencyMetrics.java` to compute fan-in/out statistics.
  - Helps identify heavily coupled services.

- **2025-07-01 00:32** – *Improved console summaries*
  - Enhanced `Main` output with clearer layout and ASCII histograms.
  - Makes terminal results easier to interpret.

- **2025-06-30 23:50** – *Robust EM algorithm*
  - Updated `TruthDiscoveryEngineEM` to handle negative and missing claims.
  - Ensures the inference engine remains stable with conflicting evidence.

- **2025-06-30 23:37** – *Exporters use EM inference*
  - Refactored exporters to rely on the EM-resolved graph.
  - Keeps all output formats consistent with truth-discovered dependencies.

---

## 🗂️ Data Sources Supported

| Source Type          | File                          | Adapter                     |
|----------------------|-------------------------------|-----------------------------|
| Wireshark (PCAP logs)| `wireshark.txt`               | `WiresharkAdapter`          |
| Process list         | `ps_aux.txt`                  | `PsAuxAdapter`              |
| Config files         | `config.ini`                  | `ConfigFileAdapter`         |
| Application logs     | `application.log`             | `ApplicationLogAdapter`     |
| Source code deps     | `code_dependencies.txt`       | `CodeDependencyAdapter`     |
| OpenTelemetry traces | `otel_traces.json`            | `OpenTelemetryAdapter`      |
| GitLab CI/CD logs    | `gitlab_pipeline.log`         | `GitlabCiAdapter`           |
| API Specifications   | `api_spec.yaml`               | `ApiSpecAdapter`            |

---

## ⚙️ Configuration

- **Alias map** – Normalizes service names (see `Normalizer.getAliasMap`) to merge synonyms like `web-tier` → `WebPortal`.
- **Multiplicity rules** – `MultiplicityClassifier` tags relationship types as `ONE_TO_ONE` or `ONE_TO_MANY` (e.g. `default_db` is 1:1).
- **EM confidence threshold** – dependencies require probability > 0.5 to be exported.

---

## 🧠 Truth Discovery Engine

- Uses an **Expectation-Maximization (EM)** algorithm based on the **Latent Truth Model (LTM)**.
- Claim confidence values weight each source's impact during inference.
- Each claim (dependency between two apps) is assigned:
  - A source
  - A confidence level
  - A belief/truth score after processing

---

## 🏗️ Project Structure

```
AppDependencyModeler/
├── pom.xml
├── raw_scanner_data/
│   └── [*.txt, *.json, *.yaml]  # Sample data files
└── src/
    └── main/java/com/modeler/app/
        ├── *.java               # All adapters, claim model, normalizer, etc.
```

---

## 🚀 How to Run

### 1. Clone or unzip the project

```bash
cd AppDependencyModeler
```

### 2. Build the project

```bash
mvn clean compile
```

### 3. Run the application

```bash
mvn exec:java -Dexec.mainClass="com.modeler.app.Main"
```

### 4. Output

Files will be created:

```
output/archimate_model.xml
output/dependency_graph.graphml
output/dependency_graph.json
output/dependency_summary.csv
output/dependency_edges.csv
output/application_dependency_audit.xlsx
output/index.html

```

The console also prints ASCII histograms summarizing outgoing and incoming dependency counts per application.

You can import the ArchiMate XML into tools like **Archi** or **BiZZdesign**.
The GraphML file works with graph tools such as **yEd** or **Gephi**.
For a quick interactive view, open `output/index.html` in your browser.

---

## 🔧 Dependencies

Make sure these are in your `pom.xml`:

```xml
<dependency>
  <groupId>org.yaml</groupId>
  <artifactId>snakeyaml</artifactId>
  <version>2.0</version>
</dependency>
<dependency>
  <groupId>org.json</groupId>
  <artifactId>json</artifactId>
  <version>20231013</version>
</dependency>
<dependency>
  <groupId>com.fasterxml.jackson.core</groupId>
  <artifactId>jackson-databind</artifactId>
  <version>2.16.1</version>
</dependency>
```

---

## 📊 Sample Output Visuals

The ArchiMate model includes:
- ApplicationComponent relationships
- Dependencies with confidence scores
- Grouped logical clusters for visualization

---
## 🏁 Example Walkthrough

1. Run `mvn clean compile exec:java -Dexec.mainClass="com.modeler.app.Main"`.
2. Review the list of normalized claims printed to the console.
3. After several iterations the final dependency graph is shown.
4. Three visualization files are produced in the `output/` directory.
5. Four files are produced in the `output/` directory (two visualization and two CSV summaries).


---

## 📝 Data File Formats

| File | Description |
|------|-------------|
| `wireshark.txt` | Simplified packet capture with lines containing `IP` flows. |
| `ps_aux.txt` | Snapshot of running processes; executables under `/usr/bin/` are treated as apps. |
| `config.ini` | INI file with `name=` and `dependencies=` entries. |
| `application.log` | Log lines with `connected to` for runtime interactions. |
| `code_dependencies.txt` | One `from -> to` dependency per line. |
| `otel_traces.json` | JSON array of spans with `service` and `targetService`. |
| `gitlab_pipeline.log` | CI/CD log lines containing `ServiceA -> ServiceB`. |
| `api_spec.yaml` | OpenAPI spec using custom `x-calls` to denote calls. |

---

## 🧪 Running Tests

Execute the unit tests using Maven:

```bash
mvn test
```

All tests should pass and provide a quick sanity check of the adapters and the credibility engine.

---

## ➕ Extending with New Sources

You can add a new adapter by:
1. Creating a class like `YourNewAdapter.java`
2. Implementing `public static List<Claim> parse(String path)`
3. Adding it to `Normalizer.java`

---

# Refactored Truth Discovery Engine (Based on Latent Truth Model with EM)

## 🔍 Goal

Improve the credibility computation for claims by using a proper **Expectation-Maximization (EM)** strategy based on the Latent Truth Model (LTM), aligning it with the original research principles.

---

## 🧠 Key Principles

- Each **claim** has an unknown **truth probability**.
- Each **source** has a trustworthiness score estimated from the data.
- Iterative EM is used to infer:
  - Which claims are true
  - Which sources are reliable

---

## 📐 Model Parameters

- Let `c` be a claim (e.g., "A depends on B")
- Let `s` be a source
- Let `z_c` be the latent variable: `z_c = 1` if claim is true, `0` otherwise
- Let `t_s` be the trustworthiness of source `s` ∈ [0,1]

---

## 🔁 Expectation-Maximization (EM) Steps

### Step 1: Initialization
- Set all trustworthiness values `t_s = 0.9` (or random in [0.7, 1.0])
- Set all claim probabilities `p(z_c = 1) = 0.5`

### Step 2: E-Step (Estimate Truths)
For each claim `c`, compute (weighted by the confidence of each supporting claim):

```math
p(z_c = 1) = ∏_{s ∈ supporting(c)} t_s × ∏_{s ∉ supporting(c)} (1 - t_s)
```

Normalize across both `z_c = 1` and `z_c = 0` to ensure values ∈ [0,1]

### Step 3: M-Step (Update Source Trust)
For each source `s`, update (weighted by the confidence of the claims it provided):

```math
t_s = (1 / N_s) × Σ_{c ∈ claims(s)} p(z_c = 1 if c supported by s else 1 - p(z_c = 1))
```

Where `N_s` is number of claims source `s` participated in.

### Step 4: Convergence
Repeat E-step and M-step until the change in trust scores and truth probabilities is very small (e.g., Δ < 0.001)

---

## ✅ Output
- Trust score for each source ∈ [0,1]
- Truth probability for each claim ∈ [0,1]
- Final decision: claim is true if `p(z_c = 1) > 0.5`

---

## 💡 Improvements Over Old Version

| Old Logic                  | New Logic                     |
|----------------------------|-------------------------------|
| Summed scores              | Probabilistic inference       |
| Scores > 1.0               | Scores constrained ∈ [0,1]    |
| All claims assumed true    | Uncertainty handled           |
| Source always trusted more | Unreliable sources penalized  |
| No convergence             | Proper EM convergence         |

---

