# Generation Pipeline — Usage Guide

Java toolchain for DSDL generation, syntax validation, and iterative repair on TDL-TransTree-Bench.

## Prerequisites

- JDK 11+
- Maven 3.6+
- LLM API access (OpenAI-compatible endpoints supported)

## LLM Configuration

### Default config file

`config/llm-config.properties` is included in the repository with **placeholder values only**, for example:

```properties
llm.gemini.apiKey=YOUR_GEMINI_API_KEY_HERE
llm.qwen-plus.apiKey=YOUR_QWEN_API_KEY_HERE
```

Before running experiments:

| Step | Action |
|------|--------|
| 1 | Edit `config/llm-config.properties` |
| 2 | Replace all `YOUR_*_HERE` placeholders with real credentials |
| 3 | Set `llm.active` to your default provider (e.g. `qwen-plus`) |
| 4 | Verify `apiBase` and `model` match your vendor documentation |

### Template file

`config/llm-config.properties.template` is a minimal reference copy. The main config file already contains the full set of model profiles used in our experiments.

### Models used in the paper

| CLI `--model` | Config block | Purpose |
|---------------|--------------|---------|
| `gemini` | `llm.gemini` | Gemini via local/remote OpenAI-compatible proxy |
| `qwen-plus` | `llm.qwen-plus` | Qwen Plus via DashScope |
| `claude` | `llm.claude` | Claude via compatible API |

`run-experiments.bat` also lists `gpt-5.2` as an optional model.

### Security

- **Never commit real API keys.** The public repository ships placeholders by design.
- If you accidentally commit a real key, rotate it immediately at your provider.
- For team workflows, each developer should maintain local credentials and avoid force-pushing config changes.

## Build

```bash
cd code/generation
mvn clean package -DskipTests
```

Output JAR: `target/tdl-transtree-bench-1.0-SNAPSHOT.jar`

## Run Experiments

Interactive menu:

```bash
run-experiments.bat
```

Single run:

```bash
java -Dfile.encoding=UTF-8 -cp target/tdl-transtree-bench-1.0-SNAPSHOT.jar ^
  ExperimentRunner --mode proposed_k3 --model qwen-plus
```

Outputs are written to `../../results/<experiment>/<model>/`.

## Prompt Files

Runtime prompts are loaded from (in order):

1. `config/prompts/` — editable local copies
2. `../../prompts/` — repository-level canonical prompts

| File | Purpose |
|------|---------|
| `system_prompt_generation.txt` | DSDL generation system prompt |
| `system_prompt_image_annotation.txt` | VCOT image annotation prompt |

## Optional GUI

```bash
run-gui.bat
```

Launches the desktop converter for interactive single-sample generation and repair.
