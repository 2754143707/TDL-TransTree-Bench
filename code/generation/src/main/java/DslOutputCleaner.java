import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DSL 输出清理工具
 * 用于移除 LLM 生成的 DSL 代码中的说明性文字、Markdown 标记等无关内容
 */
public class DslOutputCleaner {

    /**
     * 清理 LLM 输出，只保留纯 DSL 代码
     *
     * @param rawOutput LLM 的原始输出
     * @return 清理后的纯 DSL 代码
     */
    public static String clean(String rawOutput) {
        if (rawOutput == null || rawOutput.isBlank()) {
            return rawOutput;
        }

        String cleaned = rawOutput;

        // 1) 先移除 Markdown 代码块（如果 content 里用了 ``` 包裹）
        cleaned = removeMarkdownCodeBlocks(cleaned);

        // 2) 翻译树优先：只要能定位到 Message，就直接按 Message 起点 + 括号匹配截取
        String onlyMessage = extractMessageDslOnly(cleaned);
        if (onlyMessage != null && !onlyMessage.isBlank()) {
            // 如果像 chat.completion JSON 那样把整段 DSL 放在一个 JSON 字符串里，常见形态是包含大量的 \n 和 \"
            // 这种情况下，先做一次 JSON 风格反转义，避免解析器看到字面 n 或 \"。
            String msg = maybeJsonStringUnescape(onlyMessage);

            // 再对字符串外的 \n/\t 做反转义（兜底）
            String unescaped = unescapeDslOutsideQuotes(msg);
            return indentByBraces(unescaped, 4).trim();
        }

        // 3) 回退到功能模型的清洗策略（从 FUNCTION_MODEL 开始，截到最后一个顶层 }）
        cleaned = removeLeadingExplanations(cleaned);
        cleaned = removeTrailingExplanations(cleaned);
        cleaned = removeMarkdownHeaders(cleaned);

        return cleaned.trim();
    }

    /**
     * 基于花括号层级自动缩进（每层 indentSize 个空格）。
     * - 不修改双引号字符串内部内容
     * - 尽量保持原有换行（若没有换行，会在 {、}、; 后适度断行）
     */
    private static String indentByBraces(String text, int indentSize) {
        if (text == null || text.isBlank()) return text;

        String normalized = text.replace("\r\n", "\n").replace('\r', '\n');

        StringBuilder out = new StringBuilder(normalized.length() + 64);
        int indent = 0;
        boolean inString = false;
        boolean escaping = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;
        boolean atLineStart = true;

        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
            char next = (i + 1 < normalized.length()) ? normalized.charAt(i + 1) : '\0';

            // 处理换行
            if (c == '\n') {
                out.append('\n');
                atLineStart = true;
                inLineComment = false;
                continue;
            }

            // 注释状态切换（仅在非字符串中）
            if (!inString) {
                if (!inBlockComment && !inLineComment && c == '/' && next == '/') {
                    inLineComment = true;
                } else if (!inBlockComment && !inLineComment && c == '/' && next == '*') {
                    inBlockComment = true;
                } else if (inBlockComment && c == '*' && next == '/') {
                    // 写入 '*'，下一轮会写入 '/'
                    // 先让本轮继续，等下一轮关闭
                }
            }

            // 行首插入缩进（不在字符串中，且不是空白）
            if (atLineStart) {
                if (c == ' ' || c == '\t') {
                    // 跳过原始行首空白，统一由我们控制缩进
                    continue;
                }

                // 如果行首是 '}'，先减少缩进
                if (!inString && !inBlockComment && !inLineComment && c == '}') {
                    indent = Math.max(0, indent - 1);
                }

                appendIndent(out, indent, indentSize);
                atLineStart = false;
            }

            // 字符串处理
            if (inString) {
                out.append(c);
                if (escaping) {
                    escaping = false;
                } else if (c == '\\') {
                    escaping = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            } else {
                if (!inBlockComment && !inLineComment && c == '"') {
                    inString = true;
                    out.append(c);
                    continue;
                }
            }

            // 块注释结束
            if (inBlockComment && c == '*' && next == '/') {
                out.append('*');
                out.append('/');
                i++; // consume '/'
                inBlockComment = false;
                continue;
            }

            // 结构化缩进规则（仅非注释/非字符串）
            if (!inBlockComment && !inLineComment) {
                if (c == '{') {
                    out.append('{');
                    // 若后面不是换行，换行
                    if (next != '\n') {
                        out.append('\n');
                        atLineStart = true;
                    }
                    indent++;
                    continue;
                }
                if (c == '}') {
                    // '}' 已经在行首时预减缩进；若不在行首，需要这里减
                    if (!atLineStart) {
                        indent = Math.max(0, indent - 1);
                    }
                    // 若当前不是行首且 out 最后不是换行，先换行再缩进
                    if (out.length() > 0 && out.charAt(out.length() - 1) != '\n') {
                        out.append('\n');
                        atLineStart = true;
                        appendIndent(out, indent, indentSize);
                        atLineStart = false;
                    }
                    out.append('}');
                    if (next != '\n') {
                        out.append('\n');
                        atLineStart = true;
                    }
                    continue;
                }
                if (c == ';') {
                    out.append(';');
                    if (next != '\n') {
                        out.append('\n');
                        atLineStart = true;
                    }
                    continue;
                }
            }

            out.append(c);
        }

        // 清理多余空行末尾空白
        return out.toString().replaceAll("(?m)[ \\t]+$", "").trim();
    }

