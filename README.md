# AppDependencyModeler

A Maven-based Java tool for discovering, normalizing, and resolving application dependencies using multi-source evidence and truth discovery.

---

## 🔍 Overview

This system gathers dependency data from a wide variety of realistic sources (logs, config, traffic, CI/CD, APIs, etc.), processes them through dedicated **adapters**, and uses a **Latent Truth Model (LTM)** to resolve conflicts and identify the most likely truth about which applications depend on each other.

---

## 📦 Features

- Multi-source dependency discovery
- Truth discovery using Latent Truth Model (LTM)
- ArchiMate XML export for visualization
- Pluggable adapter system
- Rich sample dataset included
- Maven project structure

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

## 🧠 Truth Discovery Engine

- Uses an **Expectation-Maximization (EM)** algorithm based on the **Latent Truth Model (LTM)**.
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

A file will be created:

```
output/archimate_model.xml
```

You can import this into tools like **Archi** or **BiZZdesign**.

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
```

---

## 📊 Sample Output Visuals

The ArchiMate model includes:
- ApplicationComponent relationships
- Dependencies with confidence scores
- Grouped logical clusters for visualization

---

## ➕ Extending with New Sources

You can add a new adapter by:
1. Creating a class like `YourNewAdapter.java`
2. Implementing `public static List<Claim> parse(String path)`
3. Adding it to `Normalizer.java`

---

## 📬 Contributing

This project is designed to be extensible, verifiable, and educational. PRs and feedback welcome!

---

## 📄 License

MIT License