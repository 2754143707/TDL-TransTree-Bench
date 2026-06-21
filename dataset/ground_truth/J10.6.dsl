Role: MIL-STD-6016B Tactical Data Link (TDL) Expert & Compiler Architect.

Goal: Visual Analysis (PDF + Annotated Charts) -> Logical Topology Extraction -> BNF DSL Code Generation.

Strategy: ChartInsights (Chain-of-Charts) Reasoning.

1. Syntax Constraints (BNF_Strict)
Warning: You must strictly follow the definitions below. Do not modify any symbols, keywords, or structures.

BNF
<TranslationTree> ::= "Message" <Identifier> "Translation Trees" "{" <RequiredActionBlock> <ConditionBlock> <LogicBlock> "}"
<RequiredActionBlock> ::= "ACTION" "{" <ActionDefinition>* "}"
<ActionDefinition> ::= <ActionLabel> "." <string>
<ActionLabel> ::= <Identifier>
<ConditionBlock> ::= "CONDITION" "{" <ConditionDefinition>* "}"
<ConditionDefinition> ::= <ConditionLabel> ":" <string>
<ConditionLabel> ::= <Identifier>
<LogicBlock> ::= <IfStatement>
<Statement> ::= <IfStatement> | <ExecuteStatement>
<IfStatement> ::= "IF" "(" <ConditionLabel> ")" "{" <Statement> "}" "ELSE" "{" <Statement> "}"
<ExecuteStatement> ::= "EXECUTE" "(" <ActionLabelList> ")"
<ActionLabelList> ::= <ActionLabel> ("," <ActionLabel>)*
2. Few-Shot Example
Plaintext
Message J10.6 Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE."
        B. "FWD M.9B."
        Z. "END TRANSLATION."
    }
    CONDITION {
        Node1: "PAIRING ACTION 0-7 OR 15."
    }
    IF (Node1) {
        EXECUTE(B, Z)
    } ELSE {
        EXECUTE(A, Z)
    }
}
3. Execution Protocol: Four-Phase Reasoning
Perform the following internal steps and output only the Phase 4 Code Result.

Phase 1: Visual Knowledge Extraction (Transcription)
Source: Extract from the TEST NODE CONDITION table and REQUIRED ACTION table.

Rule: Copy text verbatim. Do not paraphrase, summarize, or correct abbreviations.

Mapping: * Node Numbers (1, 2, 3...) -> Node1, Node2...

Action Letters (A, B, C...) -> A, B, C...

Format: Wrap all descriptions in double quotes.

Phase 2: Topology Alignment & Ray-Casting
Color Logic: Red lines = Yes (Y); Green lines = No (N). Strictly follow line colors.

Vertical Scanning (Crucial): When a path enters the bottom Action Grid, perform a "penetration scan." Collect every letter encountered in that specific vertical column (e.g., A, L, Z). Do not stop at the first letter; scan to the bottom.

Phase 3: Logic Nesting Strategy
Nesting Rule: * If a branch leads to an Action List -> Generate EXECUTE(A, L, Z).

If a branch leads to another Node -> Nest a new IF/ELSE block.

Validation: Ensure every IF has a corresponding ELSE and the logic is closed.

Phase 4: DSL Code Generation
Convert the validated logic into code strictly following the BNF_Strict grammar.

Self-Correction: Ensure . follows Action labels and : follows Condition labels.