    private static void appendIndent(StringBuilder out, int indent, int indentSize) {
        int spaces = Math.max(0, indent) * indentSize;
        for (int i = 0; i < spaces; i++) {
            out.append(' ');
        }
    }

    /**
     * 只对双引号字符串之外的区域做反转义：
     * - \\n -> \n
     * - \\r -> \r
     * - \\t -> \t
     * - 其它保持不变
     *
     * 目的：修复 LLM 把换行以字面 "\\n" 输出到 DSL 中，导致解析出现 extraneous input 'n'。
     */
    private static String unescapeDslOutsideQuotes(String text) {
        if (text == null || text.isEmpty()) return text;

        StringBuilder out = new StringBuilder(text.length());
        boolean inString = false;
        boolean escaping = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (inString) {
                out.append(c);
                if (escaping) {
                    escaping = false;
                } else if (c == '\\') {
                    escaping = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }

            // 不在字符串里
            if (c == '"') {
                inString = true;
                out.append(c);
                continue;
            }

            if (c == '\\' && i + 1 < text.length()) {
                char n = text.charAt(i + 1);
                if (n == 'n') {
                    out.append('\n');
                    i++;
                    continue;
                }
                if (n == 'r') {
                    out.append('\r');
                    i++;
                    continue;
                }
                if (n == 't') {
                    out.append('\t');
                    i++;
                    continue;
                }
                // 其它反斜杠序列保持原样
            }

            out.append(c);
        }

        return out.toString();
    }

    /**
     * 仅保留翻译树 DSL：从第一个出现的 Message 开始，到其顶层配对的 } 结束。
     * 若找不到 Message 或无法匹配括号，返回 null。
     */
    private static String extractMessageDslOnly(String text) {
        if (text == null || text.isBlank()) return null;

        int msgIdx = indexOfMessageKeyword(text);
        if (msgIdx < 0) {
            return null;
        }

        // 从 Message 开始裁剪窗口，避免扫描超大前缀
        int windowEnd = Math.min(text.length(), msgIdx + 500_000);
        String window = text.substring(msgIdx, windowEnd);

        int firstBrace = findFirstBraceAfter(window, 0);
        if (firstBrace < 0) return null;

        int endBrace = findMatchingBrace(window, firstBrace);
        if (endBrace < 0) return null;

        return window.substring(0, endBrace + 1);
    }

    private static int indexOfMessageKeyword(String text) {
        // 允许前面有任意内容，只要找到以单词边界出现的 Message
        Matcher m = Pattern.compile("\\bMessage\\b").matcher(text);
        return m.find() ? m.start() : -1;
    }

