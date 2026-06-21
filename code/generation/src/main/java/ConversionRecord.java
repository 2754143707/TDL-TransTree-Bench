import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 转换记录实体
 * 记录每次DSL转换的完整信息
 */
public class ConversionRecord {
    private String id;                          // 唯一标识
    private LocalDateTime timestamp;            // 转换时间
    private String sourceFile;                  // 源文件名
    private String dslType;                     // DSL类型
    private String llmModel;                    // 使用的LLM模型
    private String status;                      // 状态: success/failed
    private int attempts;                       // 尝试次数
    private int totalErrors;                    // 错误总数
    private FilePaths files;                    // 相关文件路径
    private ConversionStats stats;              // 统计信息
    private String failureReason;               // 失败原因
    private List<AttemptInfo> attemptHistory;   // 每次尝试的详细信息

    public ConversionRecord() {
    }

    public ConversionRecord(String id, LocalDateTime timestamp, String sourceFile,
                           String dslType, String llmModel, String status,
                           int attempts, int totalErrors) {
        this.id = id;
        this.timestamp = timestamp;
        this.sourceFile = sourceFile;
        this.dslType = dslType;
        this.llmModel = llmModel;
        this.status = status;
        this.attempts = attempts;
        this.totalErrors = totalErrors;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getSourceFile() { return sourceFile; }
    public void setSourceFile(String sourceFile) { this.sourceFile = sourceFile; }

    public String getDslType() { return dslType; }
    public void setDslType(String dslType) { this.dslType = dslType; }

    public String getLlmModel() { return llmModel; }
    public void setLlmModel(String llmModel) { this.llmModel = llmModel; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }

    public int getTotalErrors() { return totalErrors; }
    public void setTotalErrors(int totalErrors) { this.totalErrors = totalErrors; }

    public FilePaths getFiles() { return files; }
    public void setFiles(FilePaths files) { this.files = files; }

    public ConversionStats getStats() { return stats; }
    public void setStats(ConversionStats stats) { this.stats = stats; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public List<AttemptInfo> getAttemptHistory() { return attemptHistory; }
    public void setAttemptHistory(List<AttemptInfo> attemptHistory) { this.attemptHistory = attemptHistory; }

    /**
     * 相关文件路径
     */
    public static class FilePaths {
        private String dsl;      // DSL文件路径
        private String log;      // 日志文件路径
        private String svg;      // SVG图形路径
        private String dot;      // DOT文件路径
        private String errors;   // 错误文件路径

        public FilePaths() {}

        public String getDsl() { return dsl; }
        public void setDsl(String dsl) { this.dsl = dsl; }

        public String getLog() { return log; }
        public void setLog(String log) { this.log = log; }

        public String getSvg() { return svg; }
        public void setSvg(String svg) { this.svg = svg; }

        public String getDot() { return dot; }
        public void setDot(String dot) { this.dot = dot; }

        public String getErrors() { return errors; }
        public void setErrors(String errors) { this.errors = errors; }
    }

    /**
     * 转换统计信息
     */
    public static class ConversionStats {
        private double durationSeconds;  // 转换耗时（秒）
        private int dslLines;            // 生成的DSL行数
        private int inputLength;         // 输入文本长度
        private Map<String, Integer> errorTypes;  // 错误类型统计

        public ConversionStats() {}

        public double getDurationSeconds() { return durationSeconds; }
        public void setDurationSeconds(double durationSeconds) { this.durationSeconds = durationSeconds; }

        public int getDslLines() { return dslLines; }
        public void setDslLines(int dslLines) { this.dslLines = dslLines; }

        public int getInputLength() { return inputLength; }
        public void setInputLength(int inputLength) { this.inputLength = inputLength; }

        public Map<String, Integer> getErrorTypes() { return errorTypes; }
        public void setErrorTypes(Map<String, Integer> errorTypes) { this.errorTypes = errorTypes; }
    }

    /**
     * 单次尝试信息
     */
    public static class AttemptInfo {
        private int attemptNumber;       // 尝试序号
        private boolean success;         // 是否成功
        private int errorCount;          // 错误数量
        private String feedback;         // 错误反馈

        public AttemptInfo() {}

        public AttemptInfo(int attemptNumber, boolean success, int errorCount, String feedback) {
            this.attemptNumber = attemptNumber;
            this.success = success;
            this.errorCount = errorCount;
            this.feedback = feedback;
        }

        public int getAttemptNumber() { return attemptNumber; }
        public void setAttemptNumber(int attemptNumber) { this.attemptNumber = attemptNumber; }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public int getErrorCount() { return errorCount; }
        public void setErrorCount(int errorCount) { this.errorCount = errorCount; }

        public String getFeedback() { return feedback; }
        public void setFeedback(String feedback) { this.feedback = feedback; }
    }
}
