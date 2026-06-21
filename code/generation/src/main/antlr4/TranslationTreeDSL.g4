grammar TranslationTreeDSL;

options {
    tokenVocab=TranslationTreeDSLLexer;
}

// ==========================================
// 翻译树DSL语法定义
// 根据BNF: <TranslationTree> ::= "Message" <Identifier> "Translation Trees" "{" <RequiredActionBlock> <ConditionBlock> <LogicBlock> "}"
// ==========================================

// 顶层规则
translationTree
    : MESSAGE identifier TRANSLATION TREES LBRACE
        requiredActionBlock
        conditionBlock
        logicBlock
      RBRACE
      EOF
    ;

// ACTION 块
// <RequiredActionBlock> ::= "ACTION" "{" <ActionDefinition>* "}"
requiredActionBlock
    : ACTION LBRACE
        actionDefinition*
      RBRACE
    ;

// <ActionDefinition> ::= <ActionLabel> "." <string>
actionDefinition
    : actionLabel DOT STRING_LITERAL
    ;

// <ActionLabel> ::= <Identifier>
actionLabel
    : identifier
    ;

// CONDITION 块
// <ConditionBlock> ::= "CONDITION" "{" <ConditionDefinition>* "}"
conditionBlock
    : CONDITION LBRACE
        conditionDefinition*
      RBRACE
    ;

// <ConditionDefinition> ::= <ConditionLabel> ":" <string>
conditionDefinition
    : conditionLabel COLON STRING_LITERAL
    ;

// <ConditionLabel> ::= <Identifier>
conditionLabel
    : identifier
    ;

// 逻辑块
// <LogicBlock> ::= <IfStatement>
logicBlock
    : statement
    ;

// <Statement> ::= <IfStatement> | <ExecuteStatement>
statement
    : ifStatement
    | executeStatement
    ;

// <IfStatement> ::= "IF" "(" <ConditionLabel> ")" "{" <Statement> "}" "ELSE" "{" <Statement> "}"
ifStatement
    : IF LPAREN conditionLabel RPAREN LBRACE
        statement
      RBRACE ELSE LBRACE
        statement
      RBRACE
    ;

// <ExecuteStatement> ::= "EXECUTE" "(" <ActionLabelList> ")"
executeStatement
    : EXECUTE LPAREN actionLabelList RPAREN
    ;

// <ActionLabelList> ::= <ActionLabel> ("," <ActionLabel>)*
actionLabelList
    : actionLabel (COMMA actionLabel)*
    ;

// 标识符
identifier
    : MSG_ID
    | IDENTIFIER
    | NUMBER  // 允许纯数字作为标识符（如 Node 1, Node 2）
    ;