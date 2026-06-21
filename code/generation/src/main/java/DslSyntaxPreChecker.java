import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DSL 语法预检查器
 * 在调用解析器之前检查常见的格式问题，可以快速修复简单错误
 */
public class DslSyntaxPreChecker {

    /**
     * 检查并修复DSL代码的常见格式问题
     * @param dsl 原始DSL代码
     * @return 修复后的DSL代码和问题列表
     */
    public static PreCheckResult check(String dsl) {
        if (dsl == null || dsl.isBlank()) {
            return new PreCheckResult(dsl, List.of("DSL内容为空"), false);
        }

        List<String> issues = new ArrayList<>();
        String fixed = dsl;
        boolean hasChanges = false;

        // 1. 移除Markdown代码块标记
        String original = fixed;
        fixed = removeMarkdownCodeBlock(fixed);
        if (!fixed.equals(original)) {
            issues.add("✓ 移除了Markdown代码块标记 (```dsl 或 ```)");
            hasChanges = true;
        }

        // 2. 移除开头和结尾的多余空白
        original = fixed;
        fixed = fixed.trim();
        if (!fixed.equals(original)) {
            issues.add("✓ 移除了开头和结尾的多余空白");
            hasChanges = true;
        }

        // 3. 检查ENUM语法（常见错误：使用逗号而不是等号）
        original = fixed;
        fixed = fixEnumSyntax(fixed, issues);
        if (!fixed.equals(original)) {
            hasChanges = true;
        }

        // 4. 检查是否以FUNCTION_MODEL或MESSAGE开头
        if (!fixed.matches("(?s)^\\s*(FUNCTION_MODEL|MESSAGE)\\s+.*")) {
            issues.add("⚠ 警告: DSL未以 FUNCTION_MODEL 或 MESSAGE 关键字开头");
        }

        // 5. 检查括号匹配
        if (!checkBraceBalance(fixed)) {
            issues.add("⚠ 警告: 大括号 {} 不匹配");
        }

        // 6. 检查常见的拼写错误关键字
        original = fixed;
        fixed = fixCommonKeywordTypos(fixed, issues);
        if (!fixed.equals(original)) {
            hasChanges = true;
        }

        return new PreCheckResult(fixed, issues, hasChanges);
    }

    /**
     * 移除Markdown代码块标记
     */
    private static String removeMarkdownCodeBlock(String dsl) {
        // 移除开头的 ```dsl 或 ```
        dsl = dsl.replaceFirst("^\\s*```\\w*\\s*\\n?", "");
        // 移除结尾的 ```
        dsl = dsl.replaceFirst("\\n?\\s*```\\s*$", "");
        return dsl;
    }

    /**
     * 修复ENUM语法：ENUM {A, B, C} 应该是 ENUM {A; B; C}
     * 或者 A = 0, B = 1 应该是 A = 0 ""; B = 1 "";
     */
    private static String fixEnumSyntax(String dsl, List<String> issues) {
        // 这是一个简化的检查，实际可能需要更复杂的逻辑
        // 目前只检测并报告，不自动修改（避免误改）

        Pattern enumPattern = Pattern.compile("ENUM\\s+\\w+\\s*\\{([^}]+)\\}");
        Matcher matcher = enumPattern.matcher(dsl);

        while (matcher.find()) {
            String enumBody = matcher.group(1);
            // 检查是否使用逗号分隔
            if (enumBody.contains(",") && !enumBody.contains("IN_RANGE")) {
                issues.add("⚠ 警告: ENUM定义中可能使用了逗号，应使用分号和字符串");
            }
        }

        return dsl;
    }

    /**
     * 检查大括号是否匹配
     */
    private static boolean checkBraceBalance(String dsl) {
        int count = 0;
        for (char c : dsl.toCharArray()) {
            if (c == '{') count++;
            if (c == '}') count--;
            if (count < 0) return false; // 右括号多于左括号
        }
        return count == 0;
    }

    /**
     * 修复常见的关键字拼写错误
     */
    private static String fixCommonKeywordTypos(String dsl, List<String> issues) {
        String fixed = dsl;

        // 常见拼写错误映射
        String[][] typos = {
            {"FUCNTION_MODEL", "FUNCTION_MODEL"},
            {"FUCTION_MODEL", "FUNCTION_MODEL"},
            {"FUNTION_MODEL", "FUNCTION_MODEL"},
            {"PROCEEDURE", "PROCEDURE"},
            {"PROCEDRUE", "PROCEDURE"},
            {"MESSSAGE", "MESSAGE"},
            {"MESAGE", "MESSAGE"}
        };

        for (String[] typo : typos) {
            if (fixed.contains(typo[0])) {
                fixed = fixed.replaceAll("\\b" + typo[0] + "\\b", typo[1]);
                issues.add("✓ 修复了拼写错误: " + typo[0] + " → " + typo[1]);
            }
        }

        return fixed;
    }

    /**
     * 预检查结果
     */
    public static class PreCheckResult {
        public final String fixedDsl;
        public final List<String> issues;
        public final boolean hasChanges;

        public PreCheckResult(String fixedDsl, List<String> issues, boolean hasChanges) {
            this.fixedDsl = fixedDsl;
            this.issues = issues;
            this.hasChanges = hasChanges;
        }

        public void printReport() {
            if (issues.isEmpty()) {
                System.out.println("   ✅ 预检查通过，未发现常见格式问题");
            } else {
                System.out.println("   📋 预检查发现 " + issues.size() + " 个问题:");
                for (String issue : issues) {
                    System.out.println("      " + issue);
                }
            }
        }
    }
}