    private static int findFirstBraceAfter(String text, int start) {
        boolean inString = false;
        boolean inComment = false;
        boolean inLineComment = false;

        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            char next = (i + 1 < text.length()) ? text.charAt(i + 1) : '\0';

            // 注释处理
            if (!inString && !inComment && !inLineComment && c == '/' && next == '/') {
                inLineComment = true;
                i++;
                continue;
            }
            if (inLineComment && c == '\n') {
                inLineComment = false;
                continue;
            }
            if (!inString && !inLineComment && !inComment && c == '/' && next == '*') {
                inComment = true;
                i++;
                continue;
            }
            if (inComment && c == '*' && next == '/') {
                inComment = false;
                i++;
                continue;
            }

            // 字符串处理
            if (!inComment && !inLineComment && c == '"') {
                if (i > 0 && text.charAt(i - 1) == '\\') {
                    continue;
                }
                inString = !inString;
                continue;
            }

            if (!inString && !inComment && !inLineComment && c == '{') {
                return i;
            }
        }
        return -1;
    }

    private static int findMatchingBrace(String text, int openBraceIndex) {
        int depth = 0;
        boolean inString = false;
        boolean inComment = false;
        boolean inLineComment = false;

        for (int i = openBraceIndex; i < text.length(); i++) {
            char c = text.charAt(i);
            char next = (i + 1 < text.length()) ? text.charAt(i + 1) : '\0';

            // 注释处理
            if (!inString && !inComment && !inLineComment && c == '/' && next == '/') {
                inLineComment = true;
                i++;
                continue;
            }
            if (inLineComment && c == '\n') {
                inLineComment = false;
                continue;
            }
            if (!inString && !inLineComment && !inComment && c == '/' && next == '*') {
                inComment = true;
                i++;
                continue;
            }
            if (inComment && c == '*' && next == '/') {
                inComment = false;
                i++;
                continue;
            }

            // 字符串处理
            if (!inComment && !inLineComment && c == '"') {
                if (i > 0 && text.charAt(i - 1) == '\\') {
                    continue;
                }
                inString = !inString;
                continue;
            }

            if (inString || inComment || inLineComment) {
                continue;
            }

            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }


    /**
     * 移除 Markdown 代码块标记
     */
    private static String removeMarkdownCodeBlocks(String text) {
        // 移除 ```dsl ... ``` 或 ``` ... ``` 包裹
        Pattern pattern = Pattern.compile("```(?:dsl)?\\s*\\n?(.+?)```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            // 如果找到代码块，只保留代码块内容
            return matcher.group(1).trim();
        }

        return text;
    }

    /**
     * 移除开头的说明性文字
     * 检测到 FUNCTION_MODEL 关键字之前的所有内容
     */
    private static String removeLeadingExplanations(String text) {
        // 支持两种 DSL：功能模型 (FUNCTION_MODEL...) 或 翻译树 (Message ...)
        Pattern pattern = Pattern.compile("^[\\s\\S]*?((FUNCTION_MODEL\\s+)|(Message\\s+))", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            int startPos = matcher.start(1);
            return text.substring(startPos);
        }

        return text;
    }

    /**
     * 移除结尾的说明性文字
     * 检测 DSL 代码的最后一个闭合花括号之后的内容
     */
    private static String removeTrailingExplanations(String text) {
        // 找到最后一个顶层闭合花括号
        int lastBrace = findLastTopLevelClosingBrace(text);

        if (lastBrace != -1) {
            // 只保留到最后一个花括号
            return text.substring(0, lastBrace + 1);
        }

        return text;
    }

    /**
     * 移除 Markdown 标题（# 开头的行）
     */
    private static String removeMarkdownHeaders(String text) {
        // 移除以 # 开头的行（Markdown 标题）
        return text.replaceAll("(?m)^#+\\s+.*$\\n?", "");
    }

    /**
     * 查找最后一个顶层闭合花括号的位置
     */
    private static int findLastTopLevelClosingBrace(String text) {
        int depth = 0;
        int lastTopLevelBrace = -1;
        boolean inString = false;
        boolean inComment = false;
        boolean inLineComment = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            char next = (i + 1 < text.length()) ? text.charAt(i + 1) : '\0';

            // 处理注释
            if (!inString && !inComment && !inLineComment && c == '/' && next == '/') {
                inLineComment = true;
                i++; // 跳过下一个字符
                continue;
            }

            if (inLineComment && c == '\n') {
                inLineComment = false;
                continue;
            }

            if (!inString && !inLineComment && !inComment && c == '/' && next == '*') {
                inComment = true;
                i++; // 跳过下一个字符
                continue;
            }

            if (inComment && c == '*' && next == '/') {
                inComment = false;
                i++; // 跳过下一个字符
                continue;
            }

            // 处理字符串
            if (!inComment && !inLineComment && c == '"') {
                // 检查是否是转义的引号
                if (i > 0 && text.charAt(i - 1) == '\\') {
                    continue;
                }
                inString = !inString;
                continue;
            }

            // 只在非字符串、非注释中计算花括号
            if (!inString && !inComment && !inLineComment) {
                if (c == '{') {
                    depth++;
                } else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        lastTopLevelBrace = i;
                    }
                }
            }
        }

        return lastTopLevelBrace;
    }

    /**
     * 检查清理后的输出是否有效（包含 FUNCTION_MODEL）
     */
    public static boolean isValidDslOutput(String cleaned) {
        return cleaned != null &&
               !cleaned.isBlank() &&
               (cleaned.contains("FUNCTION_MODEL") || cleaned.contains("Message"));
    }

    /**
     * 若检测到 Message DSL 很像“被 JSON 转义过的字符串”，则做一次轻量 JSON 风格反转义。
     * 触发条件：包含多个 "\\n"，或者包含 "\\\""。
     */
    private static String maybeJsonStringUnescape(String text) {
        if (text == null) return null;

        // 兼容两种常见形态：
        // 1) 字面 \n（两个字符：反斜杠+n），通常出现在你现在保存的 .dsl 里
        // 2) JSON 二次转义 \\n（四个字符：反斜杠+反斜杠+n），通常出现在原始 JSON 字符串里
        int nCount1 = countOccurrences(text, "\\n");
        int nCount2 = countOccurrences(text, "\\\\n");
        boolean hasEscapedQuote1 = text.contains("\\\"");
        boolean hasEscapedQuote2 = text.contains("\\\\\"");

        if (nCount1 + nCount2 < 1 && !hasEscapedQuote1 && !hasEscapedQuote2) {
            return text;
        }

        // 先处理更“外层”的 \n / \t / \r
        String s = text
                .replace("\\r", "\r")
                .replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");

        // 再处理二次转义形式 \\n / \\t / \\r（如果存在）
        return s
                .replace("\\\\r", "\r")
                .replace("\\\\n", "\n")
                .replace("\\\\t", "\t")
                .replace("\\\\\"", "\"")
                .replace("\\\\\\\\", "\\");
    }

    private static int countOccurrences(String text, String needle) {
        if (text == null || needle == null || needle.isEmpty()) return 0;
        int count = 0;
        int idx = 0;
        while (true) {
            idx = text.indexOf(needle, idx);
            if (idx < 0) return count;
            count++;
            idx += needle.length();
        }
    }
}
