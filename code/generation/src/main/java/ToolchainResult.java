import java.util.List;

public class ToolchainResult {
    public final boolean success;
    public final String dsl;
    public final String logPath;
    public final String svgPath;
    public final String dslPath;
    public final String finalDsl;
    public final List<String> errors;
    public final List<GenerationAttempt> attempts;
    public final String failureReason;

    public ToolchainResult(boolean success,
                           String dsl,
                           String logPath,
                           String svgPath,
                           List<String> errors,
                           List<GenerationAttempt> attempts,
                           String failureReason) {
        this.success = success;
        this.dsl = dsl;
        this.finalDsl = dsl;
        this.logPath = logPath;
        this.svgPath = svgPath;
        this.dslPath = logPath != null ? logPath.replace("解析日志.txt", ".dsl") : null;
        this.errors = errors;
        this.attempts = attempts;
        this.failureReason = failureReason;
    }

    /**
     * 生成尝试信息（用于 GUI 显示）
     */
    public static class GenerationAttemptInfo {
        public final int attemptNumber;
        public final boolean success;
        public final String dsl;
        public final String feedback;
        public final int errorCount;

        public GenerationAttemptInfo(int attemptNumber, boolean success, String dsl, String feedback, int errorCount) {
            this.attemptNumber = attemptNumber;
            this.success = success;
            this.dsl = dsl;
            this.feedback = feedback;
            this.errorCount = errorCount;
        }
    }
}
