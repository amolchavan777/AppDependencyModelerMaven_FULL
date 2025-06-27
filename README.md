# 🧠 App Dependency Modeler (Maven Edition)

A fully modernized Java project to infer application dependencies from various IT data sources, powered by Latent Credibility Analysis (LCA) and capable of exporting Archimate-compliant architecture models.

---

## ✅ Features

- Maven-based structure with Java 17
- Logging with SLF4J + Logback
- EM-based truth discovery
- Data adapters: Wireshark, ps_aux, config files, app logs
- Output in **Archimate Open Exchange Format (OEF)** XML
- Easily import into Archi or BiZZdesign
- Designed for extensibility and integration

---

## 📦 Project Layout

```
src/
└── main/
    └── java/
        └── com/
            └── modeler/
                └── app/
                    ├── Claim.java
                    ├── Main.java
                    ├── Normalizer.java
                    ├── LatentCredibilityEngine.java
                    ├── ArchimateExporter.java
                    ├── WiresharkAdapter.java
                    ├── PsAuxAdapter.java
                    ├── ConfigFileAdapter.java
                    └── ApplicationLogAdapter.java
raw_scanner_data/
output/
```

---

## 🚀 How to Run

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.modeler.app.Main"
```

---

## 📤 Output

After running, you get:

- Normalized claim list
- Final inferred dependency model
- Archimate OEF export: `output/archimate_model.xml`

You can import this XML into tools like **Archi**:
> File → Import → Open Exchange Format

---

## 📚 Based On Research

> “Towards Automatic IT Architecture Modeling Using Data Integration and Truth Discovery”

This system follows the truth discovery and dependency modeling process proposed in that paper.

---

## 🔮 Future Ideas

- Graph database integration
- D3.js or Graphviz visualizations
- Live REST API
- Support for more input formats (JSON, YAML, NetFlow)

---

## 🧪 License

For academic and non-commercial use.