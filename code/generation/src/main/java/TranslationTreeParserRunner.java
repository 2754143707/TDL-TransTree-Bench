import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 翻译树DSL解析器运行类
 * 用于解析和验证翻译树DSL代码
 */
public class TranslationTreeParserRunner {

    private static final Pattern ACTION_LINE_PATTERN = Pattern.compile("^(\\s*)([A-Za-z0-9_]+)\\.\\s*(.*?)\\s*$");
    private static final Pattern CONDITION_LINE_PATTERN = Pattern.compile("^(\\s*)([A-Za-z0-9_]+)\\s*:\\s*(.*?)\\s*$");

    private static class ParseAttempt {
        final boolean success;
        final List<String> errors;
        final List<SyntaxErrorDetail> syntaxErrors;
        final int syntaxErrorCount;

        ParseAttempt(boolean success, List<String> errors, List<SyntaxErrorDetail> syntaxErrors, int syntaxErrorCount) {
            this.success = success;
            this.errors = errors;
            this.syntaxErrors = syntaxErrors;
            this.syntaxErrorCount = syntaxErrorCount;
        }
    }

    /**
     * 解析翻译树DSL文件
     * @param inputFile DSL文件
     * @return 解析结果
     */
    public static ParseResult parseFile(File inputFile) {
        List<String> errors = new ArrayList<>();
        List<SyntaxErrorDetail> syntaxErrors = new ArrayList<>();
        boolean success = false;
        ByteArrayOutputStream logBuffer = new ByteArrayOutputStream();
        
        try (PrintStream logStream = new PrintStream(logBuffer, true, StandardCharsets.UTF_8)) {
            PrintStream originalOut = System.out;
            PrintStream originalErr = System.err;
            
            // 重定向输出到日志缓冲区
            System.setOut(logStream);
            System.setErr(logStream);
            
            try {
                System.out.println("==================================================");
                System.out.println("📂 开始解析翻译树DSL: " + inputFile.getName());
                System.out.println("🕒 时间: " + new java.util.Date());
                System.out.println("--------------------------------------------------");

                String dslContent = Files.readString(inputFile.toPath(), StandardCharsets.UTF_8);
                ParseAttempt strictAttempt = parseDslContent(dslContent);

                if (strictAttempt.success) {
                    System.out.println("✅ 翻译树DSL语法解析通过 (Zero Syntax Errors)");
                    success = true;
                } else {
                    String normalized = normalizeForClaudeCompatibility(dslContent);
                    if (!normalized.equals(dslContent)) {
                        System.out.println("⚠️ 检测到非标准字符串/格式，尝试宽松预处理后重新解析...");
                        ParseAttempt tolerantAttempt = parseDslContent(normalized);
                        if (tolerantAttempt.success) {
                            System.out.println("✅ 宽松模式解析通过 (已自动兼容未加引号文本/拆分token/缺失右花括号)");
                            success = true;
                        } else {
                            errors.addAll(tolerantAttempt.errors);
                            syntaxErrors.addAll(tolerantAttempt.syntaxErrors);
                            System.err.println("⛔ 宽松模式仍失败，发现 " + tolerantAttempt.syntaxErrorCount + " 个语法错误");
                        }
                    } else {
                        errors.addAll(strictAttempt.errors);
                        syntaxErrors.addAll(strictAttempt.syntaxErrors);
                        System.err.println("⛔ 解析失败，发现 " + strictAttempt.syntaxErrorCount + " 个语法错误");
                    }
                }
                
            } catch (IOException e) {
                System.err.println("❌ 文件读取异常: " + e.getMessage());
                errors.add("文件读取异常: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("❌ 解析异常: " + e.getMessage());
                errors.add("解析异常: " + e.getMessage());
                e.printStackTrace(logStream);
            } finally {
                // 恢复控制台
                System.setOut(originalOut);
                System.setErr(originalErr);
            }
            
        } catch (Exception e) {
            errors.add("日志处理异常: " + e.getMessage());
        }
        
        String logText = logBuffer.toString(StandardCharsets.UTF_8);
        int errorCount = errors.size();
        
        return new ParseResult(
            success, 
            errors, 
            errorCount, 
            logText, 
            "", // visitorOutput - 翻译树暂时不需要visitor
            null, // dotPath
            null, // svgPath
            null, // logPath
            syntaxErrors
        );
    }
    
    /**
     * 解析翻译树DSL字符串
     * @param dslContent DSL内容
     * @return 解析结果
     */
    public static ParseResult parseString(String dslContent) {
        ParseAttempt strictAttempt = parseDslContent(dslContent);
        if (strictAttempt.success) {
            return new ParseResult(
                true,
                new ArrayList<>(),
                0,
                "",
                "",
                null,
                null,
                null,
                new ArrayList<>()
            );
        }

        String normalized = normalizeForClaudeCompatibility(dslContent);
        if (!normalized.equals(dslContent)) {
            ParseAttempt tolerantAttempt = parseDslContent(normalized);
            if (tolerantAttempt.success) {
                return new ParseResult(
                    true,
                    new ArrayList<>(),
                    0,
                    "",
                    "",
                    null,
                    null,
                    null,
                    new ArrayList<>()
                );
            }
            return new ParseResult(
                false,
                tolerantAttempt.errors,
                tolerantAttempt.errors.size(),
                "",
                "",
                null,
                null,
                null,
                tolerantAttempt.syntaxErrors
            );
        }
        
        return new ParseResult(
            false,
            strictAttempt.errors,
            strictAttempt.errors.size(),
            "",
            "",
            null,
            null,
            null,
            strictAttempt.syntaxErrors
        );
    }

    private static ParseAttempt parseDslContent(String dslContent) {
        List<String> errors = new ArrayList<>();
        List<SyntaxErrorDetail> syntaxErrors = new ArrayList<>();

        try {
            CharStream input = CharStreams.fromString(dslContent);
            TranslationTreeDSLLexer lexer = new TranslationTreeDSLLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            TranslationTreeDSLParser parser = new TranslationTreeDSLParser(tokens);

            parser.removeErrorListeners();
            parser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                      int line, int charPositionInLine, String msg, RecognitionException e) {
                    String error = "❌ [语法错误] 行 " + line + ":" + charPositionInLine + " -> " + msg;
                    errors.add(error);
                    syntaxErrors.add(SyntaxErrorDetail.fromSyntaxError(line, charPositionInLine, offendingSymbol, msg));
                }
            });

