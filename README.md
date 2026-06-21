# TDL-TransTree-Bench

Public benchmark for **translation-tree-to-DSDL parsing**, built from MIL-STD-6016B (Link 16) translation tree diagrams.

## Overview

- **19** high-resolution diagram pages from **11** core translation trees
- **11** expert-annotated DSDL ground-truth files
- **11** corresponding PDF text pages
- VCOT visual annotations (full / color-only / anchor-only ablations)
- Reproducible generation + repair pipeline and evaluation scripts

### Subsets

| Subset | Input | Description |
|--------|-------|-------------|
| **image_text** | `raw_images/` + `pdf_texts/` | Baseline: original diagram + PDF text |
| **vcot_annotated** | `vcot_full/` + `pdf_texts/` | Proposed method: VCOT-annotated diagram + PDF text |

See `dataset/manifest.csv` for the full sample index.

## Repository Layout

```
TDL-TransTree-Bench/
├── README.md
├── dataset/                      # Benchmark data
│   ├── manifest.csv
│   ├── raw_images/               # 19 original diagrams
│   ├── pdf_texts/                # 11 PDF text pages
│   ├── ground_truth/             # 11 DSDL annotations
│   ├── vcot_full/                # Full VCOT annotations
│   ├── vcot_ablation_color/      # Color-path ablation
│   └── vcot_ablation_anchor/     # Anchor-only ablation
├── prompts/
│   ├── image_annotation/         # VCOT annotation prompts
│   └── dsdl_generation/          # DSDL generation system prompt
├── code/
│   ├── generation/               # Java: LLM generation + syntax repair
│   └── evaluation/               # Python: metric calculation
└── results/                      # Paper experiment outputs (optional)
```

## Quick Start

### 1. Configure LLM API

The repository includes `code/generation/config/llm-config.properties` with **placeholder** credentials (`YOUR_*_HERE`). Before running any experiment:

1. Open `code/generation/config/llm-config.properties`.
2. Replace every placeholder with your real API key / endpoint / model ID.
3. Set `llm.active` to the profile you want (e.g. `gemini`, `qwen-plus`, `claude`).
4. Ensure the corresponding `llm.<name>.enabled=true` block is configured.

**Security notes**

- The committed file intentionally contains **no real secrets**—only placeholders safe for public release.
- After filling in real keys locally, **do not push** those values to a public repository.
- If you prefer a clean workflow, copy from the template instead:

```bash
cd code/generation
copy config\llm-config.properties.template config\llm-config.properties.local
# Edit llm-config.properties.local, then point the app to it or merge into llm-config.properties
```

Alternatively, edit the shipped `config/llm-config.properties` in place (recommended for most users).

Paper experiments use `--model` values: `gemini`, `qwen-plus`, `claude`. Each must have a matching `llm.<model>` section in the config file.

See also: [code/generation/README.md](code/generation/README.md) for detailed setup.

### 2. Build and Run Experiments

Requirements: **JDK 11+**, **Maven 3.6+**

```bash
cd code/generation
mvn clean package -DskipTests
run-experiments.bat
```

Or run a single experiment:

```bash
java -Dfile.encoding=UTF-8 -cp target/tdl-transtree-bench-1.0-SNAPSHOT.jar ExperimentRunner --mode proposed_k3 --model qwen-plus
```

Experiment modes:

| Mode | Input folder | Output folder | Retries (k) |
|------|--------------|---------------|-------------|
| `baseline` | `raw_images` | `results/1_baseline_raw/` | 0 |
| `color` | `vcot_ablation_color` | `results/2_ablation_color/` | 0 |
| `anchor` | `vcot_ablation_anchor` | `results/3_ablation_anchor/` | 0 |
| `vcot_k0` | `vcot_full` | `results/4_vcot_k0/` | 0 |
| `proposed_k3` | `vcot_full` | `results/5_proposed_k3/` | 3 |

### 3. Calculate Metrics

Requirements: **Python 3.8+** (stdlib only)

```bash
cd code/evaluation
python calculate_metrics.py
```

Metrics are computed against `dataset/ground_truth/` and model outputs under `results/`.

## Citation

If you use this benchmark, please cite our paper:

```bibtex
@article{tdl-transtree-bench,
  title   = {TDL-TransTree-Bench: A Benchmark for Translation-Tree-to-DSDL Parsing},
  author  = {...},
  journal = {...},
  year    = {2026}
}
```

## License

Diagrams are derived from publicly available MIL-STD-6016B materials. Please respect the original document's distribution terms when reusing the source diagrams.