            parser.translationTree();
            int errorCount = parser.getNumberOfSyntaxErrors();
            return new ParseAttempt(errorCount == 0, errors, syntaxErrors, errorCount);
        } catch (Exception e) {
            errors.add("解析异常: " + e.getMessage());
            return new ParseAttempt(false, errors, syntaxErrors, errors.size());
        }
    }

    private static String normalizeForClaudeCompatibility(String input) {
        String[] lines = input.split("\\r?\\n", -1);
        StringBuilder sb = new StringBuilder();

        boolean inActionBlock = false;
        boolean inConditionBlock = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();

            if (trimmed.startsWith("ACTION") && trimmed.contains("{")) {
                inActionBlock = true;
                inConditionBlock = false;
            } else if (trimmed.startsWith("CONDITION") && trimmed.contains("{")) {
                inConditionBlock = true;
                inActionBlock = false;
            } else if (trimmed.startsWith("}")) {
                if (inActionBlock) {
                    inActionBlock = false;
                } else if (inConditionBlock) {
                    inConditionBlock = false;
                }
            }

            if (inActionBlock) {
                line = normalizeActionLine(line);
            } else if (inConditionBlock) {
                line = normalizeConditionLine(line);
            }

            sb.append(line);
            if (i < lines.length - 1) {
                sb.append(System.lineSeparator());
            }
        }

        return appendMissingRightBraces(sb.toString());
    }

    private static String normalizeActionLine(String line) {
        Matcher matcher = ACTION_LINE_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return line;
        }

        String indent = matcher.group(1);
        String label = matcher.group(2);
        String rhs = matcher.group(3) == null ? "" : matcher.group(3).trim();
        if (rhs.isEmpty() || isWrappedByDoubleQuotes(rhs)) {
            return line;
        }

        return indent + label + ". \"" + escapeForStringLiteral(rhs) + "\"";
    }

    private static String normalizeConditionLine(String line) {
        Matcher matcher = CONDITION_LINE_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return line;
        }

        String indent = matcher.group(1);
        String label = matcher.group(2);
        String rhs = matcher.group(3) == null ? "" : matcher.group(3).trim();
        if (rhs.isEmpty() || isWrappedByDoubleQuotes(rhs)) {
            return line;
        }

        return indent + label + ": \"" + escapeForStringLiteral(rhs) + "\"";
    }

    private static boolean isWrappedByDoubleQuotes(String text) {
        return text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"");
    }

    private static String escapeForStringLiteral(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String appendMissingRightBraces(String content) {
        int balance = 0;
        for (int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);
            if (ch == '{') {
                balance++;
            } else if (ch == '}') {
                balance--;
            }
        }

        if (balance <= 0) {
            return content;
        }

        StringBuilder sb = new StringBuilder(content);
        for (int i = 0; i < balance; i++) {
            sb.append(System.lineSeparator()).append("}");
        }
        return sb.toString();
    }
}
